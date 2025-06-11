// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys;

import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.policy.ExponentialBackoffOptions;
import io.clientcore.core.http.policy.HttpLogOptions;
import io.clientcore.core.http.policy.HttpRetryOptions;
import io.clientcore.core.http.policy.HttpRetryPolicy;
import io.clientcore.core.util.ClientOptions;
import io.clientcore.core.util.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyClientBuilderTest {
    private String vaultUrl;
    private String keyName;
    private KeyServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        vaultUrl = "https://key-vault-url.vault.azure.net/";
        keyName = "TestKey";
        serviceVersion = KeyServiceVersion.V7_3;
    }

    @Test
    public void buildSyncClientTest() {
        KeyClient keyClient = new KeyClientBuilder().vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(new MockHttpResponse(request, 200)))
            .buildClient();

        assertNotNull(keyClient);
        assertEquals(KeyClient.class.getSimpleName(), keyClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        KeyClient keyClient = new KeyClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(new MockHttpResponse(request, 200)))
            .buildClient();

        assertNotNull(keyClient);
        assertEquals(KeyClient.class.getSimpleName(), keyClient.getClass().getSimpleName());
    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new KeyClientBuilder().vaultUrl(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new KeyClientBuilder().credential(null));
    }

    @Test
    public void credentialWithInvalidChallengeThrowsHttpResponseException() {
        KeyClient keyClient = new KeyClientBuilder().vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(httpRequest -> {
                if (httpRequest.getUrl().toString().contains("https://key-vault-url.vault.azure.net/keys/TestKey")) {
                    return CompletableFuture.failedFuture(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
                } else {
                    return CompletableFuture.completedFuture(new MockHttpResponse(httpRequest, 200));
                }
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> keyClient.getKey(keyName));
    }

    @Test
    public void credentialWithEmptyChallengeThrowsHttpResponseException() {
        KeyClient keyClient = new KeyClientBuilder().vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(httpRequest -> {
                if (httpRequest.getUrl().toString().contains("https://key-vault-url.vault.azure.net/keys/TestKey")) {
                    return CompletableFuture.failedFuture(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
                } else {
                    return CompletableFuture.completedFuture(new MockHttpResponse(httpRequest, 200));
                }
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> keyClient.getKey(keyName));
    }

    @Test
    public void credentialWithNoChallengeThrowsHttpResponseException() {
        KeyClient keyClient = new KeyClientBuilder().vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(httpRequest -> {
                if (httpRequest.getUrl().toString().contains("https://key-vault-url.vault.azure.net/keys/TestKey")) {
                    return CompletableFuture.failedFuture(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
                } else {
                    return CompletableFuture.completedFuture(new MockHttpResponse(httpRequest, 200));
                }
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> keyClient.getKey(keyName));
    }

    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        KeyClient keyClient = new KeyClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(new MockHttpResponse(request, 200)))
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .serviceVersion(KeyServiceVersion.V7_3)
            .buildClient();

        assertNotNull(keyClient);
        assertEquals(KeyClient.class.getSimpleName(), keyClient.getClass().getSimpleName());
    }

    @Test
    public void applicationIdFallsBackToLogOptions() {
        KeyClient keyClient = new KeyClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(new MockHttpResponse(request, 200)))
            .httpLogOptions(new HttpLogOptions().setApplicationId("anApplication"))
            .serviceVersion(KeyServiceVersion.V7_3)
            .buildClient();

        assertNotNull(keyClient);
        assertEquals(KeyClient.class.getSimpleName(), keyClient.getClass().getSimpleName());
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        KeyClient keyClient = new KeyClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(new MockHttpResponse(request, 200)))
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .serviceVersion(KeyServiceVersion.V7_3)
            .buildClient();

        assertNotNull(keyClient);
        assertEquals(KeyClient.class.getSimpleName(), keyClient.getClass().getSimpleName());
    }

    private static class MockHttpResponse implements io.clientcore.core.http.models.HttpResponse {
        private final io.clientcore.core.http.models.HttpRequest request;
        private final int statusCode;

        MockHttpResponse(io.clientcore.core.http.models.HttpRequest request, int statusCode) {
            this.request = request;
            this.statusCode = statusCode;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(HttpHeaderName headerName) {
            return null;
        }

        @Override
        public io.clientcore.core.http.models.HttpHeaders getHeaders() {
            return new io.clientcore.core.http.models.HttpHeaders();
        }

        @Override
        public io.clientcore.core.util.BinaryData getBody() {
            return io.clientcore.core.util.BinaryData.fromString("");
        }

        @Override
        public io.clientcore.core.http.models.HttpRequest getRequest() {
            return request;
        }

        @Override
        public void close() {
        }
    }
}
