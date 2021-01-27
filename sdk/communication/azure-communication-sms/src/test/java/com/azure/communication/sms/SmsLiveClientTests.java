// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms;

import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.communication.sms.models.SendSmsResponse;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class SmsLiveClientTests extends SmsLiveTestBase {

    private List<PhoneNumberIdentifier> to;
    private PhoneNumberIdentifier from;
    private String body;
    private SendSmsOptions smsOptions; 

    @BeforeEach
    public void beforeEach() {
        to = new ArrayList<PhoneNumberIdentifier>();
        body = "Hello";
        from = new PhoneNumberIdentifier(PHONENUMBER);
        to.add(new PhoneNumberIdentifier(PHONENUMBER));
        smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(true);        
    }

    private SmsClient getTestSmsClientWithConnectionString(HttpClient httpClient, String testName) {
        SmsClientBuilder builder = getSmsClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequest(HttpClient httpClient) throws NoSuchAlgorithmException {
        SendSmsResponse response = getTestSmsClientWithConnectionString(httpClient, "sendSmsRequestSync").sendMessage(from, to, body, smsOptions);
        verifyResponse(response);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestWithManagedIdentity(HttpClient httpClient) throws NoSuchAlgorithmException {
        SendSmsResponse response = getTestSmsClientWithManagedIdentity(httpClient, "sendSmsRequestSync").sendMessage(from, to, body, smsOptions);
        verifyResponse(response);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsMessageWithResponse(HttpClient httpClient) throws NoSuchAlgorithmException {
        Response<SendSmsResponse> response = getTestSmsClientWithConnectionString(httpClient, "sendSmsMessageWithResponseSync")
            .sendMessageWithResponse(from, to, body, smsOptions, Context.NONE);
        
        verifyResponse(response);  
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsMessageWithResponseNullContext(HttpClient httpClient) throws NoSuchAlgorithmException {
        Response<SendSmsResponse> response = getTestSmsClientWithConnectionString(httpClient, "sendSmsMessageWithResponseNullContextSync")
            .sendMessageWithResponse(from, to, body, smsOptions, null);

        verifyResponse(response);           
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestNoDeliverReport(HttpClient httpClient) throws NoSuchAlgorithmException {
        smsOptions.setEnableDeliveryReport(false); 

        SendSmsResponse response = getTestSmsClientWithConnectionString(httpClient, "sendSmsRequestNoDeliverReportSync").sendMessage(from, to, body);
        verifyResponse(response);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestBadSignature(HttpClient httpClient) throws NoSuchAlgorithmException {
        smsOptions.setEnableDeliveryReport(false); 
        boolean http401ExceptionThrown = false;

        try {
            SmsClientBuilder builder = getSmsClientBuilder(httpClient);
            builder.accessKey(DEFAULT_ACCESS_KEY);
            builder = addLoggingPolicy(builder, "sendSmsRequestBadSignature");
            builder.buildClient().sendMessage(from, to, body);
        } catch (HttpResponseException ex) {
            http401ExceptionThrown = true;
        }

        assertTrue(http401ExceptionThrown);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestUnownedNumber(HttpClient httpClient) throws NoSuchAlgorithmException {
        from = new PhoneNumberIdentifier("+18885555555");        
        smsOptions.setEnableDeliveryReport(false); 
        boolean http404ExceptionThrown = false;

        try {
            getTestSmsClientWithConnectionString(httpClient, "sendSmsRequestUnownedNumberSync").sendMessage(from, to, body);
        } catch (HttpResponseException ex) {
            assertEquals(404, ex.getResponse().getStatusCode());
            http404ExceptionThrown = true;
        }

        assertTrue(http404ExceptionThrown);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestMalformedNumber(HttpClient httpClient) throws NoSuchAlgorithmException {
        from = new PhoneNumberIdentifier("+1888");        
        smsOptions.setEnableDeliveryReport(false); 
        boolean http400ExceptionThrown = false;

        try {
            getTestSmsClientWithConnectionString(httpClient, "sendSmsRequestMalformedNumberSync").sendMessage(from, to, body);
        } catch (HttpResponseException ex) {
            assertEquals(400, ex.getResponse().getStatusCode());
            http400ExceptionThrown = true;
        }

        assertTrue(http400ExceptionThrown);
    }

    private SmsClient getTestSmsClientWithManagedIdentity(HttpClient httpClient, String testName) {
        SmsClientBuilder builder = getSmsClientBuilderWithManagedIdentity(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }  
}
