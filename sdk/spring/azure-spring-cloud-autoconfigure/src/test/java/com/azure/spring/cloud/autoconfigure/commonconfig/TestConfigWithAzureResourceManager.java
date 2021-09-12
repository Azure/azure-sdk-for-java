// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.commonconfig;

import com.azure.resourcemanager.AzureResourceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class TestConfigWithAzureResourceManager {

    @Bean
    public AzureResourceManager azureResourceManager() {
        return mock(AzureResourceManager.class);
    }


}
