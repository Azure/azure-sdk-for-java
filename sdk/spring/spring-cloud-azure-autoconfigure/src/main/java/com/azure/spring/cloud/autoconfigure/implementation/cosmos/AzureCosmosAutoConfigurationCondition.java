// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos;

import com.azure.cosmos.CosmosClientBuilder;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

public class AzureCosmosAutoConfigurationCondition extends AllNestedConditions {

    AzureCosmosAutoConfigurationCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnClass(CosmosClientBuilder.class)
    static class ClassPathCondition {
    }

    @ConditionalOnProperty(value = "spring.cloud.azure.cosmos.enabled", havingValue = "true", matchIfMissing = true)
    static class EnablePropertyCondition {
    }

    @Conditional(AzureCosmosConnectionDetailsCondition.class)
    static class ConnectionDetailsCondition {
    }

    static class AzureCosmosConnectionDetailsCondition extends AnyNestedCondition {

        AzureCosmosConnectionDetailsCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "spring.cloud.azure.cosmos", name = "endpoint")
        static class PropertiesCondition {
        }

        @ConditionalOnBean(AzureCosmosConnectionDetails.class)
        static class ConnectionDetailsBeanCondition {
        }
    }
}
