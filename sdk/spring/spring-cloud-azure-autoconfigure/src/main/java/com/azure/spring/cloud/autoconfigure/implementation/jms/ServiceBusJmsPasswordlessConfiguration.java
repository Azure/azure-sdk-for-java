// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.ServiceBusJmsConnectionFactoryCustomizer;
import org.apache.qpid.jms.JmsConnectionExtensions;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Service Bus JMS passwordless support.
 *
 * @since 4.7.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.jms.servicebus.passwordless-enabled", havingValue = "true")
class ServiceBusJmsPasswordlessConfiguration {

    @Bean
    @ConditionalOnMissingBean
    AzureServiceBusJmsCredentialSupplier azureServiceBusJmsCredentialSupplier(AzureServiceBusJmsProperties azureServiceBusJmsProperties) {
        return new AzureServiceBusJmsCredentialSupplier(azureServiceBusJmsProperties.toPasswordlessProperties());
    }

    @Bean
    ServiceBusJmsConnectionFactoryCustomizer jmsAADAuthenticationCustomizer(AzureServiceBusJmsCredentialSupplier credentialSupplier) {
        return factory -> {
            factory.setExtension(JmsConnectionExtensions.USERNAME_OVERRIDE.toString(), (connection, uri) -> "$jwt");
            factory.setExtension(JmsConnectionExtensions.PASSWORD_OVERRIDE.toString(), (connection, uri) ->
                    credentialSupplier.get()
            );
        };
    }


}
