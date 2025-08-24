# Swagger : OpenAPI Documentation

# Swagger : Installation

Add the following dependencies to your ``pom.xml`` file:
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

## Swagger : OpenAPI documentation

You can access the OpenAPI documentation at the following URL:

- [http://localhost:8080/context-path/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- [http://localhost:8080/context-path/v3/api-docs](http://localhost:8080/v3/api-docs)

You must replace `context-path` with the actual context path of your application.
You also need to ensure that your application is running on port 8080 or adjust the URL accordingly.