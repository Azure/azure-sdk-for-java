// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.spring.data.cosmos.common.DynamicContainer;
import com.azure.spring.data.cosmos.common.TestConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRepositorySpELConfig extends TestRepositoryConfig {

    @Bean
    public DynamicContainer dynamicContainer() {
        return new DynamicContainer(TestConstants.DYNAMIC_BEAN_COLLECTION_NAME);
    }

}
