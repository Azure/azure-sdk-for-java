// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;

/**
 * Sample demonstrating how to send an SMS message with delivery report timeout configuration.
 * The deliveryReportTimeoutInSeconds field specifies after how many seconds to consider
 * a delivery report expired if not received.
 */
public class SendSmsWithDeliveryReportTimeoutExample {
    public static void main(String[] args) {
        // Get connection string and phone number from environment variables
        String connectionString = System.getenv("COMMUNICATION_SAMPLES_CONNECTION_STRING");
        String phoneNumber = System.getenv("AZURE_PHONE_NUMBER");

        // Create SMS client
        SmsClient smsClient = new SmsClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // Configure SMS options with delivery report timeout
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setDeliveryReportTimeoutInSeconds(60); // Wait 60 seconds for delivery report
        options.setTag("TimeoutExample");

        // Send SMS message
        SmsSendResult sendResult = smsClient.send(
            phoneNumber,
            phoneNumber,
            "This message has a 60-second delivery report timeout",
            options
        );

        // Output results
        System.out.println("Message Id: " + sendResult.getMessageId());
        System.out.println("Recipient Number: " + sendResult.getTo());
        System.out.println("Send Result Successful: " + sendResult.isSuccessful());
        System.out.println("Delivery report timeout configured for: 60 seconds");
    }
}
