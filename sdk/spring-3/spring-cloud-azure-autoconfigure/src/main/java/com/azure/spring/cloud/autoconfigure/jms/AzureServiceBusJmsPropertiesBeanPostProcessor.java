// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.StringUtils;

/**
 * {@link BeanPostProcessor} to apply a {@link ServiceConnectionStringProvider} to {@link AzureServiceBusJmsProperties}.
 */
class AzureServiceBusJmsPropertiesBeanPostProcessor implements BeanPostProcessor {

    private final ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders;

    AzureServiceBusJmsPropertiesBeanPostProcessor(ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders) {
        this.connectionStringProviders = connectionStringProviders;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AzureServiceBusJmsProperties) {
            AzureServiceBusJmsProperties jmsProperties = (AzureServiceBusJmsProperties) bean;
            if (!StringUtils.hasText(jmsProperties.getConnectionString())) {
                connectionStringProviders.ifAvailable(provider -> jmsProperties.setConnectionString(provider.getConnectionString()));
            }
        }
        return bean;
    }
}
