package com.pm.authservice.controller;

import com.pm.authservice.dto.ApiException;
import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("")
public class AuthController {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
    Optional<LoginResponseDTO> tokens = authService.authenticate(loginRequestDTO);

    if(tokens.isEmpty()){
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiException(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.toString()));
    }

    LoginResponseDTO loginResponseDTO = tokens.get();
    return ResponseEntity.ok(loginResponseDTO);
  }
}
