// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.util.StringUtils;

class AzureServiceBusJmsPropertiesBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AzureServiceBusJmsProperties) {
            AzureServiceBusJmsProperties jmsProperties = (AzureServiceBusJmsProperties) bean;
            if (!StringUtils.hasText(jmsProperties.getConnectionString())) {
                ResolvableType providerType = ResolvableType.forClassWithGenerics(ServiceConnectionStringProvider.class, AzureServiceType.ServiceBus.class);
                ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders = applicationContext.getBeanProvider(providerType);
                connectionStringProviders.ifAvailable(provider -> jmsProperties.setConnectionString(provider.getConnectionString()));
            }
        }
        return bean;
    }
}
