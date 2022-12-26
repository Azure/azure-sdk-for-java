// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.keyvault;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

import static com.azure.spring.cloud.actuator.implementation.util.ActuateConstants.DEFAULT_HEALTH_CHECK_TIMEOUT;

/**
 * Indicator class of Key Vault Certificate Health
 */
public class KeyVaultCertificateHealthIndicator extends AbstractHealthIndicator {

    private final CertificateAsyncClient certificateAsyncClient;
    private Duration timeout = DEFAULT_HEALTH_CHECK_TIMEOUT;

    /**
     * Creates a new instance of {@link KeyVaultCertificateHealthIndicator}.
     * @param certificateAsyncClient the certificate async client
     */
    public KeyVaultCertificateHealthIndicator(CertificateAsyncClient certificateAsyncClient) {
        this.certificateAsyncClient = certificateAsyncClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            this.certificateAsyncClient.getCertificateWithResponse("spring-cloud-azure-not-existing-certificate")
                .block(timeout);
            builder.up();
        } catch (ResourceNotFoundException e) {
            builder.up();
        }
    }

    /**
     * Set health check request timeout.
     * @param timeout the duration value.
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
