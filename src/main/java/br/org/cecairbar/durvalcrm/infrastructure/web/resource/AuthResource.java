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
import java.time.Instant;
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

    @Inject
    JsonWebToken jwt;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Classe para requisição do callback com suporte PKCE
    public static class CallbackRequest {
        public String code;
        public String redirectUri;
        public String codeVerifier; // Campo para PKCE (obrigatório para clientes públicos)
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
        response.put("isPublicClient", true); // Indicador de que é cliente público
        
        return response;
    }

    /**
     * Endpoint para validar token JWT
     */
    @GET
    @Path("/validate")
    @Authenticated
    public Response validateToken() {
        try {
            LOG.infof("Validando token para usuário: %s", jwt.getName());
            
            Map<String, Object> validation = new HashMap<>();
            validation.put("valid", true);
            validation.put("authenticated", true);
            validation.put("username", jwt.getName());
            validation.put("sub", jwt.getSubject());
            validation.put("email", jwt.getClaim("email"));
            validation.put("name", jwt.getClaim("name"));
            validation.put("preferred_username", jwt.getClaim("preferred_username"));
            validation.put("given_name", jwt.getClaim("given_name"));
            validation.put("family_name", jwt.getClaim("family_name"));
            validation.put("roles", jwt.getGroups());
            
            // Informações do token
            Long expirationTime = jwt.getExpirationTime();
            if (expirationTime != null) {
                validation.put("expires_at", expirationTime);
                validation.put("expires_in", expirationTime - Instant.now().getEpochSecond());
            }
            Long issuedAtTime = jwt.getIssuedAtTime();
            if (issuedAtTime != null) {
                validation.put("issued_at", issuedAtTime);
            }
            
            return Response.ok(validation).build();
            
        } catch (Exception e) {
            LOG.errorf("Erro ao validar token: %s", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("valid", false, "error", "Token inválido ou expirado"))
                    .build();
        }
    }

    /**
     * Endpoint para processar callback OAuth2 e trocar código por token
     * Para clientes públicos com PKCE
     */
    @POST
    @Path("/callback")
    @PermitAll
    public Response handleCallback(CallbackRequest request) {
        try {
            if (request.code == null || request.codeVerifier == null) {
                LOG.error("Código ou code_verifier ausente na requisição de callback");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Código ou code_verifier obrigatórios para cliente público"))
                        .build();
            }

            LOG.infof("Processando callback com código: %s, redirectUri: %s, PKCE presente: %s", 
                    request.code.substring(0, Math.min(10, request.code.length())) + "...",
                    request.redirectUri,
                    "sim");

            // Construir URL do token endpoint
            String tokenUrl = authServerUrl + "/protocol/openid-connect/token";
            
            // Parâmetros para cliente público com PKCE
            String formData = String.format(
                    "grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&code_verifier=%s",
                    URLEncoder.encode(request.code, StandardCharsets.UTF_8),
                    URLEncoder.encode(request.redirectUri != null ? request.redirectUri : "", StandardCharsets.UTF_8),
                    URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                    URLEncoder.encode(request.codeVerifier, StandardCharsets.UTF_8)
            );

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, 
                    HttpResponse.BodyHandlers.ofString());

            if (tokenResponse.statusCode() == 200) {
                LOG.info("Token obtido com sucesso");
                return Response.ok(tokenResponse.body()).build();
            } else {
                LOG.errorf("Erro ao obter token: %d - %s", tokenResponse.statusCode(), tokenResponse.body());
                return Response.status(tokenResponse.statusCode())
                        .entity(Map.of("error", "Falha na autenticação", "details", tokenResponse.body()))
                        .build();
            }

        } catch (IOException | InterruptedException e) {
            LOG.error("Erro ao processar callback", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro interno no servidor"))
                    .build();
        }
    }

    /**
     * Endpoint para logout
     */
    @GET
    @Path("/logout")
    @PermitAll
    public Map<String, Object> logout() {
        String logoutUrl = authServerUrl + "/protocol/openid-connect/logout" +
                "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&post_logout_redirect_uri=" + URLEncoder.encode("http://localhost:3000", StandardCharsets.UTF_8);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");
        response.put("logoutUrl", logoutUrl);
        
        return response;
    }

    /**
     * Endpoint protegido para obter informações detalhadas do usuário
     */
    @GET
    @Path("/user-info")
    @Authenticated
    public Response getUserInfo() {
        try {
            LOG.infof("Retornando informações do usuário autenticado: %s", jwt.getName());
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", jwt.getSubject());
            userInfo.put("username", jwt.getName());
            userInfo.put("preferred_username", jwt.getClaim("preferred_username"));
            userInfo.put("email", jwt.getClaim("email"));
            userInfo.put("email_verified", jwt.getClaim("email_verified"));
            userInfo.put("name", jwt.getClaim("name"));
            userInfo.put("given_name", jwt.getClaim("given_name"));
            userInfo.put("family_name", jwt.getClaim("family_name"));
            userInfo.put("roles", jwt.getGroups());
            userInfo.put("realm_access", jwt.getClaim("realm_access"));
            userInfo.put("resource_access", jwt.getClaim("resource_access"));
            
            return Response.ok(userInfo).build();
            
        } catch (Exception e) {
            LOG.errorf("Erro ao obter informações do usuário: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro ao obter informações do usuário"))
                    .build();
        }
    }

    /**
     * Endpoint para refrescar token (se necessário)
     */
    @POST
    @Path("/refresh")
    @PermitAll
    public Response refreshToken(@FormParam("refresh_token") String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Refresh token obrigatório"))
                        .build();
            }

            String tokenUrl = authServerUrl + "/protocol/openid-connect/token";
            
            String formData = String.format(
                    "grant_type=refresh_token&refresh_token=%s&client_id=%s",
                    URLEncoder.encode(refreshToken, StandardCharsets.UTF_8),
                    URLEncoder.encode(clientId, StandardCharsets.UTF_8)
            );

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, 
                    HttpResponse.BodyHandlers.ofString());

            if (tokenResponse.statusCode() == 200) {
                LOG.info("Token refreshed com sucesso");
                return Response.ok(tokenResponse.body()).build();
            } else {
                LOG.errorf("Erro ao fazer refresh do token: %d - %s", tokenResponse.statusCode(), tokenResponse.body());
                return Response.status(tokenResponse.statusCode())
                        .entity(Map.of("error", "Falha ao fazer refresh do token"))
                        .build();
            }

        } catch (IOException | InterruptedException e) {
            LOG.error("Erro ao fazer refresh do token", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro interno no servidor"))
                    .build();
        }
    }

    // Métodos auxiliares
    private String extractRealmFromUrl(String authServerUrl) {
        if (authServerUrl != null && authServerUrl.contains("/realms/")) {
            return authServerUrl.substring(authServerUrl.lastIndexOf("/realms/") + 8);
        }
        return "durval-crm";
    }

    private String buildLoginUrl() {
        return authServerUrl + "/protocol/openid-connect/auth" +
                "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=openid profile email" +
                "&code_challenge_method=S256"; // PKCE obrigatório para clientes públicos
    }
}