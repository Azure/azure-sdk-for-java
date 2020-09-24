// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.ConfigurableEnvironment;

public class KeyVaultHealthIndicator implements HealthIndicator {

    private final ConfigurableEnvironment environment;

    public KeyVaultHealthIndicator(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public Health health() {
        boolean isUp = environment.getPropertySources().stream()
                .filter(propertySource -> propertySource instanceof KeyVaultPropertySource)
                .anyMatch(propertySource -> ((KeyVaultPropertySource) propertySource).isUp());
        if (isUp) {
            return Health.up().build();
        } else {
            return Health.down().build();
        }
    }
}
