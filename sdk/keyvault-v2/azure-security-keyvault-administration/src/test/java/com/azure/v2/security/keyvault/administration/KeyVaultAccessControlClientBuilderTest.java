// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.policy.ExponentialBackoffOptions;
import io.clientcore.core.http.policy.FixedDelayOptions;
import io.clientcore.core.http.policy.HttpLogOptions;
import io.clientcore.core.http.policy.HttpRetryOptions;
import io.clientcore.core.http.policy.RetryPolicy;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.ClientOptions;
import io.clientcore.core.util.Header;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultAccessControlClientBuilderTest {
    private String vaultUrl;
    private String roleDefinitionId;
    private String principalId;
    private KeyVaultAdministrationServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        vaultUrl = "https://key-vault-url.vault.azure.net/";
        roleDefinitionId = "RoleDefinitionId";
        principalId = "PrincipalId";
        serviceVersion = KeyVaultAdministrationServiceVersion.getLatest();
    }

    @Test
    public void buildSyncClientTest() {
        KeyVaultAccessControlClient keyVaultAccessControlClient
            = new KeyVaultAccessControlClientBuilder().vaultUrl(vaultUrl)
                .serviceVersion(serviceVersion)
                .credential(new TestUtils.TestCredential())
                .httpClient(request -> CompletableFuture.completedFuture(
                    Response.fromValue(null, null, 200, null, null)))
                .buildClient();

        assertNotNull(keyVaultAccessControlClient);
        assertEquals(KeyVaultAccessControlClient.class.getSimpleName(),
            keyVaultAccessControlClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        KeyVaultAccessControlClient keyVaultAccessControlClient
            = new KeyVaultAccessControlClientBuilder().vaultUrl(vaultUrl)
                .credential(new TestUtils.TestCredential())
                .httpClient(request -> CompletableFuture.completedFuture(
                    Response.fromValue(null, null, 200, null, null)))
                .buildClient();

        assertNotNull(keyVaultAccessControlClient);
        assertEquals(KeyVaultAccessControlClient.class.getSimpleName(),
            keyVaultAccessControlClient.getClass().getSimpleName());
    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultAccessControlClientBuilder().vaultUrl(""));
    }

    @Test
    public void nullVaultUrlThrowsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultAccessControlClientBuilder().vaultUrl(null));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultAccessControlClientBuilder().credential(null));
    }

    @Test
    public void nullHttpClientThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultAccessControlClientBuilder().httpClient(null));
    }

    @Test
    public void clientOptionsIsPreferredOverRequestOptions() {
        KeyVaultAccessControlClient keyVaultAccessControlClient
            = new KeyVaultAccessControlClientBuilder().vaultUrl(vaultUrl)
                .serviceVersion(serviceVersion)
                .credential(new TestUtils.TestCredential())
                .httpClient(request -> CompletableFuture.completedFuture(
                    Response.fromValue(null, null, 200, null, null)))
                .clientOptions(new ClientOptions()
                    .setHeaders(Collections.singletonList(new Header("MyCustomHeader", "MyCustomValue"))))
                .buildClient();

        assertNotNull(keyVaultAccessControlClient);
        assertEquals(KeyVaultAccessControlClient.class.getSimpleName(),
            keyVaultAccessControlClient.getClass().getSimpleName());
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class, () -> new KeyVaultAccessControlClientBuilder()
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
        KeyVaultAccessControlClient keyVaultAccessControlClient
            = new KeyVaultAccessControlClientBuilder().vaultUrl(vaultUrl)
                .serviceVersion(serviceVersion)
                .credential(new TestUtils.TestCredential())
                .httpClient(request -> CompletableFuture.completedFuture(
                    Response.fromValue(null, null, 200, null, null)))
                .retryOptions(new HttpRetryOptions(new ExponentialBackoffOptions().setMaxRetries(2)))
                .buildClient();

        assertNotNull(keyVaultAccessControlClient);
    }

    @Test
    public void buildClientWithRetryPolicy() {
        KeyVaultAccessControlClient keyVaultAccessControlClient
            = new KeyVaultAccessControlClientBuilder().vaultUrl(vaultUrl)
                .serviceVersion(serviceVersion)
                .credential(new TestUtils.TestCredential())
                .httpClient(request -> CompletableFuture.completedFuture(
                    Response.fromValue(null, null, 200, null, null)))
                .retryPolicy(new RetryPolicy())
                .buildClient();

        assertNotNull(keyVaultAccessControlClient);
    }

    @Test
    public void buildClientWithHttpLogOptions() {
        KeyVaultAccessControlClient keyVaultAccessControlClient
            = new KeyVaultAccessControlClientBuilder().vaultUrl(vaultUrl)
                .serviceVersion(serviceVersion)
                .credential(new TestUtils.TestCredential())
                .httpClient(request -> CompletableFuture.completedFuture(
                    Response.fromValue(null, null, 200, null, null)))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();

        assertNotNull(keyVaultAccessControlClient);
    }

    @Test
    public void buildClientWithHttpPipeline() {
        KeyVaultAccessControlClient keyVaultAccessControlClient
            = new KeyVaultAccessControlClientBuilder().vaultUrl(vaultUrl)
                .serviceVersion(serviceVersion)
                .pipeline(TestUtils.buildHttpPipeline())
                .buildClient();

        assertNotNull(keyVaultAccessControlClient);
    }

    @Test
    public void buildClientWithNullPipelineThrowsException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .pipeline(null)
            .buildClient());
    }

    @Test
    public void buildClientWithCredentialAndPipelineThrowsException() {
        assertThrows(IllegalStateException.class, () -> new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .pipeline(TestUtils.buildHttpPipeline())
            .buildClient());
    }
}
