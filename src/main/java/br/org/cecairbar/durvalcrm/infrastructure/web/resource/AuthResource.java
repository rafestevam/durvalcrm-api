package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @ConfigProperty(name = "quarkus.oidc.auth-server-url", defaultValue = "http://localhost:8080/realms/durval-crm")
    String authServerUrl;

    @ConfigProperty(name = "quarkus.oidc.client-id", defaultValue = "durvalcrm-app")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret", defaultValue = "")
    String clientSecret;

    @Inject
    JsonWebToken jwt;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Endpoint público que retorna informações necessárias para o frontend
     * configurar a autenticação OIDC/Keycloak
     */
    @GET
    @Path("/login-info")
    @PermitAll
    public Map<String, Object> getLoginInfo() {
        LOG.infof("Retornando informações de login - authServerUrl: %s, clientId: %s", authServerUrl, clientId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("authServerUrl", authServerUrl);
        response.put("clientId", clientId);
        response.put("realm", extractRealmFromUrl(authServerUrl));
        response.put("loginUrl", buildLoginUrl());
        
        return response;
    }

    /**
     * Endpoint para processar callback OAuth2 e trocar código por token
     */
    @POST
    @Path("/callback")
    @PermitAll
    public Response handleCallback(CallbackRequest request) {
        try {
            LOG.infof("Processando callback com código: %s", request.code != null ? "presente" : "ausente");
            
            if (request.code == null || request.code.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Código de autorização é obrigatório"))
                        .build();
            }

            // Trocar código por token no Keycloak
            TokenResponse tokenResponse = exchangeCodeForToken(request.code, request.redirectUri);
            
            if (tokenResponse == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Falha na troca do código por token"))
                        .build();
            }

            // Retornar tokens para o frontend
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", tokenResponse.accessToken);
            response.put("refresh_token", tokenResponse.refreshToken);
            response.put("expires_in", tokenResponse.expiresIn);
            response.put("token_type", "Bearer");
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOG.errorf("Erro no callback de autenticação: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro interno no processamento do callback"))
                    .build();
        }
    }

    /**
     * Endpoint para obter informações do usuário autenticado
     */
    @GET
    @Path("/me")
    @Authenticated
    public Response getUserInfo() {
        try {
            LOG.infof("Retornando informações do usuário autenticado: %s", jwt.getName());
            
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("username", jwt.getName());
            userDetails.put("subject", jwt.getSubject());
            userDetails.put("email", jwt.getClaim("email"));
            userDetails.put("name", jwt.getClaim("name"));
            userDetails.put("preferredUsername", jwt.getClaim("preferred_username"));
            userDetails.put("tokenExpiry", jwt.getExpirationTime());
            userDetails.put("issuedAt", jwt.getIssuedAtTime());
            
            return Response.ok(userDetails).build();
        } catch (Exception e) {
            LOG.errorf("Erro ao obter informações do usuário: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro ao obter informações do usuário"))
                    .build();
        }
    }

    /**
     * Endpoint para validar token
     */
    @GET
    @Path("/validate")
    @Authenticated
    public Response validateToken() {
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            boolean isValid = jwt.getExpirationTime() > currentTime;
            
            Map<String, Object> validation = new HashMap<>();
            validation.put("valid", isValid);
            validation.put("username", jwt.getName());
            validation.put("subject", jwt.getSubject());
            validation.put("exp", jwt.getExpirationTime());
            validation.put("currentTime", currentTime);
            validation.put("timeToExpiry", jwt.getExpirationTime() - currentTime);
            
            return Response.ok(validation).build();
        } catch (Exception e) {
            LOG.errorf("Erro ao validar token: %s", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("valid", false, "error", "Token inválido"))
                    .build();
        }
    }

    /**
     * Endpoint para logout
     */
    @GET
    @Path("/logout")
    @PermitAll
    public Map<String, String> logout() {
        LOG.info("Endpoint de logout acessado");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");
        response.put("logoutUrl", buildLogoutUrl());
        
        return response;
    }

    /**
     * Troca código de autorização por token no Keycloak
     */
    private TokenResponse exchangeCodeForToken(String code, String redirectUri) {
        try {
            String tokenEndpoint = authServerUrl + "/protocol/openid-connect/token";
            
            // Preparar dados do formulário
            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "authorization_code");
            formData.put("client_id", clientId);
            formData.put("code", code);
            formData.put("redirect_uri", redirectUri != null ? redirectUri : getDefaultRedirectUri());
            
            // Adicionar client_secret se configurado
            if (clientSecret != null && !clientSecret.trim().isEmpty()) {
                formData.put("client_secret", clientSecret);
            }
            
            // Converter para formato application/x-www-form-urlencoded
            String formBody = formData.entrySet().stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + 
                                 URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .reduce((p1, p2) -> p1 + "&" + p2)
                    .orElse("");
            
            // Criar requisição HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenEndpoint))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();
            
            // Executar requisição
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            LOG.infof("Resposta do Keycloak: status=%d, body=%s", 
                    response.statusCode(), response.body());
            
            if (response.statusCode() == 200) {
                return parseTokenResponse(response.body());
            } else {
                LOG.errorf("Erro na troca de token: status=%d, body=%s", 
                        response.statusCode(), response.body());
                return null;
            }
            
        } catch (IOException | InterruptedException e) {
            LOG.errorf("Erro na comunicação com Keycloak: %s", e.getMessage());
            return null;
        }
    }

    /**
     * Faz parsing da resposta JSON do token
     */
    private TokenResponse parseTokenResponse(String jsonResponse) {
        try {
            // Parse manual simples do JSON (em produção, use uma biblioteca como Jackson)
            TokenResponse response = new TokenResponse();
            
            if (jsonResponse.contains("\"access_token\"")) {
                response.accessToken = extractJsonValue(jsonResponse, "access_token");
            }
            if (jsonResponse.contains("\"refresh_token\"")) {
                response.refreshToken = extractJsonValue(jsonResponse, "refresh_token");
            }
            if (jsonResponse.contains("\"expires_in\"")) {
                String expiresInStr = extractJsonValue(jsonResponse, "expires_in");
                response.expiresIn = Integer.parseInt(expiresInStr);
            }
            
            return response;
        } catch (Exception e) {
            LOG.errorf("Erro ao fazer parse da resposta do token: %s", e.getMessage());
            return null;
        }
    }

    /**
     * Extrai valor de uma chave JSON (implementação simples)
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;
        
        startIndex = startIndex + searchKey.length();
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        
        if (startIndex >= json.length()) return null;
        
        char firstChar = json.charAt(startIndex);
        int endIndex;
        
        if (firstChar == '"') {
            // String value
            startIndex++; // Skip opening quote
            endIndex = json.indexOf('"', startIndex);
        } else {
            // Number value
            endIndex = startIndex;
            while (endIndex < json.length() && 
                   (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '.')) {
                endIndex++;
            }
        }
        
        return endIndex > startIndex ? json.substring(startIndex, endIndex) : null;
    }

    private String extractRealmFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "durval-crm";
        }
        
        String[] parts = url.split("/realms/");
        if (parts.length > 1) {
            return parts[1];
        }
        
        return "durval-crm";
    }

    private String buildLogoutUrl() {
        return authServerUrl + "/protocol/openid-connect/logout";
    }

    private String buildLoginUrl() {
        return authServerUrl + "/protocol/openid-connect/auth" +
               "?client_id=" + clientId +
               "&response_type=code" +
               "&scope=openid%20profile%20email" +
               "&redirect_uri=" + getDefaultRedirectUri();
    }

    private String getDefaultRedirectUri() {
        return "http://localhost:3000/auth/callback";
    }

    // Classes auxiliares
    public static class CallbackRequest {
        public String code;
        public String redirectUri;
        public String state;
    }

    private static class TokenResponse {
        public String accessToken;
        public String refreshToken;
        public int expiresIn;
    }
}