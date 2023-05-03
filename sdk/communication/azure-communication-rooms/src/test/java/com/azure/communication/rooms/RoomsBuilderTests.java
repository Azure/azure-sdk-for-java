// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

public class RoomsBuilderTests {
    static final String MOCK_URL = "https://REDACTED.communication.azure.com";
    static final String MOCK_ACCESS_KEY = "P2tP5RwZVFcJa3sfJvHEmGaKbemSAw2e";
    static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=P2tP5RwZVFcJa3sfJvHEmGaKbemSAw2e";
    
    static class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.empty(); // NOP
        }
    }

    private final RoomsClientBuilder builder = new RoomsClientBuilder();

    @Test
    public void missingTokenCredentialTest()
            throws NullPointerException, MalformedURLException, InvalidKeyException, NoSuchAlgorithmException {
        builder.endpoint(MOCK_URL).httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, () -> {
            builder.buildAsyncClient();
        });
    }

    @Test
    public void missingUrlTest() throws NullPointerException, MalformedURLException {
        builder.credential(new AzureKeyCredential(MOCK_ACCESS_KEY)).httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, () -> {
            builder.buildAsyncClient();
        });
    }

    @Test
    public void nullPipelineTest() {
        assertThrows(NullPointerException.class, () -> {
            builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient()).pipeline(null);
        });
    }

    @Test
    public void nullCustomPolicyTest() {
        assertThrows(NullPointerException.class, () -> {
            builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient()).addPolicy(null);
        });
    }

    @Test
    public void nullConfigurationTest() {
        assertThrows(NullPointerException.class, () -> {
            builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient()).configuration(null);
        });
    }

    @Test
    public void nullHttpLogOptionsTest() {
        assertThrows(NullPointerException.class, () -> {
            builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient()).httpLogOptions(null);
        });
    }

    @Test
    public void nullRetryPolicyTest() {
        assertThrows(NullPointerException.class, () -> {
            builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient()).retryPolicy(null);
        });
    }

    @Test
    public void buildPiplineForClient() {
        RoomsAsyncClient roomsClient = builder.connectionString(MOCK_CONNECTION_STRING).httpClient(new NoOpHttpClient())
                .pipeline(new HttpPipelineBuilder().build()).buildAsyncClient();
        assertNotNull(roomsClient);
    }
}
