package com.serverless.models;

import com.amazonaws.AmazonClientException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;

/**
 * Class to handle all DynamoDB processing
 */
public class S3Adapter {

    private Logger LOG = Logger.getLogger(this.getClass());

    private final static S3Adapter adapter = new S3Adapter();

    private final AmazonS3 client;

    private S3Adapter() {
        // Uses environmental variables to construct S3 configuration
        String S3Region = System.getenv("REGION");

        client = AmazonS3ClientBuilder.standard()
					.withRegion(S3Region)
					.build();
        LOG.info("Created S3 client");
    }

    public static S3Adapter getInstance(){
        return adapter;
    }

    /**
     * Generate a Pre-Signed URL for an object in the S3 bucket
     * @param objectPath
     * @param httpMethod
     * @return
     * @throws IOException
     */
    public String generatePresignedUrl(String objectPath, HttpMethod httpMethod) throws IOException{

        // Uses environmental variables to construct S3 configuration
        String S3Bucket = System.getenv("PRIVATE_BUCKET");

        java.util.Date expiration = new java.util.Date();
        long msec = expiration.getTime();
        msec += 1000 * 60 * 60; // Add 1 hour.
        expiration.setTime(msec);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(S3Bucket, objectPath);
        generatePresignedUrlRequest.setMethod(httpMethod);
        generatePresignedUrlRequest.setExpiration(expiration);

        URL signedUrl = client.generatePresignedUrl(generatePresignedUrlRequest);

        return signedUrl.toString();
    }

}
