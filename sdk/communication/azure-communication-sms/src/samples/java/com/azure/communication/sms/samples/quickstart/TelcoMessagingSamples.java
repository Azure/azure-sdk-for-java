// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import java.util.Arrays;
import java.util.List;

import com.azure.communication.sms.TelcoMessagingClient;
import com.azure.communication.sms.TelcoMessagingAsyncClient;
import com.azure.communication.sms.TelcoMessagingClientBuilder;
import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.DeliveryReportsClient;
import com.azure.communication.sms.OptOutsClient;
import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.communication.sms.models.OptOutResult;
import com.azure.communication.sms.models.OptOutCheckResult;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Sample code showing how to use the new TelcoMessagingClient for organized SMS functionality.
 */
public class TelcoMessagingSamples {

    /**
     * Sample showing how to create a TelcoMessagingClient using connection string.
     */
    public TelcoMessagingClient createTelcoMessagingClientWithConnectionString() {
        // BEGIN: readme-sample-createTelcoMessagingClientWithConnectionString
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        TelcoMessagingClient telcoMessagingClient = new TelcoMessagingClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: readme-sample-createTelcoMessagingClientWithConnectionString

        return telcoMessagingClient;
    }

    /**
     * Sample showing how to create a TelcoMessagingClient using Azure Key Credential.
     */
    public TelcoMessagingClient createTelcoMessagingClientUsingAzureKeyCredential() {
        // BEGIN: readme-sample-createTelcoMessagingClientUsingAzureKeyCredential
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<resource-name>.communication.azure.com";
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

        TelcoMessagingClient telcoMessagingClient = new TelcoMessagingClientBuilder()
            .endpoint(endpoint)
            .credential(azureKeyCredential)
            .buildClient();
        // END: readme-sample-createTelcoMessagingClientUsingAzureKeyCredential

        return telcoMessagingClient;
    }

    /**
     * Sample showing how to create a TelcoMessagingAsyncClient.
     */
    public TelcoMessagingAsyncClient createTelcoMessagingAsyncClient() {
        // BEGIN: readme-sample-createTelcoMessagingAsyncClient
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        TelcoMessagingAsyncClient telcoMessagingAsyncClient = new TelcoMessagingClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: readme-sample-createTelcoMessagingAsyncClient

        return telcoMessagingAsyncClient;
    }

    /**
     * Sample showing how to send SMS using the organized client structure.
     */
    public void sendSmsUsingTelcoMessagingClient() {
        TelcoMessagingClient telcoMessagingClient = createTelcoMessagingClientWithConnectionString();

        // BEGIN: readme-sample-sendSmsUsingTelcoMessagingClient
        // Get the SMS client from the organized telco messaging client
        SmsClient smsClient = telcoMessagingClient.getSmsClient();

        // Send a simple SMS
        SmsSendResult result = smsClient.send(
            "+1234567890", // from
            "+0987654321", // to
            "Hello, this is a test message!"
        );

        System.out.println("Message sent. Message Id: " + result.getMessageId());
        // END: readme-sample-sendSmsUsingTelcoMessagingClient
    }

    /**
     * Sample showing how to send SMS with options using the organized client.
     */
    public void sendSmsWithOptionsUsingTelcoMessagingClient() {
        TelcoMessagingClient telcoMessagingClient = createTelcoMessagingClientWithConnectionString();

        // BEGIN: readme-sample-sendSmsWithOptionsUsingTelcoMessagingClient
        SmsClient smsClient = telcoMessagingClient.getSmsClient();

        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("marketing-campaign-2024");

        SmsSendResult result = smsClient.send(
            "+1234567890", // from
            "+0987654321", // to
            "Hello with delivery reports enabled!",
            options
        );

        System.out.println("Message sent with options. Message Id: " + result.getMessageId());
        // END: readme-sample-sendSmsWithOptionsUsingTelcoMessagingClient
    }

    /**
     * Sample showing how to get delivery reports using the organized client.
     */
    public void getDeliveryReportUsingTelcoMessagingClient() {
        TelcoMessagingClient telcoMessagingClient = createTelcoMessagingClientWithConnectionString();

        // BEGIN: readme-sample-getDeliveryReportUsingTelcoMessagingClient
        // Get the delivery reports client from the organized telco messaging client
        DeliveryReportsClient deliveryReportsClient = telcoMessagingClient.getDeliveryReportsClient();

        String messageId = "message-id-from-send-operation";

        try {
            DeliveryReport deliveryReport = deliveryReportsClient.getDeliveryReport(messageId);

            System.out.println("Delivery Status: " + deliveryReport.getDeliveryStatus());
            System.out.println("Received Timestamp: " + deliveryReport.getReceivedTimestamp());
            if (deliveryReport.getDeliveryStatusDetails() != null) {
                System.out.println("Status Details: " + deliveryReport.getDeliveryStatusDetails());
            }
        } catch (Exception e) {
            System.err.println("Failed to get delivery report: " + e.getMessage());
        }
        // END: readme-sample-getDeliveryReportUsingTelcoMessagingClient
    }

