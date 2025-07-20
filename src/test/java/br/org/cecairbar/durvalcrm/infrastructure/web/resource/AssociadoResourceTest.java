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
import static org.hamcrest.Matchers.hasSize;

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
          .get("/api/associados")
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
          .post("/api/associados")
        .then()
          .statusCode(201)
          .contentType(ContentType.JSON)
          .body("id", notNullValue())
          .body("nomeCompleto", is("Novo Associado via Teste"))
          .body("cpf", is("987.654.321-00"))
          .body("email", is("novoteste@email.com"))
          .body("telefone", is("(11) 91234-5678"));
    }

    @Test
    @Order(3)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testCreateAssociado_WithInvalidData_ShouldReturnBadRequest() {
        String invalidAssociadoJson = """
            {
                "nomeCompleto": "",
                "cpf": "123",
                "email": "email-invalido"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(invalidAssociadoJson)
        .when()
          .post("/api/associados")
        .then()
          .statusCode(400);
    }

    @Test
    @Order(4)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testCreateAssociado_WithDuplicateCpf_ShouldReturnConflict() {
        String associadoJson = """
            {
                "nomeCompleto": "Primeiro Associado",
                "cpf": "111.222.333-44",
                "email": "primeiro@email.com",
                "telefone": "(11) 91234-5678"
            }
        """;

        // Cria o primeiro associado
        given()
          .contentType(ContentType.JSON)
          .body(associadoJson)
        .when()
          .post("/api/associados")
        .then()
          .statusCode(201);

        // Tenta criar outro com o mesmo CPF
        String segundoAssociadoJson = """
            {
                "nomeCompleto": "Segundo Associado",
                "cpf": "111.222.333-44",
                "email": "segundo@email.com",
                "telefone": "(11) 91234-5679"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(segundoAssociadoJson)
        .when()
          .post("/api/associados")
        .then()
          .statusCode(409); // Conflict
    }

    @Test
    @Order(5)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testFindAllEndpoint_AfterCreation() {
        // Primeiro cria um associado
        String novoAssociadoJson = """
            {
                "nomeCompleto": "Associado para Busca",
                "cpf": "555.666.777-88",
                "email": "busca@email.com",
                "telefone": "(11) 91234-5678"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(novoAssociadoJson)
        .when()
          .post("/api/associados")
        .then()
          .statusCode(201);

        // Agora busca todos
        given()
        .when()
          .get("/api/associados")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("$", hasSize(1))
          .body("[0].nomeCompleto", is("Associado para Busca"));
    }

    @Test
    @Order(6)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testFindById_WithValidId_ShouldReturnAssociado() {
        // Primeiro cria um associado
        String novoAssociadoJson = """
            {
                "nomeCompleto": "Associado para Buscar por ID",
                "cpf": "999.888.777-66",
                "email": "buscaid@email.com",
                "telefone": "(11) 91234-5678"
            }
        """;

        String createdId = given()
          .contentType(ContentType.JSON)
          .body(novoAssociadoJson)
        .when()
          .post("/api/associados")
        .then()
          .statusCode(201)
          .extract()
          .path("id");

        // Busca por ID
        given()
        .when()
          .get("/api/associados/{id}", createdId)
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("id", is(createdId))
          .body("nomeCompleto", is("Associado para Buscar por ID"));
    }

    @Test
    @Order(7)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testFindById_WithInvalidId_ShouldReturnNotFound() {
        String invalidId = "123e4567-e89b-12d3-a456-426614174000";

        given()
        .when()
          .get("/api/associados/{id}", invalidId)
        .then()
          .statusCode(404);
    }

    @Test
    @Order(8)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testUpdateAssociado_WithValidData_ShouldUpdateSuccessfully() {
        // Primeiro cria um associado
        String novoAssociadoJson = """
            {
                "nomeCompleto": "Associado Original",
                "cpf": "123.123.123-12",
                "email": "original@email.com",
                "telefone": "(11) 91234-5678"
            }
        """;

        String createdId = given()
          .contentType(ContentType.JSON)
          .body(novoAssociadoJson)
        .when()
          .post("/api/associados")
        .then()
          .statusCode(201)
          .extract()
          .path("id");

        // Atualiza o associado
        String updateJson = """
            {
                "nomeCompleto": "Associado Atualizado",
                "cpf": "123.123.123-12",
                "email": "atualizado@email.com",
                "telefone": "(11) 98765-4321"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(updateJson)
        .when()
          .put("/api/associados/{id}", createdId)
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("id", is(createdId))
          .body("nomeCompleto", is("Associado Atualizado"))
          .body("email", is("atualizado@email.com"))
          .body("telefone", is("(11) 98765-4321"));
    }

    @Test
    @Order(9)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testDeleteAssociado_WithValidId_ShouldDeleteSuccessfully() {
        // Primeiro cria um associado
        String novoAssociadoJson = """
            {
                "nomeCompleto": "Associado para Deletar",
                "cpf": "456.456.456-45",
                "email": "deletar@email.com",
                "telefone": "(11) 91234-5678"
            }
        """;

        String createdId = given()
          .contentType(ContentType.JSON)
          .body(novoAssociadoJson)
        .when()
          .post("/api/associados")
        .then()
          .statusCode(201)
          .extract()
          .path("id");

        // Deleta o associado
        given()
        .when()
          .delete("/api/associados/{id}", createdId)
        .then()
          .statusCode(204);

        // Verifica se foi deletado
        given()
        .when()
          .get("/api/associados/{id}", createdId)
        .then()
          .statusCode(404);
    }

    @Test
    @Order(10)
    @TestSecurity(user = "testuser", roles = { "user" })
    void testSearchAssociados_WithValidQuery_ShouldReturnFilteredResults() {
        // Cria alguns associados para teste de busca
        String associado1Json = """
            {
                "nomeCompleto": "João da Silva Santos",
                "cpf": "111.111.111-11",
                "email": "joao@email.com",
                "telefone": "(11) 91234-5678"
            }
        """;

        String associado2Json = """
            {
                "nomeCompleto": "Maria dos Santos",
                "cpf": "222.222.222-22",
                "email": "maria@email.com",
                "telefone": "(11) 91234-5679"
            }
        """;

        given()
          .contentType(ContentType.JSON)
          .body(associado1Json)
        .when()
          .post("/api/associados")
        .then()
          .statusCode(201);

        given()
          .contentType(ContentType.JSON)
          .body(associado2Json)
        .when()
          .post("/api/associados")
        .then()
          .statusCode(201);

        // Busca por "Santos" - deve retornar ambos
        given()
          .queryParam("search", "Santos")
        .when()
          .get("/api/associados")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("$", hasSize(2));

        // Busca por "João" - deve retornar apenas um
        given()
          .queryParam("search", "João")
        .when()
          .get("/api/associados")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("$", hasSize(1))
          .body("[0].nomeCompleto", is("João da Silva Santos"));
    }
}