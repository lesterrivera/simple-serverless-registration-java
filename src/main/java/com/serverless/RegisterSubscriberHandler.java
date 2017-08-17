package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.data.Subscriber;
import com.serverless.models.DynamoDBAdapter;
import com.serverless.models.JWTAdapter;
import com.serverless.models.SESAdapter;
import com.serverless.utils.Validation;
import org.apache.log4j.Logger;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.*;

/**
 * Handles the registration of a subscriber
 */
public class RegisterSubscriberHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(RegisterSubscriberHandler.class);

	/**
	 * Handles the Register Subscriber event triggered by the GET /register API call.
	 * @param input - Incoming parameters from the API Call
	 * @param context
	 * @return
	 */
	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: " + input);

		Map<String, Object> output = new HashMap<String, Object>();

		// Setup POJO for subscriber
		Subscriber subscriber = new Subscriber();

		// Uses environmental variables to check feature flags
		// TODO: Consider creating a FeatureFlags class
		Boolean AllowExternalVerify = false;
		if (System.getenv("EXTERNAL_VERIFY").equalsIgnoreCase("true")){
			AllowExternalVerify = true;
		}
		LOG.info("AllowExternalVerify=" + AllowExternalVerify);

		try{

			// Get input values
			ObjectMapper mapper = new ObjectMapper();
			JsonNode body = mapper.readTree((String) input.get("body"));

			LOG.info("body: " + body);
			output.put("email", body.get("email").asText());

			// Add values to POJO
			subscriber.setEmail(body.get("email").asText());

			LOG.debug("subscriber: " + subscriber.toString());

			// Validate data in POJO
			ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
			Validator validator = factory.getValidator();
			Set<ConstraintViolation<Subscriber>> violations = validator.validate(subscriber);

			// Check for data validation errors
			if (violations.isEmpty()) {

				// Since its a valid email, we are going to issue a token regardless

				// Set TTL to 1 month by default
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				c.add(Calendar.MONTH, -1);
				long OneMonthMillis = c.getTimeInMillis();

				// Generate JWT Token
				String userToken = JWTAdapter.getInstance().generateJWT(subscriber.getEmail(), OneMonthMillis);
				// respond with token
				output.put("token", userToken);

				// Load user
				Subscriber user = null;
				user = DynamoDBAdapter.getInstance().getSubscriber(subscriber.getEmail());
				// Check if user exists
				if(user != null){
					// Respond with if the user has verified email
					output.put("isVerified", user.getIsVerified());

				} else {
					// generate and save user
					String verifyHash = "";
					String verifyToken = "";

					// Set to true as default; in case we do not want to support email verification
					subscriber.setIsVerified(Boolean.TRUE);
					
					// Check to see if we are using an external email verification
					if (!AllowExternalVerify) {
						// Generate the VerifyToken and associated hash for subscriber
						verifyToken = Validation.generateVerifyToken(7);
						verifyHash = Validation.generateHash(verifyToken);

						// Set if we want to verify user's email
						subscriber.setVerifyHash(verifyHash);
						subscriber.setIsVerified(Boolean.FALSE);

						// Generate confirmation email to send via SES
						SESAdapter.getInstance().sendConfirmationEmail(subscriber.getEmail(), verifyToken);
					}

					// Set current date as the subscriber date
					subscriber.setSubscribeDate(new Date(System.currentTimeMillis()));

					// Tell the client if the email is verified
					output.put("isVerified", subscriber.getIsVerified());

					// Send to DynamoDB
					DynamoDBAdapter.getInstance().putSubscriber(subscriber);

				}


			} else {
				// Return a data validation error
				for (ConstraintViolation<Subscriber> violation : violations) {
					// Log each of the error message
					LOG.error("Invalid Data: " + violation.getMessage());
				}
				// Add the violations to the response
				input.put("violations", violations);
				Response responseBody = new Response("Invalid data used", output);
				return ApiGatewayResponse.builder()
						.setStatusCode(422)
				        .setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
						.setObjectBody(responseBody)
						.build();
			}

		}catch(Exception e){
			LOG.error(e,e);
			Response responseBody = new Response("Failure adding subscriber", output);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
					.setObjectBody(responseBody)
					.build();
		}

		Response responseBody = new Response("Subscriber added successfully!", output);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
				.setObjectBody(responseBody)
				.build();
	}
}
