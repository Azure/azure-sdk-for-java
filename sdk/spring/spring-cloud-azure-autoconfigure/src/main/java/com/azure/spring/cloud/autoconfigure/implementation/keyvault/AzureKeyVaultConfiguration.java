// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault;

import com.azure.spring.cloud.autoconfigure.implementation.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.common.AzureKeyVaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Azure Key Vault support.
 *
 * @since 4.3.0
 */
@Configuration
public class AzureKeyVaultConfiguration extends AzureServiceConfigurationBase {

    AzureKeyVaultConfiguration(AzureGlobalProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(AzureKeyVaultProperties.PREFIX)
    AzureKeyVaultProperties azureKeyVaultProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureKeyVaultProperties());
    }

}
