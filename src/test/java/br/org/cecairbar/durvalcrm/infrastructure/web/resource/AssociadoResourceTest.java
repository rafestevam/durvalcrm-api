package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import br.org.cecairbar.durvalcrm.infrastructure.persistence.entity.AssociadoEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import jakarta.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssociadoResourceTest {

    @BeforeEach
    @Transactional
    public void cleanDatabase() {
        // Limpa o banco antes de cada teste para garantir isolamento
        AssociadoEntity.deleteAll();
    }

    @Test
    @Order(1)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testFindAllEndpoint_ReturnsEmptyListInitially() {
        given()
        .when()
          .get("/associados")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body(is("[]"));
    }

    @Test
    @Order(2)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testCreateAssociado_Success() {
        String novoAssociadoJson = """
            {
                "nomeCompleto": "Novo Associado via Teste",
                "cpf": "987.654.321-00",
                "email": "novoteste@email.com",
                "telefone": "(11) 91234-5678"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(novoAssociadoJson)
        .when()
          .post("/associados")
        .then()
          .statusCode(201)
          .contentType(ContentType.JSON)
          .body("id", notNullValue())
          .body("nomeCompleto", is("Novo Associado via Teste"))
          .body("cpf", is("987.654.321-00"));
    }

    @Test
    @Order(3)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testFindAllEndpoint_AfterCreation() {
        // Primeiro cria um associado
        String novoAssociadoJson = """
            {
                "nomeCompleto": "Associado para Busca",
                "cpf": "111.222.333-44",
                "email": "busca@email.com",
                "telefone": "(11) 98765-4321"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(novoAssociadoJson)
        .when()
          .post("/associados")
        .then()
          .statusCode(201);

        // Depois verifica se consegue encontrar
        given()
        .when()
          .get("/associados")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(4)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testCreateAssociado_ConflictCpf() {
        // Primeiro cria um associado
        String primeiroAssociadoJson = """
            {
                "nomeCompleto": "Primeiro Associado",
                "cpf": "123.456.789-00",
                "email": "primeiro@email.com",
                "telefone": "(11) 11111-1111"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(primeiroAssociadoJson)
        .when()
          .post("/associados")
        .then()
          .statusCode(201);

        // Tenta criar outro com mesmo CPF
        String segundoAssociadoJson = """
            {
                "nomeCompleto": "Segundo Associado",
                "cpf": "123.456.789-00",
                "email": "segundo@email.com",
                "telefone": "(11) 22222-2222"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(segundoAssociadoJson)
        .when()
          .post("/associados")
        .then()
          .statusCode(409); // Conflict
    }

    @Test
    @Order(5)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testCreateAssociado_ConflictEmail() {
        // Primeiro cria um associado
        String primeiroAssociadoJson = """
            {
                "nomeCompleto": "Primeiro Associado",
                "cpf": "555.666.777-88",
                "email": "conflito@email.com",
                "telefone": "(11) 11111-1111"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(primeiroAssociadoJson)
        .when()
          .post("/associados")
        .then()
          .statusCode(201);

        // Tenta criar outro com mesmo email
        String segundoAssociadoJson = """
            {
                "nomeCompleto": "Segundo Associado",
                "cpf": "999.888.777-66",
                "email": "conflito@email.com",
                "telefone": "(11) 22222-2222"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(segundoAssociadoJson)
        .when()
          .post("/associados")
        .then()
          .statusCode(409); // Conflict
    }

    @Test
    @Order(6)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testFindById_Success() {
        // Primeiro cria um associado
        String novoAssociadoJson = """
            {
                "nomeCompleto": "Associado para Buscar por ID",
                "cpf": "777.888.999-00",
                "email": "buscarid@email.com",
                "telefone": "(11) 99999-0000"
            }
        """;

        String associadoId = given()
          .contentType(ContentType.JSON)
          .body(novoAssociadoJson)
        .when()
          .post("/associados")
        .then()
          .statusCode(201)
          .extract()
          .path("id");

        // Busca o associado por ID
        given()
        .when()
          .get("/associados/{id}", associadoId)
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("id", is(associadoId))
          .body("nomeCompleto", is("Associado para Buscar por ID"))
          .body("cpf", is("777.888.999-00"));
    }

    @Test
    @Order(7)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testFindById_NotFound() {
        // Tenta buscar um ID que não existe
        given()
        .when()
          .get("/associados/{id}", "00000000-0000-0000-0000-000000000000")
        .then()
          .statusCode(404);
    }

    @Test
    @Order(8)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testUpdateAssociado_Success() {
        // Primeiro cria um associado
        String novoAssociadoJson = """
            {
                "nomeCompleto": "Associado Original",
                "cpf": "444.555.666-77",
                "email": "original@email.com",
                "telefone": "(11) 44444-4444"
            }
        """;

        String associadoId = given()
          .contentType(ContentType.JSON)
          .body(novoAssociadoJson)
        .when()
          .post("/associados")
        .then()
          .statusCode(201)
          .extract()
          .path("id");

        // Atualiza o associado
        String associadoAtualizadoJson = """
            {
                "nomeCompleto": "Associado Atualizado",
                "cpf": "444.555.666-77",
                "email": "original@email.com",
                "telefone": "(11) 55555-5555"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(associadoAtualizadoJson)
        .when()
          .put("/associados/{id}", associadoId)
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("id", is(associadoId))
          .body("nomeCompleto", is("Associado Atualizado"))
          .body("telefone", is("(11) 55555-5555"));
    }

    @Test
    @Order(9)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testUpdateAssociado_NotFound() {
        String associadoAtualizadoJson = """
            {
                "nomeCompleto": "Associado Inexistente",
                "cpf": "000.000.000-00",
                "email": "inexistente@email.com",
                "telefone": "(11) 00000-0000"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(associadoAtualizadoJson)
        .when()
          .put("/associados/{id}", "00000000-0000-0000-0000-000000000000")
        .then()
          .statusCode(404);
    }

    @Test
    @Order(10)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testDeleteAssociado_Success() {
        // Primeiro cria um associado
        String novoAssociadoJson = """
            {
                "nomeCompleto": "Associado para Deletar",
                "cpf": "333.444.555-66",
                "email": "deletar@email.com",
                "telefone": "(11) 33333-3333"
            }
        """;

        String associadoId = given()
          .contentType(ContentType.JSON)
          .body(novoAssociadoJson)
        .when()
          .post("/associados")
        .then()
          .statusCode(201)
          .extract()
          .path("id");

        // Verifica que existe antes de deletar
        given()
        .when()
          .get("/associados/{id}", associadoId)
        .then()
          .statusCode(200);

        // Deleta o associado
        given()
        .when()
          .delete("/associados/{id}", associadoId)
        .then()
          .statusCode(204); // No Content

        // Verifica que não consegue mais encontrar (soft delete)
        given()
        .when()
          .get("/associados/{id}", associadoId)
        .then()
          .statusCode(404); // Not Found
    }

    @Test
    @Order(11)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testDeleteAssociado_NotFound() {
        // Tenta deletar um ID que não existe
        given()
        .when()
          .delete("/associados/{id}", "00000000-0000-0000-0000-000000000000")
        .then()
          .statusCode(404);
    }

    @Test
    @Order(12)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testFindAll_WithSearch() {
        // Cria alguns associados para testar busca
        String associado1Json = """
            {
                "nomeCompleto": "João Silva",
                "cpf": "111.111.111-11",
                "email": "joao@email.com",
                "telefone": "(11) 11111-1111"
            }
        """;

        String associado2Json = """
            {
                "nomeCompleto": "Maria Santos",
                "cpf": "222.222.222-22",
                "email": "maria@email.com",
                "telefone": "(11) 22222-2222"
            }
        """;

        // Cria os associados
        given()
          .contentType(ContentType.JSON)
          .body(associado1Json)
        .when()
          .post("/associados")
        .then()
          .statusCode(201);

        given()
          .contentType(ContentType.JSON)
          .body(associado2Json)
        .when()
          .post("/associados")
        .then()
          .statusCode(201);

        // Busca por nome
        given()
          .queryParam("search", "João")
        .when()
          .get("/associados")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("size()", greaterThanOrEqualTo(1))
          .body("[0].nomeCompleto", is("João Silva"));

        // Busca por CPF
        given()
          .queryParam("search", "222.222.222-22")
        .when()
          .get("/associados")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("size()", greaterThanOrEqualTo(1))
          .body("[0].nomeCompleto", is("Maria Santos"));
    }
}