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
import org.springframework.boot.jms.autoconfigure.JmsPoolConnectionFactoryFactory;
import org.springframework.boot.jms.autoconfigure.JmsProperties;
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

        /**
         * Registers the appropriate ConnectionFactory bean based on configuration properties.
         * <p>
         * The ConnectionFactory type is determined by the following table:
         * <table border="1">
         *   <tr>
         *     <th>spring.jms.servicebus.pool.enabled</th>
         *     <th>spring.jms.cache.enabled</th>
         *     <th>Sender ConnectionFactory</th>
         *   </tr>
         *   <tr><td>not set</td><td>not set</td><td>CachingConnectionFactory</td></tr>
         *   <tr><td>not set</td><td>true</td><td>CachingConnectionFactory</td></tr>
         *   <tr><td>not set</td><td>false</td><td>ServiceBusJmsConnectionFactory</td></tr>
         *   <tr><td>true</td><td>not set</td><td>JmsPoolConnectionFactory</td></tr>
         *   <tr><td>true</td><td>true</td><td>CachingConnectionFactory</td></tr>
         *   <tr><td>true</td><td>false</td><td>JmsPoolConnectionFactory</td></tr>
         *   <tr><td>false</td><td>not set</td><td>CachingConnectionFactory</td></tr>
         *   <tr><td>false</td><td>true</td><td>CachingConnectionFactory</td></tr>
         *   <tr><td>false</td><td>false</td><td>ServiceBusJmsConnectionFactory</td></tr>
         * </table>
         * <p>
         * When using CachingConnectionFactory or JmsPoolConnectionFactory, the related class must be in the classpath.
         * If not present, it falls back to ServiceBusJmsConnectionFactory.
         */
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
            BindResult<Boolean> poolEnabledResult = Binder.get(environment).bind("spring.jms.servicebus.pool.enabled", Boolean.class);
            BindResult<Boolean> cacheEnabledResult = Binder.get(environment).bind("spring.jms.cache.enabled", Boolean.class);

            // Case 3: If cache.enabled is explicitly false, check pool.enabled
            if (cacheEnabledResult.isBound() && !cacheEnabledResult.get()) {
                // If pool.enabled is true, use JmsPoolConnectionFactory (or fallback if not present)
                if (poolEnabledResult.isBound() && poolEnabledResult.get()) {
                    if (isPoolConnectionFactoryClassPresent()) {
                        registerJmsPoolConnectionFactory(registry);
                        return;
                    }
                    // Fallback: pool requested but classes not available
                    registerServiceBusJmsConnectionFactory(registry);
                    return;
                }
                // Otherwise use ServiceBusJmsConnectionFactory
                registerServiceBusJmsConnectionFactory(registry);
                return;
            }

            // Case 2: If cache.enabled is true (explicitly or by default when both true)
            if (cacheEnabledResult.isBound() && cacheEnabledResult.get()) {
                if (isCacheConnectionFactoryClassPresent()) {
                    registerJmsCachingConnectionFactory(registry);
                    return;
                }
                // Fallback: cache requested but class not available
                registerServiceBusJmsConnectionFactory(registry);
                return;
            }

            // Case 1: If pool.enabled is true and cache is not set
            if (poolEnabledResult.isBound() && poolEnabledResult.get()) {
                if (isPoolConnectionFactoryClassPresent()) {
                    registerJmsPoolConnectionFactory(registry);
                    return;
                }
                // Fallback: pool requested but classes not available
                registerServiceBusJmsConnectionFactory(registry);
                return;
            }

            // Case 4: Default - neither property is set or pool.enabled is false
            // Use CachingConnectionFactory as default
            if (isCacheConnectionFactoryClassPresent()) {
                registerJmsCachingConnectionFactory(registry);
                return;
            }

            // Fallback if CachingConnectionFactory is not in classpath
            registerServiceBusJmsConnectionFactory(registry);
        }

        private static boolean isCacheConnectionFactoryClassPresent() {
            return ClassUtils.isPresent("org.springframework.jms.connection.CachingConnectionFactory", null);
        }

        private static boolean isPoolConnectionFactoryClassPresent() {
            return ClassUtils.isPresent("org.messaginghub.pooled.jms.JmsPoolConnectionFactory", null)
                && ClassUtils.isPresent("org.apache.commons.pool2.PooledObject", null);
        }

        private void registerServiceBusJmsConnectionFactory(BeanDefinitionRegistry registry) {
            BeanDefinitionBuilder definitionBuilder = genericBeanDefinition(ServiceBusJmsConnectionFactory.class,
                this::createServiceBusJmsConnectionFactory);
            registry.registerBeanDefinition(JMS_CONNECTION_FACTORY_BEAN_NAME, definitionBuilder.getBeanDefinition());
        }

        private void registerJmsCachingConnectionFactory(BeanDefinitionRegistry registry) {
            BeanDefinitionBuilder definitionBuilder = genericBeanDefinition(CachingConnectionFactory.class,
                () -> {
                    CachingConnectionFactory connectionFactory = new CachingConnectionFactory(createServiceBusJmsConnectionFactory());
                    JmsProperties jmsProperties = beanFactory.getBean(JmsProperties.class);
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
                    return new JmsPoolConnectionFactoryFactory(serviceBusJmsProperties.getPool())
                        .createPooledConnectionFactory(createServiceBusJmsConnectionFactory());
                });
            definitionBuilder.setDestroyMethodName("stop");
            registry.registerBeanDefinition(JMS_POOL_CONNECTION_FACTORY_BEAN_NAME, definitionBuilder.getBeanDefinition());
        }

        private ServiceBusJmsConnectionFactory createServiceBusJmsConnectionFactory() {
            AzureServiceBusJmsProperties serviceBusJmsProperties = beanFactory.getBean(AzureServiceBusJmsProperties.class);
            ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers = beanFactory.getBeanProvider(AzureServiceBusJmsConnectionFactoryCustomizer.class);
            return new ServiceBusJmsConnectionFactoryFactory(serviceBusJmsProperties,
                factoryCustomizers.orderedStream().collect(Collectors.toList()))
                .createConnectionFactory(ServiceBusJmsConnectionFactory.class);
        }
    }
}
