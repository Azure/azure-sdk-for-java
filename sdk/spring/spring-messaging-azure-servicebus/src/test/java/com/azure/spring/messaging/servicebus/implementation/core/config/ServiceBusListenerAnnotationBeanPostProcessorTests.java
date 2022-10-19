// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.core.config;

import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.implementation.config.AbstractAzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureListenerContainerTestFactory;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistry;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.listener.MessageListenerTestContainer;
import com.azure.spring.messaging.servicebus.implementation.core.annotation.ServiceBusListener;
import com.azure.spring.messaging.servicebus.implementation.core.annotation.ServiceBusListenerAnnotationBeanPostProcessor;
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
public class ServiceBusListenerAnnotationBeanPostProcessorTests {

    @Test
    public void simpleMessageListener() throws Exception {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(Config.class, SimpleMessageListenerTestBean.class);

        AzureListenerContainerTestFactory factory = context.getBean(AzureListenerContainerTestFactory.class);
        assertEquals(1, factory.getListenerContainers().size(), "One container should have been registered");
        MessageListenerTestContainer container = factory.getListenerContainers().get(0);

        AzureListenerEndpoint endpoint = container.getEndpoint();
        assertEquals(MethodServiceBusListenerEndpoint.class, endpoint.getClass(), "Wrong endpoint type");
        MethodServiceBusListenerEndpoint methodEndpoint = (MethodServiceBusListenerEndpoint) endpoint;

        assertEquals(SimpleMessageListenerTestBean.class, methodEndpoint.getBean().getClass());
        assertEquals(SimpleMessageListenerTestBean.class.getMethod("handleIt", String.class),
            methodEndpoint.getMethod());

        MessageListenerContainer listenerContainer = mock(MessageListenerContainer.class);
        AzureMessageConverter<?, ?> messageConverter = mock(AzureMessageConverter.class);
        methodEndpoint.setupListenerContainer(listenerContainer, messageConverter);
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
            assertEquals(MethodServiceBusListenerEndpoint.class, endpoint.getClass(), "Wrong endpoint type");
            MethodServiceBusListenerEndpoint methodEndpoint = (MethodServiceBusListenerEndpoint) endpoint;

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

    @ServiceBusListener(destination = "metaTestQueue")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface FooListener {
    }

    interface SimpleService {

        void handleIt(String value, String body);
    }

    @Component
    static class SimpleMessageListenerTestBean {

        @ServiceBusListener(destination = "testQueue")
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
        public ServiceBusListenerAnnotationBeanPostProcessor postProcessor() {
            ServiceBusListenerAnnotationBeanPostProcessor postProcessor = new ServiceBusListenerAnnotationBeanPostProcessor();
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
        @ServiceBusListener(destination = "testQueue")
        @SendTo("foobar")
        public void handleIt(@Header String value, String body) {
        }
    }

    @Component
    static class ClassProxyTestBean {

        @Transactional
        @ServiceBusListener(destination = "testQueue")
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
        @ServiceBusListener(destination = "testQueue")
        @SendTo("foobar")
        public void handleIt2(String body) {
        }
    }

}
