// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.TestMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TelcoMessagingClient wrapper functionality.
 * These tests verify that the TelcoMessagingClient properly wraps and provides access
 * to the three specialized clients: SmsClient, DeliveryReportsClient, and OptOutsClient.
 * 
 * Specific functionality tests for each client are in their respective test files:
 * - SmsClientTests / SmsAsyncClientTests
 * - DeliveryReportsClientTests / DeliveryReportsAsyncClientTests  
 * - OptOutsClientTests / OptOutsAsyncClientTests
 */
public class TelcoMessagingClientTests extends SmsTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingClientCreation(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        assertNotNull(client);
        assertNotNull(client.getSmsClient());
        assertNotNull(client.getDeliveryReportsClient());
        assertNotNull(client.getOptOutsClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingClientReturnsCorrectClientTypes(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        // Verify the wrapper returns the correct client types
        assertTrue(client.getSmsClient() instanceof SmsClient);
        assertTrue(client.getDeliveryReportsClient() instanceof DeliveryReportsClient);
        assertTrue(client.getOptOutsClient() instanceof OptOutsClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingClientReturnsConsistentInstances(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        // Verify that multiple calls return the same client instances (not recreated each time)
        SmsClient smsClient1 = client.getSmsClient();
        SmsClient smsClient2 = client.getSmsClient();
        assertSame(smsClient1, smsClient2);

        DeliveryReportsClient deliveryClient1 = client.getDeliveryReportsClient();
        DeliveryReportsClient deliveryClient2 = client.getDeliveryReportsClient();
        assertSame(deliveryClient1, deliveryClient2);

        OptOutsClient optOutsClient1 = client.getOptOutsClient();
        OptOutsClient optOutsClient2 = client.getOptOutsClient();
        assertSame(optOutsClient1, optOutsClient2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingClientBuilderWithConnectionString(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = new TelcoMessagingClientBuilder().connectionString(CONNECTION_STRING);

        if (httpClient != null) {
            builder
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        }

        TelcoMessagingClient client = builder.buildClient();

        assertNotNull(client);
        assertNotNull(client.getSmsClient());
        assertNotNull(client.getDeliveryReportsClient());
        assertNotNull(client.getOptOutsClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingClientBuilderWithEndpointAndKey(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder
            = new TelcoMessagingClientBuilder()
                .endpoint(
                    new com.azure.communication.common.implementation.CommunicationConnectionString(CONNECTION_STRING)
                        .getEndpoint())
                .credential(new com.azure.core.credential.AzureKeyCredential("test-key"));

        if (httpClient != null) {
            builder
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        }

        TelcoMessagingClient client = builder.buildClient();

        assertNotNull(client);
        assertNotNull(client.getSmsClient());
        assertNotNull(client.getDeliveryReportsClient());
        assertNotNull(client.getOptOutsClient());
    }

    /**
     * Test that verifies the wrapper integration works end-to-end.
     * This is a smoke test to ensure all clients work together through the wrapper.
     */
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingClientWrapperIntegrationSmokeTest(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        // Smoke test: verify we can get all clients and they don't throw exceptions on basic operations
        assertDoesNotThrow(() -> {
            SmsClient smsClient = client.getSmsClient();
            assertNotNull(smsClient);
        });

        assertDoesNotThrow(() -> {
            DeliveryReportsClient deliveryClient = client.getDeliveryReportsClient();
            assertNotNull(deliveryClient);
        });

        assertDoesNotThrow(() -> {
            OptOutsClient optOutsClient = client.getOptOutsClient();
            assertNotNull(optOutsClient);
        });
    }

    protected TelcoMessagingClientBuilder getTelcoMessagingClientUsingConnectionString(HttpClient httpClient) {
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
