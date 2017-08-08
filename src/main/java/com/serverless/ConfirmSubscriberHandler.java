package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.data.AuthPolicy;
import com.serverless.data.Subscriber;
import com.serverless.models.DynamoDBAdapter;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

/**
 * Handles the email confirmation procedure
 */
public class ConfirmSubscriberHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(ConfirmSubscriberHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: " + input);
		// Query the Subscriber database
		try {
			// Get the email of the subscriber
			String principalId = input.get("email").toString();

			// Send to DynamoDB
			Subscriber user = DynamoDBAdapter.getInstance().getSubscriber(principalId);

			// Ensure that the subscriber has NOT already been verified
			if (!user.getIsVerified()){
				// Verify the VerifyHash

				// Update the subscriber to verify
			}

		}catch(Exception e){
			LOG.error(e,e);
			Response responseBody = new Response("Subscriber email has not been confirmed", input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.build();
		}

		Response responseBody = new Response("Subscriber email is confirmed", input);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.build();
	}
}
