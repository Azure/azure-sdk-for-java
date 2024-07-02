// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.keyvault;

import com.azure.core.http.rest.Response;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KeyVaultSecretHealthIndicatorTests {

    @Test
    void keyvaultIsUp() {
        @SuppressWarnings("unchecked") Response<KeyVaultSecret> mockResponse =
            (Response<KeyVaultSecret>) mock(Response.class);
        SecretAsyncClient mockAsyncClient = mock(SecretAsyncClient.class);
        Mockito.when(mockAsyncClient.getSecretWithResponse("spring-cloud-azure-not-existing-secret", ""))
            .thenReturn(Mono.just(mockResponse));
        KeyVaultSecretHealthIndicator indicator = new KeyVaultSecretHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void keyvaultIsDown() {
        SecretAsyncClient mockAsyncClient = mock(SecretAsyncClient.class);
        Mockito.when(mockAsyncClient.getSecretWithResponse("spring-cloud-azure-not-existing-secret", ""))
            .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
        KeyVaultSecretHealthIndicator indicator = new KeyVaultSecretHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
