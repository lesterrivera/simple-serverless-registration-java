package com.serverless;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.models.S3Adapter;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles generating pre-signed URL for the ObjectKey (i.e. the content file) in the S3 bucket.
 */
public class GetPresignedUrlHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(GetPresignedUrlHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: " + input);

		Map<String, Object> output = new HashMap<String, Object>();

		try{

			// Get input values
			ObjectMapper mapper = new ObjectMapper();
			JsonNode body = mapper.readTree((String) input.get("body"));
			output.put("uri", body.get("uri").asText());

			// Get the object path for the file from input
			String filePath = body.get("uri").asText();

			// Validate input has no bad characters

			// Call the S3Adapter method
			String ObjectUrl = S3Adapter.getInstance().generatePresignedUrl(filePath, HttpMethod.GET);

			// Generate a response object
			output.put("url", ObjectUrl);


		}catch(Exception e){
			LOG.error(e,e);
			Response responseBody = new Response("Failure generating url", output);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
					.setObjectBody(responseBody)
					.build();
		}

		Response responseBody = new Response("Url generated successfully!", output);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
				.setObjectBody(responseBody)
				.build();
	}
}
