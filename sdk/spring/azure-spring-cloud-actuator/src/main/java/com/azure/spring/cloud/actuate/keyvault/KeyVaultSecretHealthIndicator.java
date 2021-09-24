// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.keyvault;

import com.azure.core.http.rest.Response;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Indicator class of KeyVaultHealth
 */
public class KeyVaultSecretHealthIndicator extends AbstractHealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultSecretHealthIndicator.class);

    private final SecretAsyncClient secretAsyncClient;

    public KeyVaultSecretHealthIndicator(SecretAsyncClient secretAsyncClient) {
        this.secretAsyncClient = secretAsyncClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        final Response<KeyVaultSecret> response = this.secretAsyncClient
            .getSecretWithResponse("azure-spring-none-existing-secret", "")
            .block();

        // TODO (xiada): this health indicator implementation

        if (response != null) {

        }
        if (response == null) {
            builder.down();
        } else {
            builder.up();
        }

    }

}
