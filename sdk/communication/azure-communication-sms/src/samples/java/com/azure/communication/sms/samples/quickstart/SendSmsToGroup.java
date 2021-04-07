// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;

import java.util.Arrays;

public class SendSmsToGroup {
    public static void main(String[] args) {
        String endpoint = System.getenv("AZURE_COMMUNICATION_ENDPOINT");
        String accessKey = System.getenv("AZURE_COMMUNICATION_KEY");
        String phoneNumber = System.getenv("COMMUNICATION_PHONE_NUMBER");
        AzureKeyCredential keyCredential = new AzureKeyCredential(accessKey);
        SmsClient smsClient = new SmsClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .buildClient();

        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("Marketing");


        Iterable<SmsSendResult> sendResults = smsClient.sendWithResponse(
            phoneNumber,
            Arrays.asList(phoneNumber),
            "Weekly Promotion",
            options /* Optional */,
            Context.NONE).getValue();

        for (SmsSendResult result : sendResults) {
            System.out.println("Message Id: " + result.getMessageId());
            System.out.println("Recipient Number: " + result.getTo());
            System.out.println("Send Result Successful:" + result.isSuccessful());
        }
    }
}
