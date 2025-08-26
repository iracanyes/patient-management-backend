# gRPC

## Introduction

gRPC, ou gRPC Remote Procedure Call, est un framework open source développé par Google 
pour la communication synchrone entre services.
Il permet à une application (client) d'appeler directement des méthodes sur un serveur distant
comme si elles étaient des objets locaux.

> **IMPORTANT**: 
> 1. gRPC n'est pas destiné à remplacer les API REST pour la communication externe (client-serveur)
> 2. gRPC est principalement utilisé pour la communication interne entre microservices
> 3. gRPC est optimisé pour la performance et la faible latence pour la communication entre services (OneToOne) 
> 4. gRPC et REST sont synchrones, mais gRPC supporte aussi le streaming bidirectionnel
> 5. gRPC utilise HTTP/2 et Protobuf, tandis que REST utilise HTTP/1.1 et JSON
> 6. gRPC et REST communiquent de manière synchrone, utile pour les appels directs entre services. 
> 7. Kafka est asynchrone, idéal pour la communication déconnectée et les architectures basées sur des événements. (OneTOMany)


### Fonctionnement
gRPC est basé sur un modèle d'appel de procédure distante (RPC) qui repose sur trois piliers technologiques :

- **Protocol Buffers (Protobuf) pour la définition de services et la sérialisation** :
  Au lieu d'utiliser des formats lisibles par l'homme comme JSON ou XML, gRPC utilise Protobuf. 
  Le développeur définit un "contrat" pour l'API dans un fichier .proto 
  qui spécifie les services (c'est-à-dire les fonctions que le serveur expose) 
  et la structure des messages (les données de requête et de réponse). 
  Un compilateur (protoc) génère ensuite du code client et serveur (des "stubs") 
  dans le langage de programmation souhaité. Ces stubs gèrent automatiquement 
  la sérialisation des données en un format binaire compact.

- **HTTP/2 pour le transport** : 
  Contrairement à REST qui utilise principalement HTTP/1.1, gRPC est construit sur HTTP/2. 
  Cela apporte plusieurs fonctionnalités clés pour la performance, notamment le multiplexage 
  (plusieurs requêtes peuvent être envoyées sur une seule connexion TCP) et la compression des en-têtes (Header Compression).

- **Appel de méthode** : 
  Lorsqu'un client effectue un appel de méthode gRPC, il appelle la méthode locale générée par Protobuf. 
  Ce stub client prend les paramètres, les sérialise en un message Protobuf binaire, puis envoie ce message au serveur via HTTP/2. 
  Le stub serveur reçoit la requête, désérialise le message, et appelle la méthode correspondante implémentée par le développeur. 
  La réponse est traitée de manière inverse.

gRPC prend en charge quatre types de méthodes RPC :

- **Unary RPC** : Le client envoie une seule requête et reçoit une seule réponse.

- **Server streaming RPC** : Le client envoie une requête et reçoit un flux de réponses du serveur.

- **Client streaming RPC** : Le client envoie un flux de requêtes et reçoit une seule réponse.

- **Bidirectional streaming RPC** : Les deux parties s'envoient des flux de messages simultanément.

### Pourquoi gRPC est-il rapide ?

La vitesse de gRPC est le résultat de ses choix technologiques fondamentaux, 
qui le rendent plus performant que les API REST basées sur JSON/HTTP/1.1 
pour les communications internes entre microservices.

1. **Format de données binaire (Protobuf)** : 
  Les charges utiles de Protobuf sont beaucoup plus petites que leurs équivalents JSON ou XML. 
  Protobuf stocke les données dans un format binaire compact, ce qui réduit considérablement 
  la quantité de données à transférer sur le réseau. 
  De plus, la sérialisation et la désérialisation des données binaires sont plus rapides 
  pour les ordinateurs que le traitement du texte lisible par l'homme (comme JSON).

2. **Utilisation de HTTP/2** : 
  Le protocole HTTP/2 offre des avantages significatifs par rapport à HTTP/1.1 :

  - **Multiplexage** : 
    HTTP/1.1 exige une nouvelle connexion TCP pour chaque requête parallèle. 
    HTTP/2 permet d'envoyer plusieurs requêtes et réponses simultanément sur une seule connexion TCP persistante. 
    Cela réduit la latence et l'overhead lié à l'établissement de nouvelles connexions.

  - **Compression des en-têtes (HPACK)** : 
    Les en-têtes HTTP/2 sont compressés, ce qui réduit la taille des données envoyées avec chaque requête. 
    Dans une architecture de microservices où les en-têtes sont souvent répétés, cela permet de gagner en efficacité.

  - **Streaming** : 
    HTTP/2 supporte nativement le streaming bidirectionnel, ce qui est une fonctionnalité clé pour les applications en temps réel (jeux, chat) 
    et est plus efficace que les solutions basées sur des requêtes multiples avec HTTP/1.1.

En résumé, la combinaison d'un format de données binaire et d'un protocole de transport optimisé rend gRPC très performant, 
en particulier dans les environnements où la latence et l'efficacité de la bande passante sont primordiales, 
comme les architectures de microservices.

## Mise en place

gRPC sera utilisé pour la communication interne entre les microservices de l'application.


![Microservices : gRPC communication](/docs/images/gRPC_scalability.jpg)

## Structure d'un projet gRPC
gRPC sera utilisé pour la communication interne entre les microservices suivants:
- Patient Service
- Billing Service

Chaque service aura sa propre définition de service gRPC dans un fichier `.proto`
et les stubs client et serveur seront générés automatiquement à partir de ce fichier.



## Billing service: gRPC server

### Installation des dépendances
Ajouter les dépendances suivantes dans le `pom.xml` de chaque microservice utilisant gRPC.

```xml
  <properties>
    <java.version>21</java.version>
    <grpc.version>1.72.0</grpc.version>
    <protobuf-java.version>4.30.2</protobuf-java.version>
    <protobuf.plugin.version>0.6.1</protobuf.plugin.version>
    <spring-grpc.version>0.10.0</spring-grpc.version>
  </properties>
  <dependencies>
    <!-- ============== gRPC dependencies ================================ -->
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-services</artifactId>
    </dependency>

    <!-- Protobuf dependency -->
    <dependency>
      <groupId>com.google.protobuf</groupId>
      <artifactId>protobuf-java</artifactId>
      <version>${protobuf-java.version}</version>
    </dependency>

    <!-- Spring Boot gRPC starter -->
    <dependency>
      <groupId>net.devh</groupId>
      <artifactId>grpc-server-spring-boot-starter</artifactId>
      <version>3.1.0.RELEASE</version>
    </dependency>
  </dependencies>
```

### Protobuf: Définition du service

Chaque service gRPC est défini dans un fichier `.proto` utilisant la syntaxe Protobuf.
Afin que les fichier .proto soient disponibles pour chaque microservice, il est conseillé 
d'employer un des solutions suivantes:
1. Créer un repository Git dédié aux fichiers .proto et l'inclure comme submodule dans chaque microservice
2. Créer un module Maven dédié aux fichiers .proto et l'inclure comme dépendance dans chaque microservice.
3. Copier les fichiers .proto dans chaque microservice (moins recommandé car dupliqué)

### Exemple de fichier .proto

Dans le microservice `billing-service`, créer le fichier `billing_service.proto`.
Ce fichier définit un service `BillingService` avec une méthode `CreateBillingAccount`
Elle inclut également les messages de requête et de réponse ainsi que leur format.

```proto
syntax= "proto3";

// Will split the BillingServiceProto class into multiple files
option java_multiple_files = true;


option java_package = "com.pm.billingservice.proto";
option java_outer_classname = "BillingServiceProto";


service BillingService {
  rpc CreateBillingAccount(CreateBillingAccountRequest) returns (CreateBillingAccountResponse);
}

message CreateBillingAccountRequest {
  // The number indicate the deserialization order (MUST be unique)
  string patientId = 1;
  string firstname = 2;
  string lastname = 3;
  string email = 4;

}

message CreateBillingAccountResponse {
  string accountId = 1;
  string status = 2; // e.g., "SUCCESS", "FAILURE"
  string message = 3; // Additional information or error
}

```

### Génération des stubs

Les stubs client et serveur sont générés automatiquement à partir du fichier `.proto`
à l'aide du plugin `protoc-gen-grpc-java`.

Voici un exemple de configuration Maven pour générer les stubs gRPC :

```xml
<build>
  <extensions>
    <!-- Ensure OS compatibility for protoc -->
    <extension>
      <groupId>kr.motd.maven</groupId>
      <artifactId>os-maven-plugin</artifactId>
      <version>1.7.0</version>
    </extension>
  </extensions>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <configuration>
        <excludes>
          <exclude>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
          </exclude>
        </excludes>
      </configuration>
    </plugin>
    <!-- Protobuf plugin to generate Java code from .proto files -->
    <plugin>
      <groupId>io.github.ascopes</groupId>
      <artifactId>protobuf-maven-plugin</artifactId>
      <version>3.4.2</version>
      <configuration>
        <protocVersion>${protobuf-java.version}</protocVersion>
        <binaryMavenPlugins>
          <binaryMavenPlugin>
            <groupId>io.grpc</groupId>
            <artifactId>protoc-gen-grpc-java</artifactId>
            <version>${grpc.version}</version>
            <options>@generated=omit</options>
          </binaryMavenPlugin>
        </binaryMavenPlugins>
      </configuration>
      <executions>
        <execution>
          <id>generate</id>
          <goals>
            <goal>generate</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

  </plugins>
</build>
```

On peut ensuite exécuter la commande Maven suivante pour générer les stubs :

```bash
$ mvn clean compile

[INFO] -----------------------< com.pm:patient-service >-----------------------
[INFO] Building patient-service 2025.08.18-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- clean:3.4.1:clean (default-clean) @ patient-service ---
[INFO] Deleting C:\dev\projets\cours\frameworks\java\spring\spring-boot\patient-management\patient-service\target
[INFO]
[INFO] --- protobuf:3.4.2:generate (generate) @ patient-service ---
[INFO] Ignoring provided path C:\dev\projets\cours\frameworks\java\spring\spring-boot\patient-management\patient-service\src\main\protobuf as it does not appear to actually exist
[INFO] Attempting to resolve artifact com.google.protobuf:protoc:exe:windows-x86_64:4.30.2
[INFO] Attempting to resolve artifact io.grpc:protoc-gen-grpc-java:exe:windows-x86_64:1.72.0
[INFO] Registering C:\dev\projets\cours\frameworks\java\spring\spring-boot\patient-management\patient-service\target\generated-sources\protobuf as a Maven main source root
[INFO] Registering C:\dev\projets\cours\frameworks\java\spring\spring-boot\patient-management\patient-service\target\generated-sources\protobuf as a Maven main source root
[INFO] All sources will be compiled, as no previous build data was detected
[INFO] Generating source code from 1 proto source file and 0 descriptor files (discovered a total of 1 proto source file and 0 descriptor files)
[INFO] Invoking protoc (enable debug logs for more details)
[INFO] protoc (pid 37288) returned exit code 0 (success) after 295ms
[INFO] PROTOC SUCCEEDED: Protoc invocation succeeded.
[INFO]
```

Dans notre IDE, on doit ajouter le repertoire `target/generated-sources/protobuf`
comme source root pour que les stubs générés soient reconnus.

Dans Intellij IDEA Ultimate Cliquer droit sur le répertoire > Mark Directory as > Sources Root

### Implémentation du serveur gRPC

Créer une classe qui étend la classe abstraite générée `BillingServiceGrpc.BillingServiceImplBase`
et implémente la méthode `CreateBillingAccount`.

```java
package com.pm.billingservice.grpc;

import com.pm.billingservice.proto.BillingServiceGrpc;
import com.pm.billingservice.proto.CreateBillingAccountRequest;
import com.pm.billingservice.proto.CreateBillingAccountResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(BillingGrpcService.class);
  /**
   * {@code StreamObserver} allow us to send a response back to the client asynchronously, using bidirectional streaming, or in a single response.
   * @param billingAccountRequest
   * @param responseObserver
   */
  @Override
  public void createBillingAccount(CreateBillingAccountRequest billingAccountRequest, StreamObserver<CreateBillingAccountResponse> responseObserver) {
    LOGGER.info("Billing Service gRPC : createBillingAccount for ID : {}", billingAccountRequest.getPatientId());
    // Implement the logic to create a billing account


    // For now, we will just return a dummy response
    CreateBillingAccountResponse response = CreateBillingAccountResponse.newBuilder()
      .setAccountId("a9e92f60-e466-4d30-9b1a-1602bc790fe9")
      .setStatus(Integer.toString(204))
      .setMessage("Billing account created successfully")
      .build();

    // Here we use the responseObserver to send the response back to the client
    responseObserver.onNext(response);

    // Finally, we complete the response observer to indicate that we are done sending responses
    responseObserver.onCompleted();
  }


}
```

### Configuration du serveur gRPC

Ajouter la configuration suivante dans le `application.properties` du microservice `billing-service`.

```properties
# gRPC server configuration
grpc.server.port=50051
grpc.server.address=*
grpc.server.enableReflection=true
```

## Patient service: gRPC client

### Installation des dépendances
Ajouter les dépendances suivantes dans le `pom.xml` du microservice `patient-service`.

```xml
  <properties>
    <java.version>21</java.version>
    <grpc.version>1.72.0</grpc.version>
    <protobuf-java.version>4.30.2</protobuf-java.version>
    <protobuf.plugin.version>0.6.1</protobuf.plugin.version>
    <spring-grpc.version>0.10.0</spring-grpc.version>
  </properties>
  <dependencies>
    <!-- ============== gRPC dependencies ================================ -->
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-services</artifactId>
    </dependency>

    <!-- Protobuf dependency -->
    <dependency>
      <groupId>com.google.protobuf</groupId>
      <artifactId>protobuf-java</artifactId>
      <version>${protobuf-java.version}</version>
    </dependency>

    <!-- Spring Boot gRPC starter -->
    <dependency>
      <groupId>net.devh</groupId>
      <artifactId>grpc-client-spring-boot-starter</artifactId>
      <version>3.1.0.RELEASE</version>
    </dependency>
  </dependencies>
  <build>
    <extensions>
      <!-- Ensure OS compatibility for protoc -->
      <extension>
        <groupId>kr.motd.maven</groupId>
        <artifactId>os-maven-plugin</artifactId>
        <version>1.7.0</version>
      </extension>
    </extensions>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <annotationProcessorPaths>
            <path>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
            </path>
          </annotationProcessorPaths>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <excludes>
            <exclude>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
            </exclude>
          </excludes>
        </configuration>
      </plugin>
      <!-- Protobuf plugin to generate Java code from .proto files -->
      <plugin>
        <groupId>io.github.ascopes</groupId>
        <artifactId>protobuf-maven-plugin</artifactId>
        <version>3.4.2</version>
        <configuration>
          <protocVersion>${protobuf-java.version}</protocVersion>
          <binaryMavenPlugins>
            <binaryMavenPlugin>
              <groupId>io.grpc</groupId>
              <artifactId>protoc-gen-grpc-java</artifactId>
              <version>${grpc.version}</version>
              <options>@generated=omit</options>
            </binaryMavenPlugin>
          </binaryMavenPlugins>
        </configuration>
        <executions>
          <execution>
            <id>generate</id>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
      </plugin>    
    </plugins>
  </build>
```

### Implémentation du client gRPC

Créer une classe `BillingServiceClient` qui utilise le stub gRPC généré pour appeler la méthode `CreateBillingAccount`

```java
package com.pm.patientservice.grpc;

import com.pm.billingservice.proto.BillingServiceGrpc;
import com.pm.billingservice.proto.CreateBillingAccountRequest;
import com.pm.billingservice.proto.CreateBillingAccountResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {
  // Create a variable to hold the gRPC stub
  private final BillingServiceGrpc.BillingServiceBlockingStub billingServiceBlockingStub;
  private static final Logger LOGGER = LoggerFactory.getLogger(BillingServiceGrpcClient.class.getName());
  /**
   * Constructor to initialize the gRPC client with the server address and port
   * Spring IOC will automatically inject the values from application.yml
   * and the gRPC stub bean
   *
   * gRPC endpoints possible:
   *   - local development: localhost:9090/Billingservice/CreateBillingAccount
   *   - docker compose: billing-service:9090/Billingservice/CreateBillingAccount
   *   - kubernetes: billing-service.default.svc.cluster.local:9090/Billingservice/CreateBillingAccount
   *   - AWS ECS: billing-service.aws.grpc:9090/Billingservice/CreateBillingAccount
   *
   * @param serverAddress
   * @param port
   */
  public BillingServiceGrpcClient(
    @Value("${billing.service.address:localhost}") String serverAddress,
    @Value("${billing.service.port:9001}") int port
  ) {
    LOGGER.info("Connecting to Billing gRPC service at {}:{}", serverAddress, port);

    // Initialize the gRPC stub using the server address and port
    ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, port)
      .usePlaintext() // Disable TLS for simplicity; in production, consider using TLS
      .build();

    billingServiceBlockingStub = BillingServiceGrpc.newBlockingStub(channel);
  }

  public CreateBillingAccountResponse createBillingAccount(
    String patientId,
    String firstname,
    String lastname,
    String email
  ) {
    LOGGER.info("Creating billing account for patient ID: {}", patientId);

    // Build the gRPC request
    CreateBillingAccountRequest request = CreateBillingAccountRequest.newBuilder()
      .setPatientId(patientId)
      .setFirstname(firstname)
      .setLastname(lastname)
      .setEmail(email)
      .build();

    // Call the gRPC service and get the response
    CreateBillingAccountResponse response;

    try {
      response = billingServiceBlockingStub.createBillingAccount(request);
      LOGGER.info("Received response from Billing Service: {}", response);
    } catch (Exception e) {
      LOGGER.error("Error while calling Billing Service gRPC: {}", e.getMessage());
      throw new RuntimeException("Failed to create billing account", e);
    }

    // Map the gRPC response to a local DTO
    return response;
  }
}
```

On peut ensuite utiliser ce client dans un contrôleur ou un service Spring Boot.
Dans notre exemple, nous l'utilisons dans le `PatientService` pour créer un compte de facturation
lors de la création d'un nouveau patient.
On injecte le `BillingServiceGrpcClient` dans le `PatientService`.
Ensuite, on crée un compte de facturation juste après avoir sauvegardé le patient dans la base de données.

```java
class PatientService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PatientService.class);
  private final PatientRepository patientRepository;
  private final BillingServiceGrpcClient billingServiceGrpcClient;

  public PatientService(
    PatientRepository patientRepository,
    BillingServiceGrpcClient billingServiceGrpcClient
  ) {
    this.patientRepository = patientRepository;
    this.billingServiceGrpcClient = billingServiceGrpcClient;
    LOGGER.info("PatientService initialized with PatientRepository");
  }
  
  // ... other methods ...

  public PatientResponseDTO create(Patient patient) {
    Patient savedPatient = null;
    LOGGER.info("Creating patient: {}", patient);
    if (patientRepository.existsByEmail(patient.getEmail())) {
      throw new EmailAlreadyExistsException("Patient email already exists: " + patient.getEmail());
    }

    try {
      savedPatient = patientRepository.save(patient);

      // Call the Billing Service via gRPC to create a billing account
      billingServiceGrpcClient.createBillingAccount(
        savedPatient.getId().toString(),
        savedPatient.getFirstname(),
        savedPatient.getLastname(),
        savedPatient.getEmail()
      );

      LOGGER.info("Patient created with ID: {}", savedPatient.getId());
      return PatientMapper.toPatientResponseDTO(savedPatient);

    } catch (Exception e) {
      LOGGER.error("Error while saving patient : {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
```

### Configuration du client gRPC

Ajouter la configuration suivante dans le `application.properties` du microservice `patient-service`.

```properties
# gRPC client configuration for Billing Service
billing.service.address=localhost
billing.service.port=50051
```




## Testing

On peut utiliser [gRPCurl](https://github.com/fullstorydev/grpcurl/releases) pour tester les services gRPC en ligne de commande.


### gRPCurl
You can download the binary for your OS from the [Github: gRPCurl](https://github.com/fullstorydev/grpcurl/releases)

#### Basic commands

```bash
# Lister les services disponibles
grpcurl -plaintext localhost:50051 list

# Afficher les méthodes d'un service spécifique
grpcurl -plaintext localhost:50051 list my.package.MyService

# Appeler une méthode unary RPC
grpcurl -plaintext -d '{"param1": "value1", "param2": "value2"}' localhost:50051 my.package.MyService/MyMethod  

# Appeler une méthode avec streaming RPC
grpcurl -plaintext -d '{"param1": "value1"}' localhost:50051 my.package.MyService/StreamMethod

# Utiliser un fichier JSON pour les données d'entrée
grpcurl -plaintext -d @ localhost:50051 my.package.MyService/MyMethod < request.json
```

