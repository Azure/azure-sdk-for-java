// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms;

import com.azure.communication.common.CommunicationClientCredential;
import com.azure.communication.common.PhoneNumber;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.communication.sms.models.SendSmsResponse;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SmsLiveClientTests extends SmsLiveTestBase {

    private List<PhoneNumber> to;
    private PhoneNumber from;
    private String body;
    private SendSmsOptions smsOptions; 

    @BeforeEach
    public void beforeEach() {
        to = new ArrayList<PhoneNumber>();
        body = "Hello";
        from = new PhoneNumber(PHONENUMBER);
        to.add(new PhoneNumber(PHONENUMBER));
        smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(true);        
    }

    @Test
    public void sendSmsRequest() throws NoSuchAlgorithmException {
        SendSmsResponse response = getTestSmsClient().sendMessage(from, to, body, smsOptions);
        verifyResponse(response);
    }

    @Test
    public void sendSmsMessageWithResponse() throws NoSuchAlgorithmException {
        Response<SendSmsResponse> response = getTestSmsClient()
            .sendMessageWithResponse(from, to, body, smsOptions, Context.NONE);
        
        verifyResponse(response);  
    }

    @Test
    public void sendSmsMessageWithResponseNullContext() throws NoSuchAlgorithmException {
        Response<SendSmsResponse> response = getTestSmsClient()
            .sendMessageWithResponse(from, to, body, smsOptions, null);

        verifyResponse(response);           
    }

    @Test
    public void sendSmsRequestNoDeliverReport() throws NoSuchAlgorithmException {
        smsOptions.setEnableDeliveryReport(false); 

        SendSmsResponse response = getTestSmsClient().sendMessage(from, to, body);
        verifyResponse(response);
    }

    @Test
    public void sendSmsRequestBadSignature() throws NoSuchAlgorithmException {
        smsOptions.setEnableDeliveryReport(false); 
        boolean http401ExceptionThrown = false;

        try {
            SmsClientBuilder builder = getSmsClientBuilder();

            try {
                builder.credential(new CommunicationClientCredential(DEFAULT_ACCESS_KEY));
            } catch (InvalidKeyException e) {
                fail(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                fail(e.getMessage());
            }
            
            builder.buildClient().sendMessage(from, to, body);
        } catch (HttpResponseException ex) {
            assertEquals(401, ex.getResponse().getStatusCode());
            http401ExceptionThrown = true;
        }

        assertTrue(http401ExceptionThrown);
    }

    @Test
    public void sendSmsRequestUnownedNumber() throws NoSuchAlgorithmException {
        from = new PhoneNumber("+18885555555");        
        smsOptions.setEnableDeliveryReport(false); 
        boolean http404ExceptionThrown = false;

        try {
            getTestSmsClient().sendMessage(from, to, body);
        } catch (HttpResponseException ex) {
            assertEquals(404, ex.getResponse().getStatusCode());
            http404ExceptionThrown = true;
        }

        assertTrue(http404ExceptionThrown);
    }

    @Test
    public void sendSmsRequestMalformedNumber() throws NoSuchAlgorithmException {
        from = new PhoneNumber("+1888");        
        smsOptions.setEnableDeliveryReport(false); 
        boolean http400ExceptionThrown = false;

        try {
            getTestSmsClient().sendMessage(from, to, body);
        } catch (HttpResponseException ex) {
            assertEquals(400, ex.getResponse().getStatusCode());
            http400ExceptionThrown = true;
        }

        assertTrue(http400ExceptionThrown);
    }

    private SmsClient getTestSmsClient() {
  
        return getSmsClientBuilder()
            .buildClient();
    }    
}
