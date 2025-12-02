// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.connection.CachingConnectionFactory;

import static com.azure.spring.cloud.autoconfigure.implementation.util.TestServiceBusUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusJmsConnectionFactoryConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
        .withPropertyValues(
            "spring.jms.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
        )
        .withConfiguration(AutoConfigurations.of(JmsAutoConfiguration.class,
            ServiceBusJmsAutoConfiguration.class));

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void useDefaultPoolConnection(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier
            )
            .run(context -> {
                assertThat(context).hasSingleBean(JmsPoolConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void enablePoolConnection(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.pool.enabled=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(JmsPoolConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "org.messaginghub.pooled.jms.JmsPoolConnectionFactory", "org.apache.commons.pool2.PooledObject" })
    void poolEnabledButNoPoolClasses(String poolClass) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(poolClass))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=premium",
                "spring.jms.servicebus.pool.enabled=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "org.messaginghub.pooled.jms.JmsPoolConnectionFactory", "org.apache.commons.pool2.PooledObject" })
    void fallbackUseCachingConnectionDueNoPoolClasses(String poolClass) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(poolClass))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=premium"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void useCacheConnection(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.pool.enabled=false"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void fallbackUseDefaultConnectionDueNoPoolAndCachingClasses(String pricingTier) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(
                "org.apache.commons.pool2.PooledObject",
                "org.messaginghub.pooled.jms.JmsPoolConnectionFactory",
                "org.springframework.jms.connection.CachingConnectionFactory"
            ))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier
            )
            .run(context -> {
                assertThat(context).doesNotHaveBean(JmsPoolConnectionFactory.class);
                assertThat(context).doesNotHaveBean(CachingConnectionFactory.class);
                assertThat(context).hasSingleBean(ServiceBusJmsConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void useServiceBusJmsConnection(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.pool.enabled=false",
                "spring.jms.cache.enabled=false"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusJmsConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void useCacheConnectionViaAdditionConfigurationFile(String pricingTier) {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(AdditionalPropertySourceConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
            });
    }

    @Test
    void cachingConnectionFactoryReusesSameProducerForSameDestination() throws Exception {
        // Create mock objects for JMS components
        ConnectionFactory mockTargetConnectionFactory = mock(ConnectionFactory.class);
        Connection mockConnection = mock(Connection.class);
        // Create a mock inner session that ServiceBusJmsSession will wrap
        Session mockInnerSession = mock(Session.class);
        // Create mock inner queues that the inner session returns
        Queue mockInnerQueue1 = mock(Queue.class);
        Queue mockInnerQueue2 = mock(Queue.class);
        Queue mockInnerQueue3 = mock(Queue.class);
        when(mockInnerQueue1.getQueueName()).thenReturn("queue1");
        when(mockInnerQueue2.getQueueName()).thenReturn("queue1");
        when(mockInnerQueue3.getQueueName()).thenReturn("queue2");
        when(mockInnerSession.createQueue("queue1"))
            .thenReturn(mockInnerQueue1)
            .thenReturn(mockInnerQueue2);
        when(mockInnerSession.createQueue("queue2")).thenReturn(mockInnerQueue3);

        // Create ServiceBusJmsSession using reflection (constructor is package-private)
        // ServiceBusJmsSession.createQueue() wraps the inner Queue in ServiceBusJmsQueue
        // which has the proper toString() implementation in azure-servicebus-jms 2.1.0
        Session serviceBusJmsSession = createServiceBusJmsSession(mockInnerSession);
        
        // Create ServiceBusJmsQueue instances through ServiceBusJmsSession
        Queue serviceBusQueue1FirstCall = serviceBusJmsSession.createQueue("queue1");
        Queue serviceBusQueue1SecondCall = serviceBusJmsSession.createQueue("queue1");
        Queue serviceBusQueue2 = serviceBusJmsSession.createQueue("queue2");

        MessageProducer mockProducer1 = mock(MessageProducer.class);
        MessageProducer mockProducer2 = mock(MessageProducer.class);
        MessageProducer mockProducer3 = mock(MessageProducer.class);

        // Setup mock behavior for connection and session
        when(mockTargetConnectionFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.createSession(anyBoolean(), anyInt())).thenReturn(serviceBusJmsSession);
        // Each ServiceBusJmsQueue instance gets a different producer from the underlying session
        // CachingConnectionFactory should cache based on destination.toString()
        when(mockInnerSession.createProducer(serviceBusQueue1FirstCall)).thenReturn(mockProducer1);
        when(mockInnerSession.createProducer(serviceBusQueue1SecondCall)).thenReturn(mockProducer2);
        when(mockInnerSession.createProducer(serviceBusQueue2)).thenReturn(mockProducer3);

        // Create CachingConnectionFactory with caching enabled
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(mockTargetConnectionFactory);
        cachingConnectionFactory.setCacheProducers(true);

        // Get connection and session
        Connection connection = cachingConnectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create queues - these are ServiceBusJmsQueue instances
        Queue queue1FirstCall = session.createQueue("queue1");
        Queue queue1SecondCall = session.createQueue("queue1");
        Queue queue2 = session.createQueue("queue2");

        // First call: create producer for queue1 (first ServiceBusJmsQueue instance)
        MessageProducer producer1ForQueue1 = session.createProducer(queue1FirstCall);
        // Second call: create producer for queue1 (second ServiceBusJmsQueue instance)
        // In azure-servicebus-jms 2.1.0, this returns cached producer because toString() is consistent
        // In azure-servicebus-jms 2.0.0, this would return a different producer because toString() was unique
        MessageProducer producer2ForQueue1 = session.createProducer(queue1SecondCall);
        // Third call: create producer for different queue2 - should return different producer
        MessageProducer producerForQueue2 = session.createProducer(queue2);

        // Verify: same producer is returned for same queue name
        // This assertion would fail with azure-servicebus-jms 2.0.0 because toString() returned unique values
        assertThat(producer1ForQueue1.toString())
            .as("Same producer should be returned for ServiceBusJmsQueue instances with same queue name")
            .isEqualTo(producer2ForQueue1.toString());

        // Verify: different producer is returned for different queue name
        assertThat(producer1ForQueue1.toString())
            .as("Different producer should be returned for different queue name")
            .isNotEqualTo(producerForQueue2.toString());

        // Cleanup
        connection.close();
    }
    
    /**
     * Creates a ServiceBusJmsSession instance using reflection since the constructor is package-private.
     */
    private Session createServiceBusJmsSession(Session innerSession) throws Exception {
        Class<?> sessionClass = Class.forName("com.azure.servicebus.jms.ServiceBusJmsSession");
        java.lang.reflect.Constructor<?> constructor = sessionClass.getDeclaredConstructor(Session.class);
        constructor.setAccessible(true);
        return (Session) constructor.newInstance(innerSession);
    }

    @Configuration
    @PropertySource("classpath:servicebus/additional.properties")
    static class AdditionalPropertySourceConfiguration {

    }
}
