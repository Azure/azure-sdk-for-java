// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms;

import com.azure.communication.common.PhoneNumber;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.core.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

public class SmsLiveAsyncClientTests extends SmsLiveTestBase {

    private List<PhoneNumber> to;
    private PhoneNumber from;
    private String body;

    @BeforeEach
    public void beforeEach() {
        to = new ArrayList<PhoneNumber>();
        body = "Hello";
        from = new PhoneNumber(PHONENUMBER);
        to.add(new PhoneNumber(PHONENUMBER));
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createAsyncClientUsingConnectionString(HttpClient httpClient) {
        SmsAsyncClient smsClient = getSmsClientBuilderWithConnectionString(httpClient).buildAsyncClient();
        assertNotNull(smsClient);
        // Smoke test sms client by sending message
        StepVerifier.create(smsClient.sendMessage(from, to, body, null))
            .assertNext(response -> verifyResponse(response))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestAsync(HttpClient httpClient) {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(true);
        SmsAsyncClient smsClient = getTestSmsClient(httpClient);
        StepVerifier.create(smsClient.sendMessage(from, to, body, smsOptions))
            .assertNext(response -> verifyResponse(response))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestAsyncNoDeliveryReport(HttpClient httpClient) {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);        
        SmsAsyncClient smsClient = getTestSmsClient(httpClient);
        StepVerifier.create(smsClient.sendMessage(from, to, body))
            .assertNext(response -> verifyResponse(response))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestAsyncSingleNumberNoDeliveryReport(HttpClient httpClient) {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);        
        SmsAsyncClient smsClient = getTestSmsClient(httpClient);
        StepVerifier.create(smsClient.sendMessage(from, to.get(0), body))
            .assertNext(response -> verifyResponse(response))
            .verifyComplete();
    }    

    private SmsAsyncClient getTestSmsClient(HttpClient httpClient) {
        return getSmsClientBuilder(httpClient)
            .buildAsyncClient();
    }  
}
