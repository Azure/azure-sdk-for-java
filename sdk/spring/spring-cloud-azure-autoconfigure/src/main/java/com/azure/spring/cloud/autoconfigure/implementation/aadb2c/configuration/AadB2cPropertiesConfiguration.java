// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AadB2cProperties;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Configure bean: AADB2CProperties.
 */
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.b2c.enabled", havingValue = "true")
@EnableConfigurationProperties
class AadB2cPropertiesConfiguration {

    /**
     * Azure Global Properties.
     */
    private final AzureGlobalProperties global;

    /**
     * Creates a new instance of {@link AadB2cPropertiesConfiguration}.
     *
     * @param global Azure Global properties.
     */
    AadB2cPropertiesConfiguration(AzureGlobalProperties global) {
        this.global = global;
    }

    /**
     * Declare AADB2CProperties bean.
     *
     * @return AADB2CProperties bean
     */
    @Bean
    @ConfigurationProperties(prefix = AadB2cProperties.PREFIX)
    @ConditionalOnMissingBean
    AadB2cProperties aadB2cProperties() {
        AadB2cProperties aadB2cProperties = new AadB2cProperties();
        aadB2cProperties.getCredential().setClientId(global.getCredential().getClientId());
        aadB2cProperties.getCredential().setClientSecret(global.getCredential().getClientSecret());
        aadB2cProperties.getProfile().setTenantId(global.getProfile().getTenantId());
        return aadB2cProperties;
    }
}
