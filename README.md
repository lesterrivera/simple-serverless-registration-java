# Simple Serverless Registration _built with Java_

A bootstrap template for a serverless website that enables email-based registration 
designed to offer free access to assets on an S3 bucket (using
pre-signed urls) to registered users.

This project is the built and deployed using [Serverless Framework](https://serverless.com) and the following technology stack:
- AWS Lambda _with Java 8_
- AWS API Gateway
- AWS DynamoDB
- AWS SES
- AWS S3
- Angular


## Usage

1. Set up Serverless Framework and your AWS account as detailed [here](https://serverless.com/framework/docs/getting-started/).

2. Deploy the Lambda

```
$ git clone https://github.com/lesterrivera/simple-serverless-registration-java.git

$ cd transactions-api

$ mvn clean install

$ serverless deploy

```

3. Hit the HTTP endpoints

4. Destroy the infrastructure

```
$ serverless remove
```
