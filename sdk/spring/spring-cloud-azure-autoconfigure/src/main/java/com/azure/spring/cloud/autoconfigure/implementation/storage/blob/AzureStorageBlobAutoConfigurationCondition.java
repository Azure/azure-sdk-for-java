// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.blob;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

public class AzureStorageBlobAutoConfigurationCondition  extends AllNestedConditions {

    AzureStorageBlobAutoConfigurationCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnClass(BlobServiceClientBuilder.class)
    static class ClasspathCondition {
    }

    @ConditionalOnProperty(value = { "spring.cloud.azure.storage.blob.enabled",  "spring.cloud.azure.storage.enabled" }, havingValue = "true", matchIfMissing = true)
    static class EnablePropertiesCondition {
    }

    @Conditional(PropertyOrConnectionDetailsCondition.class)
    static class ConnectionDetails {
    }

    static class PropertyOrConnectionDetailsCondition extends AnyNestedCondition {

        PropertyOrConnectionDetailsCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnAnyProperty(
            prefixes = { "spring.cloud.azure.storage.blob", "spring.cloud.azure.storage" },
            name = { "account-name", "endpoint", "connection-string" })
        static class PropertiesCondition {
        }

        @ConditionalOnBean(AzureStorageBlobConnectionDetails.class)
        static class ConnectionDetailsBeanCondition {
        }
    }
}
