package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import br.org.cecairbar.durvalcrm.application.dto.DashboardDTO;
import br.org.cecairbar.durvalcrm.application.usecase.dashboard.DashboardUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Dashboard", description = "Endpoint para dados do dashboard financeiro")
public class DashboardResource {
    
    @Inject
    DashboardUseCase dashboardUseCase;
    
    @GET
    @Operation(summary = "Obter dados do dashboard", description = "Retorna os dados consolidados do dashboard financeiro para o mês e ano especificados")
    @APIResponse(responseCode = "200", description = "Dados do dashboard obtidos com sucesso")
    public Response obterDashboard(
            @Parameter(description = "Mês (1-12)", required = false)
            @QueryParam("mes") Integer mes,
            @Parameter(description = "Ano", required = false)
            @QueryParam("ano") Integer ano) {
        
        // Se não informado, usar mês/ano atual
        if (mes == null || ano == null) {
            LocalDate hoje = LocalDate.now();
            mes = mes != null ? mes : hoje.getMonthValue();
            ano = ano != null ? ano : hoje.getYear();
        }
        
        // Validar parâmetros
        if (mes < 1 || mes > 12) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Mês deve estar entre 1 e 12")
                    .build();
        }
        
        if (ano < 2000 || ano > 2100) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Ano inválido")
                    .build();
        }
        
        DashboardDTO dashboard = dashboardUseCase.obterDashboard(mes, ano);
        return Response.ok(dashboard).build();
    }
}