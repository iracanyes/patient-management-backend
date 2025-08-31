# Testing: Unit, integration

## Introduction 

### Unit testing

Tests individual units (classes, methods) in isolation.

### Integration testing

Tests multiple components working together, such as a controller, service, repository.
Can also mean testing multiple parts of a wider system.

### E2E testing

End to End testing : Test the complete workflow of the application, from UI to API.
Test as the user will use it!

### Other testing

- Security testing
- Performance testing
- Contract testing 
- Acceptance testing
- etc

## Integration tests: Rest Assured - Testing Rest endpoints

Here we will test how multiple microservices work together. 
RestAssured allow to make HTTP requests 

As we will test multiple microservices working together. 
We create a separated module for our tests so they are decoupled from our microservices test
An other advantage to have all integration tests in a decoupled module is that it will be easier to 
integrate with CI/CD pipelines.

The module will be called ``integration-tests`` which will be a basic Java application.

First add the dependency to ``pom.xml``

````xml
  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <rest.assured.version>5.5.6</rest.assured.version>
    <junit.jupiter.version>5.9.2</junit.jupiter.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>${junit.jupiter.version}</version>
    </dependency>
    <dependency>
      <groupId>io.rest-assured</groupId>
      <artifactId>rest-assured</artifactId>
      <version>${rest.assured.version}</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
````