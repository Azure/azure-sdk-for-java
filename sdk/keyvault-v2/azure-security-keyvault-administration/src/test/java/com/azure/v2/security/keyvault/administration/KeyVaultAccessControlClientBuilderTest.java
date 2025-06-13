// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.AddHeadersPolicy;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeyVaultAccessControlClientBuilderTest {
    private String vaultUrl;
    private KeyVaultAdministrationServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        vaultUrl = "https://key-vault-url.vault.azure.net/";
        serviceVersion = KeyVaultAdministrationServiceVersion.getLatest();
    }

    @Test
    public void buildSyncClientTest() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .endpoint(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> new Response<>(request, 200, null, null))
            .buildClient();

        assertNotNull(keyVaultAccessControlClient);
        assertEquals(KeyVaultAccessControlClient.class.getSimpleName(),
            keyVaultAccessControlClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .endpoint(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> new Response<>(request, 200, null, null))
            .buildClient();

        assertNotNull(keyVaultAccessControlClient);
        assertEquals(KeyVaultAccessControlClient.class.getSimpleName(),
            keyVaultAccessControlClient.getClass().getSimpleName());
    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultAccessControlClientBuilder().endpoint(""));
    }

    @Test
    public void nullVaultUrlThrowsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultAccessControlClientBuilder().endpoint(null));
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
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .endpoint(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> new Response<>(request, 200, null, null))
            .addHttpPipelinePolicy(new AddHeadersPolicy(
                new HttpHeaders().set(HttpHeaderName.fromString("MyCustomHeader"), "MyCustomValue")))
            .buildClient();

        assertNotNull(keyVaultAccessControlClient);
        assertEquals(KeyVaultAccessControlClient.class.getSimpleName(),
            keyVaultAccessControlClient.getClass().getSimpleName());
    }

    @Test
    public void buildClientWithRetryOptions() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .endpoint(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> new Response<>(request, 200, null, null))
            .httpRetryOptions(new HttpRetryOptions(0, Duration.ZERO))
            .buildClient();

        assertNotNull(keyVaultAccessControlClient);
    }

    @Test
    public void buildClientWithHttpInstrumentationOptions() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .endpoint(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> new Response<>(request, 200, null, null))
            .httpInstrumentationOptions(new HttpInstrumentationOptions()
                .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS))
            .buildClient();

        assertNotNull(keyVaultAccessControlClient);
    }
}
