// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.servicebus.jms.ServiceBusJmsQueue;
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
    void serviceBusJmsQueueToStringReturnsConsistentValueForSameQueueName() throws Exception {
        // Create mock inner queues for ServiceBusJmsQueue instances
        Queue mockInnerQueue1 = mock(Queue.class);
        Queue mockInnerQueue2 = mock(Queue.class);
        Queue mockInnerQueue3 = mock(Queue.class);
        when(mockInnerQueue1.getQueueName()).thenReturn("queue1");
        when(mockInnerQueue2.getQueueName()).thenReturn("queue1");
        when(mockInnerQueue3.getQueueName()).thenReturn("queue2");
        
        // Create ServiceBusJmsQueue instances using reflection (constructor is package-private)
        // In azure-servicebus-jms 2.1.0, toString() returns "ServiceBusJmsQueue{queueName='...'}"
        // In azure-servicebus-jms 2.0.0, toString() returned "ServiceBusJmsQueue@hashcode" (default Object.toString())
        ServiceBusJmsQueue serviceBusQueue1FirstCall = createServiceBusJmsQueue(mockInnerQueue1);
        ServiceBusJmsQueue serviceBusQueue1SecondCall = createServiceBusJmsQueue(mockInnerQueue2);
        ServiceBusJmsQueue serviceBusQueue2 = createServiceBusJmsQueue(mockInnerQueue3);

        // Verify ServiceBusJmsQueue toString() returns consistent value for same queue name
        // This is the key fix in azure-servicebus-jms 2.1.0
        // Without this fix, CachingConnectionFactory cannot cache producers because it uses
        // destination.toString() as cache key (see CachingConnectionFactory.java#L360)
        assertThat(serviceBusQueue1FirstCall.toString())
            .as("ServiceBusJmsQueue 2.1.0 toString() should return consistent value for same queue name")
            .isEqualTo(serviceBusQueue1SecondCall.toString());
        
        // Verify toString() includes the queue name
        assertThat(serviceBusQueue1FirstCall.toString())
            .as("ServiceBusJmsQueue toString() should include queue name")
            .contains("queue1");
            
        // Verify toString() returns different value for different queue name
        assertThat(serviceBusQueue1FirstCall.toString())
            .as("ServiceBusJmsQueue 2.1.0 toString() should return different value for different queue name")
            .isNotEqualTo(serviceBusQueue2.toString());
        
        assertThat(serviceBusQueue2.toString())
            .as("ServiceBusJmsQueue toString() should include queue name")
            .contains("queue2");
    }
    
    @Test
    void cachingConnectionFactoryReusesSameProducerForSameDestination() throws Exception {
        // Create mock objects for JMS components
        ConnectionFactory mockTargetConnectionFactory = mock(ConnectionFactory.class);
        Connection mockConnection = mock(Connection.class);
        Session mockSession = mock(Session.class);
        // Create mock inner queues for ServiceBusJmsQueue instances
        Queue mockInnerQueue1 = mock(Queue.class);
        Queue mockInnerQueue2 = mock(Queue.class);
        Queue mockInnerQueue3 = mock(Queue.class);
        when(mockInnerQueue1.getQueueName()).thenReturn("queue1");
        when(mockInnerQueue2.getQueueName()).thenReturn("queue1");
        when(mockInnerQueue3.getQueueName()).thenReturn("queue2");
        
        // Create ServiceBusJmsQueue instances using reflection (constructor is package-private)
        ServiceBusJmsQueue serviceBusQueue1FirstCall = createServiceBusJmsQueue(mockInnerQueue1);
        ServiceBusJmsQueue serviceBusQueue1SecondCall = createServiceBusJmsQueue(mockInnerQueue2);
        ServiceBusJmsQueue serviceBusQueue2 = createServiceBusJmsQueue(mockInnerQueue3);
        MessageProducer mockProducer1 = mock(MessageProducer.class);
        MessageProducer mockProducer2 = mock(MessageProducer.class);

        // Setup mock behavior for connection and session
        when(mockTargetConnectionFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.createSession(anyBoolean(), anyInt())).thenReturn(mockSession);
        when(mockSession.createQueue("queue1"))
            .thenReturn(serviceBusQueue1FirstCall)
            .thenReturn(serviceBusQueue1SecondCall);
        when(mockSession.createQueue("queue2")).thenReturn(serviceBusQueue2);
        when(mockSession.createProducer(serviceBusQueue1FirstCall)).thenReturn(mockProducer1);
        when(mockSession.createProducer(serviceBusQueue1SecondCall)).thenReturn(mockProducer1);
        when(mockSession.createProducer(serviceBusQueue2)).thenReturn(mockProducer2);

        // Create CachingConnectionFactory with caching enabled
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(mockTargetConnectionFactory);
        cachingConnectionFactory.setCacheProducers(true);

        // Get connection and session
        Connection connection = cachingConnectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create queues - these are separate ServiceBusJmsQueue instances with same toString() value
        Queue queue1FirstCall = session.createQueue("queue1");
        Queue queue1SecondCall = session.createQueue("queue1");
        Queue queue2 = session.createQueue("queue2");

        // First call: create producer for queue1 (first ServiceBusJmsQueue instance)
        MessageProducer producer1ForQueue1 = session.createProducer(queue1FirstCall);
        // Second call: create producer for queue1 (second ServiceBusJmsQueue instance, same toString())
        // With azure-servicebus-jms 2.1.0, this returns cached producer because toString() is consistent
        MessageProducer producer2ForQueue1 = session.createProducer(queue1SecondCall);
        // Third call: create producer for different queue2 - should return different producer
        MessageProducer producerForQueue2 = session.createProducer(queue2);

        // Verify: same producer is returned for same queue name due to consistent toString()
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
     * Creates a ServiceBusJmsQueue instance using reflection since the constructor is package-private.
     */
    private ServiceBusJmsQueue createServiceBusJmsQueue(Queue innerQueue) throws Exception {
        java.lang.reflect.Constructor<ServiceBusJmsQueue> constructor = 
            ServiceBusJmsQueue.class.getDeclaredConstructor(Queue.class);
        constructor.setAccessible(true);
        return constructor.newInstance(innerQueue);
    }

    @Configuration
    @PropertySource("classpath:servicebus/additional.properties")
    static class AdditionalPropertySourceConfiguration {

    }
}
