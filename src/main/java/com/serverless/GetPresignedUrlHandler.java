package com.serverless;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.models.S3Adapter;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Handles generating pre-signed URL for the ObjectKey (i.e. the content file) in the S3 bucket.
 */
public class GetPresignedUrlHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(GetPresignedUrlHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: " + input);
		try{

			// Get the object path for the file from input
			String filePath = input.get("ObjectKey").toString();

			// Validate input has no bad characters

			// Call the S3Adapter method
			String ObjectUrl = S3Adapter.getInstance().generatePresignedUrl(filePath, HttpMethod.GET);

			// Generate a response object
			input.put("ObjectUrl", ObjectUrl);


		}catch(Exception e){
			LOG.error(e,e);
			Response responseBody = new Response("Failure generating url", input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.build();
		}

		Response responseBody = new Response("Url generated successfully!", input);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.build();
	}
}
