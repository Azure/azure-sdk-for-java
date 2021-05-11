// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.env.ConfigurableEnvironment;

import static com.azure.spring.utils.Constants.NOT_CONFIGURED_STATUS;

/**
 * Indicator class of KeyVaultHealth
 */
public class KeyVaultHealthIndicator implements HealthIndicator {

    private final ConfigurableEnvironment environment;

    public KeyVaultHealthIndicator(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public Health health() {
        Status status = environment.getPropertySources().stream()
                                   .filter(propertySource -> propertySource instanceof KeyVaultPropertySource)
                                   .map(propertySource -> (KeyVaultPropertySource) propertySource)
                                   .map(KeyVaultPropertySource::getStatusCode)
                                   .findFirst()
                                   .orElse(null);
        return status == null ? Health.status(NOT_CONFIGURED_STATUS).build() : Health.status(status).build();
    }
}
