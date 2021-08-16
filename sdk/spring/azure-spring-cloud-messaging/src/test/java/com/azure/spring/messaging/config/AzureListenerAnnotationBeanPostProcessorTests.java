// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.annotation.AzureMessageListener;
import com.azure.spring.messaging.container.MessageListenerContainer;
import com.azure.spring.messaging.container.SimpleMessageListenerContainer;
import com.azure.spring.messaging.endpoint.AbstractAzureListenerEndpoint;
import com.azure.spring.messaging.endpoint.AzureListenerEndpoint;
import com.azure.spring.messaging.endpoint.MethodAzureListenerEndpoint;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Warren Zhu
 */
public class AzureListenerAnnotationBeanPostProcessorTests {

    @Test
    public void simpleMessageListener() throws Exception {
        ConfigurableApplicationContext context =
            new AnnotationConfigApplicationContext(Config.class, SimpleMessageListenerTestBean.class);

        AzureListenerContainerTestFactory factory = context.getBean(AzureListenerContainerTestFactory.class);
        assertEquals(1, factory.getListenerContainers().size(), "One container should have been registered");
        MessageListenerTestContainer container = factory.getListenerContainers().get(0);

        AzureListenerEndpoint endpoint = container.getEndpoint();
        assertEquals(MethodAzureListenerEndpoint.class, endpoint.getClass(), "Wrong endpoint type");
        MethodAzureListenerEndpoint methodEndpoint = (MethodAzureListenerEndpoint) endpoint;
        assertEquals(SimpleMessageListenerTestBean.class, methodEndpoint.getBean().getClass());
        assertEquals(SimpleMessageListenerTestBean.class.getMethod("handleIt", String.class),
            methodEndpoint.getMethod());

        MessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
        methodEndpoint.setupListenerContainer(listenerContainer);
        assertNotNull(listenerContainer.getMessageHandler());

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
            assertEquals(MethodAzureListenerEndpoint.class, endpoint.getClass(), "Wrong endpoint type");
            MethodAzureListenerEndpoint methodEndpoint = (MethodAzureListenerEndpoint) endpoint;
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

    @AzureMessageListener(destination = "metaTestQueue")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface FooListener {
    }

    interface SimpleService {

        void handleIt(String value, String body);
    }

    @Component
    static class SimpleMessageListenerTestBean {

        @AzureMessageListener(destination = "testQueue")
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
        public AzureListenerAnnotationBeanPostProcessor postProcessor() {
            AzureListenerAnnotationBeanPostProcessor postProcessor = new AzureListenerAnnotationBeanPostProcessor();
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
        @AzureMessageListener(destination = "testQueue")
        @SendTo("foobar")
        public void handleIt(@Header String value, String body) {
        }
    }

    @Component
    static class ClassProxyTestBean {

        @Transactional
        @AzureMessageListener(destination = "testQueue")
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
        @AzureMessageListener(destination = "testQueue")
        @SendTo("foobar")
        public void handleIt2(String body) {
        }
    }

}
