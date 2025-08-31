package com.pm.apigateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class JwtValidationExceptionHandler {

  @ExceptionHandler(WebClientResponseException.Unauthorized.class)
  public Mono<Void> handleUnauthorizedException(ServerWebExchange exchange) {

    // Mark the response with Unauthorized status
    exchange.getResponse()
      .setStatusCode(HttpStatus.UNAUTHORIZED);

    // Mark the response ready to be sent back
    return exchange.getResponse().setComplete();

  }
}
