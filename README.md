# Simple Serverless Registration _built with Java_

A boilerplate serverless architecture for a simple email-based registration system that enables a serverless website to 
offer free access to assets stored on an S3 bucket (using pre-signed urls) to registered users; for example, offers 
access to a video once the user has registered with their confirmed email address.

This project is the built and deployed using [Serverless Framework](https://serverless.com) and the following technology stack:
- AWS Lambda _with Java 8_
- AWS API Gateway
- AWS DynamoDB
- AWS SES
- AWS S3
- Angular

Current Status: __Under Development__

## Usage

1. Set up Serverless Framework and your AWS account as detailed [here](https://serverless.com/framework/docs/getting-started/).

2. Clone the project and initialize the stack

```
$ git clone https://github.com/lesterrivera/simple-serverless-registration-java.git

$ cd simple-serverless-registration-java

$ npm install

$ mvn clean install

```
3. Build the front-end website written in Angular in the _static_ folder of the project
```
$ cd static

$ npm install

# ng build --prod

$ cd ..
```

4. Deploy the AWS stack

```
$ serverless deploy
```

5. Configure AWS SES in your AWS Console as detailed [here](http://docs.aws.amazon.com/ses/latest/DeveloperGuide/before-you-begin.html) and set the following values in serverless.yml.

```$yml
    # Allow an external party to verify email (do not send verification email)
    EXTERNAL_VERIFY: false
    # The source email for SES; must be validated via AWS Console
    SES_SOURCE_EMAIL: noreply@myhost.com
    # The name of your service; used in the validation email to subscriber
    SERVICE_NAME: myhost
    # The verification url; used in the validation email to subscriber
    VERIFY_URL: https://www.myhost.com/verify.html
```
6. Use web browser to open the front-end website on your S3 bucket

## Synchronize contents to your S3 bucket
You can deploy both private assets and the front-end website to your S3 buckets without deploying the serverless architecture as follows:

```
$ serverless s3sync
```

However, deploying the serverless architecture again also works well and does not affect the already-deployed components.

## Cleanup
If you wish to remove the AWS infrastructure created as a part of this project, use the following command to do so. 
This will remove the IAM roles, any resources defined in serverless.yml, and destroy the CloudFormation stack.

```
$ serverless remove

Serverless: Getting all objects in S3 bucket...
Serverless: Removing objects in S3 bucket...
Serverless: Removing Stack...
Serverless: Checking Stack removal progress...
..................................
Serverless: Stack removal finished...
```
