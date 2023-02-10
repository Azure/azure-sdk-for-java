// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzureServiceBusPasswordlessProperties;
import org.apache.qpid.jms.JmsConnectionExtensions;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Service Bus JMS passwordless support.
 *
 * @since 4.7.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.jms.servicebus.passwordless-enabled", havingValue = "true")
public class ServiceBusJmsPasswordlessConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.jms.servicebus")
    AzureServiceBusPasswordlessProperties serviceBusPasswordlessProperties() {
        return new AzureServiceBusPasswordlessProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    AzureServiceBusCredentialSupplier azureServiceBusCredentialSupplier(AzureGlobalProperties azureGlobalProperties, AzureServiceBusPasswordlessProperties serviceBusPasswordlessProperties) {
        Properties properties = mergeAzureProperties(azureGlobalProperties, serviceBusPasswordlessProperties).toProperties();
        return new AzureServiceBusCredentialSupplier(properties);
    }

    @Bean
    ServiceBusJmsConnectionFactoryCustomizer jmsAADAuthenticationCustomizer(AzureServiceBusCredentialSupplier credentialSupplier) {
        return factory -> {
            factory.setExtension(JmsConnectionExtensions.USERNAME_OVERRIDE.toString(), (connection, uri) -> "$jwt");
            factory.setExtension(JmsConnectionExtensions.PASSWORD_OVERRIDE.toString(), (connection, uri) ->
                    credentialSupplier.get()
            );
        };
    }

    private AzurePasswordlessProperties mergeAzureProperties(AzureGlobalProperties azureGlobalProperties, AzurePasswordlessProperties azurePasswordlessProperties) {
        AzurePasswordlessProperties mergedProperties = new AzurePasswordlessProperties();
        AzurePropertiesUtils.mergeAzureCommonProperties(azureGlobalProperties, azurePasswordlessProperties, mergedProperties);
        mergedProperties.setScopes(azurePasswordlessProperties.getScopes());
        return mergedProperties;
    }
}
