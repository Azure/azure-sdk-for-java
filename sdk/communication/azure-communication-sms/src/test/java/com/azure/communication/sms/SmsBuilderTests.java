// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SmsBuilderTests {
    static final String MOCK_URL = "https://REDACTED.communication.azure.com";
    static final String MOCK_APP_ID = "appId";
    static final String MOCK_ACCESS_KEY
        = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";
    static final String MOCK_CONNECTION_STRING
        = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";

    static class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.empty(); // NOP
        }
    }

    private final SmsClientBuilder builder = new SmsClientBuilder();

    @Test
    public void missingTokenCredentialTest() throws NullPointerException {
        builder.endpoint(MOCK_URL).configuration(Configuration.NONE).httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, builder::buildAsyncClient);
    }

    @Test
    public void missingUrlTest() throws NullPointerException {
        builder.credential(new AzureKeyCredential(MOCK_ACCESS_KEY)).httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, builder::buildAsyncClient);
    }

    @Test
    public void serviceVersionTest() {
        SmsClient client = builder.connectionString(MOCK_CONNECTION_STRING)
            .serviceVersion(SmsServiceVersion.V2021_03_07)
            .httpClient(new NoOpHttpClient())
            .buildClient();
        assertNotNull(client);
    }

    @Test
    public void serviceVersionLatestTest() {
        SmsClient client = builder.connectionString(MOCK_CONNECTION_STRING)
            .serviceVersion(SmsServiceVersion.getLatest())
            .httpClient(new NoOpHttpClient())
            .buildClient();
        assertNotNull(client);
    }

    @Test
    public void nullPipelineTest() {
        assertThrows(NullPointerException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient()).pipeline(null));
    }

    @Test
    public void nullCustomPolicyTest() {
        assertThrows(NullPointerException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient()).addPolicy(null));
    }

    @Test
    public void nullConfigurationTest() {
        assertThrows(NullPointerException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING)
                .httpClient(new NoOpHttpClient())
                .configuration(null));
    }

    @Test
    public void nullHttpLogOptionsTest() {
        assertThrows(NullPointerException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING)
                .httpClient(new NoOpHttpClient())
                .httpLogOptions(null));
    }

    @Test
    public void buildPipelineWithNullClientOptions() {
        SmsAsyncClient smsClient = builder.connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .httpLogOptions(new HttpLogOptions())
            .clientOptions(null)
            .buildAsyncClient();
        assertNotNull(smsClient);
    }

    @Test
    public void buildPipelineWithClientOptionsApplicationId() {
        SmsAsyncClient smsClient = builder.connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .clientOptions(new ClientOptions().setApplicationId(MOCK_APP_ID))
            .buildAsyncClient();
        assertNotNull(smsClient);
    }

    @Test
    public void buildPipelineWithLogOptionsApplicationId() {
        SmsAsyncClient smsClient = builder.connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .httpLogOptions(new HttpLogOptions().setApplicationId(MOCK_APP_ID))
            .buildAsyncClient();
        assertNotNull(smsClient);
    }

    @Test
    public void buildPipelineForClient() {
        SmsAsyncClient smsClient = builder.connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .pipeline(new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).build())
            .buildAsyncClient();
        assertNotNull(smsClient);
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING)
                .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
                .retryPolicy(new RetryPolicy())
                .buildClient());
    }

    @Test
    public void bothAzureKeyCredentialAndTokenCredentialSet() {
        assertThrows(IllegalArgumentException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING)
                .credential(new AzureKeyCredential(MOCK_ACCESS_KEY))
                .credential(new MockTokenCredential())
                .buildClient());
    }
}
