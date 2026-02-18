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
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jms.autoconfigure.JmsPoolConnectionFactoryFactory;
import org.springframework.boot.jms.autoconfigure.JmsProperties;
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
    static final int NOT_CONFIGURED = 0;
    static final int TRUE = 1;
    static final int FALSE = 2;
    static final int POOL = 0;
    static final int CACHE = 1;
    static final int SERVICE_BUS = 2;

    /**
     * Creates a ServiceBusJmsConnectionFactory using the provided properties and customizers.
     * This is a shared helper method used by both sender and receiver configurations.
     *
     * @param properties the Azure Service Bus JMS properties
     * @param customizers the list of customizers to apply
     * @return a configured ServiceBusJmsConnectionFactory instance
     */
    static ServiceBusJmsConnectionFactory createServiceBusJmsConnectionFactory(
        AzureServiceBusJmsProperties properties,
        java.util.List<AzureServiceBusJmsConnectionFactoryCustomizer> customizers) {
        return new ServiceBusJmsConnectionFactoryFactory(properties, customizers)
            .createConnectionFactory(ServiceBusJmsConnectionFactory.class);
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
     */
    private static final int[][] DECISION_TABLE = {
        // pool: not set
        {CACHE, CACHE, SERVICE_BUS}, // cache: not set, true, false
        // pool: true
        {POOL, CACHE, POOL}, // cache: not set, true, false
        // pool: false
        {CACHE, CACHE, SERVICE_BUS} // cache: not set, true, false
    };

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

            switch (getFactoryType(poolEnabledResult, cacheEnabledResult, DECISION_TABLE)) {
                case POOL:
                    registerJmsPoolConnectionFactory(registry);
                    break;
                case CACHE:
                    registerJmsCachingConnectionFactory(registry);
                    break;
                default:
                    registerServiceBusJmsConnectionFactory(registry);
            }
        }

        static int getFactoryType(BindResult<Boolean> poolEnabledResult, BindResult<Boolean> cacheEnabledResult, int[][] decisionTable) {
            int poolIndex = NOT_CONFIGURED;
            if (poolEnabledResult.isBound()) {
                poolIndex = poolEnabledResult.get() ? TRUE : FALSE;
            }
            int cacheIndex = NOT_CONFIGURED;
            if (cacheEnabledResult.isBound()) {
                cacheIndex = cacheEnabledResult.get() ? TRUE : FALSE;
            }
            int configuredFactoryType = decisionTable[poolIndex][cacheIndex];
            switch (configuredFactoryType) {
                case POOL:
                    if (isPoolConnectionFactoryClassPresent()) {
                        return POOL;
                    } else {
                        return SERVICE_BUS;
                    }
                case CACHE:
                    if (isCacheConnectionFactoryClassPresent()) {
                        return CACHE;
                    } else {
                        return SERVICE_BUS;
                    }
                default:
                    return SERVICE_BUS;
            }
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
            return ServiceBusJmsConnectionFactoryConfiguration.createServiceBusJmsConnectionFactory(
                serviceBusJmsProperties,
                factoryCustomizers.orderedStream().collect(Collectors.toList()));
        }
    }
}
