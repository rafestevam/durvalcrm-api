package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class AuthResourceTest {

    @Test
    void testGetLoginInfo_Public() {
        given()
        .when()
          .get("/auth/login-info")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("authServerUrl", notNullValue())
          .body("clientId", notNullValue())
          .body("realm", notNullValue());
    }

    @Test
    void testGetLoginInfo_ReturnsCorrectValues() {
        given()
        .when()
          .get("/auth/login-info")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("authServerUrl", anyOf(
              equalTo("http://localhost:8080/realms/durval-crm"),
              equalTo("")  // Pode estar vazio em ambiente de teste
          ))
          .body("clientId", equalTo("durvalcrm-app"))
          .body("realm", equalTo("durval-crm"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = { "user" })
    void testGetLoginInfo_Authenticated() {
        given()
        .when()
          .get("/auth/login-info")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("authServerUrl", notNullValue())
          .body("clientId", equalTo("durvalcrm-app"))
          .body("realm", equalTo("durval-crm"));
    }

    @Test
    void testGetLoginInfo_ReturnsCorrectStructure() {
        given()
        .when()
          .get("/auth/login-info")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("size()", is(3))  // Esperamos exatamente 3 campos: authServerUrl, clientId, realm
          .body("authServerUrl", notNullValue())
          .body("clientId", notNullValue())
          .body("realm", notNullValue());
    }

    @Test
    void testLogout_Public() {
        given()
        .when()
          .get("/auth/logout")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("message", notNullValue())
          .body("logoutUrl", notNullValue());
    }

    @Test
    @TestSecurity(user = "testuser", roles = { "user" })
    void testLogout_Authenticated() {
        given()
        .when()
          .get("/auth/logout")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("message", is("Logout realizado com sucesso"))
          .body("logoutUrl", notNullValue());
    }

    @Test
    void testLogout_ReturnsCorrectStructure() {
        given()
        .when()
          .get("/auth/logout")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("size()", is(2))  // Esperamos exatamente 2 campos: message e logoutUrl
          .body("message", notNullValue())
          .body("logoutUrl", notNullValue());
    }
}