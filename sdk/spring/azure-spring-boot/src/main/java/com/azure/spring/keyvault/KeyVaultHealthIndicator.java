// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.env.ConfigurableEnvironment;

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
        KeyVaultPropertySource keyVaultPropertySource =
            environment.getPropertySources()
                       .stream()
                       .filter(propertySource -> propertySource instanceof KeyVaultPropertySource)
                       .map(propertySource -> (KeyVaultPropertySource) propertySource)
                       .findFirst()
                       .orElse(null);
        if (keyVaultPropertySource == null) {
            return Health.status(Status.UNKNOWN).build();
        } else if (keyVaultPropertySource.isUp()) {
            return Health.status(Status.UP).build();
        } else {
            return Health.status(Status.DOWN).build();
        }
    }
}
