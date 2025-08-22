/**
 * {@code @ControllerAdvice} is used to handle exceptions globally in a Spring application.
 * It allows you to define a centralized exception handling mechanism that can be applied across all controllers.
 * This class can be used to catch exceptions thrown by any controller and return a standardized error response.
 * You can define methods annotated with @ExceptionHandler to handle specific exceptions and return appropriate responses.
 */
package com.pm.patientservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  /**
   * Handles MethodArgumentNotValidException, which is thrown when validation fails on a method argument.
   * It collects all validation errors and returns them in a Map with field names as keys and error messages as values.
   *
   * @param ex the MethodArgumentNotValidException that was thrown
   * @return a ResponseEntity containing a Map of validation errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex){
    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult().getFieldErrors()
      .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return ResponseEntity.badRequest().body(errors);

  }

  /**
   * Handles EmailAlreadyExistsException, which is thrown when an email already exists in the system.
   * It returns a ResponseEntity with a 409 Conflict status and a message indicating the email already exists.
   *
   * @param ex the EmailAlreadyExistsException that was thrown
   * @return a ResponseEntity with a conflict status and an error message
   */
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
    Map<String, String> errors = new HashMap<>();

    // Logging the exception message for debugging purposes
    LOGGER.warn("Email already exists: {}", ex.getMessage());

    errors.put("email_exists", ex.getMessage());
    return ResponseEntity.status(409).body(errors);
  }

  @ExceptionHandler(PatientNotFoundException.class)
  public ResponseEntity<ApiException> handlePatientNotFoundException(PatientNotFoundException ex) {
    Map<String, String> errors = new HashMap<>();

    // Logging the exception message for debugging purposes
    LOGGER.warn("Patient not found: {}", ex.getMessage());

    errors.put("patient_not_found", ex.getMessage());
    return ResponseEntity.status(404).body(new ApiException(404, "Patient not found!" ,errors));
  }

}
