package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @ConfigProperty(name = "quarkus.oidc.auth-server-url", defaultValue = "http://localhost:8080/realms/durval-crm")
    String authServerUrl;

    @ConfigProperty(name = "quarkus.oidc.client-id", defaultValue = "durvalcrm-app")
    String clientId;

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
        
        return response;
    }

    /**
     * Endpoint para logout - invalida a sessão
     * Marcado como PermitAll para ser acessível sem autenticação
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
}