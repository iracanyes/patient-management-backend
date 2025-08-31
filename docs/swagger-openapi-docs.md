# Swagger : OpenAPI Documentation

# Swagger : Installation

Add the following dependencies to the ``pom.xml`` file of the desired service:
```xml
    <!--
    Here we exclude the commons-lang3 dependency from springdoc-openapi-starter-webmvc-ui
    because it has a vulnerability that is fixed in the latest version.
    This allows us to use the latest version of commons-lang3 without conflicts.
    -->
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.8.10</version>
      <exclusions>
        <exclusion>
          <groupId>org.apache.commons</groupId>
          <artifactId>commons-lang3</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-lang3</artifactId>
      <version>3.18.0</version>
    </dependency>
````
 Next, add the configuration for OpenAPI documentation
to your application configuration file ``application.[properties/yaml]``

````yaml
# Swagger UI configuration
springdoc:
    api-docs:
      enabled: true
      path: /v3/api-docs
    swagger-ui:
      enabled: true
      path: /swagger-ui.html
      display-request-duration: true
````


## Swagger : OpenAPI documentation

You can access the OpenAPI documentation at the following URL:

- [http://localhost:8080/context-path/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- [http://localhost:8080/context-path/v3/api-docs](http://localhost:8080/v3/api-docs)

You must replace `context-path` with the actual context path of your application.
You also need to ensure that your application is running on port 8080 or adjust the URL accordingly.

## Spring Cloud Gateway integration

First, we add the dependencies required to our API gateway service.

````xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.8.11</version>
  </dependency>
````

Next, we configure the routes to our OpenAPI documentation

### OpenAPI documentation 

OpenAPI Swagger UI on the API Gateway service  will read the Patient service API documention, 
and generate all docs


````yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    config-url: /v3/api-docs/swagger-config
    urls:
      - name: gateway-service
        url: /v3/api-docs
      - name: patient-service
        # Here we provide the resource's path on the API gateway (defined in routes)
        # Swagger UI Client (on the api-gateway service) will request the OpenAPI documentation
        # for patient-service microservice on this route to the api-gateway microservice
        url: /api-docs/patients
````