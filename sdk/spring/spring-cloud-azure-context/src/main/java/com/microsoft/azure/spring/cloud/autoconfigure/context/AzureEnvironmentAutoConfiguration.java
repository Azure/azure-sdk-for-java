/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.context;

import com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.impl.DefaultEnvironmentProvider;
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

    @Autowired(required = false)
    private AzureProperties azureProperties;

    @Bean
    @ConditionalOnMissingBean
    public EnvironmentProvider environmentProvider() {
        DefaultEnvironmentProvider defaultEnvironmentProvider = new DefaultEnvironmentProvider();
        if (this.azureProperties != null) {
            defaultEnvironmentProvider.setEnvironment(azureProperties.getEnvironment());
        }

        return defaultEnvironmentProvider;
    }
}
