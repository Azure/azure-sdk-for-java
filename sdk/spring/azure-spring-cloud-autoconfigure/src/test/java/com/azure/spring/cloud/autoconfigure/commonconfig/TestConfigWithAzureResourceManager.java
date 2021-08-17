// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.commonconfig;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
@EnableConfigurationProperties(AzureContextProperties.class)
public class TestConfigWithAzureResourceManager {

    public static final String TEST_RESOURCE_GROUP = "test-rg";
    public static final String TEST_REGION = "test-region";

    @Bean
    public AzureResourceManager azureResourceManager() {
        return mock(AzureResourceManager.class);
    }

    @Bean
    public AzureResourceMetadata azureResourceMetadata() {
        final AzureResourceMetadata azureResourceMetadata = new AzureResourceMetadata();
        azureResourceMetadata.setResourceGroup(TEST_RESOURCE_GROUP);
        azureResourceMetadata.setRegion(TEST_REGION);
        return azureResourceMetadata;
    }

}
