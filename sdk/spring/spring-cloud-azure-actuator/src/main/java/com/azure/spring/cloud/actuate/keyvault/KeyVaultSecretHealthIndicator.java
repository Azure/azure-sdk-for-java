// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.keyvault;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

import static com.azure.spring.cloud.actuate.util.Constants.DEFAULT_HEALTH_CHECK_TIMEOUT;

/**
 * Indicator class of KeyVaultHealth
 */
public class KeyVaultSecretHealthIndicator extends AbstractHealthIndicator {

    private final SecretAsyncClient secretAsyncClient;
    private Duration timeout = DEFAULT_HEALTH_CHECK_TIMEOUT;

    /**
     * Creates a new instance of {@link KeyVaultSecretHealthIndicator}.
     * @param secretAsyncClient the secretAsyncClient
     */
    public KeyVaultSecretHealthIndicator(SecretAsyncClient secretAsyncClient) {
        this.secretAsyncClient = secretAsyncClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            this.secretAsyncClient.getSecretWithResponse("azure-spring-none-existing-secret", "")
                .block(timeout);
            builder.up();
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                builder.up();
            } else {
                throw e;
            }
        }
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
