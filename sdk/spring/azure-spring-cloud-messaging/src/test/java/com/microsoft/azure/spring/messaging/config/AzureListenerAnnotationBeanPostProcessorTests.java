// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.config;

import com.microsoft.azure.spring.messaging.annotation.AzureMessageListener;
import com.microsoft.azure.spring.messaging.container.MessageListenerContainer;
import com.microsoft.azure.spring.messaging.container.SimpleMessageListenerContainer;
import com.microsoft.azure.spring.messaging.endpoint.AbstractAzureListenerEndpoint;
import com.microsoft.azure.spring.messaging.endpoint.AzureListenerEndpoint;
import com.microsoft.azure.spring.messaging.endpoint.MethodAzureListenerEndpoint;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Warren Zhu
 */
public class AzureListenerAnnotationBeanPostProcessorTests {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void simpleMessageListener() throws Exception {
        ConfigurableApplicationContext context =
                new AnnotationConfigApplicationContext(Config.class, SimpleMessageListenerTestBean.class);

        AzureListenerContainerTestFactory factory = context.getBean(AzureListenerContainerTestFactory.class);
        assertEquals("One container should have been registered", 1, factory.getListenerContainers().size());
        MessageListenerTestContainer container = factory.getListenerContainers().get(0);

        AzureListenerEndpoint endpoint = container.getEndpoint();
        assertEquals("Wrong endpoint type", MethodAzureListenerEndpoint.class, endpoint.getClass());
        MethodAzureListenerEndpoint methodEndpoint = (MethodAzureListenerEndpoint) endpoint;
        assertEquals(SimpleMessageListenerTestBean.class, methodEndpoint.getBean().getClass());
        assertEquals(SimpleMessageListenerTestBean.class.getMethod("handleIt", String.class),
                methodEndpoint.getMethod());

        MessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
        methodEndpoint.setupListenerContainer(listenerContainer);
        assertNotNull(listenerContainer.getMessageHandler());

        assertTrue("Should have been started " + container, container.isStarted());
        context.close(); // Close and stop the listeners
        assertTrue("Should have been stopped " + container, container.isStopped());
    }

    @Test
    public void metaAnnotationIsDiscovered() throws Exception {
        ConfigurableApplicationContext context =
                new AnnotationConfigApplicationContext(Config.class, MetaAnnotationTestBean.class);

        try {
            AzureListenerContainerTestFactory factory = context.getBean(AzureListenerContainerTestFactory.class);
            assertEquals("one container should have been registered", 1, factory.getListenerContainers().size());

            AzureListenerEndpoint endpoint = factory.getListenerContainers().get(0).getEndpoint();
            assertEquals("Wrong endpoint type", MethodAzureListenerEndpoint.class, endpoint.getClass());
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
        thrown.expect(BeanCreationException.class);
        thrown.expectCause(is(instanceOf(IllegalStateException.class)));
        thrown.expectMessage("handleIt2");
        new AnnotationConfigApplicationContext(Config.class, ProxyConfig.class, InvalidProxyTestBean.class);
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
