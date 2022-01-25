// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.implementation.connectionstring.ServiceBusConnectionString;
import com.azure.spring.core.service.AzureServiceType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * {@link BeanPostProcessor} to apply a {@link ConnectionStringProvider} to
 * {@link AzureServiceBusJmsProperties}.
 */
class AzureServiceBusJmsPropertiesBeanPostProcessor implements BeanPostProcessor {

    private final ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders;

    AzureServiceBusJmsPropertiesBeanPostProcessor(ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders) {
        this.connectionStringProviders = connectionStringProviders;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AzureServiceBusJmsProperties) {
            AzureServiceBusJmsProperties jmsProperties = (AzureServiceBusJmsProperties) bean;
            connectionStringProviders.ifAvailable(provider -> jmsProperties.setConnectionString(provider.getConnectionString()));

            String connectionString = jmsProperties.getConnectionString();
            ServiceBusConnectionString serviceBusConnectionString =
                new ServiceBusConnectionString(connectionString);
            String host = serviceBusConnectionString.getEndpointUri().getHost();
            String remoteUrl = String.format(AzureServiceBusJmsProperties.AMQP_URI_FORMAT, host,
                jmsProperties.getIdleTimeout().toMillis());
            String username = serviceBusConnectionString.getSharedAccessKeyName();
            String password = serviceBusConnectionString.getSharedAccessKey();

            jmsProperties.setRemoteUrl(remoteUrl);
            jmsProperties.setUsername(username);
            jmsProperties.setPassword(password);
        }
        return bean;
    }
}
