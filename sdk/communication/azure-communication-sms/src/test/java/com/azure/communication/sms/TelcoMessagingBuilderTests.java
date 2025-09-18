// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class TelcoMessagingBuilderTests extends SmsTestBase {

    @Test
    public void createClientWithConnectionString() {
        TelcoMessagingClient client = new TelcoMessagingClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildClient();

        assertNotNull(client);
        assertNotNull(client.getSmsClient());
        assertNotNull(client.getDeliveryReportsClient());
        assertNotNull(client.getOptOutsClient());
    }

    @Test
    public void createAsyncClientWithConnectionString() {
        TelcoMessagingAsyncClient client = new TelcoMessagingClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildAsyncClient();

        assertNotNull(client);
        assertNotNull(client.getSmsAsyncClient());
        assertNotNull(client.getDeliveryReportsAsyncClient());
        assertNotNull(client.getOptOutsAsyncClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createClientWithTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = getTestMode() == TestMode.PLAYBACK
            ? new MockTokenCredential()
            : null; // Use real credential in live tests

        if (tokenCredential != null) {
            TelcoMessagingClient client = new TelcoMessagingClientBuilder()
                .endpoint("https://contoso.communication.azure.com/")
                .credential(tokenCredential)
                .httpClient(httpClient)
                .buildClient();

            assertNotNull(client);
        }
    }

    @Test
    public void createClientWithAzureKeyCredential() {
        TelcoMessagingClient client = new TelcoMessagingClientBuilder()
            .endpoint("https://contoso.communication.azure.com/")
            .credential(new AzureKeyCredential("test-key"))
            .buildClient();

        assertNotNull(client);
    }

    @Test
    public void createClientWithoutCredentialThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TelcoMessagingClientBuilder()
                .endpoint("https://contoso.communication.azure.com/")
                .buildClient();
        });
    }

    @Test
    public void createClientWithoutEndpointThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            new TelcoMessagingClientBuilder()
                .credential(new AzureKeyCredential("test-key"))
                .buildClient();
        });
    }

    @Test
    public void createClientWithInvalidConnectionStringThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TelcoMessagingClientBuilder()
                .connectionString("invalid-connection-string")
                .buildClient();
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createClientWithCustomHttpClient(HttpClient httpClient) {
        TelcoMessagingClient client = new TelcoMessagingClientBuilder()
            .connectionString(CONNECTION_STRING)
            .httpClient(httpClient)
            .buildClient();

        assertNotNull(client);
    }

    @Test
    public void createClientWithServiceVersion() {
        TelcoMessagingClient client = new TelcoMessagingClientBuilder()
            .connectionString(CONNECTION_STRING)
            .serviceVersion(SmsServiceVersion.V2021_03_07)
            .buildClient();

        assertNotNull(client);
    }

    @Test
    public void createClientWithServiceVersion() {
        TelcoMessagingClient client = new TelcoMessagingClientBuilder()
            .connectionString(CONNECTION_STRING)
            .serviceVersion(SmsServiceVersion.V2021_03_07)
            .buildClient();

        assertNotNull(client);
    }

    @Test
    public void createClientWithLatestServiceVersion() {
        TelcoMessagingClient client = new TelcoMessagingClientBuilder()
            .connectionString(CONNECTION_STRING)
            .serviceVersion(SmsServiceVersion.getLatest())
            .buildClient();

        assertNotNull(client);
    }

    @Test
    public void createClientWithConfiguration() {
        Configuration configuration = Configuration.getGlobalConfiguration();

        TelcoMessagingClient client = new TelcoMessagingClientBuilder()
            .connectionString(CONNECTION_STRING)
            .configuration(configuration)
            .buildClient();

        assertNotNull(client);
    }
}
