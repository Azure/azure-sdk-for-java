// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.AZURE_GLOBAL_PROPERTY_BEAN_NAME;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link AzureGlobalProperties}.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding(AzureGlobalProperties.class)
public class AzureGlobalPropertiesAutoConfiguration {

    @Bean(name = AZURE_GLOBAL_PROPERTY_BEAN_NAME)
    @ConditionalOnMissingBean(name = AZURE_GLOBAL_PROPERTY_BEAN_NAME)
    static AzureGlobalProperties azureGlobalProperties(Environment environment) {
        return Binder.get(environment).bindOrCreate(AzureGlobalProperties.PREFIX, AzureGlobalProperties.class);
    }
}
