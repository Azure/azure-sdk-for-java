// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import com.azure.security.keyvault.secrets.SecretClient;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@ConditionalOnClass({ SecretClient.class, HealthIndicator.class })
@PropertySource("classpath:/azure-spring-actuator.properties")
public class KeyVaultHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-key-vault")
    KeyVaultHealthIndicator keyVaultHealthIndicator(ConfigurableEnvironment environment) {
        return new KeyVaultHealthIndicator(environment);
    }
}
