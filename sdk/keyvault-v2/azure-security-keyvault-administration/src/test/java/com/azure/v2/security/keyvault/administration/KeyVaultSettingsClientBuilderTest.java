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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeyVaultSettingsClientBuilderTest {
    private String vaultUrl;
    private String settingName;
    private KeyVaultAdministrationServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        vaultUrl = "https://key-vault-url.vault.azure.net/";
        settingName = "testSetting";
        serviceVersion = KeyVaultAdministrationServiceVersion.getLatest();
    }

    @Test
    public void buildSyncClientTest() {
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .buildClient();

        assertNotNull(keyVaultSettingsClient);
        assertEquals(KeyVaultSettingsClient.class.getSimpleName(),
            keyVaultSettingsClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .buildClient();

        assertNotNull(keyVaultSettingsClient);
        assertEquals(KeyVaultSettingsClient.class.getSimpleName(),
            keyVaultSettingsClient.getClass().getSimpleName());
    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultSettingsClientBuilder().vaultUrl(""));
    }

    @Test
    public void nullVaultUrlThrowsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultSettingsClientBuilder().vaultUrl(null));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultSettingsClientBuilder().credential(null));
    }

    @Test
    public void nullHttpClientThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultSettingsClientBuilder().httpClient(null));
    }

    @Test
    public void clientOptionsIsPreferredOverRequestOptions() {
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("MyCustomHeader", "MyCustomValue"))))
            .buildClient();

        assertNotNull(keyVaultSettingsClient);
        assertEquals(KeyVaultSettingsClient.class.getSimpleName(),
            keyVaultSettingsClient.getClass().getSimpleName());
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class, () -> new KeyVaultSettingsClientBuilder()
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
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .retryOptions(new HttpRetryOptions(new ExponentialBackoffOptions().setMaxRetries(2)))
            .buildClient();

        assertNotNull(keyVaultSettingsClient);
    }

    @Test
    public void buildClientWithRetryPolicy() {
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .retryPolicy(new RetryPolicy())
            .buildClient();

        assertNotNull(keyVaultSettingsClient);
    }

    @Test
    public void buildClientWithHttpLogOptions() {
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> CompletableFuture.completedFuture(
                Response.fromValue(null, null, 200, null, null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        assertNotNull(keyVaultSettingsClient);
    }

    @Test
    public void buildClientWithHttpPipeline() {
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .pipeline(TestUtils.buildHttpPipeline())
            .buildClient();

        assertNotNull(keyVaultSettingsClient);
    }

    @Test
    public void buildClientWithNullPipelineThrowsException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultSettingsClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .pipeline(null)
            .buildClient());
    }

    @Test
    public void buildClientWithCredentialAndPipelineThrowsException() {
        assertThrows(IllegalStateException.class, () -> new KeyVaultSettingsClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .pipeline(TestUtils.buildHttpPipeline())
            .buildClient());
    }
}
