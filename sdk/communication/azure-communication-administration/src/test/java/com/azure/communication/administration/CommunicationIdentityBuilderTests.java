// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.azure.communication.administration.implementation.CommunicationIdentityResponseMocker;
import com.azure.communication.common.CommunicationClientCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

public class CommunicationIdentityBuilderTests {
    static final String MOCKURL = "https://chitchat.dev.communication.azure.net/";
    static final String MOCKTOKEN = "HuZVBcRKEA+TW30GBzdsmRyqitKk1dHj2OBtTsgRe2mzlxWUHGh06CdOVJwp07JKuss1k+/YeXL4dYXPF5El4Q==";

    static class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.empty(); // NOP
        }
    }

    private final CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();


    @Test
    public void buildAsyncClientTest() {
        try {
            CommunicationClientCredential credential = new CommunicationClientCredential(MOCKTOKEN);
            builder
                .endpoint(MOCKURL)
                .credential(credential)
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        // It would be very difficult to test the actual header
                        // values without re-creating the HMAC Policy. We will
                        // just make sure they are present and have values.
                        Map<String, String> headers = request.getHeaders().toMap();
                        assertTrue(headers.containsKey("Authorization"));
                        assertTrue(headers.containsKey("User-Agent"));
                        assertTrue(headers.containsKey("x-ms-content-sha256"));
                        assertNotNull(headers.get("Authorization"));
                        assertNotNull(headers.get("x-ms-content-sha256"));
                        return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
                    }
                });
            CommunicationIdentityAsyncClient asyncClient = builder.buildAsyncClient();
            assertNotNull(asyncClient);
            asyncClient.createUser();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void buildSyncClientTest() {
        try {
            CommunicationClientCredential credential = new CommunicationClientCredential(MOCKTOKEN);
            builder
                .endpoint(MOCKURL)
                .credential(credential)
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        // It would be very difficult to test the actual header
                        // values without re-creating the HMAC Policy. We will
                        // just make sure they are present and have values.
                        Map<String, String> headers = request.getHeaders().toMap();
                        assertTrue(headers.containsKey("Authorization"));
                        assertTrue(headers.containsKey("User-Agent"));
                        assertTrue(headers.containsKey("x-ms-content-sha256"));
                        assertNotNull(headers.get("Authorization"));
                        assertNotNull(headers.get("x-ms-content-sha256"));
                        return Mono.just(CommunicationIdentityResponseMocker.createUserResult(request));
                    }
                });
            CommunicationIdentityClient syncClient = builder.buildClient();
            assertNotNull(syncClient);
            syncClient.createUser();
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void missingTokenCredentialTest()
        throws NullPointerException, MalformedURLException, InvalidKeyException, NoSuchAlgorithmException {
        builder
            .endpoint(MOCKURL)
            .httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, () -> {
            builder.buildAsyncClient();
        });
    }

    @Test
    public void missingUrlTest()
        throws NullPointerException, MalformedURLException, InvalidKeyException, NoSuchAlgorithmException {
        CommunicationClientCredential credential = new CommunicationClientCredential(MOCKTOKEN);
        builder
            .credential(credential)
            .httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, () -> {
            builder.buildAsyncClient();
        });
    }
}
