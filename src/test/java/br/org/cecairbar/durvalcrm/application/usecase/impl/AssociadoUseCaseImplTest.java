package br.org.cecairbar.durvalcrm.application.usecase.impl;

import br.org.cecairbar.durvalcrm.application.dto.AssociadoDTO;
import br.org.cecairbar.durvalcrm.application.mapper.AssociadoMapper;
import br.org.cecairbar.durvalcrm.domain.model.Associado;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class AssociadoUseCaseImplTest {

    @InjectMocks
    private AssociadoUseCaseImpl associadoUseCase;

    @Mock
    private AssociadoRepository associadoRepository;

    @Mock
    private AssociadoMapper associadoMapper;

    private AssociadoDTO associadoDTO;
    private Associado associado;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        associadoDTO = new AssociadoDTO();
        associadoDTO.setId(testId);
        associadoDTO.setNomeCompleto("Usuário de Teste");
        associadoDTO.setCpf("123.456.789-00");
        associadoDTO.setEmail("teste@email.com");
        associadoDTO.setTelefone("(11) 99999-9999");

        associado = new Associado();
        associado.setId(testId);
        associado.setNomeCompleto("Usuário de Teste");
        associado.setCpf("123.456.789-00");
        associado.setEmail("teste@email.com");
        associado.setTelefone("(11) 99999-9999");
        associado.setAtivo(true);
    }

    @Test
    void testCreate_Success() {
        // Arrange
        Mockito.when(associadoRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        Mockito.when(associadoRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        Mockito.when(associadoMapper.toDomain(any(AssociadoDTO.class))).thenReturn(associado);
        Mockito.when(associadoRepository.save(any(Associado.class))).thenReturn(associado);
        Mockito.when(associadoMapper.toDTO(any(Associado.class))).thenReturn(associadoDTO);

        // Act
        AssociadoDTO result = associadoUseCase.create(associadoDTO);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Usuário de Teste", result.getNomeCompleto());
        Assertions.assertEquals("123.456.789-00", result.getCpf());
        
        // Verify interactions
        Mockito.verify(associadoRepository, Mockito.times(1)).findByCpf("123.456.789-00");
        Mockito.verify(associadoRepository, Mockito.times(1)).findByEmail("teste@email.com");
        Mockito.verify(associadoMapper, Mockito.times(1)).toDomain(associadoDTO);
        Mockito.verify(associadoRepository, Mockito.times(1)).save(associado);
        Mockito.verify(associadoMapper, Mockito.times(1)).toDTO(associado);
    }

    @Test
    void testCreate_ConflictCpf() {
        // Arrange
        Mockito.when(associadoRepository.findByCpf("123.456.789-00"))
                .thenReturn(Optional.of(new Associado()));

        // Act & Assert
        WebApplicationException exception = Assertions.assertThrows(WebApplicationException.class, () -> {
            associadoUseCase.create(associadoDTO);
        });

        Assertions.assertEquals(409, exception.getResponse().getStatus());
        Assertions.assertEquals("CPF já cadastrado", exception.getMessage());
        
        // Verify that save was never called
        Mockito.verify(associadoRepository, Mockito.never()).save(any(Associado.class));
    }

    @Test
    void testCreate_ConflictEmail() {
        // Arrange
        Mockito.when(associadoRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        Mockito.when(associadoRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(new Associado()));

        // Act & Assert
        WebApplicationException exception = Assertions.assertThrows(WebApplicationException.class, () -> {
            associadoUseCase.create(associadoDTO);
        });

        Assertions.assertEquals(409, exception.getResponse().getStatus());
        Assertions.assertEquals("E-mail já cadastrado", exception.getMessage());
        
        // Verify that save was never called
        Mockito.verify(associadoRepository, Mockito.never()).save(any(Associado.class));
    }

    @Test
    void testFindById_Success() {
        // Arrange
        Mockito.when(associadoRepository.findById(testId)).thenReturn(Optional.of(associado));
        Mockito.when(associadoMapper.toDTO(associado)).thenReturn(associadoDTO);

        // Act
        AssociadoDTO result = associadoUseCase.findById(testId);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(testId, result.getId());
        Assertions.assertEquals("Usuário de Teste", result.getNomeCompleto());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        Mockito.when(associadoRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(NotFoundException.class, () -> {
            associadoUseCase.findById(testId);
        });
    }

    @Test
    void testFindAll_Success() {
        // Arrange
        List<Associado> associados = Arrays.asList(associado);
        List<AssociadoDTO> associadosDTO = Arrays.asList(associadoDTO);
        
        Mockito.when(associadoRepository.findAll("teste")).thenReturn(associados);
        Mockito.when(associadoMapper.toDTOList(associados)).thenReturn(associadosDTO);

        // Act
        List<AssociadoDTO> result = associadoUseCase.findAll("teste");

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Usuário de Teste", result.get(0).getNomeCompleto());
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        AssociadoDTO updateDTO = new AssociadoDTO();
        updateDTO.setNomeCompleto("Nome Atualizado");
        updateDTO.setCpf("123.456.789-00");
        updateDTO.setEmail("teste@email.com");
        updateDTO.setTelefone("(11) 88888-8888");

        Associado updatedAssociado = new Associado();
        updatedAssociado.setId(testId);
        updatedAssociado.setNomeCompleto("Nome Atualizado");
        updatedAssociado.setCpf("123.456.789-00");
        updatedAssociado.setEmail("teste@email.com");
        updatedAssociado.setTelefone("(11) 88888-8888");
        updatedAssociado.setAtivo(true);

        AssociadoDTO updatedDTO = new AssociadoDTO();
        updatedDTO.setId(testId);
        updatedDTO.setNomeCompleto("Nome Atualizado");
        updatedDTO.setTelefone("(11) 88888-8888");

        Mockito.when(associadoRepository.findById(testId)).thenReturn(Optional.of(associado));
        Mockito.when(associadoRepository.save(any(Associado.class))).thenReturn(updatedAssociado);
        Mockito.when(associadoMapper.toDTO(updatedAssociado)).thenReturn(updatedDTO);

        // Act
        AssociadoDTO result = associadoUseCase.update(testId, updateDTO);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Nome Atualizado", result.getNomeCompleto());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        Mockito.when(associadoRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(NotFoundException.class, () -> {
            associadoUseCase.update(testId, associadoDTO);
        });
    }

    @Test
    void testDelete_Success() {
        // Arrange
        Mockito.when(associadoRepository.findById(testId)).thenReturn(Optional.of(associado));

        // Act
        associadoUseCase.delete(testId);

        // Assert
        Mockito.verify(associadoRepository, Mockito.times(1)).deleteById(testId);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        Mockito.when(associadoRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(NotFoundException.class, () -> {
            associadoUseCase.delete(testId);
        });
        
        // Verify that deleteById was never called
        Mockito.verify(associadoRepository, Mockito.never()).deleteById(any(UUID.class));
    }
}