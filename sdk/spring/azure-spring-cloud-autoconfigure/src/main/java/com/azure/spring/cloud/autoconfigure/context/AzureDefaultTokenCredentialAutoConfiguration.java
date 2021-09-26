// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * Auto-configuration for Azure Spring default token credential.
 */
@Configuration
public class AzureDefaultTokenCredentialAutoConfiguration {

    private final AzureGlobalProperties azureGlobalProperties;

    public AzureDefaultTokenCredentialAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @Bean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @ConditionalOnMissingBean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @Order
    public TokenCredential azureTokenCredential(AzureCredentialBuilderFactory<DefaultAzureCredentialBuilder> factory) {
        return factory.build().build();
    }

    @Bean
    public AzureCredentialBuilderFactory<DefaultAzureCredentialBuilder> defaultAzureCredentialBuilderFactory() {
        return new AzureCredentialBuilderFactory<>(azureGlobalProperties, new DefaultAzureCredentialBuilder());
    }

}
