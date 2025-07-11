package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class AssociadoResourceTest {

    @Test
    // Esta anotação simula uma requisição com um token JWT válido
    // para um usuário chamado 'testuser' com o papel 'user'.
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
          .body("id", notNullValue()) // Verifica se um ID foi gerado
          .body("nomeCompleto", is("Novo Associado via Teste"))
          .body("cpf", is("987.654.321-00"));
    }
}