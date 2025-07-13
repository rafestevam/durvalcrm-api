package br.org.cecairbar.durvalcrm.application.usecase.impl;

import br.org.cecairbar.durvalcrm.application.dto.AssociadoDTO;
import br.org.cecairbar.durvalcrm.application.usecase.AssociadoUseCase;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.AssociadoEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@QuarkusTest
class AssociadoUseCaseImplTest {

    @Inject
    AssociadoUseCase associadoUseCase;

    @Inject
    AssociadoRepository associadoRepository;

    // Limpa o banco de dados antes de cada teste
    @BeforeEach
    @Transactional
    void setUp() {
        AssociadoEntity.deleteAll();
    }

    private AssociadoDTO createInitialAssociado(String cpf, String email, String nome) {
        AssociadoDTO dto = new AssociadoDTO();
        dto.setNomeCompleto(nome);
        dto.setCpf(cpf);
        dto.setEmail(email);
        dto.setTelefone("(11) 99999-9999");
        return associadoUseCase.create(dto);
    }

    @Test
    void testCreate_Success() {
        AssociadoDTO dto = new AssociadoDTO();
        dto.setNomeCompleto("Usuário de Teste");
        dto.setCpf("123.456.789-00");
        dto.setEmail("teste@email.com");

        AssociadoDTO result = associadoUseCase.create(dto);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals("Usuário de Teste", result.getNomeCompleto());

        // Verifica se foi salvo no repositório
        Assertions.assertEquals(1, associadoRepository.findAll("").size());
    }

    @Test
    void testCreate_ConflictCpf() {
        createInitialAssociado("111.222.333-44", "primeiro@email.com", "Primeiro Usuário");

        AssociadoDTO dtoComCpfRepetido = new AssociadoDTO();
        dtoComCpfRepetido.setNomeCompleto("Usuário com CPF Repetido");
        dtoComCpfRepetido.setCpf("111.222.333-44");
        dtoComCpfRepetido.setEmail("segundo@email.com");

        WebApplicationException exception = Assertions.assertThrows(WebApplicationException.class, () -> {
            associadoUseCase.create(dtoComCpfRepetido);
        });

        Assertions.assertEquals(409, exception.getResponse().getStatus());
        Assertions.assertEquals("CPF já cadastrado", exception.getMessage());
    }

    @Test
    void testCreate_ConflictEmail() {
        createInitialAssociado("111.222.333-44", "email@repetido.com", "Primeiro Usuário");

        AssociadoDTO dtoComEmailRepetido = new AssociadoDTO();
        dtoComEmailRepetido.setNomeCompleto("Usuário com Email Repetido");
        dtoComEmailRepetido.setCpf("444.555.666-77");
        dtoComEmailRepetido.setEmail("email@repetido.com");

        WebApplicationException exception = Assertions.assertThrows(WebApplicationException.class, () -> {
            associadoUseCase.create(dtoComEmailRepetido);
        });

        Assertions.assertEquals(409, exception.getResponse().getStatus());
        Assertions.assertEquals("E-mail já cadastrado", exception.getMessage());
    }

    @Test
    void testFindById_NotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            associadoUseCase.findById(UUID.randomUUID());
        });
    }

    @Test
    void testDelete_NotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            associadoUseCase.delete(UUID.randomUUID());
        });
    }

    @Test
    void testUpdate_NotFound() {
        AssociadoDTO dto = new AssociadoDTO();
        dto.setNomeCompleto("Qualquer Nome");
        Assertions.assertThrows(NotFoundException.class, () -> {
            associadoUseCase.update(UUID.randomUUID(), dto);
        });
    }

    @Test
    @Transactional
    void testFullLifecycle_CreateFindUpdateDelete() {
        // 1. Create
        AssociadoDTO createdDto = createInitialAssociado("123.456.789-00", "ciclo@vida.com", "Ciclo de Vida");
        UUID id = createdDto.getId();
        Assertions.assertNotNull(id);

        // 2. Find
        AssociadoDTO foundDto = associadoUseCase.findById(id);
        Assertions.assertNotNull(foundDto);
        Assertions.assertEquals("Ciclo de Vida", foundDto.getNomeCompleto());

        // 3. Update
        foundDto.setNomeCompleto("Nome Atualizado");
        AssociadoDTO updatedDto = associadoUseCase.update(id, foundDto);
        Assertions.assertEquals("Nome Atualizado", updatedDto.getNomeCompleto());

        // 4. Delete (Soft Delete)
        associadoUseCase.delete(id);

        // 5. Verify Not Found after delete
        Assertions.assertThrows(NotFoundException.class, () -> {
            associadoUseCase.findById(id);
        });
    }
}