// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context.condition;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnMissingProperty;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.ConfigurationCondition;

public class SpringTokenCredentialProviderContextCondition extends AnyNestedCondition {

    SpringTokenCredentialProviderContextCondition() {
        super(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(DataSourceProperties.class)
    @ConditionalOnClass(name = "com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate")
    static class JdbcPasswordlessCondition {

    }

    @ConditionalOnProperty(value = "spring.jms.servicebus.passwordless-enabled", havingValue = "true")
    @ConditionalOnMissingProperty(prefix = "spring.jms.servicebus", name = "connection-string")
    static class ServiceBusJmsPasswordlessCondition {

    }
}
