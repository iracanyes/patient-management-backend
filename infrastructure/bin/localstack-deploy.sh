#!/bin/bash


set -eux;

SECONDS=0;

# Delete old stack
aws --endpoint-url=http://localhost:4566 --profile localstack cloudformation delete-stack \
  --stack-name patient-management

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

echo "Execution time $SECONDS sec."