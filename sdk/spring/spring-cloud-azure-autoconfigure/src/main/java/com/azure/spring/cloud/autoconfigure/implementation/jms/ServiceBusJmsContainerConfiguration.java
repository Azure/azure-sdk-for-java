// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import jakarta.jms.ConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jms.autoconfigure.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.jms.autoconfigure.JmsPoolConnectionFactoryFactory;
import org.springframework.boot.jms.autoconfigure.JmsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

import java.util.stream.Collectors;

import static com.azure.spring.cloud.autoconfigure.implementation.jms.ServiceBusJmsConnectionFactoryConfiguration.CACHE;
import static com.azure.spring.cloud.autoconfigure.implementation.jms.ServiceBusJmsConnectionFactoryConfiguration.POOL;
import static com.azure.spring.cloud.autoconfigure.implementation.jms.ServiceBusJmsConnectionFactoryConfiguration.SERVICE_BUS;
import static com.azure.spring.cloud.autoconfigure.implementation.jms.ServiceBusJmsConnectionFactoryConfiguration.Registrar.getFactoryType;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableJms.class)
class ServiceBusJmsContainerConfiguration {

    /**
     * <p>
     * The ConnectionFactory type is determined by the following table:
     * <table border="1">
     *   <tr>
     *     <th>spring.jms.servicebus.pool.enabled</th>
     *     <th>spring.jms.cache.enabled</th>
     *     <th>Receiver ConnectionFactory</th>
     *   </tr>
     *   <tr><td>not set</td><td>not set</td><td>ServiceBusJmsConnectionFactory</td></tr>
     *   <tr><td>not set</td><td>true</td><td>CachingConnectionFactory</td></tr>
     *   <tr><td>not set</td><td>false</td><td>ServiceBusJmsConnectionFactory</td></tr>
     *   <tr><td>true</td><td>not set</td><td>JmsPoolConnectionFactory</td></tr>
     *   <tr><td>true</td><td>true</td><td>CachingConnectionFactory</td></tr>
     *   <tr><td>true</td><td>false</td><td>JmsPoolConnectionFactory</td></tr>
     *   <tr><td>false</td><td>not set</td><td>ServiceBusJmsConnectionFactory</td></tr>
     *   <tr><td>false</td><td>true</td><td>CachingConnectionFactory</td></tr>
     *   <tr><td>false</td><td>false</td><td>ServiceBusJmsConnectionFactory</td></tr>
     * </table>
     */
    private static final int[][] DECISION_TABLE = {
        // pool: not set
        {SERVICE_BUS, CACHE, SERVICE_BUS}, // cache: not set, true, false
        // pool: true
        {POOL, CACHE, POOL}, // cache: not set, true, false
        // pool: false
        {SERVICE_BUS, CACHE, SERVICE_BUS} // cache: not set, true, false
    };

    private final AzureServiceBusJmsProperties azureServiceBusJMSProperties;
    private final ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers;
    private final Environment environment;
    private final JmsProperties jmsProperties;

