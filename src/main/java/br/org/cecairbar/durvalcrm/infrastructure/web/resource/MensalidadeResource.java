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
    public Response listar(
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
            // Log do erro para debug
            System.err.println("Erro ao listar mensalidades: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao listar mensalidades: " + e.getMessage())
                .build();
        }
    }

    /**
     * Endpoint para gerar cobranças para um período
     * POST /mensalidades/gerar?mes=7&ano=2025
     */
    @POST
    @Path("/gerar")
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
            // Log do erro para debug
            System.err.println("Erro ao gerar cobranças: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao gerar cobranças: " + e.getMessage())
                .build();
        }
    }

    /**
     * Endpoint para obter uma mensalidade específica
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
                .entity("Erro ao obter mensalidade")
                .build();
        }
    }

    /**
     * Endpoint para atualizar uma mensalidade
     * PUT /mensalidades/{id}
     */
    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") String id, MensalidadeDTO mensalidadeDTO) {
        try {
            // Implementar lógica de atualização
            // MensalidadeDTO mensalidadeAtualizada = atualizarMensalidadeUseCase.executar(id, mensalidadeDTO);
            return Response.ok().entity("Atualização de mensalidade não implementada ainda").build();
        } catch (Exception e) {
            System.err.println("Erro ao atualizar mensalidade: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao atualizar mensalidade")
                .build();
        }
    }

    /**
     * Endpoint para obter QR Code de uma mensalidade
     * GET /mensalidades/{id}/qrcode
     */
    @GET
    @Path("/{id}/qrcode")
    public Response obterQRCode(@PathParam("id") String id) {
        try {
            // Por enquanto, retornar um QR Code simulado
            String qrCodeSimulado = "00020126580014br.gov.bcb.pix0136123e4567-e12b-12d1-a456-426614174000052040000530398654041.005802BR5913Associacao XYZ6009Guarulhos6304ABCD";
            
            return Response.ok()
                .entity("{\"qrCode\":\"" + qrCodeSimulado + "\"}")
                .build();
        } catch (Exception e) {
            System.err.println("Erro ao obter QR Code: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao obter QR Code")
                .build();
        }
    }

    /**
     * Endpoint para healthcheck específico das mensalidades
     * GET /mensalidades/health
     */
    @GET
    @Path("/health")
    @PermitAll // Permitir acesso sem autenticação para healthcheck
    public Response health() {
        return Response.ok()
            .entity("{\"status\":\"UP\",\"service\":\"mensalidades\"}")
            .build();
    }
}