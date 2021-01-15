// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms;

import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.sms.models.SendSmsOptions;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

public class SmsAsyncClientTests extends SmsTestBase {

    private List<PhoneNumberIdentifier> to;
    private PhoneNumberIdentifier from;
    private String body;

    @BeforeEach
    public void beforeEach() {
        to = new ArrayList<PhoneNumberIdentifier>();
        body = "Hello";
        from = new PhoneNumberIdentifier("+18443394604");
        to.add(new PhoneNumberIdentifier("+18006427676"));
    }

    @Test
    public void sendSmsRequestAsync() {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(true);
        SmsAsyncClient smsClient = getTestSmsClient(from, to, body, smsOptions);
        StepVerifier.create(smsClient.sendMessage(from, to, body, smsOptions))
            .verifyComplete();
    }

    @Test
    public void sendSmsRequestAsyncNoDeliveryReport() {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);        
        SmsAsyncClient smsClient = getTestSmsClient(from, to, body, smsOptions);
        StepVerifier.create(smsClient.sendMessage(from, to, body))
            .verifyComplete();
    }

    @Test
    public void sendSmsRequestAsyncSingleNumberNoDeliveryReport() {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);        
        SmsAsyncClient smsClient = getTestSmsClient(from, to, body, smsOptions);
        StepVerifier.create(smsClient.sendMessage(from, to.get(0), body))
            .verifyComplete();
    }    

    @Test
    public void sendSmsRequestAsyncNullFrom() {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);        
        SmsAsyncClient smsClient = getTestSmsClient(from, to, body, smsOptions);
        StepVerifier.create(smsClient.sendMessage(null, to.get(0), body))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void sendSmsRequestAsyncNullTo() {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);        
        SmsAsyncClient smsClient = getTestSmsClient(from, to, body, smsOptions);
        PhoneNumberIdentifier toNull = null;
        StepVerifier.create(smsClient.sendMessage(from, toNull, body))
            .verifyError(NullPointerException.class);
    }    

    @Test
    public void sendSmsRequestAsyncNullToList() {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);        
        SmsAsyncClient smsClient = getTestSmsClient(from, to, body, smsOptions);
        List<PhoneNumberIdentifier> toNull = null;
        StepVerifier.create(smsClient.sendMessage(from, toNull, body))
            .verifyError(NullPointerException.class);
    }   

    private SmsAsyncClient getTestSmsClient(PhoneNumberIdentifier from, List<PhoneNumberIdentifier> to, String body, 
        SendSmsOptions smsOptions) {
        return getTestSmsClientBuilder(from, to, body, smsOptions).buildAsyncClient();
    }  
}
