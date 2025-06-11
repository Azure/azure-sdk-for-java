// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.MockHttpResponse;
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

public class SecretClientBuilderTest {
    private String vaultUrl;
    private String secretName;
    private SecretServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        vaultUrl = "https://key-vault-url.vault.azure.net/";
        secretName = "TestSecret";
        serviceVersion = SecretServiceVersion.V7_3;
    }

    @Test
    public void buildSyncClientTest() {
        SecretClient secretClient = new SecretClientBuilder().vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(new MockHttpResponse(request, 200)))
            .buildClient();

        assertNotNull(secretClient);
        assertEquals(SecretClient.class.getSimpleName(), secretClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SecretClient secretClient = new SecretClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(new MockHttpResponse(request, 200)))
            .buildClient();

        assertNotNull(secretClient);
        assertEquals(SecretClient.class.getSimpleName(), secretClient.getClass().getSimpleName());    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SecretClientBuilder().vaultUrl(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SecretClientBuilder().credential(null));
    }

    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        SecretClient secretClient = new SecretClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue(HttpHeaderName.USER_AGENT).contains("aNewApplication"));
                return CompletableFuture.failedFuture(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> secretClient.getSecret(secretName));
    }

    @Test
    public void applicationIdFallsBackToLogOptions() {
        SecretClient secretClient = new SecretClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue(HttpHeaderName.USER_AGENT).contains("anOldApplication"));
                return CompletableFuture.failedFuture(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> secretClient.getSecret(secretName));
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        SecretClient secretClient = new SecretClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .clientOptions(
                new ClientOptions().setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue(HttpHeaderName.USER_AGENT));
                return CompletableFuture.failedFuture(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> secretClient.getSecret(secretName));
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class,
            () -> new SecretClientBuilder().vaultUrl(vaultUrl)
                .serviceVersion(serviceVersion)
                .credential(new TestUtils.TestCredential())
                .retryOptions(new HttpRetryOptions())
                .retryPolicy(new HttpRetryPolicy())
                .httpClient(request -> CompletableFuture.completedFuture(new MockHttpResponse(request, 200)))
                .buildClient());
    }

    @Test
    public void defaultPipelineTest() {
        SecretClient secretClient = new SecretClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .buildClient();

        assertNotNull(secretClient);
        assertEquals(SecretClient.class.getSimpleName(), secretClient.getClass().getSimpleName());
    }

    @Test
    public void customPipelineTest() {
        HttpPipeline pipeline = HttpPipeline.builder()
            .build();

        SecretClient secretClient = new SecretClientBuilder()
            .pipeline(pipeline)
            .vaultUrl(vaultUrl)
            .buildClient();

        assertNotNull(secretClient);
        assertEquals(SecretClient.class.getSimpleName(), secretClient.getClass().getSimpleName());
    }
}