    /**
     * Sample showing how to get delivery reports with response information.
     */
    public void getDeliveryReportWithResponseUsingTelcoMessagingClient() {
        TelcoMessagingClient telcoMessagingClient = createTelcoMessagingClientWithConnectionString();

        // BEGIN: readme-sample-getDeliveryReportWithResponseUsingTelcoMessagingClient
        DeliveryReportsClient deliveryReportsClient = telcoMessagingClient.getDeliveryReportsClient();

        String messageId = "message-id-from-send-operation";

        try {
            Response<DeliveryReport> response = deliveryReportsClient
                .getDeliveryReportWithResponse(messageId, Context.NONE);

            System.out.println("HTTP Status Code: " + response.getStatusCode());
            DeliveryReport deliveryReport = response.getValue();
            System.out.println("Delivery Status: " + deliveryReport.getDeliveryStatus());
        } catch (Exception e) {
            System.err.println("Failed to get delivery report: " + e.getMessage());
        }
        // END: readme-sample-getDeliveryReportWithResponseUsingTelcoMessagingClient
    }

    /**
     * Sample showing how to add recipients to opt-out list using the organized client.
     */
    public void addOptOutUsingTelcoMessagingClient() {
        TelcoMessagingClient telcoMessagingClient = createTelcoMessagingClientWithConnectionString();

        // BEGIN: readme-sample-addOptOutUsingTelcoMessagingClient
        // Get the opt-outs client from the organized telco messaging client
        OptOutsClient optOutsClient = telcoMessagingClient.getOptOutsClient();

        List<OptOutResult> results = optOutsClient.addOptOut(
            "+1234567890", // from
            "+0987654321"  // to
        );

        for (OptOutResult result : results) {
            System.out.println("Recipient: " + result.getTo());
            System.out.println("HTTP Status: " + result.getHttpStatusCode());
            if (result.getErrorMessage() != null) {
                System.out.println("Error: " + result.getErrorMessage());
            }
        }
        // END: readme-sample-addOptOutUsingTelcoMessagingClient
    }

    /**
     * Sample showing how to add multiple recipients to opt-out list.
     */
    public void addOptOutMultipleUsingTelcoMessagingClient() {
        TelcoMessagingClient telcoMessagingClient = createTelcoMessagingClientWithConnectionString();

        // BEGIN: readme-sample-addOptOutMultipleUsingTelcoMessagingClient
        OptOutsClient optOutsClient = telcoMessagingClient.getOptOutsClient();

        List<String> recipients = Arrays.asList("+0987654321", "+1122334455", "+5566778899");

        List<OptOutResult> results = optOutsClient.addOptOut(
            "+1234567890", // from
            recipients      // to
        );

        System.out.println("Processed " + results.size() + " opt-out requests:");
        for (OptOutResult result : results) {
            System.out.printf("  %s: %s%n", result.getTo(),
                result.getHttpStatusCode() == 200 ? "SUCCESS" : "FAILED");
        }
        // END: readme-sample-addOptOutMultipleUsingTelcoMessagingClient
    }

    /**
     * Sample showing how to check opt-out status using the organized client.
     */
    public void checkOptOutUsingTelcoMessagingClient() {
        TelcoMessagingClient telcoMessagingClient = createTelcoMessagingClientWithConnectionString();

        // BEGIN: readme-sample-checkOptOutUsingTelcoMessagingClient
        OptOutsClient optOutsClient = telcoMessagingClient.getOptOutsClient();

        List<OptOutCheckResult> results = optOutsClient.checkOptOut(
            "+1234567890", // from
            "+0987654321"  // to
        );

        for (OptOutCheckResult result : results) {
            System.out.println("Recipient: " + result.getTo());
            System.out.println("HTTP Status: " + result.getHttpStatusCode());
            if (result.isOptedOut() != null) {
                System.out.println("Is Opted Out: " + result.isOptedOut());
            }
            if (result.getErrorMessage() != null) {
                System.out.println("Error: " + result.getErrorMessage());
            }
        }
        // END: readme-sample-checkOptOutUsingTelcoMessagingClient
    }

