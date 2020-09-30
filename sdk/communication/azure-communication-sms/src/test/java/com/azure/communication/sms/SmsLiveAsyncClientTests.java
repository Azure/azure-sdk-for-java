// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms;

import com.azure.communication.common.PhoneNumber;
import com.azure.communication.sms.models.SendSmsOptions;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    public void sendSmsRequestAsync() {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(true);
        SmsAsyncClient smsClient = getTestSmsClient();
        StepVerifier.create(smsClient.sendMessage(from, to, body, smsOptions))
            .assertNext(response -> verifyResponse(response))
            .verifyComplete();
    }

    @Test
    public void sendSmsRequestAsyncNoDeliveryReport() {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);        
        SmsAsyncClient smsClient = getTestSmsClient();
        StepVerifier.create(smsClient.sendMessage(from, to, body))
            .assertNext(response -> verifyResponse(response))
            .verifyComplete();
    }

    @Test
    public void sendSmsRequestAsyncSingleNumberNoDeliveryReport() {
        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);        
        SmsAsyncClient smsClient = getTestSmsClient();
        StepVerifier.create(smsClient.sendMessage(from, to.get(0), body))
            .assertNext(response -> verifyResponse(response))
            .verifyComplete();
    }    

    private SmsAsyncClient getTestSmsClient() {
        return getSmsClientBuilder()
            .buildAsyncClient();
    }  
}
