package br.org.cecairbar.durvalcrm.application.usecase;

import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.application.service.PixService;
import br.org.cecairbar.durvalcrm.application.dto.ResultadoGeracaoDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

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

    @BeforeEach
    void setUp() {
        // Reset dos mocks antes de cada teste
        reset(associadoRepository, mensalidadeRepository, pixService);
    }

    @Test
    void deveGerarCobrancasParaAssociadosAtivos() {
        // Arrange
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

        // Act
        ResultadoGeracaoDTO resultado = useCase.executar(7, 2025);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.cobrancasGeradas);
        assertEquals(0, resultado.jaExistiam);
        assertEquals(2, resultado.totalAssociados);
        
        // Verifica se save foi chamado para cada associado
        verify(mensalidadeRepository, times(2)).save(any());
        
        // Verifica se foram consultados os associados ativos
        verify(associadoRepository, times(1)).findByAtivo(true);
        
        // Verifica se foi checado se já existiam mensalidades
        verify(mensalidadeRepository, times(2)).existsByAssociadoEPeriodo(any(), eq(7), eq(2025));
        
        // Verifica se QR Code foi gerado para cada cobrança
        verify(pixService, times(2)).gerarQRCode(any(), any(), any());
    }

    @Test
    void deveRetornarZeroQuandoNaoHouverAssociadosAtivos() {
        // Arrange
        when(associadoRepository.findByAtivo(true))
            .thenReturn(Arrays.asList());

        // Act
        ResultadoGeracaoDTO resultado = useCase.executar(7, 2025);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.cobrancasGeradas);
        assertEquals(0, resultado.jaExistiam);
        assertEquals(0, resultado.totalAssociados);
        
        // Verifica que nada foi salvo
        verify(mensalidadeRepository, never()).save(any());
        verify(pixService, never()).gerarQRCode(any(), any(), any());
    }

    @Test
    void deveContarCorretamenteCobrancasJaExistentes() {
        // Arrange
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
        
        // Mock que primeira mensalidade já existe, segunda não
        when(mensalidadeRepository.existsByAssociadoEPeriodo(associado1, 7, 2025))
            .thenReturn(true);
        when(mensalidadeRepository.existsByAssociadoEPeriodo(associado2, 7, 2025))
            .thenReturn(false);
        
        // Mock PIX service
        when(pixService.gerarQRCode(any(), any(), any()))
            .thenReturn("mock-qr-code");

        // Act
        ResultadoGeracaoDTO resultado = useCase.executar(7, 2025);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.cobrancasGeradas);
        assertEquals(1, resultado.jaExistiam);
        assertEquals(2, resultado.totalAssociados);
        
        // Verifica que save foi chamado apenas uma vez (para o associado2)
        verify(mensalidadeRepository, times(1)).save(any());
        
        // Verifica que QR Code foi gerado apenas uma vez
        verify(pixService, times(1)).gerarQRCode(any(), any(), any());
    }
}