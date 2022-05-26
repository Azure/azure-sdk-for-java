// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.environment;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.common.AzureKeyVaultGlobalProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureKeyVaultConfiguration extends AzureServiceConfigurationBase {

    AzureKeyVaultConfiguration(AzureGlobalProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(AzureKeyVaultGlobalProperties.PREFIX)
    AzureKeyVaultGlobalProperties storageProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureKeyVaultGlobalProperties());
    }

}
