// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Common configuration for Azure Storage support.
 *
 * @since 4.3.0
 */
@Configuration
public class AzureStorageConfiguration extends AzureServiceConfigurationBase {

    AzureStorageConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageProperties.PREFIX)
    AzureStorageProperties azureStorageProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureStorageProperties());
    }

}
