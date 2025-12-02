// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void cachingConnectionFactoryCachesProducersAndConsumersForSameDestination(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.pool.enabled=false",
                "spring.jms.cache.producers=true",
                "spring.jms.cache.consumers=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
                CachingConnectionFactory cachingFactory = context.getBean(CachingConnectionFactory.class);
                // Verify that producer and consumer caching is enabled
                // When these properties are true, CachingConnectionFactory will cache and reuse
                // MessageProducer and MessageConsumer instances for the same destination
                assertThat(cachingFactory.isCacheProducers())
                    .as("CachingConnectionFactory should cache MessageProducers for the same destination")
                    .isTrue();
                assertThat(cachingFactory.isCacheConsumers())
                    .as("CachingConnectionFactory should cache MessageConsumers for the same destination")
                    .isTrue();
            });
    }

    @Test
    void cachingConnectionFactoryReusesSameProducerForSameDestination() throws JMSException {
        // Create mock objects for JMS components
        ConnectionFactory mockTargetConnectionFactory = mock(ConnectionFactory.class);
        Connection mockConnection = mock(Connection.class);
        Session mockSession = mock(Session.class);
        // Create two separate Queue instances for the same queue name "queue1"
        // In azure-servicebus-jms 2.0.0, each Queue instance had a unique toString() like "ServiceBusJmsQueue@11655"
        // In azure-servicebus-jms 2.1.0, toString() returns the queue name, enabling proper caching
        Queue mockQueue1FirstCall = mock(Queue.class);
        Queue mockQueue1SecondCall = mock(Queue.class);
        Queue mockQueue2 = mock(Queue.class);
        MessageProducer mockProducer1 = mock(MessageProducer.class);
        MessageProducer mockProducer2 = mock(MessageProducer.class);

        // Setup mock behavior
        when(mockTargetConnectionFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.createSession(anyBoolean(), anyInt())).thenReturn(mockSession);
        // Return different Queue instances for the same queue name (simulating real JMS behavior)
        when(mockSession.createQueue("queue1"))
            .thenReturn(mockQueue1FirstCall)
            .thenReturn(mockQueue1SecondCall);
        when(mockSession.createQueue("queue2")).thenReturn(mockQueue2);
        when(mockSession.createProducer(mockQueue1FirstCall)).thenReturn(mockProducer1);
        when(mockSession.createProducer(mockQueue1SecondCall)).thenReturn(mockProducer1);
        when(mockSession.createProducer(mockQueue2)).thenReturn(mockProducer2);
        // Key fix in azure-servicebus-jms 2.1.0: toString() returns the queue name
        // CachingConnectionFactory uses destination.toString() as cache key
        // See: https://github.com/spring-projects/spring-framework/blob/main/spring-jms/src/main/java/org/springframework/jms/connection/CachingConnectionFactory.java#L360
        when(mockQueue1FirstCall.toString()).thenReturn("queue1");
        when(mockQueue1SecondCall.toString()).thenReturn("queue1");
        when(mockQueue2.toString()).thenReturn("queue2");

        // Create CachingConnectionFactory with caching enabled
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(mockTargetConnectionFactory);
        cachingConnectionFactory.setCacheProducers(true);

        // Get connection and session
        Connection connection = cachingConnectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create queues - these are separate Queue instances but with same toString() value
        Queue queue1FirstCall = session.createQueue("queue1");
        Queue queue1SecondCall = session.createQueue("queue1");
        Queue queue2 = session.createQueue("queue2");

        // First call: create producer for queue1 (first Queue instance)
        MessageProducer producer1ForQueue1 = session.createProducer(queue1FirstCall);
        // Second call: create producer for queue1 (second Queue instance, but same toString())
        // With azure-servicebus-jms 2.1.0 fix, this should return cached producer
        MessageProducer producer2ForQueue1 = session.createProducer(queue1SecondCall);
        // Third call: create producer for different queue2 - should return different producer
        MessageProducer producerForQueue2 = session.createProducer(queue2);

        // Verify: same producer is returned for different Queue instances with same toString() value
        // This demonstrates the azure-servicebus-jms 2.1.0 fix where toString() returns queue name
        assertThat(producer1ForQueue1.toString())
            .as("Same producer should be returned when destination.toString() returns the same value")
            .isEqualTo(producer2ForQueue1.toString());

        // Verify: different producer is returned for different destination
        assertThat(producer1ForQueue1.toString())
            .as("Different producer should be returned for different destination")
            .isNotEqualTo(producerForQueue2.toString());

        // Verify the underlying mock was only called once for queue1 Queue instances.
        // This proves caching is working based on destination.toString() value.
        // In azure-servicebus-jms 2.0.0, createProducer would be called twice because
        // each Queue had unique toString() like "ServiceBusJmsQueue@11655".
        // In azure-servicebus-jms 2.1.0, createProducer is called once because
        // toString() returns "queue1" for both Queue instances.
        verify(mockSession, times(1)).createProducer(mockQueue1FirstCall);
        verify(mockSession, times(0)).createProducer(mockQueue1SecondCall);
        verify(mockSession, times(1)).createProducer(mockQueue2);

        // Cleanup
        connection.close();
    }

    @Configuration
    @PropertySource("classpath:servicebus/additional.properties")
    static class AdditionalPropertySourceConfiguration {

    }
}
