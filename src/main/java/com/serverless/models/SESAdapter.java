package com.serverless.models;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import org.apache.log4j.Logger;

/**
 * Class to handle all SES email processing
 */
public class SESAdapter {

    private Logger LOG = Logger.getLogger(this.getClass());

    private final static SESAdapter adapter = new SESAdapter();

    private final AmazonSimpleEmailService client;

    private SESAdapter() {
        // Uses environmental variables to construct S3 configuration
        String SESRegion = System.getenv("REGION");

        client = AmazonSimpleEmailServiceClientBuilder.standard()
					.withRegion(SESRegion)
					.build();
        LOG.info("Created SES client");
    }

    public static SESAdapter getInstance(){
        return adapter;
    }

    /**
     * Send an email using SES
     * @param toEmail - recipient email address
     * @param subjectLine - subject line for the email
     * @param messageBody - message body for the email
     */
    public void sendMail(final String toEmail,
                         final String subjectLine,
                         final String messageBody) {
        // Environment variable for SES validated email address
        String FromEmailAddress = System.getenv("SES_SOURCE_EMAIL");

        // Construct an object to contain the recipient address.
        Destination destination = new Destination()
                .withToAddresses(new String[] { toEmail });

        // Create the subject and body of the message.
        Content subject = new Content().withData(subjectLine);
        Content textBody = new Content().withData(messageBody);
        Body body = new Body().withText(textBody);

        // Create a message with the specified subject and body.
        Message message = new Message().withSubject(subject).withBody(body);

        // Assemble the email.
        SendEmailRequest request = new SendEmailRequest()
                .withSource(FromEmailAddress)
                .withDestination(destination)
                .withMessage(message);

        // Send the email.
        client.sendEmail(request);

        LOG.info("Email sent");
    }

}
