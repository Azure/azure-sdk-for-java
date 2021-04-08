// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.util.Context;
import java.util.Arrays;

public class SendSmsToGroupExample {
    public static void main(String[] args) {
        String connectionString = System.getenv("COMMUNICATION_CONNECTION_STRING");
        String phoneNumber = System.getenv("COMMUNICATION_PHONE_NUMBER");
        SmsClient smsClient = new SmsClientBuilder()
            .connectionString(connectionString)
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
