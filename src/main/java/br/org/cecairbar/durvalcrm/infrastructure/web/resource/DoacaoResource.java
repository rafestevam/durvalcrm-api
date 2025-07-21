package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import br.org.cecairbar.durvalcrm.application.doacao.*;
import br.org.cecairbar.durvalcrm.domain.model.MetodoPagamento;

@Path("/api/doacoes")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DoacaoResource {
    
    @Inject
    DoacaoService doacaoService;
    
    @GET
    public Response listar() {
        return Response.ok(doacaoService.listarTodas()).build();
    }
    
    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") UUID id) {
        DoacaoDTO doacao = doacaoService.buscarPorId(id);
        if (doacao == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(doacao).build();
    }
    
    @GET
    @Path("/associado/{associadoId}")
    public Response listarPorAssociado(@PathParam("associadoId") UUID associadoId) {
        return Response.ok(doacaoService.listarPorAssociado(associadoId)).build();
    }
    
    @GET
    @Path("/periodo")
    public Response listarPorPeriodo(
            @QueryParam("inicio") String inicioStr,
            @QueryParam("fim") String fimStr) {
        
        LocalDateTime inicio = parseDateTime(inicioStr);
        LocalDateTime fim = parseDateTime(fimStr);
        
        return Response.ok(doacaoService.listarPorPeriodo(inicio, fim)).build();
    }
    
    @POST
    public Response criar(@Valid DoacaoDTO dto) {
        try {
            DoacaoDTO doacao = doacaoService.criar(dto);
            return Response.status(Response.Status.CREATED).entity(doacao).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }
    
    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") UUID id, @Valid DoacaoDTO dto) {
        try {
            DoacaoDTO doacao = doacaoService.atualizar(id, dto);
            return Response.ok(doacao).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }
    
    @POST
    @Path("/{id}/confirmar-pagamento")
    public Response confirmarPagamento(
            @PathParam("id") UUID id,
            ConfirmarPagamentoRequest request) {
        try {
            MetodoPagamento metodoPagamento = MetodoPagamento.valueOf(request.getMetodoPagamento());
            DoacaoDTO doacao = doacaoService.confirmarPagamento(
                    id, request.getCodigoTransacao(), metodoPagamento);
            return Response.ok(doacao).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }
    
    @POST
    @Path("/{id}/cancelar")
    public Response cancelar(@PathParam("id") UUID id) {
        try {
            DoacaoDTO doacao = doacaoService.cancelar(id);
            return Response.ok(doacao).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") UUID id) {
        try {
            doacaoService.excluir(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }
    
    @GET
    @Path("/estatisticas")
    public Response obterEstatisticas(
            @QueryParam("inicio") String inicioStr,
            @QueryParam("fim") String fimStr) {
        
        LocalDateTime inicio = parseDateTime(inicioStr);
        LocalDateTime fim = parseDateTime(fimStr);
        
        return Response.ok(doacaoService.obterEstatisticas(inicio, fim)).build();
    }
    
    @GET
    @Path("/{id}/pix")
    public Response gerarCodigoPix(@PathParam("id") UUID id) {
        try {
            String codigoPix = doacaoService.gerarCodigoPix(id);
            return Response.ok(new PixResponse(codigoPix)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }
    
    public static class ConfirmarPagamentoRequest {
        private String codigoTransacao;
        private String metodoPagamento;
        
        public String getCodigoTransacao() {
            return codigoTransacao;
        }
        
        public void setCodigoTransacao(String codigoTransacao) {
            this.codigoTransacao = codigoTransacao;
        }
        
        public String getMetodoPagamento() {
            return metodoPagamento;
        }
        
        public void setMetodoPagamento(String metodoPagamento) {
            this.metodoPagamento = metodoPagamento;
        }
    }
    
    public static class PixResponse {
        private String codigoPix;
        
        public PixResponse(String codigoPix) {
            this.codigoPix = codigoPix;
        }
        
        public String getCodigoPix() {
            return codigoPix;
        }
        
        public void setCodigoPix(String codigoPix) {
            this.codigoPix = codigoPix;
        }
    }
    
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // Primeiro tenta com timezone (ISO_OFFSET_DATE_TIME)
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTimeStr);
            return offsetDateTime.toLocalDateTime();
        } catch (Exception e) {
            try {
                // Se falhar, tenta sem timezone (ISO_LOCAL_DATE_TIME)
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e2) {
                throw new IllegalArgumentException("Formato de data inválido: " + dateTimeStr);
            }
        }
    }
}