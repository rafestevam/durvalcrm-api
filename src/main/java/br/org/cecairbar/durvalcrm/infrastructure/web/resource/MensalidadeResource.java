package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import br.org.cecairbar.durvalcrm.application.usecase.GerarCobrancasMensaisUseCase;
import br.org.cecairbar.durvalcrm.application.usecase.ConsultarMensalidadesUseCase;
import br.org.cecairbar.durvalcrm.application.dto.MensalidadeDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResumoMensalidadesDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResultadoGeracaoDTO;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/mensalidades")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class MensalidadeResource {

    @Inject
    GerarCobrancasMensaisUseCase gerarCobrancasUseCase;

    @Inject
    ConsultarMensalidadesUseCase consultarMensalidadesUseCase;

    @GET
    @Path("/resumo")
    public Response obterResumo(
        @QueryParam("mes") @Min(1) @Max(12) int mes,
        @QueryParam("ano") @Min(2020) @Max(2030) int ano
    ) {
        try {
            ResumoMensalidadesDTO resumo = consultarMensalidadesUseCase.obterResumo(mes, ano);
            return Response.ok(resumo).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao obter resumo das mensalidades")
                .build();
        }
    }

    @GET
    public Response listar(
        @QueryParam("mes") @Min(1) @Max(12) int mes,
        @QueryParam("ano") @Min(2020) @Max(2030) int ano
    ) {
        try {
            List<MensalidadeDTO> mensalidades = consultarMensalidadesUseCase.listarPorPeriodo(mes, ano);
            return Response.ok(mensalidades).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao listar mensalidades")
                .build();
        }
    }

    @POST
    @Path("/gerar")
    public Response gerarCobrancas(
        @QueryParam("mes") @Min(1) @Max(12) int mes,
        @QueryParam("ano") @Min(2020) @Max(2030) int ano
    ) {
        try {
            ResultadoGeracaoDTO resultado = gerarCobrancasUseCase.executar(mes, ano);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao gerar cobranças")
                .build();
        }
    }
}