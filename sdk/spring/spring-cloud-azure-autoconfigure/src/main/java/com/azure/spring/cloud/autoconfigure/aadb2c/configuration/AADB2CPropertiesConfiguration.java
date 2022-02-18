// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aadb2c.configuration;

import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AADB2CProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.AzureGlobalProperties;
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
public class AADB2CPropertiesConfiguration {

    /**
     * Azure Global Properties.
     */
    private final AzureGlobalProperties global;

    /**
     * Creates a new instance of {@link AADB2CPropertiesConfiguration}.
     *
     * @param global Azure Global properties.
     */
    public AADB2CPropertiesConfiguration(AzureGlobalProperties global) {
        this.global = global;
    }

    /**
     * Declare AADB2CProperties bean.
     *
     * @return AADB2CProperties bean
     */
    @Bean
    @ConfigurationProperties(prefix = AADB2CProperties.PREFIX)
    @ConditionalOnMissingBean
    AADB2CProperties aadB2cProperties() {
        AADB2CProperties aadB2cProperties = new AADB2CProperties();
        aadB2cProperties.getCredential().setClientId(global.getCredential().getClientId());
        aadB2cProperties.getCredential().setClientSecret(global.getCredential().getClientSecret());
        aadB2cProperties.getProfile().setTenantId(global.getProfile().getTenantId());
        return aadB2cProperties;
    }
}
