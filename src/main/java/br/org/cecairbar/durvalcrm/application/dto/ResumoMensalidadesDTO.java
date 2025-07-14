package br.org.cecairbar.durvalcrm.application.dto;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.model.StatusMensalidade;

import java.math.BigDecimal;
import java.util.List;

public class ResumoMensalidadesDTO {
    public int totalAssociados;
    public int pagas;
    public int pendentes;
    public int atrasadas;
    public BigDecimal valorArrecadado;
    public BigDecimal valorPendente;

    public static ResumoMensalidadesDTO criarDoList(List<Mensalidade> mensalidades) {
        ResumoMensalidadesDTO resumo = new ResumoMensalidadesDTO();
        
        resumo.totalAssociados = mensalidades.size();
        resumo.pagas = (int) mensalidades.stream()
            .filter(m -> m.getStatus() == StatusMensalidade.PAGA).count();
        resumo.pendentes = (int) mensalidades.stream()
            .filter(m -> m.getStatus() == StatusMensalidade.PENDENTE).count();
        resumo.atrasadas = (int) mensalidades.stream()
            .filter(m -> m.getStatus() == StatusMensalidade.ATRASADA).count();
        
        resumo.valorArrecadado = mensalidades.stream()
            .filter(m -> m.getStatus() == StatusMensalidade.PAGA)
            .map(Mensalidade::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        resumo.valorPendente = mensalidades.stream()
            .filter(m -> m.getStatus() != StatusMensalidade.PAGA)
            .map(Mensalidade::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return resumo;
    }
}
