package br.org.cecairbar.durvalcrm.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
        assertNotNull(mensalidade.getCriadoEm());
        
        // Verificar se a data de vencimento está correta (dia 10 do mês)
        assertEquals(LocalDate.of(2025, 7, 10), mensalidade.getDataVencimento());
        
        // Verificar se a chave de referência está correta
        assertEquals("2025-07", mensalidade.getChaveReferencia());
    }

    @Test
    void deveMarcarComoPaga() {
        UUID associadoId = UUID.randomUUID();
        Mensalidade mensalidade = Mensalidade.criar(associadoId, 7, 2025, new BigDecimal("10.90"));
        Instant dataPagamento = Instant.now();
        
        mensalidade.marcarComoPaga(dataPagamento);
        
        assertEquals(StatusMensalidade.PAGA, mensalidade.getStatus());
        assertEquals(dataPagamento, mensalidade.getDataPagamento());
    }

    @Test
    void naoDevePermitirMarcarJaPagaComoPaga() {
        UUID associadoId = UUID.randomUUID();
        Mensalidade mensalidade = Mensalidade.criar(associadoId, 7, 2025, new BigDecimal("10.90"));
        mensalidade.marcarComoPaga(Instant.now());
        
        assertThrows(IllegalStateException.class, () -> {
            mensalidade.marcarComoPaga(Instant.now());
        });
    }

    @Test
    void deveGerarIdentificadorPixUnico() {
        UUID associadoId1 = UUID.randomUUID();
        UUID associadoId2 = UUID.randomUUID();
        
        Mensalidade mens1 = Mensalidade.criar(associadoId1, 7, 2025, new BigDecimal("10.90"));
        Mensalidade mens2 = Mensalidade.criar(associadoId2, 7, 2025, new BigDecimal("10.90"));
        
        assertNotEquals(mens1.getIdentificadorPix(), mens2.getIdentificadorPix());
        
        // Verificar formato do identificador PIX
        assertTrue(mens1.getIdentificadorPix().startsWith("MENS"));
        assertTrue(mens1.getIdentificadorPix().contains("072025"));
    }

    @Test
    void deveTerCriadoEmPreenchidoAutomaticamente() {
        UUID associadoId = UUID.randomUUID();
        Instant antes = Instant.now();
        
        Mensalidade mensalidade = Mensalidade.criar(associadoId, 7, 2025, new BigDecimal("10.90"));
        
        Instant depois = Instant.now();
        
        assertNotNull(mensalidade.getCriadoEm());
        assertTrue(mensalidade.getCriadoEm().isAfter(antes.minusSeconds(1)));
        assertTrue(mensalidade.getCriadoEm().isBefore(depois.plusSeconds(1)));
    }

    @Test
    void deveIdentificarMensalidadeVencida() {
        UUID associadoId = UUID.randomUUID();
        
        // Criar mensalidade com data no passado
        Mensalidade mensalidade = Mensalidade.criar(associadoId, 1, 2024, new BigDecimal("10.90"));
        
        assertTrue(mensalidade.isVencida());
        assertEquals(StatusMensalidade.PENDENTE, mensalidade.getStatus());
        
        // Atualizar status baseado na data
        mensalidade.atualizarStatus();
        assertEquals(StatusMensalidade.ATRASADA, mensalidade.getStatus());
    }

    @Test
    void naoDeveEstarVencidaQuandoDataFutura() {
        UUID associadoId = UUID.randomUUID();
        
        // Criar mensalidade para próximo ano
        Mensalidade mensalidade = Mensalidade.criar(associadoId, 12, 2026, new BigDecimal("10.90"));
        
        assertFalse(mensalidade.isVencida());
        assertEquals(StatusMensalidade.PENDENTE, mensalidade.getStatus());
        
        // Status deve permanecer pendente
        mensalidade.atualizarStatus();
        assertEquals(StatusMensalidade.PENDENTE, mensalidade.getStatus());
    }

    @Test
    void deveManterStatusPagaAposPagamento() {
        UUID associadoId = UUID.randomUUID();
        Mensalidade mensalidade = Mensalidade.criar(associadoId, 1, 2024, new BigDecimal("10.90"));
        
        // Marcar como paga
        mensalidade.marcarComoPaga(Instant.now());
        assertEquals(StatusMensalidade.PAGA, mensalidade.getStatus());
        
        // Mesmo sendo vencida, deve manter status PAGA
        mensalidade.atualizarStatus();
        assertEquals(StatusMensalidade.PAGA, mensalidade.getStatus());
    }
}