// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import jakarta.jms.ConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryFactory;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.util.ClassUtils;

import java.util.stream.Collectors;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * Configuration for {@link ConnectionFactory}.
 *
 * @since 5.19.0
 */
@Import(ServiceBusJmsConnectionFactoryConfiguration.Registrar.class)
class ServiceBusJmsConnectionFactoryConfiguration {

    static class Registrar implements BeanFactoryAware, EnvironmentAware, ImportBeanDefinitionRegistrar {

        private Environment environment;
        private BeanFactory beanFactory;
        private static final String JMS_CONNECTION_FACTORY_BEAN_NAME = "jmsConnectionFactory";
        private static final String JMS_POOL_CONNECTION_FACTORY_BEAN_NAME = "jmsPoolConnectionFactory";

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
            BindResult<Boolean> poolEnabledResult = Binder.get(environment).bind("spring.jms.servicebus.pool.enabled", Boolean.class);
            BindResult<Boolean> cacheEnabledResult = Binder.get(environment).bind("spring.jms.cache.enabled", Boolean.class);

            if (isPoolConnectionFactoryClassPresent()
                && ((!cacheEnabledResult.isBound() && !poolEnabledResult.isBound())) || poolEnabledResult.orElseGet(() -> false)) {
                registerJmsPoolConnectionFactory(registry);
                return;
            }

            if (isCacheConnectionFactoryClassPresent() && (!cacheEnabledResult.isBound() || cacheEnabledResult.orElseGet(() -> false))) {
                registerJmsCacheConnectionFactory(registry);
                return;
            }

            if (cacheEnabledResult.isBound() && !cacheEnabledResult.orElseGet(() -> false)) {
                registerJmsConnectionFactory(registry);
            }
        }

        private static boolean isCacheConnectionFactoryClassPresent() {
            return ClassUtils.isPresent("org.springframework.jms.connection.CachingConnectionFactory", null);
        }

        private static boolean isPoolConnectionFactoryClassPresent() {
            return ClassUtils.isPresent("org.messaginghub.pooled.jms.JmsPoolConnectionFactory", null)
                && ClassUtils.isPresent("org.apache.commons.pool2.PooledObject", null);
        }

        private void registerJmsConnectionFactory(BeanDefinitionRegistry registry) {
            BeanDefinitionBuilder definitionBuilder = genericBeanDefinition(ServiceBusJmsConnectionFactory.class,
                () -> createJmsConnectionFactory(beanFactory));
            registry.registerBeanDefinition(JMS_CONNECTION_FACTORY_BEAN_NAME, definitionBuilder.getBeanDefinition());
        }

        private void registerJmsCacheConnectionFactory(BeanDefinitionRegistry registry) {
            BeanDefinitionBuilder definitionBuilder = genericBeanDefinition(CachingConnectionFactory.class,
                () -> {
                    JmsProperties jmsProperties = beanFactory.getBean(JmsProperties.class);
                    ServiceBusJmsConnectionFactory factory = createJmsConnectionFactory(beanFactory);
                    CachingConnectionFactory connectionFactory = new CachingConnectionFactory(factory);
                    JmsProperties.Cache cacheProperties = jmsProperties.getCache();
                    connectionFactory.setCacheConsumers(cacheProperties.isConsumers());
                    connectionFactory.setCacheProducers(cacheProperties.isProducers());
                    connectionFactory.setSessionCacheSize(cacheProperties.getSessionCacheSize());
                    return connectionFactory;
                });
            registry.registerBeanDefinition(JMS_CONNECTION_FACTORY_BEAN_NAME, definitionBuilder.getBeanDefinition());
        }

        private void registerJmsPoolConnectionFactory(BeanDefinitionRegistry registry) {
            BeanDefinitionBuilder definitionBuilder = genericBeanDefinition(JmsPoolConnectionFactory.class,
                () -> {
                    AzureServiceBusJmsProperties serviceBusJmsProperties = beanFactory.getBean(AzureServiceBusJmsProperties.class);
                    ServiceBusJmsConnectionFactory factory = createJmsConnectionFactory(beanFactory, serviceBusJmsProperties);
                    return new JmsPoolConnectionFactoryFactory(serviceBusJmsProperties.getPool())
                        .createPooledConnectionFactory(factory);
                });
            definitionBuilder.setDestroyMethodName("stop");
            registry.registerBeanDefinition(JMS_POOL_CONNECTION_FACTORY_BEAN_NAME, definitionBuilder.getBeanDefinition());
        }

        private ServiceBusJmsConnectionFactory createJmsConnectionFactory(BeanFactory beanFactory) {
            AzureServiceBusJmsProperties serviceBusJmsProperties = beanFactory.getBean(AzureServiceBusJmsProperties.class);
            ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers = beanFactory.getBeanProvider(AzureServiceBusJmsConnectionFactoryCustomizer.class);
            return createJmsConnectionFactory(serviceBusJmsProperties, factoryCustomizers);
        }

        private ServiceBusJmsConnectionFactory createJmsConnectionFactory(BeanFactory beanFactory,
                                                                          AzureServiceBusJmsProperties serviceBusJmsProperties) {
            ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers = beanFactory.getBeanProvider(AzureServiceBusJmsConnectionFactoryCustomizer.class);
            return createJmsConnectionFactory(serviceBusJmsProperties, factoryCustomizers);
        }

        private ServiceBusJmsConnectionFactory createJmsConnectionFactory(AzureServiceBusJmsProperties serviceBusJmsProperties,
                                                                          ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
            return new ServiceBusJmsConnectionFactoryFactory(serviceBusJmsProperties,
                factoryCustomizers.orderedStream().collect(Collectors.toList()))
                .createConnectionFactory(ServiceBusJmsConnectionFactory.class);
        }
    }
}
