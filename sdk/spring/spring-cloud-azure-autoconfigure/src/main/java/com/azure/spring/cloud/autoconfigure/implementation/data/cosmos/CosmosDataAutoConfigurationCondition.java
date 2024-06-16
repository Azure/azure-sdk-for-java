// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.cosmos.AzureCosmosAutoConfigurationCondition;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

public class CosmosDataAutoConfigurationCondition extends AllNestedConditions {

    CosmosDataAutoConfigurationCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnClass(CosmosTemplate.class)
    static class ClassPathCondition {
    }

    @ConditionalOnProperty(prefix = "spring.cloud.azure.cosmos", name = "database")
    static class PropertiesCondition {
    }

    @Conditional(AzureCosmosAutoConfigurationCondition.class)
    static class CosmosAutoConfigurationCondition {
    }
}
