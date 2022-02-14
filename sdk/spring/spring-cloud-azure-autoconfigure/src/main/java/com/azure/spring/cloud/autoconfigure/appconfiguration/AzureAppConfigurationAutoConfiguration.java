// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.appconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.Utils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * Autoconfiguration for a {@link ConfigurationClientBuilder} and Azure App Configuration clients.
 */
@ConditionalOnClass(ConfigurationClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.appconfiguration.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.appconfiguration", name = {"endpoint", "connection-string"})
public class AzureAppConfigurationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ConfigurationClient azureConfigurationClient(ConfigurationClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    ConfigurationClientBuilder configurationClientBuilder(ConfigurationBuilder configurationBuilder,
                                                          @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                          Optional<AzureServiceClientBuilderCustomizer<ConfigurationClientBuilder>> builderCustomizer) {
        return Utils.configureBuilder(
            new ConfigurationClientBuilder(),
            configurationBuilder.buildSection("appconfiguration"),
            defaultTokenCredential,
            builderCustomizer);
    }
}
