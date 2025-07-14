package br.org.cecairbar.durvalcrm.infrastructure.web.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class HealthResourceTest {

    @Test
    void testGetStatus_Public() {
        // Testa se o endpoint de status está acessível sem autenticação
        given()
        .when()
          .get("/health/api/status")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("status", is("UP"))
          .body("service", is("DurvalCRM API"))
          .body("timestamp", notNullValue());
    }

    @Test
    void testGetReadiness_Public() {
        // Testa se o endpoint de readiness está acessível sem autenticação
        given()
        .when()
          .get("/health/readiness")
        .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("status", is("UP"))
          .body("checks.database", is("UP"))
          .body("checks.application", is("UP"));
    }
}