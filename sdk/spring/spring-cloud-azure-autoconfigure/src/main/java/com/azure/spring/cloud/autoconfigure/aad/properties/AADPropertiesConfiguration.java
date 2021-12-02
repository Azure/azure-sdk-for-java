// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure Azure Active Directory related property beans
 */
@Configuration
@EnableConfigurationProperties
public class AADPropertiesConfiguration {

    /**
     * Azure Global Properties.
     */
    private final AzureGlobalProperties global;

    /**
     *
     * @param global Azure global properties.
     */
    public AADPropertiesConfiguration(AzureGlobalProperties global) {
        this.global = global;
    }

    @Bean
    @ConfigurationProperties(AADAuthenticationProperties.PREFIX)
    @ConditionalOnMissingBean
    public AADAuthenticationProperties aadAuthenticationProperties() {
        AADAuthenticationProperties aad = new AADAuthenticationProperties();
        aad.getProfile().setCloud(global.getProfile().getCloud());
        aad.getProfile().getEnvironment().setActiveDirectoryEndpoint(
            global.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        aad.getProfile().getEnvironment().setMicrosoftGraphEndpoint(
            global.getProfile().getEnvironment().getMicrosoftGraphEndpoint());
        aad.getCredential().setClientId(global.getCredential().getClientId());
        aad.getCredential().setClientSecret(global.getCredential().getClientSecret());
        aad.getProfile().setTenantId(global.getProfile().getTenantId());
        return aad;
    }

    @Bean
    @ConfigurationProperties(AADResourceServerProperties.PREFIX)
    @ConditionalOnMissingBean
    AADResourceServerProperties aadResourceServerProperties() {
        return new AADResourceServerProperties();
    }
}
