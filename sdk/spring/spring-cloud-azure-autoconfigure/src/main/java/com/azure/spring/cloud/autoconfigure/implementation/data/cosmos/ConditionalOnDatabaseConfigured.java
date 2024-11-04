// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(ConditionalOnDatabaseConfigured.ConditionalDatabaseConfiguredCondition.class)
@interface ConditionalOnDatabaseConfigured {

    class ConditionalDatabaseConfiguredCondition extends AnyNestedCondition {

        ConditionalDatabaseConfiguredCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "spring.cloud.azure.cosmos", name = "database")
        static class DataBasePropertyConfigured {
        }

        @ConditionalOnBean(type = "com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosConnectionDetails")
        static class ConnectionDetailBeanConfigured {
        }
    }
}
