package br.org.cecairbar.durvalcrm.application.doacao;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.org.cecairbar.durvalcrm.domain.model.StatusDoacao;
import br.org.cecairbar.durvalcrm.domain.model.TipoDoacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DoacaoResumoDTO {
    private Long id;
    private String nomeAssociado;
    private BigDecimal valor;
    private TipoDoacao tipo;
    private StatusDoacao status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataDoacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeAssociado() {
        return nomeAssociado;
    }

    public void setNomeAssociado(String nomeAssociado) {
        this.nomeAssociado = nomeAssociado;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public TipoDoacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoDoacao tipo) {
        this.tipo = tipo;
    }

    public StatusDoacao getStatus() {
        return status;
    }

    public void setStatus(StatusDoacao status) {
        this.status = status;
    }

    public LocalDateTime getDataDoacao() {
        return dataDoacao;
    }

    public void setDataDoacao(LocalDateTime dataDoacao) {
        this.dataDoacao = dataDoacao;
    }
}