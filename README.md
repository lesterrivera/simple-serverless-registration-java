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

3. Deploy the AWS stack

```
$ serverless deploy
```

4. Use web browser to open the front-end website on your S3 bucket

## Configure optional support for Verification Email using SES
Its possible to send the registering user an email verification link over SES. Doing so will ensure that 
the user cannot retrieve a pre-signed url to the private assets in the s3 bucket until the email
has been separately verified using the `verifyToken` generated and sent over SES.

1. Configure AWS SES in your AWS Console as detailed [here](http://docs.aws.amazon.com/ses/latest/DeveloperGuide/before-you-begin.html) 

2. Set appropriate values to the following  in serverless.yml.

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

## Viewing the website locally
The sample website is built with angular 4 using bootstrap 4. When using the angular cli,
you can view the website locally rather than thru the s3 bucket that serverless framework deploys. 

To run the website locally, use the following commands:
```bash

$ cd static

$ npm install

$ ng serve

```

## Synchronize contents to your S3 bucket
You can deploy both private assets and the front-end website to your S3 buckets without deploying the serverless architecture as follows:

```
$ serverless syncToS3
```

However, deploying the serverless architecture again also works well and does not affect the already-deployed components.

## Cleanup
If you wish to remove the AWS infrastructure created as a part of this project, use the following command to do so. 
This will remove the IAM roles, any resources defined in serverless.yml, and destroy the CloudFormation stack.

```
$ serverless deleteFromS3

$ serverless remove

Serverless: Getting all objects in S3 bucket...
Serverless: Removing objects in S3 bucket...
Serverless: Removing Stack...
Serverless: Checking Stack removal progress...
..................................
Serverless: Stack removal finished...
```
