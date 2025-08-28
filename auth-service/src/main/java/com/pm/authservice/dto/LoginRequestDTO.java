package com.pm.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginRequestDTO {
  @NotBlank(message = "Email is required!")
  @Email(message = "Email should be a valid email address!")
  private String email;

  @NotBlank(message = "Password is required!")
  @Size(min = 8, max = 100, message = "Password must be at least 8 to 100 characters!")
  private String password;
}
