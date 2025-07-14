package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import br.org.cecairbar.durvalcrm.application.usecase.GerarCobrancasMensaisUseCase;
import br.org.cecairbar.durvalcrm.application.usecase.ConsultarMensalidadesUseCase;
import br.org.cecairbar.durvalcrm.application.dto.MensalidadeDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResumoMensalidadesDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResultadoGeracaoDTO;

import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll; // Added missing import
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

    /**
     * Endpoint para obter resumo das mensalidades por período
     * GET /mensalidades/resumo?mes=7&ano=2025
     */
    @GET
    @Path("/resumo")
    public Response obterResumo(
        @QueryParam("mes") @Min(1) @Max(12) int mes,
        @QueryParam("ano") @Min(2020) @Max(2030) int ano
    ) {
        try {
            // Validação dos parâmetros
            if (mes == 0 || ano == 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parâmetros mes e ano são obrigatórios")
                    .build();
            }

            ResumoMensalidadesDTO resumo = consultarMensalidadesUseCase.obterResumo(mes, ano);
            return Response.ok(resumo).build();
        } catch (Exception e) {
            // Log do erro para debug
            System.err.println("Erro ao obter resumo das mensalidades: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao obter resumo das mensalidades: " + e.getMessage())
                .build();
        }
    }

    /**
     * Endpoint para listar mensalidades por período
     * GET /mensalidades?mes=7&ano=2025
     */
    @GET
    public Response listarPorPeriodo(
        @QueryParam("mes") @Min(1) @Max(12) int mes,
        @QueryParam("ano") @Min(2020) @Max(2030) int ano
    ) {
        try {
            // Validação dos parâmetros
            if (mes == 0 || ano == 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parâmetros mes e ano são obrigatórios")
                    .build();
            }

            List<MensalidadeDTO> mensalidades = consultarMensalidadesUseCase.listarPorPeriodo(mes, ano);
            return Response.ok(mensalidades).build();
        } catch (Exception e) {
            System.err.println("Erro ao listar mensalidades: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao listar mensalidades: " + e.getMessage())
                .build();
        }
    }

    /**
     * Endpoint para obter uma mensalidade específica por ID
     * GET /mensalidades/{id}
     */
    @GET
    @Path("/{id}")
    public Response obterPorId(@PathParam("id") String id) {
        try {
            MensalidadeDTO mensalidade = consultarMensalidadesUseCase.obterPorId(id);
            
            if (mensalidade == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Mensalidade não encontrada")
                    .build();
            }
            
            return Response.ok(mensalidade).build();
        } catch (Exception e) {
            System.err.println("Erro ao obter mensalidade por ID: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao obter mensalidade: " + e.getMessage())
                .build();
        }
    }

    /**
     * Endpoint para listar mensalidades por status
     * GET /mensalidades/status/{status}
     */
    @GET
    @Path("/status/{status}")
    public Response listarPorStatus(@PathParam("status") String status) {
        try {
            List<MensalidadeDTO> mensalidades = consultarMensalidadesUseCase.listarPorStatus(status);
            return Response.ok(mensalidades).build();
        } catch (Exception e) {
            System.err.println("Erro ao listar mensalidades por status: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao listar mensalidades por status: " + e.getMessage())
                .build();
        }
    }

    /**
     * Endpoint para gerar cobranças mensais
     * POST /mensalidades/gerar-cobrancas?mes=7&ano=2025
     */
    @POST
    @Path("/gerar-cobrancas")
    public Response gerarCobrancas(
        @QueryParam("mes") @Min(1) @Max(12) int mes,
        @QueryParam("ano") @Min(2020) @Max(2030) int ano
    ) {
        try {
            // Validação dos parâmetros
            if (mes == 0 || ano == 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parâmetros mes e ano são obrigatórios")
                    .build();
            }

            ResultadoGeracaoDTO resultado = gerarCobrancasUseCase.executar(mes, ano);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            System.err.println("Erro ao gerar cobranças: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao gerar cobranças: " + e.getMessage())
                .build();
        }
    }

    /**
     * Endpoint público para webhook de PIX (se necessário)
     * POST /mensalidades/webhook/pix
     */
    @POST
    @Path("/webhook/pix")
    @PermitAll // This endpoint now has the proper import for PermitAll
    public Response webhookPix(String payload) {
        try {
            // TODO: Implementar processamento do webhook PIX
            return Response.ok().build();
        } catch (Exception e) {
            System.err.println("Erro no webhook PIX: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro no processamento do webhook")
                .build();
        }
    }
}