    /**
     * Sample showing how to remove recipients from opt-out list using the organized client.
     */
    public void removeOptOutUsingTelcoMessagingClient() {
        TelcoMessagingClient telcoMessagingClient = createTelcoMessagingClientWithConnectionString();

        // BEGIN: readme-sample-removeOptOutUsingTelcoMessagingClient
        OptOutsClient optOutsClient = telcoMessagingClient.getOptOutsClient();

        List<OptOutResult> results = optOutsClient.removeOptOut(
            "+1234567890", // from
            "+0987654321"  // to
        );

        for (OptOutResult result : results) {
            System.out.println("Recipient: " + result.getTo());
            System.out.println("HTTP Status: " + result.getHttpStatusCode());
            if (result.getErrorMessage() != null) {
                System.out.println("Error: " + result.getErrorMessage());
            }
        }
        // END: readme-sample-removeOptOutUsingTelcoMessagingClient
    }

    /**
     * Sample showing a complete opt-out management workflow.
     */
    public void completeOptOutWorkflow() {
        TelcoMessagingClient telcoMessagingClient = createTelcoMessagingClientWithConnectionString();

        // BEGIN: readme-sample-completeOptOutWorkflow
        OptOutsClient optOutsClient = telcoMessagingClient.getOptOutsClient();
        String senderNumber = "+1234567890";
        String recipientNumber = "+0987654321";

        // Step 1: Check current opt-out status
        System.out.println("Checking current opt-out status...");
        List<OptOutCheckResult> checkResults = optOutsClient.checkOptOut(senderNumber, recipientNumber);
        boolean isCurrentlyOptedOut = checkResults.get(0).isOptedOut() != null && checkResults.get(0).isOptedOut();
        System.out.println("Currently opted out: " + isCurrentlyOptedOut);

        // Step 2: Add to opt-out list
        System.out.println("Adding to opt-out list...");
        List<OptOutResult> addResults = optOutsClient.addOptOut(senderNumber, recipientNumber);
        System.out.println("Add operation result: " + (addResults.get(0).getHttpStatusCode() == 200 ? "SUCCESS" : "FAILED"));

        // Step 3: Verify opt-out status
        System.out.println("Verifying opt-out status...");
        checkResults = optOutsClient.checkOptOut(senderNumber, recipientNumber);
        boolean nowOptedOut = checkResults.get(0).isOptedOut() != null && checkResults.get(0).isOptedOut();
        System.out.println("Now opted out: " + nowOptedOut);

        // Step 4: Remove from opt-out list
        System.out.println("Removing from opt-out list...");
        List<OptOutResult> removeResults = optOutsClient.removeOptOut(senderNumber, recipientNumber);
        System.out.println("Remove operation result: " + (removeResults.get(0).getHttpStatusCode() == 200 ? "SUCCESS" : "FAILED"));

        // Step 5: Final verification
        System.out.println("Final verification...");
        checkResults = optOutsClient.checkOptOut(senderNumber, recipientNumber);
        boolean finallyOptedOut = checkResults.get(0).isOptedOut() != null && checkResults.get(0).isOptedOut();
        System.out.println("Finally opted out: " + finallyOptedOut);
        // END: readme-sample-completeOptOutWorkflow
    }

    /**
     * Sample showing async operations with the TelcoMessagingAsyncClient.
     */
    public void asyncOperationsExample() {
        TelcoMessagingAsyncClient telcoMessagingAsyncClient = createTelcoMessagingAsyncClient();

        // BEGIN: readme-sample-asyncOperationsExample
        // Send SMS asynchronously
        telcoMessagingAsyncClient.getSmsAsyncClient()
            .send("+1234567890", "+0987654321", "Hello async!")
            .subscribe(
                result -> System.out.println("SMS sent: " + result.getMessageId()),
                error -> System.err.println("Failed to send SMS: " + error.getMessage())
            );

        // Check opt-out status asynchronously
        telcoMessagingAsyncClient.getOptOutsAsyncClient()
            .checkOptOut("+1234567890", "+0987654321")
            .subscribe(
                results -> {
                    OptOutCheckResult result = results.get(0);
                    System.out.println("Async opt-out check - Recipient: " + result.getTo() +
                                     ", Opted out: " + result.isOptedOut());
                },
                error -> System.err.println("Failed to check opt-out status: " + error.getMessage())
            );

        // Get delivery report asynchronously
        String messageId = "your-message-id";
        telcoMessagingAsyncClient.getDeliveryReportsAsyncClient()
            .getDeliveryReport(messageId)
            .subscribe(
                deliveryReport -> System.out.println("Async delivery status: " + deliveryReport.getDeliveryStatus()),
                error -> System.err.println("Failed to get delivery report: " + error.getMessage())
            );
        // END: readme-sample-asyncOperationsExample
    }
}
