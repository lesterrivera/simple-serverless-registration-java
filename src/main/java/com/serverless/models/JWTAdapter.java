package com.serverless.models;

import org.apache.log4j.Logger;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import io.jsonwebtoken.*;
import java.util.Date;

import java.io.IOException;


/**
 * Class to handle all JWT Token processing
 */
public class JWTAdapter {

    private Logger LOG = Logger.getLogger(this.getClass());

    private final static JWTAdapter adapter = new JWTAdapter();

    private JWTAdapter() {
        LOG.info("Created JWT client");
    }

    public static JWTAdapter getInstance(){
        return adapter;
    }

    /**
     * Generate the JWT Token
     * @param subject - The subject of the token; user name or email address
     * @param ttlMillis - EXpiration time for token; in milliseconds
     * @return
     * @throws IOException
     */
    public String generateJWT(String subject, long ttlMillis) throws IOException{

        // Uses environmental variables
        String apiKey = System.getenv("JWTKEY");
        String ServiceName = System.getenv("SERVICE_NAME"); // The service name from the environment variables

        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(apiKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(ServiceName)
                .signWith(signatureAlgorithm, signingKey);

        //if it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    /**
     * Parse the JWT Token into claims
     * @param jwt
     */
    public Claims parseJWT(String jwt) {

        // Uses environmental variables
        String apiKey = System.getenv("JWTKEY");

        try {

            //This line will throw an exception if it is not a signed JWS (as expected)
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(apiKey))
                    .parseClaimsJws(jwt).getBody();

            LOG.debug("JWT parsed: ["
                    + "Subject=" + claims.getSubject()
                    + ", Issuer=" + claims.getIssuer()
                    + ", Expiration=" + claims.getExpiration()
                    + ", IssuedAt=" + claims.getIssuedAt()
                    + "]");

            return claims;
        } catch (SignatureException | ExpiredJwtException e) {
            // Isolated these exceptions in case I decide to do something different later
            LOG.info("Untrusted JWT Token: " + e);
            return null;
        } catch(Exception ex){
            // don't trust the JWT!
            LOG.info("Untrusted JWT Token: " + ex);
            return null;
        }
    }

}
