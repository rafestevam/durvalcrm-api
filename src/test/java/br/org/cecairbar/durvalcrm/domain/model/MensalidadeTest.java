package br.org.cecairbar.durvalcrm.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MensalidadeTest {

    @Test
    void deveCriarMensalidadeComDadosCorretos() {
        UUID associadoId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("10.90");
        
        Mensalidade mensalidade = Mensalidade.criar(associadoId, 7, 2025, valor);
        
        assertNotNull(mensalidade.getId());
        assertEquals(associadoId, mensalidade.getAssociadoId());
        assertEquals(7, mensalidade.getMesReferencia());
        assertEquals(2025, mensalidade.getAnoReferencia());
        assertEquals(valor, mensalidade.getValor());
        assertEquals(StatusMensalidade.PENDENTE, mensalidade.getStatus());
        assertNotNull(mensalidade.getDataVencimento());
        assertNotNull(mensalidade.getIdentificadorPix());
    }

    @Test
    void deveMarcarComoPaga() {
        UUID associadoId = UUID.randomUUID();
        Mensalidade mensalidade = Mensalidade.criar(associadoId, 7, 2025, new BigDecimal("10.90"));
        LocalDateTime dataPagamento = LocalDateTime.now();
        
        mensalidade.marcarComoPaga(dataPagamento);
        
        assertEquals(StatusMensalidade.PAGA, mensalidade.getStatus());
        assertEquals(dataPagamento, mensalidade.getDataPagamento());
    }

    @Test
    void naoDevePermitirMarcarJaPagaComoPaga() {
        UUID associadoId = UUID.randomUUID();
        Mensalidade mensalidade = Mensalidade.criar(associadoId, 7, 2025, new BigDecimal("10.90"));
        mensalidade.marcarComoPaga(LocalDateTime.now());
        
        assertThrows(IllegalStateException.class, () -> {
            mensalidade.marcarComoPaga(LocalDateTime.now());
        });
    }

    @Test
    void deveGerarIdentificadorPixUnico() {
        UUID associadoId1 = UUID.randomUUID();
        UUID associadoId2 = UUID.randomUUID();
        
        Mensalidade mens1 = Mensalidade.criar(associadoId1, 7, 2025, new BigDecimal("10.90"));
        Mensalidade mens2 = Mensalidade.criar(associadoId2, 7, 2025, new BigDecimal("10.90"));
        
        assertNotEquals(mens1.getIdentificadorPix(), mens2.getIdentificadorPix());
    }
}