// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.networktraversal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.azure.communication.networktraversal.implementation.CommunicationRelayResponseMocker;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

public class CommunicationRelayBuilderTests {
    static final String MOCK_URL = "https://REDACTED.communication.azure.com";
    static final String MOCK_ACCESS_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";
    static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";

    static class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.empty(); // NOP
        }
    }

    private final CommunicationRelayClientBuilder builder = new CommunicationRelayClientBuilder();

    @Test
    public void buildAsyncClientTest() {
        builder
            .endpoint(MOCK_URL)
            .credential(new AzureKeyCredential(MOCK_ACCESS_KEY))
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Map<String, String> headers = request.getHeaders().toMap();
                    assertHMACHeadersExist(headers);
                    return Mono.just(CommunicationRelayResponseMocker.createUserResult(request));
                }
            });
        CommunicationRelayAsyncClient asyncClient = builder.buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void buildSyncClientTest() {
        builder
            .endpoint(MOCK_URL)
            .credential(new AzureKeyCredential(MOCK_ACCESS_KEY))
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Map<String, String> headers = request.getHeaders().toMap();
                    assertHMACHeadersExist(headers);
                    return Mono.just(CommunicationRelayResponseMocker.createUserResult(request));
                }
            });
        CommunicationRelayClient syncClient = builder.buildClient();
        assertNotNull(syncClient);
    }

    @Test
    public void buildAsyncClientTestUsingConnectionString() {
        builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Map<String, String> headers = request.getHeaders().toMap();

                    return Mono.just(CommunicationRelayResponseMocker.createUserResult(request));
                }
            });
        CommunicationRelayAsyncClient asyncClient = builder.buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void buildAsyncClientTestUsingConnectionStringAndClientOptions() {
        builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Map<String, String> headers = request.getHeaders().toMap();
                    assertHMACHeadersExist(headers);
                    return Mono.just(CommunicationRelayResponseMocker.createUserResult(request));
                }
            });
        CommunicationRelayAsyncClient asyncClient = builder
            .clientOptions(new ClientOptions().setApplicationId("testApplicationId"))
            .buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void buildAsyncClientTestUsingConnectionStringAndHttpLogOptions() {
        builder
            .connectionString(MOCK_CONNECTION_STRING)
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Map<String, String> headers = request.getHeaders().toMap();
                    assertHMACHeadersExist(headers);
                    return Mono.just(CommunicationRelayResponseMocker.createUserResult(request));
                }
            });
        CommunicationRelayAsyncClient asyncClient = builder
            .httpLogOptions(new HttpLogOptions().setApplicationId("testApplicationId"))
            .buildAsyncClient();
        assertNotNull(asyncClient);
    }

    @Test
    public void createClientWithNoTokenCredentialThrows()
        throws NullPointerException, MalformedURLException, InvalidKeyException, NoSuchAlgorithmException {
        builder
            .endpoint(MOCK_URL)
            .httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, () -> {
            builder.buildAsyncClient();
        });
    }

    @Test
    public void createClientWithNoUrlThrows()
        throws NullPointerException, MalformedURLException {
        builder
            .credential(new AzureKeyCredential(MOCK_ACCESS_KEY))
            .httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, () -> {
            builder.buildAsyncClient();
        });
    }

    @Test
    public void nullTokenTest() {
        assertThrows(NullPointerException.class, () -> {
            builder.buildAsyncClient();
        });
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySetSync() {
        assertThrows(IllegalStateException.class, () -> builder
            .endpoint(MOCK_URL)
            .credential(new AzureKeyCredential(MOCK_ACCESS_KEY))
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Map<String, String> headers = request.getHeaders().toMap();
                    assertHMACHeadersExist(headers);
                    return Mono.just(CommunicationRelayResponseMocker.createUserResult(request));
                }
            })
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
            .retryPolicy(new RetryPolicy())
            .buildClient());
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySetSyncAsync() {
        assertThrows(IllegalStateException.class, () -> builder
            .endpoint(MOCK_URL)
            .credential(new AzureKeyCredential(MOCK_ACCESS_KEY))
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Map<String, String> headers = request.getHeaders().toMap();
                    assertHMACHeadersExist(headers);
                    return Mono.just(CommunicationRelayResponseMocker.createUserResult(request));
                }
            })
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
            .retryPolicy(new RetryPolicy())
            .buildAsyncClient());
    }

    private void assertHMACHeadersExist(Map<String, String> headers) {
        assertTrue(headers.containsKey("Authorization"));
        assertTrue(headers.containsKey("x-ms-content-sha256"));
        assertNotNull(headers.get("Authorization"));
        assertNotNull(headers.get("x-ms-content-sha256"));
    }
}
