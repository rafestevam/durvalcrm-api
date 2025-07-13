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

import java.util.HashMap;
import java.util.Map;

@Path("/auth")
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
     * Endpoint para obter informações do usuário autenticado
     * Este endpoint será chamado após o login via Keycloak
     */
    @GET
    @Path("/me")
    @Authenticated
    public Response getUserInfo() {
        try {
            LOG.infof("Retornando informações do usuário autenticado: %s", jwt.getName());
            
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("username", jwt.getName());
            
            // Safely extract claims that might not exist
            userDetails.put("email", jwt.getClaim("email"));
            userDetails.put("firstName", jwt.getClaim("given_name"));
            userDetails.put("lastName", jwt.getClaim("family_name"));
            userDetails.put("preferredUsername", jwt.getClaim("preferred_username"));
            
            // Handle groups safely
            if (jwt.getGroups() != null) {
                userDetails.put("groups", jwt.getGroups());
            }
            
            // Handle realm access safely
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                userDetails.put("roles", realmAccess);
            }
            
            userDetails.put("tokenExpiry", jwt.getExpirationTime());
            userDetails.put("issuedAt", jwt.getIssuedAtTime());
            userDetails.put("subject", jwt.getSubject());
            
            return Response.ok(userDetails).build();
        } catch (Exception e) {
            LOG.errorf("Erro ao obter informações do usuário: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro ao obter informações do usuário"))
                    .build();
        }
    }

    /**
     * Endpoint para verificar se o token é válido
     */
    @GET
    @Path("/validate")
    @Authenticated
    public Response validateToken() {
        try {
            LOG.infof("Validando token para usuário: %s", jwt.getName());
            
            long currentTime = System.currentTimeMillis() / 1000;
            boolean isExpired = jwt.getExpirationTime() < currentTime;
            
            Map<String, Object> validation = new HashMap<>();
            validation.put("valid", !isExpired);
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
     * Endpoint para logout - invalida a sessão
     * Disponível tanto para usuários autenticados quanto não autenticados
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
     * Endpoint POST para logout (para compatibilidade)
     */
    @POST
    @Path("/logout")
    @PermitAll
    public Response logoutPost() {
        LOG.info("Endpoint POST de logout acessado");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");
        response.put("logoutUrl", buildLogoutUrl());
        
        return Response.ok(response).build();
    }

    /**
     * Endpoint para refresh token (se necessário)
     */
    @POST
    @Path("/refresh")
    @Authenticated
    public Response refreshToken() {
        try {
            LOG.infof("Solicitação de refresh token para usuário: %s", jwt.getName());
            
            // O Quarkus/Keycloak gerencia automaticamente o refresh
            // Este endpoint pode ser usado para triggering manual se necessário
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Token ainda válido");
            response.put("username", jwt.getName());
            response.put("subject", jwt.getSubject());
            response.put("exp", jwt.getExpirationTime());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.errorf("Erro ao processar refresh token: %s", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Erro ao processar refresh token"))
                    .build();
        }
    }

    /**
     * Extrai o nome do realm da URL do servidor de autenticação
     */
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

    /**
     * Constrói a URL de logout do Keycloak
     */
    private String buildLogoutUrl() {
        return authServerUrl + "/protocol/openid-connect/logout";
    }

    /**
     * Constrói a URL de login do Keycloak
     */
    private String buildLoginUrl() {
        return authServerUrl + "/protocol/openid-connect/auth" +
               "?client_id=" + clientId +
               "&response_type=code" +
               "&scope=openid%20profile%20email" +
               "&redirect_uri=" + getRedirectUri();
    }

    /**
     * Retorna a URI de redirecionamento padrão
     * Em produção, isso deve vir de configuração
     */
    private String getRedirectUri() {
        // Por enquanto, retorna uma URI padrão para desenvolvimento
        // Em produção, isso deve ser configurável
        return "http://localhost:3000/auth/callback";
    }
}