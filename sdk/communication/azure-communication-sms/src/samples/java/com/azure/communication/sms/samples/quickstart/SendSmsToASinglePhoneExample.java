// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;

public class SendSmsToASinglePhoneExample {
    public static void main(String[] args) {
        String connectionString = System.getenv("COMMUNICATION_SAMPLES_CONNECTION_STRING");
        String phoneNumber = System.getenv("AZURE_PHONE_NUMBER");
        SmsClient smsClient = new SmsClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("Marketing");

        SmsSendResult sendResult = smsClient.send(phoneNumber, phoneNumber, "Weekly promotion", options);

        System.out.println("Message Id: " + sendResult.getMessageId());
        System.out.println("Recipient Number: " + sendResult.getTo());
        System.out.println("Send Result Successful:" + sendResult.isSuccessful());
    }
}
