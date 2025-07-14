package br.org.cecairbar.durvalcrm.application.usecase;

import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.application.service.PixService;
import br.org.cecairbar.durvalcrm.application.dto.ResultadoGeracaoDTO;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class GerarCobrancasMensaisUseCaseTest {

    @Mock
    AssociadoRepository associadoRepository;
    
    @Mock
    MensalidadeRepository mensalidadeRepository;
    
    @Mock
    PixService pixService;

    private GerarCobrancasMensaisUseCase useCase;

    @Test
    void deveGerarCobrancasParaAssociadosAtivos() {
        MockitoAnnotations.openMocks(this);
        useCase = new GerarCobrancasMensaisUseCase();
        useCase.associadoRepository = associadoRepository;
        useCase.mensalidadeRepository = mensalidadeRepository;
        useCase.pixService = pixService;

        // Mock associados ativos
        Associado associado1 = new Associado();
        associado1.setId(UUID.randomUUID());
        associado1.setNomeCompleto("João Silva");
        
        Associado associado2 = new Associado();
        associado2.setId(UUID.randomUUID());
        associado2.setNomeCompleto("Maria Santos");

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
}