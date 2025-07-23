package br.org.cecairbar.durvalcrm.application.usecase.dashboard;

import br.org.cecairbar.durvalcrm.application.dto.DashboardDTO;
import br.org.cecairbar.durvalcrm.application.dto.DashboardDTO.AssociadoResumoDTO;
import br.org.cecairbar.durvalcrm.application.dto.ReceitasPorMetodoPagamentoDTO;
import br.org.cecairbar.durvalcrm.domain.model.OrigemVenda;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.domain.repository.VendaRepository;
import br.org.cecairbar.durvalcrm.domain.repository.DoacaoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

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
    
    @Inject
    EntityManager entityManager;
    
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
        List<String> associadosComMensalidadesVencidas = mensalidadeRepository.obterAssociadosComMensalidadesVencidas(mes, ano);
        Long pagantesMes = (long) associadosComMensalidadePaga.size();
        
        // Obter lista de adimplentes e inadimplentes
        List<AssociadoResumoDTO> adimplentes = new ArrayList<>();
        List<AssociadoResumoDTO> inadimplentes = new ArrayList<>();
        
        associadoRepository.findAll().forEach(associado -> {
            String associadoId = associado.getId().toString();
            AssociadoResumoDTO resumo = AssociadoResumoDTO.builder()
                .id(associadoId)
                .nomeCompleto(associado.getNomeCompleto())
                .email(associado.getEmail())
                .cpf(associado.getCpf())
                .build();
            
            if (associadosComMensalidadePaga.contains(associadoId)) {
                // Associado com mensalidade paga = adimplente
                adimplentes.add(resumo);
            } else if (associadosComMensalidadesVencidas.contains(associadoId)) {
                // Associado com mensalidade vencida (PENDENTE ou ATRASADA e vencida) = inadimplente
                inadimplentes.add(resumo);
            }
            // Associados sem mensalidade para o período não aparecem em nenhuma lista
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
    
    @Override
    public ReceitasPorMetodoPagamentoDTO obterReceitasPorMetodoPagamento() {
        // Query para obter totais por método de pagamento de TODAS as receitas
        // Combina mensalidades pagas + vendas + doações confirmadas
        String jpql = "SELECT " +
                      "COALESCE(SUM(total_pix), 0) AS total_pix, " +
                      "COALESCE(SUM(total_dinheiro), 0) AS total_dinheiro " +
                      "FROM ( " +
                      "SELECT " +
                      "CASE WHEN m.metodo_pagamento = 'PIX' THEN m.valor " +
                      "WHEN m.metodo_pagamento IS NULL THEN m.valor " +
                      "ELSE 0 END AS total_pix, " +
                      "CASE WHEN m.metodo_pagamento = 'DINHEIRO' THEN m.valor " +
                      "ELSE 0 END AS total_dinheiro " +
                      "FROM mensalidades m " +
                      "WHERE m.status = 'PAGA' " +
                      "UNION ALL " +
                      "SELECT " +
                      "CASE WHEN v.forma_pagamento = 'PIX' THEN v.valor ELSE 0 END AS total_pix, " +
                      "CASE WHEN v.forma_pagamento = 'DINHEIRO' THEN v.valor ELSE 0 END AS total_dinheiro " +
                      "FROM vendas v " +
                      "UNION ALL " +
                      "SELECT " +
                      "CASE WHEN d.metodo_pagamento = 'PIX' THEN d.valor ELSE 0 END AS total_pix, " +
                      "CASE WHEN d.metodo_pagamento = 'DINHEIRO' THEN d.valor ELSE 0 END AS total_dinheiro " +
                      "FROM doacoes d " +
                      "WHERE d.status = 'CONFIRMADA' " +
                      ") AS receitas_consolidadas";
        
        System.out.println("Executando query de receitas por método:");
        System.out.println(jpql);
        
        var query = entityManager.createNativeQuery(jpql);
        var resultado = query.getSingleResult();
        
        BigDecimal totalPix = BigDecimal.ZERO;
        BigDecimal totalDinheiro = BigDecimal.ZERO;
        
        if (resultado != null) {
            Object[] row = (Object[]) resultado;
            totalPix = (BigDecimal) row[0];
            totalDinheiro = (BigDecimal) row[1];
        }
        
        BigDecimal totalGeral = totalPix.add(totalDinheiro);
        
        System.out.println("Receitas consolidadas por método:");
        System.out.println("  PIX: R$ " + totalPix);
        System.out.println("  Dinheiro: R$ " + totalDinheiro);
        System.out.println("  Total: R$ " + totalGeral);
        
        return ReceitasPorMetodoPagamentoDTO.builder()
            .totalPix(totalPix)
            .totalDinheiro(totalDinheiro)
            .totalGeral(totalGeral)
            .build();
    }
}