    ServiceBusJmsContainerConfiguration(AzureServiceBusJmsProperties azureServiceBusJMSProperties,
                                        ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers,
                                        Environment environment,
                                        JmsProperties jmsProperties) {
        this.azureServiceBusJMSProperties = azureServiceBusJMSProperties;
        this.factoryCustomizers = factoryCustomizers;
        this.environment = environment;
        this.jmsProperties = jmsProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    JmsListenerContainerFactory<?> jmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        // Use the bean ConnectionFactory if it's pooled or cached, otherwise create a dedicated one for receiver
        ConnectionFactory receiverConnectionFactory = getReceiverConnectionFactory(connectionFactory);
        configurer.configure(jmsListenerContainerFactory, receiverConnectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.FALSE);
        configureCommonListenerContainerFactory(jmsListenerContainerFactory);
        return jmsListenerContainerFactory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "topicJmsListenerContainerFactory")
    JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        // Use the bean ConnectionFactory if it's pooled or cached, otherwise create a dedicated one for receiver
        ConnectionFactory receiverConnectionFactory = getReceiverConnectionFactory(connectionFactory);
        configurer.configure(jmsListenerContainerFactory, receiverConnectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.TRUE);
        configureCommonListenerContainerFactory(jmsListenerContainerFactory);
        configureTopicListenerContainerFactory(jmsListenerContainerFactory);
        return jmsListenerContainerFactory;
    }

    /**
     * Determines the appropriate ConnectionFactory for JMS listener containers based on configuration properties.
     *
     * @param connectionFactory the ConnectionFactory bean registered by {@link ServiceBusJmsConnectionFactoryConfiguration}
     * @return the ConnectionFactory to use for the receiver
     */
    private ConnectionFactory getReceiverConnectionFactory(ConnectionFactory connectionFactory) {
        BindResult<Boolean> poolEnabledResult = Binder.get(environment).bind("spring.jms.servicebus.pool.enabled", Boolean.class);
        BindResult<Boolean> cacheEnabledResult = Binder.get(environment).bind("spring.jms.cache.enabled", Boolean.class);

        switch (getFactoryType(poolEnabledResult, cacheEnabledResult, DECISION_TABLE)) {
            case POOL:
                if (connectionFactory instanceof JmsPoolConnectionFactory) {
                    return connectionFactory;
                } else {
                    return new JmsPoolConnectionFactoryFactory(azureServiceBusJMSProperties.getPool())
                        .createPooledConnectionFactory(createServiceBusJmsConnectionFactory());
                }
            case CACHE:
                if (connectionFactory instanceof CachingConnectionFactory) {
                    return connectionFactory;
                } else {
                    CachingConnectionFactory cacheFactory = new CachingConnectionFactory(createServiceBusJmsConnectionFactory());
                    JmsProperties.Cache cacheProperties = jmsProperties.getCache();
                    cacheFactory.setCacheConsumers(cacheProperties.isConsumers());
                    cacheFactory.setCacheProducers(cacheProperties.isProducers());
                    cacheFactory.setSessionCacheSize(cacheProperties.getSessionCacheSize());
                    return cacheFactory;
                }
            default:
                return createServiceBusJmsConnectionFactory();
        }
    }

    private ServiceBusJmsConnectionFactory createServiceBusJmsConnectionFactory() {
        return new ServiceBusJmsConnectionFactoryFactory(azureServiceBusJMSProperties,
            factoryCustomizers.orderedStream().collect(Collectors.toList()))
            .createConnectionFactory(ServiceBusJmsConnectionFactory.class);
    }

    private void configureCommonListenerContainerFactory(DefaultJmsListenerContainerFactory jmsListenerContainerFactory) {
        AzureServiceBusJmsProperties.Listener listener = azureServiceBusJMSProperties.getListener();
        if (listener.getReplyQosSettings() != null) {
            jmsListenerContainerFactory.setReplyQosSettings(listener.getReplyQosSettings());
        }
        if (listener.getPhase() != null) {
            jmsListenerContainerFactory.setPhase(listener.getPhase());
        }
    }

    private void configureTopicListenerContainerFactory(DefaultJmsListenerContainerFactory jmsListenerContainerFactory) {
        AzureServiceBusJmsProperties.Listener listener = azureServiceBusJMSProperties.getListener();
        if (listener.isReplyPubSubDomain() != null) {
            jmsListenerContainerFactory.setReplyPubSubDomain(listener.isReplyPubSubDomain());
        }
        if (listener.isSubscriptionDurable() != null) {
            jmsListenerContainerFactory.setSubscriptionDurable(listener.isSubscriptionDurable());
        }
        if (listener.isSubscriptionShared() != null) {
            jmsListenerContainerFactory.setSubscriptionShared(listener.isSubscriptionShared());
        }
    }
}
