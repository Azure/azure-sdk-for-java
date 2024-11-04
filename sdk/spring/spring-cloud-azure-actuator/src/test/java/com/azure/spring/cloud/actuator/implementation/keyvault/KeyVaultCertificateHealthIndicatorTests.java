// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.keyvault;

import com.azure.core.http.rest.Response;
import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KeyVaultCertificateHealthIndicatorTests {

    @Test
    void certificateIsUp() {
        @SuppressWarnings("unchecked") Response<KeyVaultCertificateWithPolicy> mockResponse =
            (Response<KeyVaultCertificateWithPolicy>) mock(Response.class);
        CertificateAsyncClient mockAsyncClient = mock(CertificateAsyncClient.class);
        Mockito.when(mockAsyncClient.getCertificateWithResponse("spring-cloud-azure-not-existing-certificate"))
            .thenReturn(Mono.just(mockResponse));
        KeyVaultCertificateHealthIndicator indicator = new KeyVaultCertificateHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void certificateIsDown() {
        CertificateAsyncClient mockAsyncClient = mock(CertificateAsyncClient.class);
        Mockito.when(mockAsyncClient.getCertificateWithResponse("spring-cloud-azure-not-existing-certificate"))
            .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
        KeyVaultCertificateHealthIndicator indicator = new KeyVaultCertificateHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
