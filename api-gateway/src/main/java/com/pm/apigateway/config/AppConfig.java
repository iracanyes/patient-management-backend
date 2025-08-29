package com.pm.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class AppConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

  private static boolean isRunningInsideDocker(){
    try{
      String cgroup = new String(Files.readAllBytes(Paths.get("/proc/1/cgroup")));
      return cgroup.contains("docker") || cgroup.contains("kubepods");
    }catch (Exception e){
      return false;
    }
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();

//    String serverPort = System.getProperty("SERVER_PORT");
//
//    if((serverPort == null || serverPort.isEmpty()) || AppConfig.isRunningInsideDocker()) {
//      // Additional .env properties
//      configurer.setLocation(new FileSystemResource(".env"));
//    }


    return configurer;
  }

  @Bean
  public RouteLocator customRouteLocator(
    RouteLocatorBuilder builder,
    Environment environment
  ) {
    String authServiceUrl = environment.getProperty("AUTH_SERVICE_URL");
    String patientServiceUrl = environment.getProperty("PATIENT_SERVICE_URL");
    String billingServiceUrl = environment.getProperty("BILLING_SERVICE_URL");
    String analyticsServiceUrl = environment.getProperty("ANALYTICS_SERVICE_URL");




    LOGGER.info("Configuring routes:");
    LOGGER.info("Patient Service URL: {}", patientServiceUrl);
    LOGGER.info("Billing Service URL: {}", billingServiceUrl);
    LOGGER.info("Analytics Service URL: {}", analyticsServiceUrl);

    return builder.routes()
      .route("auth-service", r -> r.path("/api/auth/**")
        .filters(
          f -> {
            // Remove the first part of the path "/api" before forwarding the remaining "/patients/**"
            f.stripPrefix(1);
            // Rewrite the path to match the patient service endpoint server context path
            f.rewritePath("//?(?<segment>.*)", "/${segment}");

            return f;
          }
        )
        .uri(authServiceUrl)
      )
      .route("patient-service", r -> r.path("/api/patients/**")
        .filters(
          f -> {
            // Remove the first part of the path "/api" before forwarding the remaining "/patients/**"
            f.stripPrefix(1);
            // Rewrite the path to match the patient service endpoint server context path
            f.rewritePath("//?(?<segment>.*)", "/patient-service/${segment}");

            return f;
          }
        )
        .uri(patientServiceUrl)
      )
      .route("patient-service-docs", r -> r.path("/api-docs/patients")
        .filters(
          f -> {
            // Rewrite the path to match the patient service endpoint server context path
            f.rewritePath("//?(?<segment>.*)", "/patient-service/v3/api-docs");

            return f;
          }
        )
        .uri(patientServiceUrl)
      )
      .route("patient-service-swagger-ui", r -> r.path("/api-docs-ui/patients")
        .filters(
          f -> {
            // Rewrite the path to match the patient service endpoint server context path
            f.rewritePath("//?(?<segment>.*)", "/patient-service/swagger-ui/index.html");

            return f;
          }
        )
        .uri(patientServiceUrl)
      )
      .route("billing-service", r -> r.path("/api/billings/**")
        .filters(
          f -> {
            //
            f.stripPrefix(1);
            f.rewritePath("//?(?<segment>.*)", "/billing-service/${segment}");

            return f;
          }
        )
        .uri(billingServiceUrl)
      )
      .route("analytics-service", r -> r.path("/api/analytics/**")
        .filters(
          f -> {
            f.stripPrefix(1);
            f.rewritePath("//?(?<segment>.*)", "/analytics-service/${segment}");

            return f;
          }
        )
        .uri(analyticsServiceUrl)
      )
      .build();
  }
}
