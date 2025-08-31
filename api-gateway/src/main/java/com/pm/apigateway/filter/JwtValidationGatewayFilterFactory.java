/**
 * By extending AbstractGatewayFilterFactory, we tell Spring Boot to add the method defined
 * to the request lifecycle
 */
package com.pm.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
  private static final Logger LOGGER = LoggerFactory.getLogger(JwtValidationGatewayFilterFactory.class);
  private final WebClient webClient;

  public JwtValidationGatewayFilterFactory(
    WebClient.Builder webClientBuilder,
    @Value("${service.auth.url}") String authServiceUrl
  ) {
    this.webClient = webClientBuilder
      .baseUrl(authServiceUrl)
      .build();
  }

  @Override
  public GatewayFilter apply(Object config) {
    return (exchange, chain) -> {

      // Extract the token from authorization header
      String token = exchange
        .getRequest()
        .getHeaders()
        .getFirst("Authorization");

      LOGGER.info("token extracted: {}", token);

      // Check if it exists and in the right format
      if(token == null || !token.startsWith("Bearer ")) {
        // Mark as unauthorized request
        exchange.getResponse()
          .setStatusCode(HttpStatus.UNAUTHORIZED);

        // Return the response object marked as ready to returned
        return exchange.getResponse().setComplete();
      }

      // If the token is valid, we send a request to auth-service for token's authorization
      //
      return webClient.get()
        .uri("/auth/validate")
        .header(HttpHeaders.AUTHORIZATION, token)
        .retrieve()
        .toBodilessEntity()
        .then(chain.filter(exchange));
    };
  }
}
