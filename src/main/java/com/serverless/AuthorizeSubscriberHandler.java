package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.data.AuthPolicy;
import com.serverless.data.Subscriber;
import com.serverless.data.TokenAuthorizerContext;
import com.serverless.models.DynamoDBAdapter;
import com.serverless.models.JWTAdapter;
import io.jsonwebtoken.Claims;
import org.apache.log4j.Logger;


/**
 * Handles IO for a Java Lambda function as a custom authorizer for API Gateway
 */
public class AuthorizeSubscriberHandler implements RequestHandler<TokenAuthorizerContext, AuthPolicy> {

	private static final Logger LOG = Logger.getLogger(AuthorizeSubscriberHandler.class);

	@Override
	public AuthPolicy handleRequest(TokenAuthorizerContext input, Context context) {
		LOG.info("received: " + input.toString());

		// Get variables from the input required to generate the policy document
		String methodArn = input.getMethodArn();
		String[] arnPartials = methodArn.split(":");
		String region = arnPartials[3];
		String awsAccountId = arnPartials[4];
		String[] apiGatewayArnPartials = arnPartials[5].split("/");
		String restApiId = apiGatewayArnPartials[0];
		String stage = apiGatewayArnPartials[1];
		String httpMethod = apiGatewayArnPartials[2];
		String resource = ""; // root resource
		if (apiGatewayArnPartials.length == 4) {
			resource = apiGatewayArnPartials[3];
		}
		// Initialize with default user
		String principalId = "User";

		// Retrieve the incoming token generated by the API Gateway
		String token = input.getAuthorizationToken().replace("Bearer", "").trim(); // Remove Bearer string
		LOG.debug("token: " + token);

		// Get the claims in the token
		Claims userToken = JWTAdapter.getInstance().parseJWT(token);
		LOG.debug("userToken: " + userToken.toString());

		if (userToken != null) {

			// and produce the principal user identifier associated with the token
			principalId = userToken.getSubject();
			LOG.debug("principalId: " + principalId);
			// Query the Subscriber database
			try {
				// load from  DynamoDB
				Subscriber user = DynamoDBAdapter.getInstance().getSubscriber(principalId);
				LOG.debug("user: " + user.toString());
				// Ensure that the subscriber has also been verified
				if (user != null && user.getIsVerified()) {
					LOG.info("user authorized: " + user);
					// Generate the AllowAll policy document to be processed by the API Gateway
					return new AuthPolicy(principalId,
							AuthPolicy.PolicyDocument.getAllowAllPolicy(region,
									awsAccountId,
									restApiId,
									stage));
				}

			} catch (Exception e) {
				LOG.error(e, e);
//				Response responseBody = new Response("Unauthorized", output);
				return new AuthPolicy(principalId,
						AuthPolicy.PolicyDocument.getDenyAllPolicy(region,
								awsAccountId,
								restApiId,
								stage));
			}
		}
		LOG.info("user not authorized: " + principalId);
		return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
	}
}
