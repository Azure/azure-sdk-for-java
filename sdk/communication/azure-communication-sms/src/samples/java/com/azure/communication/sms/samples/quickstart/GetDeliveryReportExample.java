// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import com.azure.communication.sms.SmsAsyncClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.core.exception.HttpResponseException;

import java.time.Duration;

/**
 * Complete example demonstrating how to retrieve delivery reports for sent SMS
 * messages.
 *
 * This sample demonstrates:
 * 1. Basic delivery report retrieval (recommended approach)
 * 2. Advanced usage with HTTP response details
 * 3. Proper error handling for common scenarios
 *
 * Choose the approach that best fits your needs - most developers will want
 * Example 1.
 */
public class GetDeliveryReportExample {
    public static void main(String[] args) {
        // Get connection string and phone number from environment variables
        String connectionString = System.getenv("COMMUNICATION_SAMPLES_CONNECTION_STRING");
        String phoneNumber = System.getenv("AZURE_PHONE_NUMBER");

        if (connectionString == null || phoneNumber == null) {
            System.err.println(
                    "Please set COMMUNICATION_SAMPLES_CONNECTION_STRING and AZURE_PHONE_NUMBER environment variables.");
            return;
        }

        // Create SMS async client
        SmsAsyncClient smsAsyncClient = new SmsClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();

        // Example 1: Simple synchronous-style approach (easiest to understand)
        simpleDeliveryReportExample(smsAsyncClient, phoneNumber);

        // Example 2: Advanced async approach with full response details
        advancedDeliveryReportExample(smsAsyncClient, phoneNumber);

        // Example 3: Error handling demonstration
        errorHandlingExample(smsAsyncClient);

        // Wait a bit for async operations to complete
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Example 1: Simple approach using .block() for synchronous-style code
     * (recommended for beginners)
     */
    private static void simpleDeliveryReportExample(SmsAsyncClient smsAsyncClient, String phoneNumber) {
        System.out.println("=== Example 1: Simple Delivery Report Retrieval (Synchronous Style) ===");

        try {
            // Configure SMS options with delivery report enabled
            SmsSendOptions options = new SmsSendOptions();
            options.setDeliveryReportEnabled(true);
            options.setTag("SimpleExample");

            // Send SMS message (blocking call)
            var sendResult = smsAsyncClient.send(
                    phoneNumber,
                    phoneNumber,
                    "Hello! This message has delivery reporting enabled.",
                    options)
                    .block(); // Block to get result synchronously

            if (sendResult != null && sendResult.isSuccessful()) {
                System.out.println("✅ SMS sent successfully! Message ID: " + sendResult.getMessageId());

                // Wait for delivery processing
                System.out.println("⏳ Waiting 10 seconds for delivery processing...");
                Thread.sleep(10000);

                // Retrieve delivery report (blocking call)
                DeliveryReport deliveryReport = smsAsyncClient
                        .getDeliveryReport(sendResult.getMessageId())
                        .block(); // Block to get result synchronously

                if (deliveryReport != null) {
                    System.out.println("✅ Delivery report retrieved:");
                    System.out.println("  Status: " + deliveryReport.getDeliveryStatus());
                    System.out.println("  From: " + deliveryReport.getFrom());
                    System.out.println("  To: " + deliveryReport.getTo());
                    System.out.println("  Tag: " + deliveryReport.getTag());
                    if (deliveryReport.getReceivedTimestamp() != null) {
                        System.out.println("  Received: " + deliveryReport.getReceivedTimestamp());
                    }
                } else {
                    System.err.println("❌ Delivery report not found");
                }
            } else {
                System.err.println("❌ Failed to send SMS");
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Example 2: Advanced async approach with full reactive programming
     */
    private static void advancedDeliveryReportExample(SmsAsyncClient smsAsyncClient, String phoneNumber) {
        System.out.println("\n=== Example 2: Advanced Async Delivery Report with Response Details ===");

        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("AdvancedExample");

        // Send SMS and retrieve delivery report
        smsAsyncClient.send(
                phoneNumber,
                phoneNumber,
                "Test message for delivery report - Basic Example",
                options)
                .delayElement(Duration.ofSeconds(10)) // Wait for message to be processed
                .flatMap(sendResult -> {
                    System.out.println("Message sent successfully. Message ID: " + sendResult.getMessageId());

                    // Retrieve delivery report using message ID
                    return smsAsyncClient.getDeliveryReport(sendResult.getMessageId());
                })
                .subscribe(
                        deliveryReport -> {
                            System.out.println("✅ Delivery Report Retrieved:");
                            System.out.println("  Status: " + deliveryReport.getDeliveryStatus());
                            System.out.println("  Message ID: " + deliveryReport.getMessageId());
                            System.out.println("  From: " + deliveryReport.getFrom());
                            System.out.println("  To: " + deliveryReport.getTo());
                            System.out.println("  Tag: " + deliveryReport.getTag());
                            System.out.println("  Received Timestamp: " + deliveryReport.getReceivedTimestamp());

                            if (deliveryReport.getDeliveryStatusDetails() != null) {
                                System.out.println("  Status Details: " + deliveryReport.getDeliveryStatusDetails());
                            }

                            if (deliveryReport.getDeliveryAttempts() != null
                                    && !deliveryReport.getDeliveryAttempts().isEmpty()) {
                                System.out
                                        .println("  Delivery Attempts: " + deliveryReport.getDeliveryAttempts().size());
                            }
                        },
                        error -> {
                            System.err.println("❌ Error in basic example: " + error.getMessage());
                            if (error instanceof HttpResponseException) {
                                HttpResponseException httpError = (HttpResponseException) error;
                                System.err.println("  HTTP Status: " + httpError.getResponse().getStatusCode());
                            }
                        });
    }

    /**
     * Example 3: Demonstrate error handling for different scenarios
     */
    private static void errorHandlingExample(SmsAsyncClient smsAsyncClient) {
        System.out.println("\n=== Example 3: Error Handling ===");

        // Test with invalid message ID to demonstrate error handling
        String invalidMessageId = "invalid-message-id-12345";

        smsAsyncClient.getDeliveryReport(invalidMessageId)
                .subscribe(
                        deliveryReport -> {
                            // This shouldn't happen with invalid ID
                            System.out.println("Unexpected success for invalid message ID");
                        },
                        error -> {
                            System.out.println("✅ Expected error for invalid message ID:");
                            System.out.println("  Error Type: " + error.getClass().getSimpleName());
                            System.out.println("  Error Message: " + error.getMessage());

                            if (error instanceof HttpResponseException) {
                                HttpResponseException httpError = (HttpResponseException) error;
                                int statusCode = httpError.getResponse().getStatusCode();

                                switch (statusCode) {
                                    case 404:
                                        System.out.println(
                                                "  ℹ️  Message not found - may not exist or delivery report not enabled");
                                        break;
                                    case 401:
                                        System.out.println("  ℹ️  Authentication error - check credentials");
                                        break;
                                    case 403:
                                        System.out.println("  ℹ️  Forbidden - check permissions");
                                        break;
                                    case 429:
                                        System.out.println("  ℹ️  Rate limited - retry after delay");
                                        break;
                                    default:
                                        System.out.println("  ℹ️  HTTP " + statusCode + " error");
                                }
                            } else if (error instanceof IllegalArgumentException) {
                                System.out.println("  ℹ️  Invalid argument provided");
                            }
                        });

        // Test with null message ID
        try {
            smsAsyncClient.getDeliveryReport(null).subscribe();
        } catch (Exception e) {
            System.out.println("✅ Null message ID handled correctly: " + e.getMessage());
        }

        // Test with empty message ID
        smsAsyncClient.getDeliveryReport("")
                .subscribe(
                        deliveryReport -> System.out.println("Unexpected success for empty message ID"),
                        error -> System.out.println("✅ Empty message ID handled correctly: " + error.getMessage()));
    }
}
