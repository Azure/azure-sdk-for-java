// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography;

import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.policy.ExponentialBackoffOptions;
import io.clientcore.core.http.policy.HttpLogOptions;
import io.clientcore.core.http.policy.HttpRetryOptions;
import io.clientcore.core.http.policy.RetryPolicy;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.ClientOptions;
import io.clientcore.core.util.Header;
import com.azure.v2.security.keyvault.keys.KeyClientBuilder;
import com.azure.v2.security.keyvault.keys.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CryptographyClientBuilderTest {
    private String keyIdentifier;
    private CryptographyServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        keyIdentifier = "https://key-vault-url.vault.azure.net/keys/TestKey/someVersion";
        serviceVersion = CryptographyServiceVersion.V7_3;
    }

    @Test
    public void buildSyncClientTest() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder().keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .buildClient();

        assertNotNull(cryptographyClient);
        assertEquals(CryptographyClient.class.getSimpleName(), cryptographyClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder().keyIdentifier(keyIdentifier)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .buildClient();

        assertNotNull(cryptographyClient);
        assertEquals(CryptographyClient.class.getSimpleName(), cryptographyClient.getClass().getSimpleName());
    }

    @Test
    public void emptyKeyIdentifierThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CryptographyClientBuilder().keyIdentifier(""));
    }

    @Test
    public void nullKeyIdentifierThrowsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new CryptographyClientBuilder().keyIdentifier(null));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CryptographyClientBuilder().credential(null));
    }

    @Test
    public void nullHttpClientThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CryptographyClientBuilder().httpClient(null));
    }

    @Test
    public void clientOptionsIsPreferredOverRequestOptions() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder().keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("MyCustomHeader", "MyCustomValue"))))
            .buildClient();

        assertNotNull(cryptographyClient);
        assertEquals(CryptographyClient.class.getSimpleName(), cryptographyClient.getClass().getSimpleName());
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class, () -> new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .retryOptions(new HttpRetryOptions(new ExponentialBackoffOptions().setMaxRetries(2)))
            .retryPolicy(new RetryPolicy())
            .buildClient());
    }

    @Test
    public void buildClientWithRetryOptions() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder().keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .retryOptions(new HttpRetryOptions(new ExponentialBackoffOptions().setMaxRetries(2)))
            .buildClient();

        assertNotNull(cryptographyClient);
    }

    @Test
    public void buildClientWithRetryPolicy() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder().keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .retryPolicy(new RetryPolicy())
            .buildClient();

        assertNotNull(cryptographyClient);
    }

    @Test
    public void buildClientWithHttpLogOptions() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder().keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        assertNotNull(cryptographyClient);
    }

    @Test
    public void buildClientWithHttpPipeline() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder().keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .pipeline(TestUtils.buildHttpPipeline())
            .buildClient();

        assertNotNull(cryptographyClient);
    }

    @Test
    public void buildClientWithNullPipelineThrowsException() {
        assertThrows(NullPointerException.class, () -> new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .pipeline(null)
            .buildClient());
    }

    @Test
    public void buildClientWithCredentialAndPipelineThrowsException() {
        assertThrows(IllegalStateException.class, () -> new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .pipeline(TestUtils.buildHttpPipeline())
            .buildClient());
    }
}
