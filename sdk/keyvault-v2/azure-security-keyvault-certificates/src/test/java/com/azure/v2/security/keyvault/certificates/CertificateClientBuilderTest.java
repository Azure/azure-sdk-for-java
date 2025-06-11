// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CertificateClientBuilderTest {
    private String vaultUrl;
    private String certificateName;
    private CertificateServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        vaultUrl = "https://key-vault-url.vault.azure.net/";
        certificateName = "TestCertificate";
        serviceVersion = CertificateServiceVersion.V7_3;
    }

    @Test
    public void buildSyncClientTest() {
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .buildClient();

        assertNotNull(certificateClient);
        assertEquals(CertificateClient.class.getSimpleName(), certificateClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .buildClient();

        assertNotNull(certificateClient);
        assertEquals(CertificateClient.class.getSimpleName(), certificateClient.getClass().getSimpleName());
    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CertificateClientBuilder().vaultUrl(""));
    }

    @Test
    public void nullVaultUrlThrowsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new CertificateClientBuilder().vaultUrl(null));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CertificateClientBuilder().credential(null));
    }

    @Test
    public void nullHttpClientThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CertificateClientBuilder().httpClient(null));
    }

    @Test
    public void clientOptionsIsPreferredOverRequestOptions() {
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("MyCustomHeader", "MyCustomValue"))))
            .buildClient();

        assertNotNull(certificateClient);
        assertEquals(CertificateClient.class.getSimpleName(), certificateClient.getClass().getSimpleName());
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class, () -> new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
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
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .retryOptions(new HttpRetryOptions(new ExponentialBackoffOptions().setMaxRetries(2)))
            .buildClient();

        assertNotNull(certificateClient);
    }

    @Test
    public void buildClientWithRetryPolicy() {
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .retryPolicy(new RetryPolicy())
            .buildClient();

        assertNotNull(certificateClient);
    }

    @Test
    public void buildClientWithHttpLogOptions() {
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        assertNotNull(certificateClient);
    }

    @Test
    public void buildClientWithHttpPipeline() {
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .pipeline(TestUtils.buildHttpPipeline())
            .buildClient();

        assertNotNull(certificateClient);
    }

    @Test
    public void buildClientWithNullPipelineThrowsException() {
        assertThrows(NullPointerException.class, () -> new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .pipeline(null)
            .buildClient());
    }

    @Test
    public void buildClientWithCredentialAndPipelineThrowsException() {
        assertThrows(IllegalStateException.class, () -> new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .pipeline(TestUtils.buildHttpPipeline())
            .buildClient());
    }
}
