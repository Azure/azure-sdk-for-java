// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class AzureCosmosConnectionDetailsCondition extends AnyNestedCondition {

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
