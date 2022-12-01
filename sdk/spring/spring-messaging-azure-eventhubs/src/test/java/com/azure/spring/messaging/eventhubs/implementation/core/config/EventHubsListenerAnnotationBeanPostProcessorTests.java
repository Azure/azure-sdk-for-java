// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.core.config;

import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.eventhubs.implementation.core.annotation.EventHubsListener;
import com.azure.spring.messaging.eventhubs.implementation.core.annotation.EventHubsListenerAnnotationBeanPostProcessor;
import com.azure.spring.messaging.implementation.config.AbstractAzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureListenerContainerTestFactory;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistry;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.listener.MessageListenerTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class EventHubsListenerAnnotationBeanPostProcessorTests {

    @Test
    public void simpleMessageListener() throws Exception {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(Config.class, SimpleMessageListenerTestBean.class);

        AzureListenerContainerTestFactory factory = context.getBean(AzureListenerContainerTestFactory.class);
        assertEquals(1, factory.getListenerContainers().size(), "One container should have been registered");
        MessageListenerTestContainer container = factory.getListenerContainers().get(0);

        AzureListenerEndpoint endpoint = container.getEndpoint();
        assertEquals(MethodEventHubsListenerEndpoint.class, endpoint.getClass(), "Wrong endpoint type");
        MethodEventHubsListenerEndpoint methodEndpoint = (MethodEventHubsListenerEndpoint) endpoint;

        assertEquals(SimpleMessageListenerTestBean.class, methodEndpoint.getBean().getClass());
        assertEquals(SimpleMessageListenerTestBean.class.getMethod("handleIt", String.class),
            methodEndpoint.getMethod());

        MessageListenerContainer listenerContainer = mock(MessageListenerContainer.class);
        AzureMessageConverter<?, ?> converter = mock(AzureMessageConverter.class);
        methodEndpoint.setupListenerContainer(listenerContainer, converter);
        assertTrue(container.isStarted(), "Should have been started " + container);
        context.close(); // Close and stop the listeners
        assertTrue(container.isStopped(), "Should have been stopped " + container);
    }

    @Test
    public void metaAnnotationIsDiscovered() throws Exception {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(Config.class, MetaAnnotationTestBean.class);

        try {
            AzureListenerContainerTestFactory factory = context.getBean(AzureListenerContainerTestFactory.class);
            assertEquals(1, factory.getListenerContainers().size(), "one container should have been registered");

            AzureListenerEndpoint endpoint = factory.getListenerContainers().get(0).getEndpoint();
            assertEquals(MethodEventHubsListenerEndpoint.class, endpoint.getClass(), "Wrong endpoint type");
            MethodEventHubsListenerEndpoint methodEndpoint = (MethodEventHubsListenerEndpoint) endpoint;

            assertEquals(MetaAnnotationTestBean.class, methodEndpoint.getBean().getClass());
            assertEquals(MetaAnnotationTestBean.class.getMethod("handleIt", String.class), methodEndpoint.getMethod());
            assertEquals("metaTestQueue", ((AbstractAzureListenerEndpoint) endpoint).getDestination());
        } finally {
            context.close();
        }
    }

    @Test
    @SuppressWarnings("resource")
    public void invalidProxy() {
        assertThrows(BeanCreationException.class,
            () -> new AnnotationConfigApplicationContext(Config.class, ProxyConfig.class, InvalidProxyTestBean.class),
            "handleIt2");
    }

    @EventHubsListener(destination = "metaTestQueue")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface FooListener {
    }

    interface SimpleService {

        void handleIt(String value, String body);
    }

    @Component
    static class SimpleMessageListenerTestBean {

        @EventHubsListener(destination = "testQueue")
        public void handleIt(String body) {
        }
    }

    @Component
    static class MetaAnnotationTestBean {

        @FooListener
        public void handleIt(String body) {
        }
    }

    @Configuration
    static class Config {

        @Bean
        public EventHubsListenerAnnotationBeanPostProcessor postProcessor() {
            EventHubsListenerAnnotationBeanPostProcessor postProcessor = new EventHubsListenerAnnotationBeanPostProcessor();
            postProcessor.setContainerFactoryBeanName("testFactory");
            return postProcessor;
        }

        @Bean
        public AzureListenerEndpointRegistry azureListenerEndpointRegistry() {
            return new AzureListenerEndpointRegistry();
        }

        @Bean
        public AzureListenerContainerTestFactory testFactory() {
            return new AzureListenerContainerTestFactory();
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class ProxyConfig {

        @Bean
        public PlatformTransactionManager transactionManager() {
            return mock(PlatformTransactionManager.class);
        }
    }

    @Component
    static class InterfaceProxyTestBean implements SimpleService {

        @Override
        @Transactional
        @EventHubsListener(destination = "testQueue")
        @SendTo("foobar")
        public void handleIt(@Header String value, String body) {
        }
    }

    @Component
    static class ClassProxyTestBean {

        @Transactional
        @EventHubsListener(destination = "testQueue")
        @SendTo("foobar")
        public void handleIt(@Header String value, String body) {
        }
    }

    @Component
    static class InvalidProxyTestBean implements SimpleService {

        @Override
        public void handleIt(String value, String body) {
        }

        @Transactional
        @EventHubsListener(destination = "testQueue")
        @SendTo("foobar")
        public void handleIt2(String body) {
        }
    }

}
