// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import java.util.Arrays;
import com.azure.communication.sms.SmsAsyncClient;
import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.SmsServiceVersion;
import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ReadmeSamples {
    public SmsClient createSmsClientUsingAzureKeyCredential() {
        // BEGIN: readme-sample-createSmsClientUsingAzureKeyCredential
        // You can find your endpoint and access key from your resource in the Azure
        // Portal
        String endpoint = "https://<resource-name>.communication.azure.com";
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

        SmsClient smsClient = new SmsClientBuilder()
                .endpoint(endpoint)
                .credential(azureKeyCredential)
                .buildClient();
        // END: readme-sample-createSmsClientUsingAzureKeyCredential

        return smsClient;
    }

    public SmsAsyncClient createSmsAsyncClientUsingAzureKeyCredential() {
        // You can find your endpoint and access key from your resource in the Azure
        // Portal
        String endpoint = "https://<resource-name>.communication.azure.com";
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

        SmsAsyncClient smsClient = new SmsClientBuilder()
                .endpoint(endpoint)
                .credential(azureKeyCredential)
                .buildAsyncClient();

        return smsClient;
    }

    public SmsClient createSmsClientWithConnectionString() {
        // BEGIN: readme-sample-createSmsClientWithConnectionString
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        SmsClient smsClient = new SmsClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        // END: readme-sample-createSmsClientWithConnectionString

        return smsClient;
    }

    public SmsClient createSmsClientWithAAD() {
        // BEGIN: readme-sample-createSmsClientWithAAD
        // You can find your endpoint and access key from your resource in the Azure
        // Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        SmsClient smsClient = new SmsClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
        // END: readme-sample-createSmsClientWithAAD

        return smsClient;
    }

    public SmsClient createSyncClientUsingTokenCredential() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // You can find your endpoint and access key from your resource in the Azure
        // Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        SmsClient smsClient = new SmsClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .buildClient();
        return smsClient;
    }

    public void sendMessageToOneRecipient() {
        SmsClient smsClient = createSmsClientUsingAzureKeyCredential();

        // BEGIN: readme-sample-sendMessageToOneRecipient
        SmsSendResult sendResult = smsClient.send(
                "<from-phone-number>",
                "<to-phone-number>",
                "Weekly Promotion");

        System.out.println("Message Id: " + sendResult.getMessageId());
        System.out.println("Recipient Number: " + sendResult.getTo());
        System.out.println("Send Result Successful:" + sendResult.isSuccessful());
        // END: readme-sample-sendMessageToOneRecipient
    }

    public void sendMessageToGroupWithOptions() {
        SmsClient smsClient = createSmsClientUsingAzureKeyCredential();

        // BEGIN: readme-sample-sendMessageToGroupWithOptions
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("Marketing");

        Iterable<SmsSendResult> sendResults = smsClient.sendWithResponse(
                "<from-phone-number>",
                Arrays.asList("<to-phone-number1>", "<to-phone-number2>"),
                "Weekly Promotion",
                options /* Optional */,
                Context.NONE).getValue();

        for (SmsSendResult result : sendResults) {
            System.out.println("Message Id: " + result.getMessageId());
            System.out.println("Recipient Number: " + result.getTo());
            System.out.println("Send Result Successful:" + result.isSuccessful());
        }
        // END: readme-sample-sendMessageToGroupWithOptions
    }

    /**
     * Sample code for troubleshooting
     */
    public void catchHttpErrorOnRequest() {
        SmsClient smsClient = createSmsClientUsingAzureKeyCredential();
        try {
            SmsSendResult sendResult = smsClient.send(
                    "<from-phone-number>",
                    "<to-phone-number>",
                    "Weekly Promotion");
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void sendMessageTroubleShooting() {
        SmsClient smsClient = createSmsClientUsingAzureKeyCredential();

        // BEGIN: readme-sample-sendMessageTroubleShooting
        try {
            SmsSendOptions options = new SmsSendOptions();
            options.setDeliveryReportEnabled(true);
            options.setTag("Marketing");

            Response<Iterable<SmsSendResult>> sendResults = smsClient.sendWithResponse(
                    "<from-phone-number>",
                    Arrays.asList("<to-phone-number1>", "<to-phone-number2>"),
                    "Weekly Promotion",
                    options /* Optional */,
                    Context.NONE);

            Iterable<SmsSendResult> smsSendResults = sendResults.getValue();
            for (SmsSendResult result : smsSendResults) {
                if (result.isSuccessful()) {
                    System.out.println(
                            "Successfully sent this message: " + result.getMessageId() + " to " + result.getTo());
                } else {
                    System.out.println("Something went wrong when trying to send this message " + result.getMessageId()
                            + " to " + result.getTo());
                    System.out.println("Status code " + result.getHttpStatusCode() + " and error message "
                            + result.getErrorMessage());
                }
            }
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
        // END: readme-sample-sendMessageTroubleShooting
    }

    public SmsClient createSmsClientWithApiVersion() {
        // BEGIN: readme-sample-createSmsClientWithApiVersion
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        SmsClient smsClient = new SmsClientBuilder()
                .connectionString(connectionString)
                .serviceVersion(SmsServiceVersion.V2026_01_23) // Specify API version
                .buildClient();
        // END: readme-sample-createSmsClientWithApiVersion

        return smsClient;
    }

    public void getDeliveryReportAsync() {
        SmsAsyncClient smsAsyncClient = createSmsAsyncClientUsingAzureKeyCredential();

        // BEGIN: readme-sample-getDeliveryReportAsync
        // Send an SMS with delivery report enabled
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);

        smsAsyncClient.send(
                "<from-phone-number>",
                "<to-phone-number>",
                "Your order has been shipped!",
                options)
                .flatMap(sendResult -> {
                    System.out.println("Message sent. Message ID: " + sendResult.getMessageId());

                    // Wait a moment for delivery (in real scenarios, this would be based on your
                    // application flow)
                    try {
                        Thread.sleep(10000); // Wait 10 seconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return smsAsyncClient.getDeliveryReport(sendResult.getMessageId());
                    }

                    // Get delivery report using message ID
                    return smsAsyncClient.getDeliveryReport(sendResult.getMessageId());
                })
                .subscribe(
                        deliveryReport -> {
                            System.out.println("Delivery Report Status: " + deliveryReport.getDeliveryStatus());
                            System.out.println("Delivery Status Details: " + deliveryReport.getDeliveryStatusDetails());
                            System.out.println("Received Timestamp: " + deliveryReport.getReceivedTimestamp());
                            System.out.println("Message ID: " + deliveryReport.getMessageId());
                            System.out.println("From: " + deliveryReport.getFrom());
                            System.out.println("To: " + deliveryReport.getTo());
                            System.out.println("Tag: " + deliveryReport.getTag());
                        },
                        error -> {
                            System.err.println("Error getting delivery report: " + error.getMessage());
                        });
        // END: readme-sample-getDeliveryReportAsync
    }

    public void getDeliveryReportAsyncWithResponse() {
        SmsAsyncClient smsAsyncClient = createSmsAsyncClientUsingAzureKeyCredential();

        // BEGIN: readme-sample-getDeliveryReportAsyncWithResponse
        // Send an SMS with delivery report enabled
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);

        smsAsyncClient.send(
                "<from-phone-number>",
                "<to-phone-number>",
                "Your order has been shipped!",
                options)
                .flatMap(sendResult -> {
                    System.out.println("Message sent. Message ID: " + sendResult.getMessageId());

                    // Wait a moment for delivery
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return smsAsyncClient.getDeliveryReportWithResponse(sendResult.getMessageId(), Context.NONE);
                    }

                    // Get delivery report with response details
                    return smsAsyncClient.getDeliveryReportWithResponse(sendResult.getMessageId(), Context.NONE);
                })
                .subscribe(
                        response -> {
                            System.out.println("HTTP Status Code: " + response.getStatusCode());
                            DeliveryReport deliveryReport = response.getValue();
                            System.out.println("Delivery Report Status: " + deliveryReport.getDeliveryStatus());
                            System.out.println("Delivery Status Details: " + deliveryReport.getDeliveryStatusDetails());
                            System.out.println("Received Timestamp: " + deliveryReport.getReceivedTimestamp());
                            System.out.println("Message ID: " + deliveryReport.getMessageId());
                            System.out.println("From: " + deliveryReport.getFrom());
                            System.out.println("To: " + deliveryReport.getTo());
                            System.out.println("Tag: " + deliveryReport.getTag());
                        },
                        error -> {
                            System.err.println("Error getting delivery report: " + error.getMessage());
                        });
        // END: readme-sample-getDeliveryReportAsyncWithResponse
    }

    public void handleDeliveryReportErrors() {
        SmsAsyncClient smsAsyncClient = createSmsAsyncClientUsingAzureKeyCredential();

        // BEGIN: readme-sample-handleDeliveryReportErrors
        String messageId = "<message-id>";

        smsAsyncClient.getDeliveryReport(messageId)
                .doOnError(throwable -> {
                    if (throwable instanceof RuntimeException) {
                        RuntimeException ex = (RuntimeException) throwable;
                        System.err.println("Error retrieving delivery report: " + ex.getMessage());

                        // Handle specific error cases
                        if (ex.getMessage().contains("404")) {
                            System.err.println(
                                    "Delivery report not found - message may not exist or delivery report not enabled");
                        } else if (ex.getMessage().contains("401")) {
                            System.err.println("Authentication error - check your credentials");
                        } else if (ex.getMessage().contains("403")) {
                            System.err.println("Forbidden - check your permissions");
                        }
                    }
                })
                .subscribe(
                        deliveryReport -> {
                            System.out.println("Delivery Report Retrieved Successfully");
                            System.out.println("Status: " + deliveryReport.getDeliveryStatus());
                        },
                        error -> {
                            // This will be called if doOnError doesn't handle the error
                            System.err.println("Unhandled error: " + error.getMessage());
                        });
        // END: readme-sample-handleDeliveryReportErrors
    }
}
