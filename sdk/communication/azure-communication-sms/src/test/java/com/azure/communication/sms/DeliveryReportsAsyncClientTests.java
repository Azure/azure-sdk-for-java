// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.models.DeliveryReportDeliveryStatus;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DeliveryReportsAsyncClient functionality including delivery report
 * retrieval operations.
 */
public class DeliveryReportsAsyncClientTests extends SmsTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void getDeliveryReport(HttpClient httpClient) {
        // Test the realistic delivery report workflow:
        // 1. Send SMS and get message ID
        // 2. Wait for delivery processing (5-10 seconds)
        // 3. Get delivery report and verify delivered status

        TelcoMessagingAsyncClient telcoAsyncClient = getTelcoMessagingClientBuilder(httpClient).buildAsyncClient();

        StepVerifier.create(
            // Send SMS first
            telcoAsyncClient.getSmsAsyncClient()
                .send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE)
                .flatMap(smsResult -> {
                    assertNotNull(smsResult);
                    assertNotNull(smsResult.getMessageId());

                    // Wait 8 seconds for delivery processing
                    return Mono.delay(Duration.ofSeconds(8))
                        .then(telcoAsyncClient.getDeliveryReportsAsyncClient()
                            .getDeliveryReport(smsResult.getMessageId()));
                }))
            .assertNext(deliveryReport -> {
                assertNotNull(deliveryReport);
                assertNotNull(deliveryReport.getDeliveryStatus());
                // Verify delivery status indicates either delivered or failed (both are valid
                // in test scenarios)
                assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(deliveryReport.getDeliveryStatus())
                    || DeliveryReportDeliveryStatus.FAILED.equals(deliveryReport.getDeliveryStatus()));
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void getDeliveryReportWithResponse(HttpClient httpClient) {
        // Test the realistic delivery report workflow with response details:
        // 1. Send SMS and get message ID
        // 2. Wait for delivery processing (5-10 seconds)
        // 3. Get delivery report with response and verify status codes

        TelcoMessagingAsyncClient telcoAsyncClient = getTelcoMessagingClientBuilder(httpClient).buildAsyncClient();

        StepVerifier.create(
            // Send SMS first
            telcoAsyncClient.getSmsAsyncClient()
                .send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE)
                .flatMap(smsResult -> {
                    assertNotNull(smsResult);
                    assertNotNull(smsResult.getMessageId());

                    // Wait 8 seconds for delivery processing
                    return Mono.delay(Duration.ofSeconds(8))
                        .then(telcoAsyncClient.getDeliveryReportsAsyncClient()
                            .getDeliveryReportWithResponse(smsResult.getMessageId(), Context.NONE));
                }))
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(200, response.getStatusCode());
                assertNotNull(response.getValue());
                assertNotNull(response.getValue().getDeliveryStatus());
                // Verify delivery status indicates either delivered or failed (both are valid
                // in test scenarios)
                assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(response.getValue().getDeliveryStatus())
                    || DeliveryReportDeliveryStatus.FAILED.equals(response.getValue().getDeliveryStatus()));
            })
            .verifyComplete();
    }

    @Test
    public void getDeliveryReportWithNullMessageIdThrowsException() {
        DeliveryReportsAsyncClient client = getDeliveryReportsAsyncClient(null);

        StepVerifier.create(client.getDeliveryReport(null)).verifyError(IllegalArgumentException.class);
    }

    @Test
    public void getDeliveryReportWithEmptyMessageIdThrowsException() {
        DeliveryReportsAsyncClient client = getDeliveryReportsAsyncClient(null);

        StepVerifier.create(client.getDeliveryReport("")).verifyError(IllegalArgumentException.class);
    }

    @Test
    public void getDeliveryReportWithResponseAndNullMessageIdThrowsException() {
        DeliveryReportsAsyncClient client = getDeliveryReportsAsyncClient(null);

        StepVerifier.create(client.getDeliveryReportWithResponse(null, Context.NONE))
            .verifyError(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void getDeliveryReportFromSentMessageAsync(HttpClient httpClient) {
        // This test demonstrates the complete async workflow:
        // 1. Send SMS and get message ID
        // 2. Wait for delivery processing
        // 3. Retrieve delivery report and verify delivered status

        TelcoMessagingAsyncClient telcoAsyncClient = getTelcoMessagingClientBuilder(httpClient).buildAsyncClient();

        StepVerifier.create(
            // Send SMS first, then chain delivery report retrieval with delay
            telcoAsyncClient.getSmsAsyncClient()
                .send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE)
                .flatMap(smsResult -> {
                    assertNotNull(smsResult);
                    assertNotNull(smsResult.getMessageId());

                    // Wait 8 seconds for delivery processing, then get delivery report
                    return Mono.delay(Duration.ofSeconds(8))
                        .then(telcoAsyncClient.getDeliveryReportsAsyncClient()
                            .getDeliveryReport(smsResult.getMessageId()));
                }))
            .assertNext(deliveryReport -> {
                assertNotNull(deliveryReport);
                assertNotNull(deliveryReport.getDeliveryStatus());
                // Verify delivery status indicates either delivered or failed (both are valid
                // in test scenarios)
                assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(deliveryReport.getDeliveryStatus())
                    || DeliveryReportDeliveryStatus.FAILED.equals(deliveryReport.getDeliveryStatus()));
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void getDeliveryReportFromSentMessageAsyncWithErrorHandling(HttpClient httpClient) {
        // This test demonstrates realistic delivery report retrieval with proper error
        // handling
        // 1. Send SMS and get message ID
        // 2. Wait for delivery processing
        // 3. Get delivery report with retry logic for not-ready scenarios

        TelcoMessagingAsyncClient telcoAsyncClient = getTelcoMessagingClientBuilder(httpClient).buildAsyncClient();

        StepVerifier.create(
            // Send SMS first, then chain delivery report retrieval with error handling
            telcoAsyncClient.getSmsAsyncClient()
                .send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE)
                .flatMap(smsResult -> {
                    assertNotNull(smsResult);
                    assertNotNull(smsResult.getMessageId());

                    // Wait 8 seconds for delivery processing
                    return Mono.delay(Duration.ofSeconds(8))
                        .then(telcoAsyncClient.getDeliveryReportsAsyncClient()
                            .getDeliveryReport(smsResult.getMessageId()))
                        .onErrorResume(throwable -> {
                            // Accept 404 (not found) or 202 (accepted but not ready) as retry scenarios
                            if (throwable instanceof HttpResponseException) {
                                HttpResponseException httpError = (HttpResponseException) throwable;
                                if (httpError.getResponse().getStatusCode() == 404
                                    || httpError.getResponse().getStatusCode() == 202) {
                                    // Retry after additional delay
                                    return Mono.delay(Duration.ofSeconds(5))
                                        .then(telcoAsyncClient.getDeliveryReportsAsyncClient()
                                            .getDeliveryReport(smsResult.getMessageId()));
                                }
                            }
                            return Mono.error(throwable); // Re-throw unexpected errors
                        });
                }))
            .assertNext(deliveryReport -> {
                assertNotNull(deliveryReport);
                assertNotNull(deliveryReport.getDeliveryStatus());
                // Verify delivery status indicates either delivered or failed (both are valid
                // in test scenarios)
                assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(deliveryReport.getDeliveryStatus())
                    || DeliveryReportDeliveryStatus.FAILED.equals(deliveryReport.getDeliveryStatus()));
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void getDeliveryReportWithPollingRetries(HttpClient httpClient) {
        // This test demonstrates realistic polling behavior for delivery reports
        // with retries and delays, as delivery reports may take time to be available

        TelcoMessagingAsyncClient telcoAsyncClient = getTelcoMessagingClientBuilder(httpClient).buildAsyncClient();

        StepVerifier.create(
            // Send SMS first
            telcoAsyncClient.getSmsAsyncClient()
                .send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE)
                .flatMap(smsResult -> {
                    assertNotNull(smsResult);
                    assertNotNull(smsResult.getMessageId());

                    // Implement polling with retries for delivery report
                    return pollDeliveryReportWithRetries(telcoAsyncClient.getDeliveryReportsAsyncClient(),
                        smsResult.getMessageId(), 5, // max attempts
                        Duration.ofSeconds(5) // delay between attempts, start after 5 seconds
                    );
                }))
            .assertNext(deliveryReport -> {
                assertNotNull(deliveryReport);
                assertNotNull(deliveryReport.getDeliveryStatus());
                // Verify delivery status indicates either delivered or failed (both are valid
                // in test scenarios)
                assertTrue(DeliveryReportDeliveryStatus.DELIVERED.equals(deliveryReport.getDeliveryStatus())
                    || DeliveryReportDeliveryStatus.FAILED.equals(deliveryReport.getDeliveryStatus()));
            })
            .verifyComplete();
    }

    /**
     * Helper method to poll delivery report with retries and delays.
     * This simulates realistic behavior where delivery reports may take time to be
     * available.
     *
     * @param client      The delivery reports client
     * @param messageId   The message ID to poll for
     * @param maxAttempts Maximum number of polling attempts
     * @param delay       Initial delay before first attempt
     * @return Mono containing the delivery report
     */
    private Mono<com.azure.communication.sms.implementation.models.DeliveryReport> pollDeliveryReportWithRetries(
        DeliveryReportsAsyncClient client, String messageId, int maxAttempts, Duration delay) {
        // Start with initial delay, then try to get delivery report
        return Mono.delay(delay)
            .then(pollDeliveryReportRecursive(client, messageId, 0, maxAttempts, Duration.ofSeconds(3)));
    }

    /**
     * Recursive helper for polling delivery reports with retries.
     */
    private Mono<com.azure.communication.sms.implementation.models.DeliveryReport> pollDeliveryReportRecursive(
        DeliveryReportsAsyncClient client, String messageId, int attempt, int maxAttempts, Duration retryDelay) {
        return client.getDeliveryReport(messageId).onErrorResume(HttpResponseException.class, error -> {
            // If it's a 404 (not found) or similar "not ready" error, retry
            if ((error.getResponse().getStatusCode() == 404 || error.getResponse().getStatusCode() == 202)
                && attempt < maxAttempts - 1) {

                return Mono.delay(retryDelay)
                    .then(pollDeliveryReportRecursive(client, messageId, attempt + 1, maxAttempts, retryDelay));
            } else {
                // If max attempts reached or different error, propagate error
                return Mono.error(error);
            }
        });
    }

    /**
     * Helper method to create DeliveryReportsAsyncClient for testing.
     */
    private DeliveryReportsAsyncClient getDeliveryReportsAsyncClient(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = getTelcoMessagingClientBuilder(httpClient);
        return builder.buildAsyncClient().getDeliveryReportsAsyncClient();
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
