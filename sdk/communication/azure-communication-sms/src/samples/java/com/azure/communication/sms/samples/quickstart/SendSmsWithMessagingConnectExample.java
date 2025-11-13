// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.implementation.models.MessagingConnectOptions;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating how to send an SMS message using Messaging Connect
 * feature.
 * Messaging Connect allows connecting with partners to deliver SMS messages.
 * When using MessagingConnect, both partner name and partnerParams must be
 * provided.
 * The partnerParams should contain the required parameters for your specific
 * messaging partner.
 */
public class SendSmsWithMessagingConnectExample {
    public static void main(String[] args) {
        // Get connection string and phone number from environment variables
        String connectionString = System.getenv("COMMUNICATION_SAMPLES_CONNECTION_STRING");
        String phoneNumber = System.getenv("AZURE_PHONE_NUMBER");

        // Create SMS client
        SmsClient smsClient = new SmsClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Configure MessagingConnect options with dynamic partner parameters
        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        messagingConnect.setPartner("YourPartnerName"); // Partner name

        // Create partner-specific parameters map
        Map<String, Object> partnerParams = new HashMap<>();
        partnerParams.put("apiKey", "your-partner-api-key"); // API key from partner portal
        // Add other partner-specific parameters as needed
        // partnerParams.put("servicePlanId", "your-service-plan-id");
        // partnerParams.put("authToken", "your-auth-token");

        messagingConnect.setPartnerParams(partnerParams);

        // Configure SMS options with MessagingConnect
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setMessagingConnect(messagingConnect);
        options.setTag("MessagingConnectExample");

        // Send SMS message through Messaging Connect partner
        SmsSendResult sendResult = smsClient.send(
                phoneNumber,
                phoneNumber,
                "This message is sent via Messaging Connect partner",
                options);

        // Output results
        System.out.println("Message Id: " + sendResult.getMessageId());
        System.out.println("Recipient Number: " + sendResult.getTo());
        System.out.println("Send Result Successful: " + sendResult.isSuccessful());
        System.out.println("Message sent via Messaging Connect partner: " +
                messagingConnect.getPartner());
    }
}
