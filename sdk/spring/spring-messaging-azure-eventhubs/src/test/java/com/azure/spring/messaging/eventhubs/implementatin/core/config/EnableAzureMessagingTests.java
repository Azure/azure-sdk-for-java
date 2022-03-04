// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementatin.core.config;

import com.azure.spring.messaging.eventhubs.implementation.core.annotation.EventHubsListener;
import com.azure.spring.messaging.eventhubs.implementation.core.annotation.EventHubsListeners;
import com.azure.spring.messaging.eventhubs.implementation.core.config.MethodEventHubsListenerEndpoint;
import com.azure.spring.messaging.implementation.annotation.AzureListenerConfigurer;
import com.azure.spring.messaging.implementation.annotation.EnableAzureMessaging;
import com.azure.spring.messaging.implementation.config.AbstractAzureMessagingAnnotationDrivenTests;
import com.azure.spring.messaging.implementation.config.AzureListenerContainerTestFactory;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistrar;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistry;
import com.azure.spring.messaging.implementation.endpoint.SimpleAzureListenerTestEndpoint;
import com.azure.spring.messaging.listener.MessageListenerTestContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class EnableAzureMessagingTests extends AbstractAzureMessagingAnnotationDrivenTests<MethodEventHubsListenerEndpoint> {
    private static final String DEFAULT_CONTAINER_FACTORY_NAME = "azureEventHubsListenerContainerFactory";

    @Override
    @Test
    public void sampleConfiguration() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingSampleConfig.class, SampleBean.class);
        testSampleConfiguration(context);
    }

    @Override
    @Test
    public void fullConfiguration() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingFullConfig.class, FullBean.class);
        testFullConfiguration(context);
    }

    @Override
    public void fullConfigurableConfiguration() {

    }

    @Override
    @Test
    @Disabled("This test can work in Intellij, but maven build can work. For the spring.factories file in dependency's test-jar.")
    public void customConfiguration() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingCustomConfig.class, CustomBean.class);
        testCustomConfiguration(context);
    }

    @Override
    @Test
    public void explicitContainerFactory() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingCustomContainerFactoryConfig.class,
                DefaultBean.class);
        testExplicitContainerFactoryConfiguration(context);
    }

    @Override
    @Test
    public void defaultContainerFactory() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingDefaultContainerFactoryConfig.class,
                DefaultBean.class);
        testDefaultContainerFactoryConfiguration(context);
    }

    @Test
    public void containerAreStartedByDefault() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingDefaultContainerFactoryConfig.class,
                DefaultBean.class);
        AzureListenerContainerTestFactory factory = context.getBean(AzureListenerContainerTestFactory.class);
        MessageListenerTestContainer container = factory.getListenerContainers().get(0);
        assertTrue(container.isAutoStartup());
        assertTrue(container.isStarted());
    }

    @Test
    public void containerCanBeStarterViaTheRegistry() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingAutoStartupFalseConfig.class,
                DefaultBean.class);
        AzureListenerContainerTestFactory factory = context.getBean(AzureListenerContainerTestFactory.class);
        MessageListenerTestContainer container = factory.getListenerContainers().get(0);
        assertFalse(container.isAutoStartup());
        assertFalse(container.isStarted());
        AzureListenerEndpointRegistry registry = context.getBean(AzureListenerEndpointRegistry.class);
        registry.start();
        assertTrue(container.isStarted());
    }

    @Override
    @Test
    public void azureMessageListenerIsRepeatable() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingDefaultContainerFactoryConfig.class,
                AzureListenerRepeatableBean.class);
        testAzureListenerRepeatable(context);
    }

    @Override
    @Test
    public void azureMessageListeners() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingDefaultContainerFactoryConfig.class,
                AzureListenersBean.class);
        testAzureListenerRepeatable(context);
    }

    @Override
    protected String getDefaultListenerContainerFactoryName() {
        return DEFAULT_CONTAINER_FACTORY_NAME;
    }

    @Test
    public void composedAzureMessageListeners() {
        try (ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
            EnableAzureMessagingDefaultContainerFactoryConfig.class, ComposedAzureMessageListenersBean.class)) {
            AzureListenerContainerTestFactory simpleFactory = context.getBean(getDefaultListenerContainerFactoryName(),
                AzureListenerContainerTestFactory.class);
            assertEquals(2, simpleFactory.getListenerContainers().size());

            MethodEventHubsListenerEndpoint first =
                (MethodEventHubsListenerEndpoint) simpleFactory.getListenerContainer("first").getEndpoint();
            assertEquals("first", first.getId());
            assertEquals("orderQueue", first.getDestination());
            assertNull(first.getConcurrency());

            MethodEventHubsListenerEndpoint second =
                (MethodEventHubsListenerEndpoint) simpleFactory.getListenerContainer("second").getEndpoint();
            assertEquals("second", second.getId());
            assertEquals("billingQueue", second.getDestination());
            assertEquals("2-10", second.getConcurrency());
        }
    }

    @Test
    @SuppressWarnings("resource")
    public void unknownFactory() {
        assertThrows(BeanCreationException.class,
            () -> new AnnotationConfigApplicationContext(EnableAzureMessagingSampleConfig.class, CustomBean.class),
            "customFactory");
    }

    @Test
    public void lazyComponent() {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(EnableAzureMessagingDefaultContainerFactoryConfig.class,
                LazyBean.class);
        AzureListenerContainerTestFactory defaultFactory = context.getBean(getDefaultListenerContainerFactoryName(),
            AzureListenerContainerTestFactory.class);
        assertEquals(0, defaultFactory.getListenerContainers().size());

        context.getBean(LazyBean.class);  // trigger lazy resolution
        assertEquals(1, defaultFactory.getListenerContainers().size());
        MessageListenerTestContainer container = defaultFactory.getListenerContainers().get(0);
        assertTrue(container.isStarted(), "Should have been started " + container);
        context.close();  // close and stop the listeners
        assertTrue(container.isStopped(), "Should have been stopped " + container);
    }

    @EventHubsListener(destination = "orderQueue")
    @Retention(RetentionPolicy.RUNTIME)
    private @interface OrderQueueListener {

        @AliasFor(annotation = EventHubsListener.class) String id() default "";

        @AliasFor(annotation = EventHubsListener.class) String concurrency() default "";
    }

    @EventHubsListener(destination = "billingQueue")
    @Retention(RetentionPolicy.RUNTIME)
    private @interface BillingQueueListener {

        @AliasFor(annotation = EventHubsListener.class) String id() default "";

        @AliasFor(annotation = EventHubsListener.class) String concurrency() default "";
    }

    @EnableAzureMessaging
    @Configuration
    static class EnableAzureMessagingSampleConfig {

        @Bean(name = DEFAULT_CONTAINER_FACTORY_NAME)
        public AzureListenerContainerTestFactory azureListenerContainerFactory() {
            return new AzureListenerContainerTestFactory();
        }

        @Bean
        public AzureListenerContainerTestFactory simpleFactory() {
            return new AzureListenerContainerTestFactory();
        }
    }

    @EnableAzureMessaging
    @Configuration
    static class EnableAzureMessagingFullConfig {

        @Bean
        public AzureListenerContainerTestFactory simpleFactory() {
            return new AzureListenerContainerTestFactory();
        }

    }

    @Configuration
    @EnableAzureMessaging
    static class EnableAzureMessagingCustomConfig implements AzureListenerConfigurer {
        @Override
        public void configureAzureListeners(AzureListenerEndpointRegistrar registrar) {
            registrar.setEndpointRegistry(customRegistry());

            // Also register a custom endpoint
            SimpleAzureListenerTestEndpoint endpoint = new SimpleAzureListenerTestEndpoint();
            endpoint.setId("myCustomEndpointId");
            endpoint.setDestination("myQueue");
            registrar.registerEndpoint(endpoint);
        }

        @Bean(name = DEFAULT_CONTAINER_FACTORY_NAME)
        public AzureListenerContainerTestFactory azureListenerContainerFactory() {
            return new AzureListenerContainerTestFactory();
        }

        @Bean
        public AzureListenerEndpointRegistry customRegistry() {
            return new AzureListenerEndpointRegistry();
        }

        @Bean
        public AzureListenerContainerTestFactory customFactory() {
            return new AzureListenerContainerTestFactory();
        }

    }

    @Configuration
    @EnableAzureMessaging
    static class EnableAzureMessagingCustomContainerFactoryConfig implements AzureListenerConfigurer {

        @Override
        public void configureAzureListeners(AzureListenerEndpointRegistrar registrar) {
            registrar.setContainerFactory(simpleFactory());
        }

        @Bean
        public AzureListenerContainerTestFactory simpleFactory() {
            return new AzureListenerContainerTestFactory();
        }

    }

    @Configuration
    @EnableAzureMessaging
    static class EnableAzureMessagingDefaultContainerFactoryConfig {

        @Bean(name = DEFAULT_CONTAINER_FACTORY_NAME)
        public AzureListenerContainerTestFactory azureListenerContainerFactory() {
            return new AzureListenerContainerTestFactory();
        }

    }

    @Configuration
    @EnableAzureMessaging
    static class EnableAzureMessagingAutoStartupFalseConfig implements AzureListenerConfigurer {

        @Override
        public void configureAzureListeners(AzureListenerEndpointRegistrar registrar) {
            registrar.setContainerFactory(simpleFactory());
        }

        @Bean
        public AzureListenerContainerTestFactory simpleFactory() {
            AzureListenerContainerTestFactory factory = new AzureListenerContainerTestFactory();
            factory.setAutoStartup(false);
            return factory;
        }

    }

    @Component
    @Lazy
    static class LazyBean {

        @EventHubsListener(destination = "myQueue")
        public void handle(String msg) {
        }
    }

    @Component
    static class ComposedAzureMessageListenersBean {

        @OrderQueueListener(id = "first")
        @BillingQueueListener(id = "second", concurrency = "2-10")
        public void repeatableHandle(String msg) {
        }
    }

    @Component
    static class SampleBean {

        @EventHubsListener(destination = "myQueue")
        public void defaultHandle(String msg) {
        }

        @EventHubsListener(containerFactory = "simpleFactory", destination = "myQueue")
        public void simpleHandle(String msg) {
        }
    }

    @Component
    static class FullBean {

        @EventHubsListener(id = "listener1", containerFactory = "simpleFactory", destination = "queueIn",
            group = "group1", concurrency = "1-10")
        @SendTo("queueOut")
        public String fullHandle(String msg) {
            return "reply";
        }
    }

    @Component
    static class CustomBean {

        @EventHubsListener(id = "listenerId", containerFactory = "customFactory", destination = "myQueue")
        public void customHandle(String msg) {
        }
    }

    static class DefaultBean {

        @EventHubsListener(destination = "myQueue")
        public void handleIt(String msg) {
        }
    }

    @Component
    static class AzureListenerRepeatableBean {

        @EventHubsListener(id = "first", destination = "myQueue")
        @EventHubsListener(id = "second", destination = "anotherQueue", concurrency = "2-10")
        public void repeatableHandle(String msg) {
        }
    }

    @Component
    static class AzureListenersBean {

        @EventHubsListeners({
            @EventHubsListener(id = "first", destination = "myQueue"),
            @EventHubsListener(id = "second", destination = "anotherQueue", concurrency = "2-10")
        })
        public void repeatableHandle(String msg) {
        }
    }

}
