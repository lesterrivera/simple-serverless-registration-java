package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.data.Subscriber;
import com.serverless.models.DynamoDBAdapter;
import com.serverless.models.SESAdapter;
import com.serverless.utils.Validation;
import org.apache.log4j.Logger;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.Set;

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

			String verifyHash = "";
			String verifyToken = "";
			// Check to see if we are using an external email verification
			if (!AllowExternalVerify) {
				// Generate the VerifyToken and associated hash for subscriber
				verifyToken = Validation.generateVerifyToken(7);
				verifyHash = Validation.generateHash(verifyToken);
			}

			// Add values to POJO
			subscriber.setEmail(input.get("email").toString());
			subscriber.setSubscribeDate(new Date(System.currentTimeMillis()));
			subscriber.setVerifyHash(verifyHash);
			subscriber.setIsVerified(Boolean.FALSE);

			LOG.debug("subscriber: " + subscriber.toString());

			// Validate data in POJO
			ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
			Validator validator = factory.getValidator();
			Set<ConstraintViolation<Subscriber>> violations = validator.validate(subscriber);

			// Check for data validation errors
			if (violations.isEmpty()) {
				// Send to DynamoDB
				DynamoDBAdapter.getInstance().putSubscriber(subscriber);

				// Check to see if we are using an external email verification
				if (!AllowExternalVerify) {
					// Generate confirmation email to send via SES
					String ServiceName = System.getenv("SERVICE_NAME"); // The service name from the environment variables
					String VerifyLinkURL = System.getenv("VERIFY_URL"); // The verify url
					String VerifyLink = VerifyLinkURL + "?email" + URLEncoder.encode(subscriber.getEmail(), "UTF-8") + "&verifyToken=" + verifyToken;
					String verifySubjectLine = "Please confirm the email address for " + ServiceName;
					String verifyMessage = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
							+ "<title>" + verifySubjectLine + "</title>"
							+ "</head><body>"
							+ "Please <a href\"" + VerifyLink + "\">click here to verify your email address</a> or copy & paste the following link in a browser:"
							+ "<br><br>"
							+ "<a href=\"" + VerifyLink + "\">" + VerifyLink + "</a>"
							+ "</body></html>";

					LOG.debug("Verify email is ["
							+ "subject=" + verifySubjectLine
							+ "message=" + verifyMessage
							+ "]");

					// Send email
					SESAdapter.getInstance().sendMail(subscriber.getEmail(), verifySubjectLine, verifyMessage);
				}

			} else {
				// Return a data validation error
				for (ConstraintViolation<Subscriber> violation : violations) {
					// Log each of the error message
					LOG.error("Invalid Data: " + violation.getMessage());
				}
				// Add the violations to the response
				input.put("violations", violations);
				Response responseBody = new Response("Invalid data used", input);
				return ApiGatewayResponse.builder()
						.setStatusCode(422)
						.setObjectBody(responseBody)
						.build();
			}

		}catch(Exception e){
			LOG.error(e,e);
			Response responseBody = new Response("Failure adding subscriber", input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.build();
		}
		// Add new values to response
		input.put("subscribeDate", subscriber.getSubscribeDate());
		input.put("isActive", subscriber.getIsVerified());
		Response responseBody = new Response("Subscriber added successfully!", input);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.build();
	}
}
