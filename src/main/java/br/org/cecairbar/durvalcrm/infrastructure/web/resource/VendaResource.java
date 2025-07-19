package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import br.org.cecairbar.durvalcrm.application.usecase.venda.VendaUseCase;
import br.org.cecairbar.durvalcrm.application.dto.VendaDTO;
import br.org.cecairbar.durvalcrm.application.dto.ResumoVendasDTO;
import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/vendas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class VendaResource {

    @Inject
    VendaUseCase vendaUseCase;

    /**
     * Criar nova venda
     * POST /api/vendas
     */
    @POST
    public Response criar(@Valid VendaDTO vendaDTO) {
        try {
            VendaDTO vendaCriada = vendaUseCase.criar(vendaDTO);
            return Response.status(Response.Status.CREATED).entity(vendaCriada).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Erro ao criar venda",
                    "message", e.getMessage(),
                    "status", 500
                ))
                .build();
        }
    }

    /**
     * Buscar venda por ID
     * GET /api/vendas/{id}
     */
    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") String id) {
        try {
            UUID vendaId = UUID.fromString(id);
            VendaDTO venda = vendaUseCase.buscarPorId(vendaId);
            return Response.ok(venda).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "ID inválido",
                    "status", 400
                ))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Erro ao buscar venda",
                    "message", e.getMessage(),
                    "status", 500
                ))
                .build();
        }
    }

    /**
     * Atualizar venda
     * PUT /api/vendas/{id}
     */
    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") String id, @Valid VendaDTO vendaDTO) {
        try {
            UUID vendaId = UUID.fromString(id);
            VendaDTO vendaAtualizada = vendaUseCase.atualizar(vendaId, vendaDTO);
            return Response.ok(vendaAtualizada).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "ID inválido",
                    "status", 400
                ))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Erro ao atualizar venda",
                    "message", e.getMessage(),
                    "status", 500
                ))
                .build();
        }
    }

    /**
     * Deletar venda
     * DELETE /api/vendas/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deletar(@PathParam("id") String id) {
        try {
            UUID vendaId = UUID.fromString(id);
            vendaUseCase.deletar(vendaId);
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "ID inválido",
                    "status", 400
                ))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Erro ao deletar venda",
                    "message", e.getMessage(),
                    "status", 500
                ))
                .build();
        }
    }

    /**
     * Listar todas as vendas
     * GET /api/vendas
     */
    @GET
    public Response listarTodas() {
        try {
            List<VendaDTO> vendas = vendaUseCase.listarTodas();
            return Response.ok(vendas).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Erro ao listar vendas",
                    "message", e.getMessage(),
                    "status", 500
                ))
                .build();
        }
    }

    /**
     * Listar vendas por período
     * GET /api/vendas/periodo?dataInicio=2025-01-01&dataFim=2025-01-31
     */
    @GET
    @Path("/periodo")
    public Response listarPorPeriodo(
        @QueryParam("dataInicio") String dataInicioStr,
        @QueryParam("dataFim") String dataFimStr
    ) {
        try {
            // Parse das datas
            Instant dataInicio = LocalDate.parse(dataInicioStr).atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant dataFim = LocalDate.parse(dataFimStr).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();
            
            List<VendaDTO> vendas = vendaUseCase.listarPorPeriodo(dataInicio, dataFim);
            return Response.ok(vendas).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Erro ao processar período",
                    "message", e.getMessage(),
                    "status", 400
                ))
                .build();
        }
    }

    /**
     * Listar vendas por origem
     * GET /api/vendas/origem/{origem}
     */
    @GET
    @Path("/origem/{origem}")
    public Response listarPorOrigem(@PathParam("origem") String origem) {
        try {
            OrigemVenda origemVenda = OrigemVenda.valueOf(origem.toUpperCase());
            List<VendaDTO> vendas = vendaUseCase.listarPorOrigem(origemVenda);
            return Response.ok(vendas).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Origem inválida. Use: CANTINA, BAZAR ou LIVROS",
                    "status", 400
                ))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Erro ao listar vendas por origem",
                    "message", e.getMessage(),
                    "status", 500
                ))
                .build();
        }
    }

    /**
     * Listar vendas recentes (últimos 30 dias)
     * GET /api/vendas/recentes
     */
    @GET
    @Path("/recentes")
    public Response listarRecentes() {
        try {
            List<VendaDTO> vendas = vendaUseCase.listarRecentes();
            return Response.ok(vendas).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Erro ao listar vendas recentes",
                    "message", e.getMessage(),
                    "status", 500
                ))
                .build();
        }
    }

    /**
     * Obter resumo de vendas por período
     * GET /api/vendas/resumo?dataInicio=2025-01-01&dataFim=2025-01-31
     */
    @GET
    @Path("/resumo")
    public Response obterResumo(
        @QueryParam("dataInicio") String dataInicioStr,
        @QueryParam("dataFim") String dataFimStr
    ) {
        try {
            // Parse das datas
            Instant dataInicio = LocalDate.parse(dataInicioStr).atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant dataFim = LocalDate.parse(dataFimStr).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();
            
            ResumoVendasDTO resumo = vendaUseCase.obterResumo(dataInicio, dataFim);
            return Response.ok(resumo).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Erro ao processar período",
                    "message", e.getMessage(),
                    "status", 400
                ))
                .build();
        }
    }

    /**
     * Obter resumo de vendas por origem e período
     * GET /api/vendas/resumo/origem/{origem}?dataInicio=2025-01-01&dataFim=2025-01-31
     */
    @GET
    @Path("/resumo/origem/{origem}")
    public Response obterResumoPorOrigem(
        @PathParam("origem") String origem,
        @QueryParam("dataInicio") String dataInicioStr,
        @QueryParam("dataFim") String dataFimStr
    ) {
        try {
            OrigemVenda origemVenda = OrigemVenda.valueOf(origem.toUpperCase());
            
            // Parse das datas
            Instant dataInicio = LocalDate.parse(dataInicioStr).atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant dataFim = LocalDate.parse(dataFimStr).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();
            
            ResumoVendasDTO resumo = vendaUseCase.obterResumoPorOrigem(origemVenda, dataInicio, dataFim);
            return Response.ok(resumo).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Origem inválida. Use: CANTINA, BAZAR ou LIVROS",
                    "status", 400
                ))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Erro ao obter resumo por origem",
                    "message", e.getMessage(),
                    "status", 500
                ))
                .build();
        }
    }
}