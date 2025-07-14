package br.org.cecairbar.durvalcrm.application.usecase;

import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.application.service.PixService;
import br.org.cecairbar.durvalcrm.application.dto.ResultadoGeracaoDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class GerarCobrancasMensaisUseCaseTest {

    @InjectMock
    AssociadoRepository associadoRepository;
    
    @InjectMock
    MensalidadeRepository mensalidadeRepository;
    
    @InjectMock
    PixService pixService;

    @Inject
    GerarCobrancasMensaisUseCase useCase;

    @Test
    void deveGerarCobrancasParaAssociadosAtivos() {
        // Mock associados ativos
        Associado associado1 = new Associado();
        associado1.setId(UUID.randomUUID());
        associado1.setNomeCompleto("João Silva");
        associado1.setAtivo(true);
        
        Associado associado2 = new Associado();
        associado2.setId(UUID.randomUUID());
        associado2.setNomeCompleto("Maria Santos");
        associado2.setAtivo(true);

        when(associadoRepository.findByAtivo(true))
            .thenReturn(Arrays.asList(associado1, associado2));
        
        // Mock não existir mensalidades
        when(mensalidadeRepository.existsByAssociadoEPeriodo(any(), anyInt(), anyInt()))
            .thenReturn(false);
        
        // Mock PIX service
        when(pixService.gerarQRCode(any(), any(), any()))
            .thenReturn("mock-qr-code");

        ResultadoGeracaoDTO resultado = useCase.executar(7, 2025);

        assertEquals(2, resultado.cobrancasGeradas);
        assertEquals(0, resultado.jaExistiam);
        assertEquals(2, resultado.totalAssociados);
        verify(mensalidadeRepository, times(2)).save(any());
    }
    
    @Test
    void naoDeveGerarCobrancasJaExistentes() {
        // Mock associado ativo
        Associado associado = new Associado();
        associado.setId(UUID.randomUUID());
        associado.setNomeCompleto("João Silva");
        associado.setAtivo(true);

        when(associadoRepository.findByAtivo(true))
            .thenReturn(Arrays.asList(associado));
        
        // Mock mensalidade já existe
        when(mensalidadeRepository.existsByAssociadoEPeriodo(any(), anyInt(), anyInt()))
            .thenReturn(true);

        ResultadoGeracaoDTO resultado = useCase.executar(7, 2025);

        assertEquals(0, resultado.cobrancasGeradas);
        assertEquals(1, resultado.jaExistiam);
        assertEquals(1, resultado.totalAssociados);
        verify(mensalidadeRepository, never()).save(any());
    }
}