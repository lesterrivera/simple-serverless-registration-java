package com.serverless.models;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import org.apache.log4j.Logger;

import java.net.URLEncoder;

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

    /**
     * Sends the confirmation email
     * @param userEmail
     * @param verifyToken
     */
    public void sendConfirmationEmail(final String userEmail,
                         final String verifyToken) {
        // Environment variable
        String ServiceName = System.getenv("SERVICE_NAME"); // The service name from the environment variables
        String VerifyLinkURL = System.getenv("VERIFY_URL"); // The verify url

        try{
            // Generate confirmation email to send via SES
            String VerifyLink = VerifyLinkURL + "?email" + URLEncoder.encode(userEmail, "UTF-8")  + "&verifyToken=" + verifyToken;
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

            // Send the email.
            this.sendMail(userEmail, verifySubjectLine, verifyMessage);

            LOG.info("Email sent");

        }catch(Exception e){
            LOG.error(e,e);
        }
    }

}
