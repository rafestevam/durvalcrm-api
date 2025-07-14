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

@Path("/api/auth")
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

    // Classe para requisição do callback com suporte PKCE
    public static class CallbackRequest {
        public String code;
        public String redirectUri;
        public String codeVerifier; // Novo campo para PKCE
    }

    // Classe para resposta de token
    public static class TokenResponse {
        public String accessToken;
        public String refreshToken;
        public Long expiresIn;
    }

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
     * Agora com suporte PKCE
     */
    @POST
    @Path("/callback")
    @PermitAll
    public Response handleCallback(CallbackRequest request) {
        try {
            LOG.infof("Processando callback com código: %s, redirectUri: %s, PKCE: %s", 
                    request.code != null ? request.code.substring(0, 10) + "..." : "ausente",
                    request.redirectUri,
                    request.codeVerifier != null ? "presente" : "ausente");
            
            if (request.code == null || request.code.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Código de autorização é obrigatório"))
                        .build();
            }

            // Trocar código por token no Keycloak
            TokenResponse tokenResponse = exchangeCodeForToken(request.code, request.redirectUri, request.codeVerifier);
            
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
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", jwt.getSubject());
            userInfo.put("preferred_username", jwt.getName());
            userInfo.put("email", jwt.getClaim("email"));
            userInfo.put("name", jwt.getClaim("name"));
            userInfo.put("given_name", jwt.getClaim("given_name"));
            userInfo.put("family_name", jwt.getClaim("family_name"));
            
            return Response.ok(userInfo).build();
            
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
            LOG.infof("Validando token para usuário: %s", jwt.getName());
            
            Map<String, Object> validation = new HashMap<>();
            validation.put("valid", true);
            validation.put("username", jwt.getName());
            validation.put("expires_at", jwt.getExpirationTime());
            
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
     * Endpoint para logout via POST
     */
    @POST
    @Path("/logout")
    @PermitAll
    public Map<String, String> logoutPost() {
        LOG.info("Endpoint de logout POST acessado");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");
        response.put("logoutUrl", buildLogoutUrl());
        
        return response;
    }

    /**
     * Troca código de autorização por token no Keycloak
     * Agora com suporte PKCE
     */
    private TokenResponse exchangeCodeForToken(String code, String redirectUri, String codeVerifier) {
        try {
            String tokenEndpoint = authServerUrl + "/protocol/openid-connect/token";
            
            // Preparar dados do formulário
            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "authorization_code");
            formData.put("client_id", clientId);
            formData.put("code", code);
            formData.put("redirect_uri", redirectUri != null ? redirectUri : getDefaultRedirectUri());
            
            // Adicionar code_verifier se presente (PKCE)
            if (codeVerifier != null && !codeVerifier.trim().isEmpty()) {
                formData.put("code_verifier", codeVerifier);
                LOG.infof("Incluindo code_verifier no request (PKCE): %s...", codeVerifier.substring(0, 8));
            }
            
            // Adicionar client_secret se configurado (para clientes confidenciais)
            if (clientSecret != null && !clientSecret.trim().isEmpty()) {
                formData.put("client_secret", clientSecret);
                LOG.info("Incluindo client_secret no request");
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
                response.expiresIn = Long.parseLong(expiresInStr);
            }
            
            return response;
            
        } catch (Exception e) {
            LOG.errorf("Erro no parsing da resposta JSON: %s", e.getMessage());
            return null;
        }
    }

    /**
     * Extrai valor de uma chave JSON de forma simples
     */
    private String extractJsonValue(String json, String key) {
        String searchFor = "\"" + key + "\":\"";
        int start = json.indexOf(searchFor);
        if (start == -1) {
            // Tentar sem aspas para números
            searchFor = "\"" + key + "\":";
            start = json.indexOf(searchFor);
            if (start == -1) return null;
            start += searchFor.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        
        start += searchFor.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    /**
     * Extrai o realm da URL do auth server
     */
    private String extractRealmFromUrl(String authServerUrl) {
        try {
            // Formato esperado: http://localhost:8080/realms/durval-crm
            String[] parts = authServerUrl.split("/realms/");
            return parts.length > 1 ? parts[1] : "durval-crm";
        } catch (Exception e) {
            LOG.warn("Erro ao extrair realm da URL, usando default");
            return "durval-crm";
        }
    }

    /**
     * Constrói URL de login
     */
    private String buildLoginUrl() {
        return authServerUrl + "/protocol/openid-connect/auth";
    }

    /**
     * Constrói URL de logout
     */
    private String buildLogoutUrl() {
        return authServerUrl + "/protocol/openid-connect/logout";
    }

    /**
     * Retorna URI de redirecionamento padrão
     */
    private String getDefaultRedirectUri() {
        return "http://localhost:5173/auth/callback";
    }
}