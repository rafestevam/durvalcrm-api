package br.org.cecairbar.durvalcrm.application.usecase.impl;

import br.org.cecairbar.durvalcrm.application.dto.AssociadoDTO;
import br.org.cecairbar.durvalcrm.application.mapper.AssociadoMapper;
import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

// 1. Remove @QuarkusTest e adiciona a extensão do Mockito para JUnit 5
@ExtendWith(MockitoExtension.class)
class AssociadoUseCaseImplTest {

    // 2. @InjectMocks cria uma instância de AssociadoUseCaseImpl e injeta
    // os mocks criados com @Mock automaticamente.
    @InjectMocks
    private AssociadoUseCaseImpl associadoUseCase;

    // 3. @Mock cria um dublê (mock) para cada dependência.
    @Mock
    private AssociadoRepository associadoRepository;

    @Mock
    private AssociadoMapper associadoMapper;

    private AssociadoDTO associadoDTO;
    private Associado associado;

    @BeforeEach
    void setUp() {
        // A preparação dos dados continua a mesma
        UUID id = UUID.randomUUID();
        associadoDTO = new AssociadoDTO();
        associadoDTO.setId(id);
        associadoDTO.setNomeCompleto("Usuário de Teste");
        associadoDTO.setCpf("123.456.789-00");
        associadoDTO.setEmail("teste@email.com");

        associado = new Associado();
        associado.setId(id);
        associado.setNomeCompleto("Usuário de Teste");
        associado.setCpf("123.456.789-00");
        associado.setEmail("teste@email.com");
        associado.setAtivo(true);
    }

    @Test
    void testCreate_Success() {
        // Arrange
        Mockito.when(associadoRepository.findByCpf(any())).thenReturn(Optional.empty());
        Mockito.when(associadoRepository.findByEmail(any())).thenReturn(Optional.empty());
        Mockito.when(associadoRepository.save(any(Associado.class))).thenReturn(associado);
        Mockito.when(associadoMapper.toDomain(any(AssociadoDTO.class))).thenReturn(associado);
        Mockito.when(associadoMapper.toDTO(any(Associado.class))).thenReturn(associadoDTO);

        // Act
        AssociadoDTO result = associadoUseCase.create(associadoDTO);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Usuário de Teste", result.getNomeCompleto());
        Mockito.verify(associadoRepository, Mockito.times(1)).save(any(Associado.class));
    }

    @Test
    void testCreate_ConflictCpf() {
        // Arrange
        Mockito.when(associadoRepository.findByCpf("123.456.789-00")).thenReturn(Optional.of(new Associado()));

        // Act & Assert
        WebApplicationException exception = Assertions.assertThrows(WebApplicationException.class, () -> {
            associadoUseCase.create(associadoDTO);
        });

        Assertions.assertEquals(409, exception.getResponse().getStatus());
        Assertions.assertEquals("CPF já cadastrado", exception.getMessage());
        Mockito.verify(associadoRepository, Mockito.never()).save(any(Associado.class));
    }
}