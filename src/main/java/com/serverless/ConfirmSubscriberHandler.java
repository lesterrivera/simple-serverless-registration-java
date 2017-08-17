package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.data.AuthPolicy;
import com.serverless.data.Subscriber;
import com.serverless.models.DynamoDBAdapter;
import com.serverless.utils.Validation;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the email confirmation procedure
 */
public class ConfirmSubscriberHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(ConfirmSubscriberHandler.class);

	/**
	 * Handles the Confirm Subscriber event triggered by the GET /register API call.
	 * @param input
	 * @param context
	 * @return
	 */
	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: " + input);

		Map<String, Object> output = new HashMap<String, Object>();

		// Query the Subscriber database
		try {

			// Get input values
			ObjectMapper mapper = new ObjectMapper();
			JsonNode body = mapper.readTree((String) input.get("body"));
			output.put("email", body.get("email").asText());
			output.put("verifyToken", body.get("verifyToken").asText());

			// Get the email of the subscriber
			String principalId = body.get("email").asText();
			// Get the VerifyToken of the subscriber
			String verifyToken = body.get("verifyToken").asText();

			// Send to DynamoDB
			Subscriber user = DynamoDBAdapter.getInstance().getSubscriber(principalId);

			// Ensure that the subscriber has NOT already been verified
			if (!user.getIsVerified()){
				// Verify the verifyToken against the user's stored verifyHash
				if (Validation.validateHash(verifyToken, user.getVerifyHash())) {
					LOG.info("Subscriber verifyToken is valid");
					// Update the isVerified value for the user
					user.setIsVerified(Boolean.TRUE);
					// Send to DynamoDB
					DynamoDBAdapter.getInstance().putSubscriber(user);
					// Add isVerified to response
					LOG.info("Update subscriber isVerified in database");
				} else {
					LOG.info("Subscriber verifyToken is NOT valid");
				}
				LOG.info("Subscriber email is already verified");
			}

			// Add value to the response object
			output.put("isVerified", user.getIsVerified());

		}catch(Exception e){
			LOG.error(e,e);
			Response responseBody = new Response("Subscriber email has not been confirmed", output);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
					.setObjectBody(responseBody)
					.build();
		}

		Response responseBody = new Response("Subscriber email is confirmed", output);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
				.setObjectBody(responseBody)
				.build();
	}
}
