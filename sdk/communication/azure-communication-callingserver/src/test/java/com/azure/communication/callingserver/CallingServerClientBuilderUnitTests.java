// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CallingServerClientBuilderUnitTests {
    static final String MOCK_URL = "https://REDACTED.communication.azure.com";
    static final String MOCK_ACCESS_KEY = "eyKfcHciOiJIUzI1NiIsInR5cCI6IkqXVCJ9eyJzdWIiOiIxMjM0NTY5ODkwIiwibmFtZSI7IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadUs4s5d";
    static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";
    private static final String APPLICATION_ID = "833bad32-4432-4d41-8bb4";

    static class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.empty(); // NOP
        }
    }

    private final CallingServerClientBuilder builder = new CallingServerClientBuilder();

    @Test
    public void missingTokenCredentialTest() throws NullPointerException {
        builder
            .endpoint(MOCK_URL)
            .httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, builder::buildAsyncClient);
    }

    @Test
    public void missingUrlTest()
        throws NullPointerException {
        builder
            .credential(new AzureKeyCredential(MOCK_ACCESS_KEY))
            .httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, builder::buildAsyncClient);
    }

    @Test
    public void nullPipelineTest() {
        assertThrows(NullPointerException.class, () -> builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .pipeline(null));
    }

    @Test
    public void nullCustomPolicyTest() {
        assertThrows(NullPointerException.class, () -> builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .addPolicy(null));
    }

    @Test
    public void nullConfigurationTest() {
        assertThrows(NullPointerException.class, () -> builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .configuration(null));
    }

    @Test
    public void nullHttpLogOptionsTest() {
        assertThrows(NullPointerException.class, () -> builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .httpLogOptions(null));
    }

    @Test
    public void nullRetryPolicyTest() {
        assertThrows(
            NullPointerException.class, () -> builder
                .connectionString(MOCK_CONNECTION_STRING)
                .httpClient(new NoOpHttpClient())
                .retryPolicy(null));
    }

    @Test
    public void buildPiplineForClient() {
        CallingServerAsyncClient callingServerAsyncClient = builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .pipeline(new HttpPipelineBuilder().build())
            .buildAsyncClient();
        assertNotNull(callingServerAsyncClient);
    }

    @Test
    public void setHttpLogOptions() {
        HttpLogOptions options = new HttpLogOptions().setApplicationId(APPLICATION_ID);
        CallingServerAsyncClient callAsyncClient = builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpLogOptions(options)
            .httpClient(new NoOpHttpClient())
            .buildAsyncClient();
        assertNotNull(callAsyncClient);
    }

    @Test
    public void setClientOptions() {
        ClientOptions options = new ClientOptions().setApplicationId(APPLICATION_ID);
        CallingServerAsyncClient callAsyncClient = builder
            .connectionString(MOCK_CONNECTION_STRING)
            .clientOptions(options)
            .httpClient(new NoOpHttpClient())
            .buildAsyncClient();
        assertNotNull(callAsyncClient);
    }

    @Test
    public void noClientOptionsNoPipeline() {
        CallingServerAsyncClient callAsyncClient = builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient())
            .buildAsyncClient();
        assertNotNull(callAsyncClient);
    }

    @Test
    public void addPolicy() {
        AzureKeyCredential credential = new AzureKeyCredential("key");
        CallingServerAsyncClient callAsyncClient =
            builder
            .connectionString(MOCK_CONNECTION_STRING)
            .addPolicy(new HmacAuthenticationPolicy(credential))
            .httpClient(new NoOpHttpClient())
            .pipeline(new HttpPipelineBuilder().build())
            .retryPolicy(new RetryPolicy())
            .buildAsyncClient();
        assertNotNull(callAsyncClient);
    }

    @Test
    public void argumentExceptionOnConnectionStringAndEndpoint() {
        assertThrows(IllegalArgumentException.class,
            () -> builder
                .connectionString(MOCK_CONNECTION_STRING)
                .endpoint(MOCK_URL)
                .httpClient(new NoOpHttpClient())
                .buildAsyncClient());
    }

    @Test
    public void argumentExceptionOnEmptyConnectionString() {
        assertThrows(NullPointerException.class,
            () -> builder
                .connectionString("")
                .httpClient(new NoOpHttpClient())
                .buildAsyncClient());
    }

    @Test
    public void argumentExceptionOnConnectionStringAndAzureKeyCredential() {
        AzureKeyCredential credential = new AzureKeyCredential("key");
        assertThrows(
            IllegalArgumentException.class, () -> builder
                .connectionString(MOCK_CONNECTION_STRING)
                .credential(credential)
                .httpClient(new NoOpHttpClient())
                .buildAsyncClient());
    }

    @Test
    public void argumentExceptionOnConnectionStringAndTokenCredential() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        assertThrows(
            IllegalArgumentException.class, () -> builder
                .connectionString(MOCK_CONNECTION_STRING)
                .credential(tokenCredential)
                .httpClient(new NoOpHttpClient())
                .buildAsyncClient());
    }

    @Test
    public void argumentExceptionOnAzureKeyCredentialAndTokenCredential() {
        AzureKeyCredential credential = new AzureKeyCredential("key");
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        assertThrows(
            IllegalArgumentException.class, () -> builder
                .credential(credential)
                .credential(tokenCredential)
                .httpClient(new NoOpHttpClient())
                .buildAsyncClient());
    }

    @Test
    public void noPipelineWithToken() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        CallingServerAsyncClient callAsyncClient = builder
            .endpoint(MOCK_URL)
            .credential(tokenCredential)
            .httpClient(new NoOpHttpClient())
            .buildAsyncClient();

        assertNotNull(callAsyncClient);
    }

    @Test
    public void noCredential() {
        assertThrows(
            IllegalArgumentException.class, () -> builder
                .endpoint(MOCK_URL)
                .httpClient(new NoOpHttpClient())
                .buildAsyncClient());
    }

    @Test
    public void noEndpoint() {
        assertThrows(
            NullPointerException.class, () -> builder
                .httpClient(new NoOpHttpClient())
                .buildAsyncClient());
    }
}

