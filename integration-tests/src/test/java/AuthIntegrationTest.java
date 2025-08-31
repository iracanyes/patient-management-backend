import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static io.restassured.RestAssured.given;

public class AuthIntegrationTest {
  @BeforeAll
  static void setup(){
    // Set the API Gateway URI for RestAssured requests
    RestAssured.baseURI = "http://localhost:4003/";


  }

  /**
   * There is 3 required steps in a test:
   * - Arrange
   * - Act
   * - Assert
   */
  @Test
  void shouldReturnOkWithValidToken(){
    // Arrange
    String loginPayload = """
      {
        "email": "testuser@test.com",
        "password": "password123"
      }
    """;

    Response response = given()
      .contentType("application/json")   // Arrange: Prepare the request
      .body(loginPayload)
      .when()                               // Act: make a POST request to API Gateway
      .post("/api/auth/login")
      .then()                               // Assert: Returned response is not null and status code is 200
      .statusCode(200)
      .body("token", Matchers.notNullValue())
      .extract().response();

    System.out.printf("\nGenerated token : %s\n", response.jsonPath().getString("token"));
  }

  @Test
  void shouldReturnUnauthorizedOnInvalidLogin(){
    // Arrange
    String loginPayload = """
      {
        "email": "invalid_user@test.com",
        "password": "password123"
      }
    """;

    given()
      .contentType("application/json")   // Arrange: Prepare the request
      .body(loginPayload)
      .when()                               // Act: make a POST request to API Gateway
      .post("/api/auth/login")
      .then()                               // Assert: Returned response is not null and status code is 200
      .statusCode(401);

  }


}
