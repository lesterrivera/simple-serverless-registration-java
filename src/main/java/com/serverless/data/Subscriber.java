package com.serverless.data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Email;

import java.util.Date;

/**
 * POJO represents the DynamoDB Items in the subscriber table.
 */
@DynamoDBTable(tableName = "subscriberTable")
public class Subscriber {

    @NotNull(message = "Email cannot be null")
    @Email(message="Please provide a valid email address")
    @Pattern(regexp=".+@.+\\..+", message="Please provide a valid email address")
    @DynamoDBHashKey(attributeName = "email")
    String email;

    @DynamoDBAttribute(attributeName = "subscribeDate")
    Date subscribeDate;

    @DynamoDBAttribute(attributeName = "verifyHash")
    String verifyHash;

    @DynamoDBAttribute(attributeName = "isVerified")
    Boolean isVerified;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getSubscribeDate() {
        return subscribeDate;
    }

    public void setSubscribeDate(Date subscribeDate) {
        this.subscribeDate = subscribeDate;
    }

    public String getVerifyHash() {
        return verifyHash;
    }

    public void setVerifyHash(String verifyHash) {
        this.verifyHash = verifyHash;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    @Override
    public String toString() {
        return "Subscriber [email=" + email
                + ", subscribeDate=" + subscribeDate
                + ", verifyHash=" + verifyHash
                + ", isVerified=" + isVerified + "]";
    }
}
