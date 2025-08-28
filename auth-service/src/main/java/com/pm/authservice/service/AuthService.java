package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(
    UserService userService,
    PasswordEncoder passwordEncoder,
    JwtUtil jwtUtil
  ){
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  public Optional<LoginResponseDTO> authenticate(LoginRequestDTO loginRequestDTO) {
    // Find user by email
    Optional<LoginResponseDTO> optionalResponse = userService.findByEmail(loginRequestDTO.getEmail()) // Find user by email
      .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))   // Check password
      .map(u -> jwtUtil.generateTokens(u.getEmail(), u.getRole()));   // Generate tokens


    if(optionalResponse.isEmpty()){
      throw new UsernameNotFoundException("Username not found");
    }

    return optionalResponse;
  }
}
