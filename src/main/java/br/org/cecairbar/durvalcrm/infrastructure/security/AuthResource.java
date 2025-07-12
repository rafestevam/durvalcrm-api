package br.org.cecairbar.durvalcrm.infrastructure.security;

import io.quarkus.oidc.OidcConfigurationMetadata;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

// IMPORTANTE: Descomente a linha abaixo APENAS se tiver adicionado a dependência microprofile-jwt-auth-api
// import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * AuthResource - Versão compatível com Quarkus 3.x
 * 
 * INSTRUÇÕES DE USO:
 * 
 * OPÇÃO 1 - Usar versão simplificada (RECOMENDADO):
 * - Use o código atual sem modificações
 * - Funciona apenas com as dependências já presentes no projeto
 * 
 * OPÇÃO 2 - Usar JsonWebToken (SE NECESSÁRIO):
 * 1. Adicione a dependência no pom.xml:
 *    <dependency>
 *        <groupId>org.eclipse.microprofile.jwt</groupId>
 *        <artifactId>microprofile-jwt-auth-api</artifactId>
 *        <version>2.1</version>
 *    </dependency>
 * 
 * 2. Descomente o import JsonWebToken acima
 * 
 * 3. Substitua o método getUserInfoFromPrincipal pelo código comentado abaixo
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    OidcConfigurationMetadata oidcMetadata;

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @GET
    @Path("/config")
    @PermitAll
    public Response getOidcConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("authServerUrl", authServerUrl);
        config.put("clientId", clientId);
        config.put("realm", extractRealmFromUrl(authServerUrl));
        
        if (oidcMetadata != null) {
            config.put("authorizationEndpoint", oidcMetadata.getAuthorizationUri());
            config.put("tokenEndpoint", oidcMetadata.getTokenUri());
            config.put("logoutEndpoint", oidcMetadata.getEndSessionUri());
        }
        
        return Response.ok(config).build();
    }

    @GET
    @Path("/login")
    @PermitAll
    public Response login(@QueryParam("redirect_uri") String redirectUri) {
        if (oidcMetadata == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("error", "OIDC não configurado"))
                    .build();
        }

        String authUrl = oidcMetadata.getAuthorizationUri() + 
                "?client_id=" + clientId +
                "&response_type=code" +
                "&scope=openid profile email" +
                "&redirect_uri=" + (redirectUri != null ? redirectUri : getDefaultRedirectUri());

        return Response.seeOther(URI.create(authUrl)).build();
    }

    @POST
    @Path("/logout")
    @Authenticated
    public Response logout(@QueryParam("redirect_uri") String redirectUri) {
        if (oidcMetadata == null || oidcMetadata.getEndSessionUri() == null) {
            return Response.ok(Map.of("message", "Logout realizado localmente")).build();
        }

        String logoutUrl = oidcMetadata.getEndSessionUri();
        if (redirectUri != null) {
            logoutUrl += "?redirect_uri=" + redirectUri;
        }

        return Response.ok(Map.of(
                "message", "Logout realizado com sucesso",
                "logoutUrl", logoutUrl
        )).build();
    }

    @GET
    @Path("/userinfo")
    @Authenticated
    public Response getUserInfo(@Context SecurityContext securityContext) {
        try {
            Map<String, Object> userInfo = new HashMap<>();
            
            if (securityContext.getUserPrincipal() instanceof OidcJwtCallerPrincipal) {
                OidcJwtCallerPrincipal principal = (OidcJwtCallerPrincipal) securityContext.getUserPrincipal();
                userInfo = getUserInfoFromPrincipal(principal);
            } else {
                userInfo.put("username", securityContext.getUserPrincipal().getName());
            }
            
            return Response.ok(userInfo).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro ao obter informações do usuário: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * VERSÃO ATUAL - Usando apenas métodos básicos do OidcJwtCallerPrincipal
     */
    private Map<String, Object> getUserInfoFromPrincipal(OidcJwtCallerPrincipal principal) {
        Map<String, Object> userInfo = new HashMap<>();
        
        try {
            userInfo.put("name", principal.getName());
            userInfo.put("subject", principal.getSubject());
            
            // Tentando acessar claims básicos
            userInfo.put("email", principal.getClaim("email"));
            userInfo.put("preferred_username", principal.getClaim("preferred_username"));
            userInfo.put("email_verified", principal.getClaim("email_verified"));
            
            // Grupos/roles
            Set<String> groups = principal.getGroups();
            if (groups != null) {
                userInfo.put("roles", groups);
            }
            
        } catch (Exception e) {
            userInfo.put("warning", "Alguns claims não puderam ser acessados: " + e.getMessage());
            userInfo.put("name", principal.getName());
        }
        
        return userInfo;
    }

    /*
     * VERSÃO ALTERNATIVA - Descomente APENAS se tiver adicionado a dependência JsonWebToken
     * 
    private Map<String, Object> getUserInfoFromPrincipal(OidcJwtCallerPrincipal principal) {
        Map<String, Object> userInfo = new HashMap<>();
        
        try {
            JsonWebToken jwt = principal.getAccessToken();
            
            userInfo.put("sub", jwt.getSubject());
            userInfo.put("name", jwt.getClaim("name"));
            userInfo.put("preferred_username", jwt.getClaim("preferred_username"));
            userInfo.put("email", jwt.getClaim("email"));
            userInfo.put("email_verified", jwt.getClaim("email_verified"));
            userInfo.put("roles", jwt.getGroups());
            
            // Adiciona claims customizados
            jwt.getClaimNames().forEach(claimName -> {
                if (!userInfo.containsKey(claimName)) {
                    userInfo.put(claimName, jwt.getClaim(claimName));
                }
            });
            
        } catch (Exception e) {
            // Fallback para métodos básicos
            userInfo.put("warning", "Erro ao acessar JWT, usando métodos básicos");
            userInfo.put("name", principal.getName());
            userInfo.put("subject", principal.getSubject());
        }
        
        return userInfo;
    }
    */

    @GET
    @Path("/check")
    @Authenticated
    public Response checkAuthentication() {
        return Response.ok(Map.of(
                "authenticated", true,
                "timestamp", System.currentTimeMillis()
        )).build();
    }

    @GET
    @Path("/health")
    @PermitAll
    public Response health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("oidcConfigured", oidcMetadata != null);
        health.put("authServerUrl", authServerUrl);
        
        if (oidcMetadata != null) {
            health.put("issuer", oidcMetadata.getIssuer());
        }
        
        return Response.ok(health).build();
    }

    // Métodos auxiliares
    
    private String extractRealmFromUrl(String authServerUrl) {
        if (authServerUrl != null && authServerUrl.contains("/realms/")) {
            return authServerUrl.substring(authServerUrl.lastIndexOf("/realms/") + 8);
        }
        return "master";
    }
    
    private String getDefaultRedirectUri() {
        return "http://localhost:3000/auth/callback"; // Ajuste conforme seu frontend
    }
}