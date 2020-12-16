// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms;

import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.common.CommunicationClientCredential;
import com.azure.communication.common.HmacAuthenticationPolicy;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Context;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmsClientTests extends SmsTestBase {

    private List<PhoneNumberIdentifier> to;
    private PhoneNumberIdentifier from;
    private String body;
    private SendSmsOptions smsOptions; 

    @BeforeEach
    public void beforeEach() {
        to = new ArrayList<PhoneNumberIdentifier>();
        body = "Hello";
        from = new PhoneNumberIdentifier("+18443394604");
        to.add(new PhoneNumberIdentifier("+18006427676"));
        smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(true);        
    }

    @Test
    public void sendSmsRequestWithHttpPipeline() {
        smsOptions.setEnableDeliveryReport(true); 
        
        HttpClient httpClient = getHttpClient(from, to, body, smsOptions);        
        SmsClientBuilder builder = new SmsClientBuilder();
        CommunicationClientCredential credential = new CommunicationClientCredential(ACCESSKEY);
        HttpPipelinePolicy[] policies = new HttpPipelinePolicy[2];
        policies[0] = new HmacAuthenticationPolicy(credential);
        policies[1] = new UserAgentPolicy();

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies)
            .httpClient(httpClient)
            .build();

        builder.endpoint(PROTOCOL + ENDPOINT);
        builder.pipeline(pipeline);
        builder.buildClient()
            .sendMessage(from, to, body, smsOptions);
    }

    @Test
    public void sendSmsRequest() {
        getTestSmsClient(from, to, body, smsOptions, null)
            .sendMessage(from, to, body, smsOptions);
    }

    @Test
    public void sendSmsMessageWithResponse() {
        getTestSmsClient(from, to, body, smsOptions, null)
            .sendMessageWithResponse(from, to, body, smsOptions, Context.NONE);
    }

    @Test
    public void sendSmsMessageWithResponseNullContext() {
        getTestSmsClient(from, to, body, smsOptions, null)
            .sendMessageWithResponse(from, to, body, smsOptions, null);
    }

    @Test
    public void sendSmsRequestAdditionalPolicy() {
        HttpPipelinePolicy policy = new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                assertNotNull(next);
                return next.process();
            }
        };

        getTestSmsClient(from, to, body, smsOptions, policy)
            .sendMessage(from, to, body, smsOptions);
    }

    @Test
    public void sendSmsRequestNoDeliverReport() {
        smsOptions.setEnableDeliveryReport(false); 

        getTestSmsClient(from, to, body, smsOptions, null)
            .sendMessage(from, to, body);
    }

    @Test
    public void sendSmsRequestSingleNumberNoDeliverReport() {
        smsOptions.setEnableDeliveryReport(false); 

        getTestSmsClient(from, to, body, smsOptions, null)
            .sendMessage(from, to.get(0), body);
    }

    @Test
    public void sendSmsRequestNoDeliverReportExplicit() {
        smsOptions.setEnableDeliveryReport(false);

        getTestSmsClient(from, to, body, smsOptions, null)
            .sendMessage(from, to, body, smsOptions);
    }

    @Test
    public void sendSmsRequestComplicated() {
        body = "今日は"; // "Hello" - Japanese

        getTestSmsClient(from, to, body, smsOptions, null)
            .sendMessage(from, to, body, smsOptions);
    }

    @Test
    public void sendSmsRequestNullFrom() {
        boolean threwException = false;

        try {
            getTestSmsClient(null, to, body, smsOptions, null)
                .sendMessage(null, to, body, smsOptions);
        } catch (NullPointerException e) {
            threwException = true;
        }

        assertTrue(threwException);
    }    

    @Test
    public void sendSmsRequestNullTo() {
        boolean threwException = false;
        PhoneNumberIdentifier toNull = null;

        try {
            getTestSmsClient(from, to, body, smsOptions, null)
                .sendMessage(from, toNull, body);
        } catch (NullPointerException e) {
            threwException = true;
        }

        assertTrue(threwException);
    } 

    private SmsClient getTestSmsClient(PhoneNumberIdentifier from, List<PhoneNumberIdentifier> to, String body, SendSmsOptions smsOptions,
            HttpPipelinePolicy policy) {
  
        return getTestSmsClientBuilder(from, to, body, smsOptions, policy)
            .buildClient();
    }
}
