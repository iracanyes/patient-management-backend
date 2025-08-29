package com.pm.authservice.util;

import com.pm.authservice.dto.LoginResponseDTO;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);
  private final Key secretKey;
  private final int tokenExpiration;
  private final int refreshTokenExpiration;

  public JwtUtil(
    @Value("${jwt.secret_key}") String secretKey,
    @Value("${jwt.expiration.token}") int tokenExpiration,
    @Value("${jwt.expiration.refresh_token}") int refreshTokenExpiration
  ) {
    byte[] keyBytes = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    this.tokenExpiration = tokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  public LoginResponseDTO generateTokens(String email, String role){
    String token = Jwts.builder()
      .subject(email)
      .claim("role", role)
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
      .signWith(secretKey)
      .compact();

    String refreshToken = Jwts.builder()
      .subject(email)
      .claim("role", role)
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
      .signWith(secretKey)
      .compact();

    // Save using a token entity

    return new LoginResponseDTO(token, refreshToken);
  }

  public void validateToken(String token){
    try{
      Jwts.parser().verifyWith((SecretKey) secretKey)
        .build()
        .parseSignedClaims(token);
    } catch (SignatureException e) {
      throw new JwtException("Invalid JWT Signature");
    } catch (JwtException e) {
      throw new JwtException("Invalid JWT");
    }
  }
}
