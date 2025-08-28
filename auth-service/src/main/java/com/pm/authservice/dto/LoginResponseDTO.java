package com.pm.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class LoginResponseDTO {

  private final String token;

  private final String refreshToken;


}
