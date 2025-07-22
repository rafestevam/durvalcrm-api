package br.org.cecairbar.durvalcrm.application.usecase.dashboard;

import br.org.cecairbar.durvalcrm.application.dto.DashboardDTO;

public interface DashboardUseCase {
    
    DashboardDTO obterDashboard(int mes, int ano);
    
}