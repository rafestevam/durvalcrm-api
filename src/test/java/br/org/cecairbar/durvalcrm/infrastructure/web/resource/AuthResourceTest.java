package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class AuthResourceTest {

    @Test
    void testGetLoginInfo_Public() {
        // Testa se o endpoint de informações de login está acessível sem autenticação
        given()
        .when()
          .get("/auth/login-info")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("authServerUrl", notNullValue())
          .body("clientId", is("durvalcrm-app"))
          .body("redirectUri", is("http://localhost:3000/callback"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = { "user" })
    void testLogout_Authenticated() {
        // Testa o endpoint de logout com usuário autenticado
        given()
          .contentType(ContentType.JSON)
          .body("{}")
        .when()
          .post("/auth/logout")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("message", is("Logout realizado com sucesso"));
    }

    @Test
    void testLogout_Unauthenticated() {
        // Testa se o endpoint de logout requer autenticação
        given()
          .contentType(ContentType.JSON)
          .body("{}")
        .when()
          .post("/auth/logout")
        .then()
          .statusCode(401); // Unauthorized
    }

    @Test
    @TestSecurity(user = "admin", roles = { "admin" })
    void testGetLoginInfo_Authenticated() {
        // Testa se usuários autenticados também podem acessar as informações de login
        given()
        .when()
          .get("/auth/login-info")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("authServerUrl", notNullValue())
          .body("clientId", is("durvalcrm-app"));
    }

    @Test
    @TestSecurity(user = "user1", roles = { "user" })
    void testLogout_WithDifferentUser() {
        // Testa o logout com diferentes tipos de usuário
        given()
          .contentType(ContentType.JSON)
          .body("{}")
        .when()
          .post("/auth/logout")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("message", is("Logout realizado com sucesso"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = { "user" })
    void testLogout_EmptyBody() {
        // Testa o logout sem corpo da requisição
        given()
          .contentType(ContentType.JSON)
        .when()
          .post("/auth/logout")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("message", is("Logout realizado com sucesso"));
    }

    @Test
    void testGetLoginInfo_ReturnsCorrectStructure() {
        // Testa se a estrutura de resposta está correta
        given()
        .when()
          .get("/auth/login-info")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("size()", is(3)) // Deve ter exatamente 3 campos
          .body("authServerUrl", notNullValue())
          .body("clientId", notNullValue())
          .body("redirectUri", notNullValue());
    }
}