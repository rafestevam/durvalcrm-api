package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import io.quarkus.oidc.OidcConfiguration;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    OidcConfiguration oidcConfiguration;

    @GET
    @Path("/login-info")
    public Response getLoginInfo() {
        // Endpoint público para fornecer informações de login
        Map<String, Object> loginInfo = Map.of(
            "authServerUrl", oidcConfiguration.authServerUrl().orElse("http://localhost:8080/realms/durval-crm"),
            "clientId", oidcConfiguration.clientId().orElse("durvalcrm-app"),
            "redirectUri", "http://localhost:3000/callback"
        );
        return Response.ok(loginInfo).build();
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout() {
        // Endpoint para logout - em uma implementação completa,
        // invalidaria o token no servidor de autorização
        Map<String, String> result = Map.of(
            "message", "Logout realizado com sucesso"
        );
        return Response.ok(result).build();
    }
}