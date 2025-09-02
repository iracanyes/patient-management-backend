package com.pm.stack;

import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalStack extends Stack {
  private final Vpc vpc;
  // ECS cluster
  // Will be needed to create ECS service and load balancers
  private final Cluster ecsCluster;


  public LocalStack(final App scope, final String id, final StackProps props){
    super(scope, id, props);

    // First we create the VPC network which allow all our services to communicate
    this.vpc = createVpc();

    // Next, we create the database instance required for our microservices
    DatabaseInstance authServiceDB = createDatabase("AuthServiceDB", "auth_db");
    DatabaseInstance patientServiceDB =  createDatabase( "PatientServiceDB", "patient_db");

    // Configure DB healthcheck
    // Can be used by other service to assert required database service are healthy
    CfnHealthCheck authDbHealthcheck = createDbHealthCheck(authServiceDB, "AuthServiceDBHealthCheck");
    CfnHealthCheck patientDbHealthcheck = createDbHealthCheck(patientServiceDB, "PatientServiceDBHealthCheck");


    // Next, we create a Kafka cluster
    CfnCluster kafkaCluster = createKafkaCluster("PatientManagementMskCluster");

    // AWS ECS - Elastic Container Service
    // Manage all ECS service
    this.ecsCluster = createEcsCluster("PatientManagementEcsCluster");


    // ============ AWS Fargate service - Auth service ===========================================
    Map<String, String> authServiceEnvs = new HashMap<>();
    authServiceEnvs.put("SERVER_PORT", "4004");
    authServiceEnvs.put("SERVER_CONTEXT_PATH","/auth");

    authServiceEnvs.put("DB_DATABASE", "auth_db");
    authServiceEnvs.put("DB_SCHEMA", "auth_service");
    authServiceEnvs.put("SPRING_SQL_INIT_MODE", "always");
    authServiceEnvs.put(
      "JWT_SECRET_KEY",
      "zWDW5c6b4Pvk3bYUAMq345v03k1MjHpraiMYmxdKd7uP9IZPwIsQwTHNNbUdzpU29zBYyfeWbd0KUpFHaCh"
    );
    authServiceEnvs.put("JWT_TOKEN_EXPIRATION", "36000000");
    authServiceEnvs.put("JWT_REFRESH_TOKEN_EXPIRATION", "86400000");

    FargateService authFargateService = createFargateService(
      "AuthFargateService",
      "auth-service",
      "auth_db",
      List.of(4004),
      authServiceDB,
      authServiceEnvs
    );

    // Depend on auth service DB
    authFargateService.getNode()
      .addDependency(authServiceDB);
    // Depend on auth service DB condition healthy
    authFargateService.getNode()
      .addDependency(authDbHealthcheck);


    // =========== AWS Fargate - Billing Service ================================
    Map<String, String> billingServiceEnvs = new HashMap<>();
    billingServiceEnvs.put("SERVER_PORT", "4001");
    billingServiceEnvs.put("SERVER_CONTEXT_PATH","/billing-service");
    billingServiceEnvs.put("GRPC_SERVER_PORT", "9001");

    FargateService billingService = createFargateService(
      "BillingFargateService",
      "billing-service",
      null,
      List.of(4001,9001),
      null,
      null
    );

    // ========== AWS Fargate service - Patient service ===========================
    Map<String, String> patientServiceEnvs = new HashMap<>();
    patientServiceEnvs.put("SERVER_PORT", "4000");
    patientServiceEnvs.put("SERVER_CONTEXT_PATH", "/patient-service");
    patientServiceEnvs.put("DB_DATABASE", "patient_db");
    patientServiceEnvs.put("DB_SCHEMA", "patient_service");
    patientServiceEnvs.put("SPRING_SQL_INIT_MODE", "always");
    patientServiceEnvs.put("SPRING_JPA_HIBERNATE_DLL_AUTO", "none");
    patientServiceEnvs.put("GRPC_SERVER_PORT", "9001");
    patientServiceEnvs.put("GRPC_SERVER_ADDRESS", "host.docker.internal");
    patientServiceEnvs.put("BILLING_SERVICE_SERVER_ADDRESS", "host.docker.internal");
    patientServiceEnvs.put("BILLING_SERVICE_SERVER_PORT", "4001");


    FargateService patientService = createFargateService(
      "PatientFargateService",
      "patient-service",
      "patient_service",
      List.of(4000),
      patientServiceDB,
      patientServiceEnvs
    );

    // Depend on patient service db
    patientService.getNode()
      .addDependency(patientServiceDB);

    // Depend on patient service db condition healthy
    patientService.getNode()
        .addDependency(patientDbHealthcheck);

    // Depend on billing service
    patientService.getNode()
        .addDependency(billingService);

    // Depend on Kakfa cluster
    patientService.getNode()
      .addDependency(kafkaCluster);




    // ============== AWS Fargate - Analytics Service ===========================
    Map<String, String> analyticsServiceEnvs = new HashMap<>();
    analyticsServiceEnvs.put("SERVER_PORT", "4002");
    analyticsServiceEnvs.put("SERVER_CONTEXT_PATH", "/analytics-service");

    FargateService analyticsService = createFargateService(
      "AnalyticsFargateService",
      "analytics-service",
      null,
      List.of(4002),
      null,
      analyticsServiceEnvs

      );

    analyticsService.getNode()
      .addDependency(kafkaCluster);

    // ==== AWS Fargate - API Gateway with load balancer ===================================================
    Map<String, String> apiGatewayEnvs = new HashMap<>();
    apiGatewayEnvs.put("SERVER_PORT", "4003");
    apiGatewayEnvs.put("SERVER_CONTEXT_PATH", "/api");
    apiGatewayEnvs.put("SPRING_PROFILE_ACTIVE", "prod");

    // Localstack issue to integrate docker overlay network
    // DOCKER INFO: Docker container must use internal network which all services with each other
    // but not with any other network
    // Communication with the gateway IP address (and thus appropriately configured host services) is possible,
    // and the host may communicate with any container IP directly
    // The AWS Application Load Balancer will used to reach the API Gateway from public internet
    //apiGatewayEnvs.put("AUTH_SERVICE_URL", "http://auth-service:4004");
    //apiGatewayEnvs.put("PATIENT_SERVICE_URL", "http://patient-service:4000");
    //apiGatewayEnvs.put("BILLING_SERVICE_URL", "http://billing-service:4001");
    //apiGatewayEnvs.put("ANALYTICS_SERVICE_URL", "http://analytics-service:4002");

    // Prevent Docker service discovery issue with localstack, use Docker internal network instead
    apiGatewayEnvs.put("AUTH_SERVICE_URL", "http://host.docker.internal:4004");
    apiGatewayEnvs.put("PATIENT_SERVICE_URL", "http://host.docker.internal:4000");
    apiGatewayEnvs.put("BILLING_SERVICE_URL", "http://host.docker.internal:4001");
    apiGatewayEnvs.put("ANALYTICS_SERVICE_URL", "http://host.docker.internal:4002");


    createFargateServiceWithApplicationLoadBalancer(
      "APIGatewayFargateService",
      "api-gateway",
      List.of(4003),
      apiGatewayEnvs
    );

  }

  /**
   * Create an AWS VPC instance configuration
   * with routing, public and internal subnets
   * @return A VPC instance
   */
  private Vpc createVpc(){
    // First parameter (scope) indicate to which stack it will be attached
    return Vpc.Builder.create(this, "PatientManagementVPC")
      .vpcName("PatientManagementVPC")
      .maxAzs(2)    // Max availability zone (VPC will be available in 2 AWS zone)
      .build();
  }

  /**
   * Create AWS RDBS instance configuration
   * @param id
   * @param dbName
   * @return
   */
  private DatabaseInstance createDatabase(String id, String dbName){
    return DatabaseInstance.Builder
      .create(this, id)
      .engine(
        DatabaseInstanceEngine.postgres(
          PostgresInstanceEngineProps.builder()
          .version(PostgresEngineVersion.VER_17_5)
          .build()
        )
      )
      .vpc(this.vpc)
      // AWS Engine and Storage service in which we launch our database
      .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
      // Amount of space allocated (GB)
      .allocatedStorage(20)
      // Create an admin for the database
      .credentials(Credentials.fromGeneratedSecret("pm_admin"))
      .databaseName(dbName)
      // IMPORTANT: Not to do in production, Every time you destroy the stack, you destroy also database's data
      .removalPolicy(RemovalPolicy.DESTROY)
      .build();
  }

  /**
   * Database healthcheck
   * @param db
   * @param id
   * @return
   */
  private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id){
    return CfnHealthCheck.Builder
      .create(this, id)
      .healthCheckConfig(
        CfnHealthCheck.HealthCheckConfigProperty
          .builder()
          .type("TCP")  // Test use TCP protocol
          .port(Token.asNumber(db.getDbInstanceEndpointPort())) //
          .ipAddress(db.getDbInstanceEndpointAddress())
          .requestInterval(30)  // Request interval is 30sec
          .failureThreshold(3)  // Failure threshold is 3
          .build()
      )
      .build();
  }

  /**
   * Create AWS MSK (Managed Streaming for Apache Kafka) instance
   * @param id  ID of the Kafka cluster
   */
  private CfnCluster createKafkaCluster(String id){
    return CfnCluster.Builder.create(this, id)
      .clusterName("pm-kafka-cluster")
      .kafkaVersion("4.0.0")
      .numberOfBrokerNodes(2)   // Number of broker nodes: Recommended is 3 for reliability reasons
      .brokerNodeGroupInfo(
        CfnCluster.BrokerNodeGroupInfoProperty
          .builder()
          // Define the MSK instance type
          .instanceType("kafka.m5.xlarge")
          // Here is how we connect the MSK cluster to our VPC subnetwork using its ID
          .clientSubnets(
            vpc.getPrivateSubnets()
              .stream()
              .map(ISubnet::getSubnetId)
              .collect(Collectors.toList())

          )
          .brokerAzDistribution("DEFAULT")
          .build()
      )
      .build();

  }

  private Cluster createEcsCluster(String id){
    return Cluster.Builder.create(this, id)
      .vpc(this.vpc)
      .defaultCloudMapNamespace(
        // Create a CloudMap namespace for service discovery
        // allowing microservices to communicate with other microservices
        // Internal URL naming convention: <service-name>.<namespace>
        // Ex: "auth-service.patient-management.local" will be used by other services
        // to communicate with the auth service
        CloudMapNamespaceOptions.builder()
          .name("patient-management.local")
          .build()
      )
      .build();
  }

  /**
   * Create ECS Service
   * Fargate services It's a type of ECS service used in enterprise
   * @param id
   * @param imageName
   * @return
   */
  private FargateService createFargateService(
    String id,
    String imageName,
    String dbName,
    List<Integer> ports,
    DatabaseInstance db,
    Map<String, String> additionalEnvVars
  ){

    // ECS Task definition
    FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
      .create(this, id + "Task")
      .cpu(256)
      .memoryLimitMiB(512)
      .build();

    // Container definition
    // Pull the imageName from repository
    // When we build and deploy a container image for a microservice
    // Docker store those images on local registry
    // Localstack will into the local container registry
    ContainerDefinitionOptions.Builder containerOptionsBuilder = ContainerDefinitionOptions.builder()
      .image(ContainerImage.fromRegistry(imageName))
      .environment(additionalEnvVars)
      .portMappings(
        ports.stream()
          .map(port -> PortMapping.builder()
            .containerPort(port)
            .hostPort(port)
            .protocol(Protocol.TCP)
            .build()
          )
          .toList()
      )
      .logging(LogDriver.awsLogs(
        AwsLogDriverProps.builder()
          .logGroup(
            LogGroup.Builder.create(this, id + "LogGroup")
              .logGroupName("/ecs/" + imageName)
              .removalPolicy(RemovalPolicy.DESTROY)
              .retention(RetentionDays.ONE_DAY)
              .build()
          )
          .streamPrefix(imageName)
          .build()
      ));

    // Create Environment variables used in our container
    Map<String, String> envVars = new HashMap<>();

    // 3 adresses that localstack could set to communicate with Kafka cluster
    envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510,localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");

    // Add all environment variables provided
    if(additionalEnvVars != null){
      envVars.putAll(additionalEnvVars);
    }

    if(db != null && dbName != null){
      envVars.put(
        "DB_URL",
        "jdbc:postgresql://%s:%s/%s".formatted(
          db.getDbInstanceEndpointAddress(),
          db.getDbInstanceEndpointPort(),
          dbName
        )
      );

      envVars.put("DB_USERNAME", "pm_admin");
      envVars.put("DB_PASSWORD", db.getSecret().secretValueFromJson("password").toString());

      envVars.put("DB_DRIVER", "org.postgresql.Driver");
      envVars.put("DB_HOST", db.getDbInstanceEndpointAddress());
      envVars.put("DB_PORT", db.getDbInstanceEndpointPort());
      envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");


    }

    // Add environment variables into container options
    containerOptionsBuilder.environment(envVars);

    // Add the container to our task definition
    taskDefinition.addContainer(imageName + "Container", containerOptionsBuilder.build());

    return FargateService.Builder.create(this, id)
      .cluster(this.ecsCluster)     // Attach
      .taskDefinition(taskDefinition)
      .assignPublicIp(false)    // Open to public internet
      .serviceName(imageName)
      .build();
  }

  /**
   * Create a API Gateway Fargate Service which integrate an Application Load Balancer
   * This doesn't return an object, as it would be used anywhere else!
   *
   * @param id
   * @param imageName
   * @param ports
   * @param additionalEnvVars
   *
   */
  private void createFargateServiceWithApplicationLoadBalancer(
    String id,
    String imageName,
    List<Integer> ports,
    Map<String, String> additionalEnvVars
  ){

    // ECS Task definition
    FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
      .create(this, id + "Task")
      .cpu(256)
      .memoryLimitMiB(512)
      .build();

    ContainerDefinitionOptions.Builder containerOptionsBuilder = ContainerDefinitionOptions.builder()
      .image(ContainerImage.fromRegistry(imageName))
      .environment(additionalEnvVars)
      .portMappings(
        ports.stream()
          .map(port -> PortMapping.builder()
            .containerPort(port)
            .hostPort(port)
            .protocol(Protocol.TCP)
            .build()
          )
          .toList()
      )
      .logging(LogDriver.awsLogs(
        AwsLogDriverProps.builder()
          .logGroup(
            LogGroup.Builder.create(this, id + "LogGroup")
              .logGroupName("/ecs/" + imageName)
              .removalPolicy(RemovalPolicy.DESTROY)
              .retention(RetentionDays.ONE_DAY)
              .build()
          )
          .streamPrefix(imageName)
          .build()
      ));

    // Add all environment variables provided
    if(additionalEnvVars != null){
      // Add environment variables into container options
      containerOptionsBuilder.environment(additionalEnvVars);
    }

    // Add the container to our task definition
    taskDefinition.addContainer(id + "Container", containerOptionsBuilder.build());

    // Here we configure a FargateService which integrate an Application Load Balancer
    //
    ApplicationLoadBalancedFargateService apiGateway = ApplicationLoadBalancedFargateService.Builder
      .create(this, id)
      .cluster(this.ecsCluster)
      .taskDefinition(taskDefinition)
      .assignPublicIp(true)
      .serviceName(imageName)
      .desiredCount(1)
      .healthCheckGracePeriod(Duration.seconds(60))
      .build();





  }


  public static void main(final String[] args) {
    // Define where generated output template will be stored
    App app = new App(
      AppProps.builder()
        .outdir("./cdk.out")
        .build()
    );

    // Synthesizer will compile our code into a CloudFormation template
    StackProps props = StackProps.builder()
      .synthesizer(new BootstraplessSynthesizer())
      .build();

    // Linked the CDK app to our props 
    new LocalStack(app, "localstack", props);

    // Take the stack, add any props
    app.synth();
  }
}
