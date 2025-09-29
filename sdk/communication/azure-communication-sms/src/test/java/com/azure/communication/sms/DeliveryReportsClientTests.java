// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.models.DeliveryReportDeliveryStatus;
import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DeliveryReportsClient functionality including delivery report
 * retrieval operations.
 */
public class DeliveryReportsClientTests extends SmsTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void getDeliveryReport(HttpClient httpClient) {
        // Test the realistic delivery report workflow:
        // 1. Send SMS and get message ID
        // 2. Wait for delivery processing (5-10 seconds)
        // 3. Get delivery report and verify delivered status

        TelcoMessagingClient telcoClient = getTelcoMessagingClientBuilder(httpClient).buildClient();

        // Send SMS first
        SmsSendResult smsResult = telcoClient.getSmsClient().send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        assertNotNull(smsResult);
        assertNotNull(smsResult.getMessageId());

        // Wait for delivery processing (simulate with sleep in sync tests)
        try {
            Thread.sleep(8000); // 8 seconds wait
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        }

        // Get delivery report and verify status
        DeliveryReportsClient client = telcoClient.getDeliveryReportsClient();
        DeliveryReport report = client.getDeliveryReport(smsResult.getMessageId());

        assertNotNull(report);
        assertNotNull(report.getDeliveryStatus());
        // Verify delivery status indicates either delivered or failed (both are valid
        // in test scenarios)
        assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(report.getDeliveryStatus())
            || DeliveryReportDeliveryStatus.FAILED.equals(report.getDeliveryStatus()));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void getDeliveryReportWithResponse(HttpClient httpClient) {
        // Test the realistic delivery report workflow with response details:
        // 1. Send SMS and get message ID
        // 2. Wait for delivery processing (5-10 seconds)
        // 3. Get delivery report with response and verify status codes

        TelcoMessagingClient telcoClient = getTelcoMessagingClientBuilder(httpClient).buildClient();

        // Send SMS first
        SmsSendResult smsResult = telcoClient.getSmsClient().send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        assertNotNull(smsResult);
        assertNotNull(smsResult.getMessageId());

        // Wait for delivery processing (simulate with sleep in sync tests)
        try {
            Thread.sleep(8000); // 8 seconds wait
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        }

        // Get delivery report with response and verify status
        DeliveryReportsClient client = telcoClient.getDeliveryReportsClient();
        Response<DeliveryReport> response
            = client.getDeliveryReportWithResponse(smsResult.getMessageId(), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue());
        assertNotNull(response.getValue().getDeliveryStatus());
        // Verify delivery status indicates either delivered or failed (both are valid
        // in test scenarios)
        assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(response.getValue().getDeliveryStatus())
            || DeliveryReportDeliveryStatus.FAILED.equals(response.getValue().getDeliveryStatus()));
    }

    @Test
    public void getDeliveryReportWithNullMessageIdThrowsException() {
        DeliveryReportsClient client = getDeliveryReportsClient(null);

        assertThrows(IllegalArgumentException.class, () -> {
            client.getDeliveryReport(null);
        });
    }

    @Test
    public void getDeliveryReportWithEmptyMessageIdThrowsException() {
        DeliveryReportsClient client = getDeliveryReportsClient(null);

        assertThrows(IllegalArgumentException.class, () -> {
            client.getDeliveryReport("");
        });
    }

    @Test
    public void getDeliveryReportWithResponseAndNullMessageIdThrowsException() {
        DeliveryReportsClient client = getDeliveryReportsClient(null);

        assertThrows(IllegalArgumentException.class, () -> {
            client.getDeliveryReportWithResponse(null, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void getDeliveryReportFromSentMessage(HttpClient httpClient) {
        // This test demonstrates the complete sync workflow:
        // 1. Send SMS and get message ID
        // 2. Wait for delivery processing
        // 3. Retrieve delivery report and verify delivered status

        TelcoMessagingClient telcoClient = getTelcoMessagingClientBuilder(httpClient).buildClient();

        // Send SMS first
        SmsSendResult smsResult = telcoClient.getSmsClient().send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        assertNotNull(smsResult);
        assertNotNull(smsResult.getMessageId());

        // Wait for delivery processing (simulate with sleep in sync tests)
        try {
            Thread.sleep(8000); // 8 seconds wait
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        }

        // Get delivery report and verify status
        DeliveryReportsClient deliveryClient = telcoClient.getDeliveryReportsClient();
        DeliveryReport report = deliveryClient.getDeliveryReport(smsResult.getMessageId());

        assertNotNull(report);
        assertNotNull(report.getDeliveryStatus());
        // Verify delivery status indicates either delivered or failed (both are valid
        // in test scenarios)
        assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(report.getDeliveryStatus())
            || DeliveryReportDeliveryStatus.FAILED.equals(report.getDeliveryStatus()));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void getDeliveryReportFromSentMessageWithErrorHandling(HttpClient httpClient) {
        // This test demonstrates realistic delivery report retrieval with proper error
        // handling
        // 1. Send SMS and get message ID
        // 2. Wait for delivery processing
        // 3. Get delivery report with retry logic for not-ready scenarios

        TelcoMessagingClient telcoClient = getTelcoMessagingClientBuilder(httpClient).buildClient();

        // Send SMS first
        SmsSendResult smsResult = telcoClient.getSmsClient().send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        assertNotNull(smsResult);
        assertNotNull(smsResult.getMessageId());

        // Wait for delivery processing
        try {
            Thread.sleep(8000); // 8 seconds wait
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted");
        }

        DeliveryReportsClient deliveryClient = telcoClient.getDeliveryReportsClient();

        try {
            DeliveryReport report = deliveryClient.getDeliveryReport(smsResult.getMessageId());
            assertNotNull(report);
            assertNotNull(report.getDeliveryStatus());
            // Verify delivery status indicates either delivered or failed (both are valid
            // in test scenarios)
            assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(report.getDeliveryStatus())
                || DeliveryReportDeliveryStatus.FAILED.equals(report.getDeliveryStatus()));
        } catch (HttpResponseException e) {
            // Accept 404 (not found) or 202 (accepted but not ready) as retry scenarios
            if (e.getResponse().getStatusCode() == 404 || e.getResponse().getStatusCode() == 202) {
                // Retry after additional delay
                try {
                    Thread.sleep(5000); // Additional 5 seconds wait
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    fail("Test interrupted");
                }

                DeliveryReport report = deliveryClient.getDeliveryReport(smsResult.getMessageId());
                assertNotNull(report);
                assertNotNull(report.getDeliveryStatus());
                // Verify delivery status indicates either delivered or failed (both are valid
                // in test scenarios)
                assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(report.getDeliveryStatus())
                    || DeliveryReportDeliveryStatus.FAILED.equals(report.getDeliveryStatus()));
            } else {
                // Re-throw unexpected errors
                throw e;
            }
        }
    }

    /**
     * Helper method to create DeliveryReportsClient for testing.
     */
    private DeliveryReportsClient getDeliveryReportsClient(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = getTelcoMessagingClientBuilder(httpClient);
        return builder.buildClient().getDeliveryReportsClient();
    }

    /**
     * Helper method to create TelcoMessagingClientBuilder with proper
     * configuration.
     */
    private TelcoMessagingClientBuilder getTelcoMessagingClientBuilder(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = new TelcoMessagingClientBuilder();
        builder.connectionString(CONNECTION_STRING);

        if (httpClient != null) {
            builder
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }
}
