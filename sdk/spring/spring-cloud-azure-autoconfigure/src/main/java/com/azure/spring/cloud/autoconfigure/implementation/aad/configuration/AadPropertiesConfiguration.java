// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadResourceServerProperties;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
class AadPropertiesConfiguration {

    private final AzureGlobalProperties global;

    AadPropertiesConfiguration(AzureGlobalProperties global) {
        this.global = global;
    }

    @Bean
    @ConfigurationProperties(AadAuthenticationProperties.PREFIX)
    @ConditionalOnMissingBean
    AadAuthenticationProperties aadAuthenticationProperties() {
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

    @Bean
    @ConfigurationProperties(AadResourceServerProperties.PREFIX)
    @ConditionalOnMissingBean
    AadResourceServerProperties aadResourceServerProperties() {
        return new AadResourceServerProperties();
    }
}
