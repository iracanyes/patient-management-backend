/**
 * Test class must be independent to others
 */

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;


public class PatientIntegrationTest {
  /**
   * Before all the tests in that class
   */
  @BeforeAll
  static void setup(){
    RestAssured.baseURI = "http://localhost:4003/";

  }

  @Test
  void shouldReturnOkWithPatientsList(){
    String loginPayload = """
      {
        "email": "testuser@test.com",
        "password": "password123"
      }
    """;

    // Login to get JWT token
    String token = given()
      .contentType(ContentType.JSON)
      .body(loginPayload)
      .when()
      .post("/api/auth/login")
      .then()
      .statusCode(200)
      .body("token", Matchers.notNullValue())
      .extract()
      .jsonPath()
      .getString("token");

    Response patients = given()
      .contentType(ContentType.JSON)
      .header("Authorization", "Bearer " + token)
      .when()
      .get("api/patients")
      .then()
      .statusCode(200)
      .body("patients", Matchers.notNullValue())
      .extract().response();

    System.out.println(patients.getBody().asString());

  }
}
