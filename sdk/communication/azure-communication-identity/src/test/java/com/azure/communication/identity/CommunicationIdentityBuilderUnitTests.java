// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommunicationIdentityBuilderUnitTests {
    static final String MOCK_URL = "https://REDACTED.communication.azure.com";
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

    private final CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();

    @Test
    public void buildAsyncClientTest() {
        builder.endpoint(MOCK_URL).credential(new AzureKeyCredential(MOCK_ACCESS_KEY)).httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertHMACHeadersExist(request.getHeaders());
                return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
            }
        });
        CommunicationIdentityAsyncClient asyncClient = builder.buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void buildSyncClientTest() {
        builder.endpoint(MOCK_URL).credential(new AzureKeyCredential(MOCK_ACCESS_KEY)).httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertHMACHeadersExist(request.getHeaders());
                return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
            }
        });
        CommunicationIdentityClient syncClient = builder.buildClient();
        assertNotNull(syncClient);
    }

    @Test
    public void buildAsyncClientTestUsingConnectionString() {
        builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                Map<String, String> headers = request.getHeaders().toMap();

                return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
            }
        });
        CommunicationIdentityAsyncClient asyncClient = builder.buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void buildAsyncClientTestUsingConnectionStringAndClientOptions() {
        builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertHMACHeadersExist(request.getHeaders());
                return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
            }
        });
        CommunicationIdentityAsyncClient asyncClient
            = builder.clientOptions(new ClientOptions().setApplicationId("testApplicationId")).buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void buildAsyncClientTestUsingConnectionStringAndHttpLogOptions() {
        builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertHMACHeadersExist(request.getHeaders());
                return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
            }
        });
        CommunicationIdentityAsyncClient asyncClient
            = builder.httpLogOptions(new HttpLogOptions().setApplicationId("testApplicationId")).buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void buildAsyncClientTestUsingConnectionStringAndConfigurationOptions() {
        builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertHMACHeadersExist(request.getHeaders());
                return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
            }
        });
        CommunicationIdentityAsyncClient asyncClient
            = builder.configuration(Configuration.getGlobalConfiguration()).buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void buildAsyncClientTestUsingConnectionStringAndPipelineOptions() {
        builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertHMACHeadersExist(request.getHeaders());
                return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
            }
        });
        CommunicationIdentityAsyncClient asyncClient
            = builder.pipeline(new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).build()).buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void buildAsyncClientTestUsingConnectionStringAndServiceVersion() {
        builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertHMACHeadersExist(request.getHeaders());
                return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
            }
        });
        CommunicationIdentityAsyncClient asyncClient
            = builder.serviceVersion(CommunicationIdentityServiceVersion.getLatest()).buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void createClientWithNoTokenCredentialThrows() throws NullPointerException {
        builder.endpoint(MOCK_URL).httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, builder::buildAsyncClient);
    }

    @Test
    public void createClientWithNoUrlThrows() throws NullPointerException {
        builder.credential(new AzureKeyCredential(MOCK_ACCESS_KEY)).httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, builder::buildAsyncClient);
    }

    @Test
    public void builderWithNullPipelineOptionsThrows() {
        assertThrows(NullPointerException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient()).pipeline(null));
    }

    @Test
    public void builderWithNullCustomPolicyOptionsThrows() {
        assertThrows(NullPointerException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient()).addPolicy(null));
    }

    @Test
    public void builderWithNullConfigurationOptionsThrows() {
        assertThrows(NullPointerException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING)
                .httpClient(new NoOpHttpClient())
                .configuration(null));
    }

    @Test
    public void builderWithNullClientOptionsThrows() {
        assertThrows(NullPointerException.class,
            () -> builder.connectionString(MOCK_CONNECTION_STRING)
                .httpClient(new NoOpHttpClient())
                .clientOptions(null));
    }

    @Test
    public void nullTokenTest() {
        assertThrows(NullPointerException.class, builder::buildAsyncClient);
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySetSync() {
        assertThrows(IllegalStateException.class,
            () -> builder.endpoint(MOCK_URL)
                .credential(new AzureKeyCredential(MOCK_ACCESS_KEY))
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        assertHMACHeadersExist(request.getHeaders());
                        return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
                    }
                })
                .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
                .retryPolicy(new RetryPolicy())
                .buildClient());
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySetAsync() {
        assertThrows(IllegalStateException.class,
            () -> builder.endpoint(MOCK_URL)
                .credential(new AzureKeyCredential(MOCK_ACCESS_KEY))
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        assertHMACHeadersExist(request.getHeaders());
                        return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
                    }
                })
                .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
                .retryPolicy(new RetryPolicy())
                .buildAsyncClient());
    }

    private void assertHMACHeadersExist(HttpHeaders headers) {
        assertNotNull(headers.get(HttpHeaderName.AUTHORIZATION));
        assertNotNull(headers.get("x-ms-content-sha256"));
    }
}
