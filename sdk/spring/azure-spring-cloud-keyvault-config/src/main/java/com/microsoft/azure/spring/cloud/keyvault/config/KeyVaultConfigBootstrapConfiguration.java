// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.keyvault.config;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryAutoConfiguration;
import com.microsoft.azure.spring.cloud.keyvault.config.auth.AadKeyVaultCredentials;
import com.microsoft.azure.spring.cloud.keyvault.config.auth.AuthenticationExecutor;
import com.microsoft.azure.spring.cloud.keyvault.config.auth.AuthenticationExecutorFactory;
import com.microsoft.azure.spring.cloud.keyvault.config.auth.Credentials;
import com.microsoft.azure.spring.cloud.keyvault.config.auth.DefaultAuthenticationExecutorFactory;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Spring Cloud Bootstrap Configuration for setting up an
 * {@link KeyVaultPropertySourceLocator}.
 */
@Configuration
@ConditionalOnProperty(name = KeyVaultConfigProperties.ENABLED, matchIfMissing = true)
@EnableConfigurationProperties(KeyVaultConfigProperties.class)
@AutoConfigureAfter(TelemetryAutoConfiguration.class)
public class KeyVaultConfigBootstrapConfiguration {
    private static final String KEY_VAULT_CONFIG = "KeyVaultConfig";

    @Autowired(required = false)
    private TelemetryCollector telemetryCollector;

    @PostConstruct
    public void collectTelemetry() {
        if (telemetryCollector != null) {
            telemetryCollector.addService(KEY_VAULT_CONFIG);
        }
    }

    @Bean
    public KeyVaultClient keyVaultClient(KeyVaultConfigProperties properties, AuthenticationExecutorFactory factory) {
        final Credentials credentials = properties.getCredentials();
        AuthenticationExecutor executor = factory.create(credentials);
        return new KeyVaultClient(new AadKeyVaultCredentials(executor));
    }

    @Bean
    public KeyVaultPropertySourceLocator keyVaultPropertySourceLocator(KeyVaultClient client,
            KeyVaultConfigProperties properties) {
        return new KeyVaultPropertySourceLocator(client, properties);
    }

    @Bean
    public AuthenticationExecutorFactory authenticationExecutorFactory() {
        return new DefaultAuthenticationExecutorFactory();
    }
}
