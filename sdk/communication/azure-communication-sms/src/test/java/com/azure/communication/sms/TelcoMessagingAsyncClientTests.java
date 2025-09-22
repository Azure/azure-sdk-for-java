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
 * Tests for TelcoMessagingAsyncClient wrapper functionality.
 * These tests verify that the TelcoMessagingAsyncClient properly wraps and
 * provides access
 * to the three specialized async clients: SmsAsyncClient,
 * DeliveryReportsAsyncClient, and OptOutsAsyncClient.
 *
 * Specific functionality tests for each async client are in their respective
 * test files:
 * - SmsAsyncClientTests
 * - DeliveryReportsAsyncClientTests
 * - OptOutsAsyncClientTests
 */
public class TelcoMessagingAsyncClientTests extends SmsTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingAsyncClientCreation(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        assertNotNull(client);
        assertNotNull(client.getSmsAsyncClient());
        assertNotNull(client.getDeliveryReportsAsyncClient());
        assertNotNull(client.getOptOutsAsyncClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingAsyncClientReturnsCorrectClientTypes(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        // Verify the wrapper returns the correct async client types
        assertTrue(client.getSmsAsyncClient() instanceof SmsAsyncClient);
        assertTrue(client.getDeliveryReportsAsyncClient() instanceof DeliveryReportsAsyncClient);
        assertTrue(client.getOptOutsAsyncClient() instanceof OptOutsAsyncClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingAsyncClientReturnsConsistentInstances(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        // Verify that multiple calls return the same client instances (not recreated
        // each time)
        SmsAsyncClient smsAsyncClient1 = client.getSmsAsyncClient();
        SmsAsyncClient smsAsyncClient2 = client.getSmsAsyncClient();
        assertSame(smsAsyncClient1, smsAsyncClient2);

        DeliveryReportsAsyncClient deliveryAsyncClient1 = client.getDeliveryReportsAsyncClient();
        DeliveryReportsAsyncClient deliveryAsyncClient2 = client.getDeliveryReportsAsyncClient();
        assertSame(deliveryAsyncClient1, deliveryAsyncClient2);

        OptOutsAsyncClient optOutsAsyncClient1 = client.getOptOutsAsyncClient();
        OptOutsAsyncClient optOutsAsyncClient2 = client.getOptOutsAsyncClient();
        assertSame(optOutsAsyncClient1, optOutsAsyncClient2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingAsyncClientBuilderWithConnectionString(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = new TelcoMessagingClientBuilder().connectionString(CONNECTION_STRING);

        if (httpClient != null) {
            builder
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        }

        TelcoMessagingAsyncClient client = builder.buildAsyncClient();

        assertNotNull(client);
        assertNotNull(client.getSmsAsyncClient());
        assertNotNull(client.getDeliveryReportsAsyncClient());
        assertNotNull(client.getOptOutsAsyncClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingAsyncClientBuilderWithEndpointAndKey(HttpClient httpClient) {
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

        TelcoMessagingAsyncClient client = builder.buildAsyncClient();

        assertNotNull(client);
        assertNotNull(client.getSmsAsyncClient());
        assertNotNull(client.getDeliveryReportsAsyncClient());
        assertNotNull(client.getOptOutsAsyncClient());
    }

    /**
     * Test that verifies the async wrapper integration works end-to-end.
     * This is a smoke test to ensure all async clients work together through the
     * wrapper.
     */
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void telcoMessagingAsyncClientWrapperIntegrationSmokeTest(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        // Smoke test: verify we can get all async clients and they don't throw
        // exceptions on basic operations
        assertDoesNotThrow(() -> {
            SmsAsyncClient smsAsyncClient = client.getSmsAsyncClient();
            assertNotNull(smsAsyncClient);
        });

        assertDoesNotThrow(() -> {
            DeliveryReportsAsyncClient deliveryAsyncClient = client.getDeliveryReportsAsyncClient();
            assertNotNull(deliveryAsyncClient);
        });

        assertDoesNotThrow(() -> {
            OptOutsAsyncClient optOutsAsyncClient = client.getOptOutsAsyncClient();
            assertNotNull(optOutsAsyncClient);
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
