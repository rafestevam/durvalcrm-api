package br.org.cecairbar.durvalcrm.application.usecase.dashboard;

import br.org.cecairbar.durvalcrm.application.dto.DashboardDTO;
import br.org.cecairbar.durvalcrm.application.dto.DashboardDTO.AssociadoResumoDTO;
import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.domain.repository.VendaRepository;
import br.org.cecairbar.durvalcrm.domain.repository.DoacaoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.ArrayList;

@ApplicationScoped
public class DashboardUseCaseImpl implements DashboardUseCase {
    
    @Inject
    AssociadoRepository associadoRepository;
    
    @Inject
    MensalidadeRepository mensalidadeRepository;
    
    @Inject
    VendaRepository vendaRepository;
    
    @Inject
    DoacaoRepository doacaoRepository;
    
    @Override
    public DashboardDTO obterDashboard(int mes, int ano) {
        // Definir período
        YearMonth mesAno = YearMonth.of(ano, mes);
        LocalDate dataInicio = mesAno.atDay(1);
        LocalDate dataFim = mesAno.atEndOfMonth();
        
        Instant inicio = dataInicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant fim = dataFim.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        
        // Obter receitas
        BigDecimal receitaMensalidades = mensalidadeRepository.obterValorArrecadadoPorPeriodo(mes, ano);
        BigDecimal receitaCantina = vendaRepository.sumValorByOrigemAndPeriodo(OrigemVenda.CANTINA, inicio, fim);
        BigDecimal receitaBazar = vendaRepository.sumValorByOrigemAndPeriodo(OrigemVenda.BAZAR, inicio, fim);
        BigDecimal receitaLivros = vendaRepository.sumValorByOrigemAndPeriodo(OrigemVenda.LIVROS, inicio, fim);
        BigDecimal receitaDoacoes = doacaoRepository.obterTotalDoacoesPorPeriodo(inicio, fim);
        
        // Receita consolidada
        BigDecimal receitaConsolidada = BigDecimal.ZERO
            .add(receitaMensalidades != null ? receitaMensalidades : BigDecimal.ZERO)
            .add(receitaCantina != null ? receitaCantina : BigDecimal.ZERO)
            .add(receitaBazar != null ? receitaBazar : BigDecimal.ZERO)
            .add(receitaLivros != null ? receitaLivros : BigDecimal.ZERO)
            .add(receitaDoacoes != null ? receitaDoacoes : BigDecimal.ZERO);
        
        // Obter estatísticas de associados
        Long totalAssociados = associadoRepository.count();
        List<String> associadosComMensalidadePaga = mensalidadeRepository.obterAssociadosComStatusPorPeriodo(mes, ano, StatusMensalidade.PAGA);
        Long pagantesMes = (long) associadosComMensalidadePaga.size();
        
        // Obter lista de adimplentes e inadimplentes
        List<AssociadoResumoDTO> adimplentes = new ArrayList<>();
        List<AssociadoResumoDTO> inadimplentes = new ArrayList<>();
        
        associadoRepository.findAll().forEach(associado -> {
            AssociadoResumoDTO resumo = AssociadoResumoDTO.builder()
                .id(associado.getId().toString())
                .nomeCompleto(associado.getNomeCompleto())
                .email(associado.getEmail())
                .cpf(associado.getCpf())
                .build();
            
            if (associadosComMensalidadePaga.contains(associado.getId().toString())) {
                adimplentes.add(resumo);
            } else {
                inadimplentes.add(resumo);
            }
        });
        
        return DashboardDTO.builder()
            .receitaConsolidada(receitaConsolidada)
            .receitaMensalidades(receitaMensalidades != null ? receitaMensalidades : BigDecimal.ZERO)
            .receitaCantina(receitaCantina != null ? receitaCantina : BigDecimal.ZERO)
            .receitaBazar(receitaBazar != null ? receitaBazar : BigDecimal.ZERO)
            .receitaLivros(receitaLivros != null ? receitaLivros : BigDecimal.ZERO)
            .receitaDoacoes(receitaDoacoes != null ? receitaDoacoes : BigDecimal.ZERO)
            .pagantesMes(pagantesMes)
            .totalAssociados(totalAssociados)
            .adimplentes(adimplentes)
            .inadimplentes(inadimplentes)
            .build();
    }
}