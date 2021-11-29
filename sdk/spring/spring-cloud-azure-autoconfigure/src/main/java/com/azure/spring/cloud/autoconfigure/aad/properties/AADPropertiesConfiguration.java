// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure properties for Azure Active Directory.
 */
@Configuration
@EnableConfigurationProperties
public class AADPropertiesConfiguration implements InitializingBean {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = AzureGlobalProperties.PREFIX)
    public AzureGlobalProperties azureGlobalProperties() {
        return new AzureGlobalProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = AADAuthenticationProperties.PREFIX)
    public AADAuthenticationProperties aadAuthenticationProperties() {
        return new AADAuthenticationProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = AADResourceServerProperties.PREFIX)
    public AADResourceServerProperties aadResourceServerProperties() {
        return new AADResourceServerProperties();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        aadAuthenticationProperties().setDefaultValueFromAzureGlobalProperties(azureGlobalProperties());
    }
}
