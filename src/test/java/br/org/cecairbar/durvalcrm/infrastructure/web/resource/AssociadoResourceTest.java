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
        AssociadoEntity.update("ativo = false");
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
}