// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure Azure Active Directory related property beans
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public class AadPropertiesConfiguration {

    /**
     * Azure Global Properties.
     */
    private final AzureGlobalProperties global;

    /**
     *
     * @param global Azure global properties.
     */
    AadPropertiesConfiguration(AzureGlobalProperties global) {
        this.global = global;
    }

    /**
     * AAD Authentication Properties.
     *
     * @return AAd Authentication Properties Bean
     */
    @Bean
    @ConfigurationProperties(AadAuthenticationProperties.PREFIX)
    @ConditionalOnMissingBean
    public AadAuthenticationProperties aadAuthenticationProperties() {
        AadAuthenticationProperties aad = new AadAuthenticationProperties();
        aad.getProfile().setCloudType(global.getProfile().getCloudType());
        aad.getProfile().getEnvironment().setActiveDirectoryEndpoint(
            global.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        aad.getProfile().getEnvironment().setMicrosoftGraphEndpoint(
            global.getProfile().getEnvironment().getMicrosoftGraphEndpoint());
        aad.getCredential().setClientId(global.getCredential().getClientId());
        aad.getCredential().setClientSecret(global.getCredential().getClientSecret());
        aad.getCredential().setClientCertificatePath(global.getCredential().getClientCertificatePath());
        aad.getCredential().setClientCertificatePassword(global.getCredential().getClientCertificatePassword());
        aad.getProfile().setTenantId(global.getProfile().getTenantId());
        return aad;
    }


    /**
     * AAD Resource Server Properties.
     *
     * @return AAd Resource Server Properties Bean
     */
    @Bean
    @ConfigurationProperties(AadResourceServerProperties.PREFIX)
    @ConditionalOnMissingBean
    AadResourceServerProperties aadResourceServerProperties() {
        return new AadResourceServerProperties();
    }
}
