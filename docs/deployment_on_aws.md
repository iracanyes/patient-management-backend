# Deployment

## AWS - Amazon Web Services

Cloud service provider which offer free-tiers access to multiple services.
But you can exceed your free-tiers and your credit card will be automatically charged!

### AWS infrastructure

- AWS isolate resources inside a VPC, like a private network, and is secure from the outside world
- A VPC (Virtual Private Cloud) defines how ECS (Elastic Container Service) communicates with other AWS services (like RDS, S3, ALB, etc.)
- Services like ECS, RDS, and MSK run inside this VPC to communicate securely

#### Project infrastructure

![Project infrastructure](/docs/images/aws_ecs_cluster_architecture.jpg)

#### AWS ECS: Introduction 

Here AWS concept to understand before starting using thoses services.

- **ECS**: 
  A container orchestration service that runs and manages Docker containers on AWS.

- **ECS cluster**: 
  A logical group of ECS services and tasks within a VPC.
  Manage all our ECS services. 

- **ECS service**: 
  Ensures a specified number of ECS tasks are running and integrates with load balancers
  
  Manage ECS task: load balancing, scaling, availability, failure. 
  
- **ECS tasks**: 
  A running instance of a container, created from a task definition.
  Define the resources (CPU, memory) available for the task

- **ECS Task Definition**: 
  A blueprint that defines how a container should run, 
  including its image, CPU, memory and environment variables

For each microservice, we going to define a service and task definition

#### AWS ALB

**Application Load Balancer** (ALB) routes external traffic to ECS services inside our VPC

It sits in the public subnet and forward requests to ECS services in private subnet

Secure entry point for internet traffic (such as frontend clients)

#### AWS - Localstack
Localstack provide a local dev environment which simulate AWS cloud services.
You can test and iterate your infrastructure deployments without cost.


Advantages:
- Running our stack in local dev environment using Docker
- Free, sage environment to learn and experiment with AWS services and fits into our current development workflow nicely
- Some features are not supported or have limited support (e.g: security groups)

##### Installation
Visit the below link and download Localstack CLI & Desktop. 
You must authenticate on the platform in order to access free trial for Pro features. 
[Localstack - installation docs](https://docs.localstack.cloud/aws/getting-started/installation/#localstack-desktop)

#### AWS CLI

The AWS Command Line Interface allow to manage our AWS infrastructure from command line.

##### Installation 

###### Create a profile for localstack

If it's your first install of AWS CLI, there are no existing profile configured so you can just
use the following command:
````shell
$ aws configure
````
If you already use AWS CLI, you can create a new profile for localstack

````shell
$ aws configure --profile <profile-name>
````

the following command, list all AWS CLI profiles configured

````shell
$ aws configure list-profiles
````

You can also access configuration files

````shell
# Configuration file 
$ cat ~/.aws/config

# Credential file
$ cat ~/.aws/credentials
````
##### Using a specific profile

###### Profile flag: 
To use a specific profile when you run some AWS CLI command, 
you just add the tag ```--profile <profile-name>``` to your command

````shell
$ aws s3 ls --profile localstack
````

##### Environment variable: AWS_PROFILE

The environment variable ``AWS_PROFILE`` will be used by AWS CLI to set the AWS CLI profile

````shell
# Linux
export AWS_PROFILE=localstack

# Windows Powershell
$env:AWS_PROFILE=localstack
````

#### First command to our localstack

Here is a first command to list functions in AWS Localstack Lambda.
We provide the localstack docker container URI and the profile 
````shell
$ aws --endpoint-url=http://localhost:4566 --profile localstack lambda list-functions
````

### AWS Infrastructure as code

#### IaC - Infrastructure as code

Write our infrastructure in Java code, which can be put into source control, like we do with our application code

Environments can be created, destroyed easily and consistently

Easier to debug and follow along

#### AWS CDK (Cloud Resource as Code)

##### Documentation

- [AWS CDK Java](https://docs.aws.amazon.com/cdk/v2/guide/work-with-cdk-java.html)

#### Project workflow

We define our infrastructure using Java code then generate a **CloudFormation Template**.

CloudFormation is a AWS service which let you provide a infrastructure definition as JSON or YAML file.
Those templates contains all services definition, other information required to deploy an cloud environment

The template can be used to generate local environment (localstack) or production environment (AWS)

### Create project 
We create a simple Java app project named ``infrastructure``.
Next we add the required dependencies for configuring our application

````xml
  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <aws-cdk-lib.version>2.213.0</aws-cdk-lib.version>
    <aws-java-sdk.version>1.12.788</aws-java-sdk.version>
  </properties>
  
  <dependencies>
  <dependency>
    <groupId>software.amazon.awscdk</groupId>
    <artifactId>aws-cdk-lib</artifactId>
    <version>${aws-cdk-lib.version}</version>
  </dependency>
  <dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk</artifactId>
    <version>${aws-java-sdk.version}</version>
  </dependency>
  </dependencies>
````

Next we configure the application starting point:
- We define directory where to 

````java
package com.pm.stack;

import software.amazon.awscdk.*;

public class LocalStack extends Stack {

  public LocalStack(final App scope, final String id, final StackProps props){
    super(scope, id, props);
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
````

We configure our IDE so it automate the application starts

![Intellij Ultimate IDE - Infrastructure as code run configuration](/docs/images/intellij_ide_infrastructure_as_code_running_config.jpg)

Next, start the application, it will take some time to generate the directory ``./cdk.out``
which contains all our file for CloudFormation 
- ``/cdk.out/localstack.template.json`` : CloudFormation template 

#### Define our infrastructure

![AWS : Project Infrastructure](/docs/images/aws_ecs_cluster_architecture.jpg)



#### Compile Docker container image

Now, we create the Docker images that will be used by AWS Cloud Resource as Code.
The following commands will run inside each project's service root directory
````shell
$ cd /path/to/app_root_dir

# Here we build the analytics service based on Dockerfile file in the directory
$ docker build -t <image-name>:<version> .
````


#### Create a script to generate the infrastructure

In ``infrastructure`` directory, we create a bash script ``bin/localstack-deploy.sh``
````shell
#!/bin/bash


set -eux;

# --endpoint-url  MUST be set to redirect to localstack instance on localhost or
# if AWS credentials are set, AWS CLI will try to create the infrastructure on the AWS Cloud and you will be charged!
# --profile localstack : allow to use AWS credentials set for localstack
aws --endpoint-url=http://localhost:4566 --profile localstack cloudformation deploy  \
  --stack-name patient-management \
  --template-file "./cdk.out/localstack.template.json";

# Ask Elastic Load Balancer service to provide info about its configuration
# Extract the load balancers endpoint URL at the end of the script
aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
  --query "LoadBalancers[0].DNSName" --output text;
````

Next, we can create our infrastructure

````shell
$ cd infrastructure
$ ./bin/localstack-deploy.sh
````

To fetch the list of events leading up to the failure

````shell
$ aws cloudformation describe-stack-events \
  --stack-name patient-management \
  --profile localstack \
  --endpoint-url=http://localhost:4566
````









