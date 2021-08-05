// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.DefaultEnvironmentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-config to provide default {@link EnvironmentProvider} for all Azure services
 *
 * @author Warren Zhu
 */
@Configuration
public class AzureEnvironmentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EnvironmentProvider environmentProvider(@Autowired(required = false) AzureProperties azureProperties) {
        DefaultEnvironmentProvider defaultEnvironmentProvider = new DefaultEnvironmentProvider();

        if (azureProperties != null) {
            defaultEnvironmentProvider.setEnvironment(azureProperties.getEnvironment().getAzureEnvironment());
        }

        return defaultEnvironmentProvider;
    }

}
