package com.serverless.models;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.serverless.data.Subscriber;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Process all DynamoDB tasks
 */
public class DynamoDBAdapter {

    private Logger logger = Logger.getLogger(this.getClass());

    private final static DynamoDBAdapter adapter = new DynamoDBAdapter();

    private final AmazonDynamoDB client;
    private final DynamoDBMapperConfig configs;

    private DynamoDBAdapter() {
        // Uses environmental variables to construct dynamodb configuration
        String DynamoTableRegion = System.getenv("REGION");
        String DynamoTableName = System.getenv("SUBSCRIBER_TABLE");

        client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                        "https://dynamodb."+ DynamoTableRegion + ".amazonaws.com",
                        DynamoTableRegion)) // Use the region in environment variables
                .build();

        configs = new DynamoDBMapperConfig.Builder()
                .withTableNameOverride(TableNameOverride.withTableNameReplacement(DynamoTableName)) // Use the table in the environment variables
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES) // Allow updates but do not remove any previous values
                .build();

        logger.info("Created DynamoDB client");
    }

    public static DynamoDBAdapter getInstance(){
        return adapter;
    }

    /**
     * Upsert a subscriber to DynamoDB table
     * @param subscriber - POJO for the Subscriber item
     * @throws IOException
     */
    public void putSubscriber(Subscriber subscriber) throws IOException{

        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(subscriber, configs);
    }

    /**
     * Get a subscriber from the DynamoDB table
     * @param email - the identifier of the Subscriber (an email address)
     * @return - A single subscriber
     * @throws IOException
     */
    public Subscriber getSubscriber(String email) throws IOException {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        return mapper.load(Subscriber.class, email, configs);
    }

}
