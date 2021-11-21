// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.annotation.AzureMessageListener;
import com.azure.spring.messaging.annotation.AzureMessageListeners;
import com.azure.spring.messaging.endpoint.AzureListenerEndpoint;
import com.azure.spring.messaging.endpoint.MethodAzureListenerEndpoint;
import com.azure.spring.messaging.endpoint.SimpleAzureListenerEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;


/**
 * @author Warren Zhu
 */

public abstract class AbstractAzureMessagingAnnotationDrivenTests {

    @Test
    public abstract void sampleConfiguration();

    @Test
    public abstract void fullConfiguration();

    @Test
    public abstract void fullConfigurableConfiguration();

    @Test
    public abstract void customConfiguration();

    @Test
    public abstract void explicitContainerFactory();

    @Test
    public abstract void defaultContainerFactory();

    @Test
    public abstract void azureMessageListenerIsRepeatable();

    @Test
    public abstract void azureMessageListeners();

    /**
     * Test for {@link SampleBean} discovery. If a factory with the default name is set, an endpoint will use it
     * automatically
     */
    public void testSampleConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory defaultFactory =
            context.getBean("azureListenerContainerFactory", AzureListenerContainerTestFactory.class);
        AzureListenerContainerTestFactory simpleFactory =
            context.getBean("simpleFactory", AzureListenerContainerTestFactory.class);
        assertEquals(1, defaultFactory.getListenerContainers().size());
        assertEquals(1, simpleFactory.getListenerContainers().size());
    }

    /**
     * Test for {@link FullBean} discovery. In this case, no default is set because all endpoints provide a default
     * registry. This shows that the default factory is only retrieved if it needs to be.
     */
    public void testFullConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory simpleFactory =
            context.getBean("simpleFactory", AzureListenerContainerTestFactory.class);
        assertEquals(1, simpleFactory.getListenerContainers().size());
        MethodAzureListenerEndpoint endpoint =
            (MethodAzureListenerEndpoint) simpleFactory.getListenerContainers().get(0).getEndpoint();
        assertEquals("listener1", endpoint.getId());
        assertEquals("queueIn", endpoint.getDestination());
        assertEquals("group1", endpoint.getGroup());
        assertEquals("1-10", endpoint.getConcurrency());
    }

    /**
     * Test for {@link CustomBean} and an manually endpoint registered with "myCustomEndpointId". The custom endpoint
     * does not provide any factory so it's registered with the default one
     */
    public void testCustomConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory defaultFactory = context.getBean(
            AzureListenerAnnotationBeanPostProcessor.DEFAULT_AZURE_LISTENER_CONTAINER_FACTORY_BEAN_NAME,
            AzureListenerContainerTestFactory.class);
        AzureListenerContainerTestFactory customFactory =
            context.getBean("customFactory", AzureListenerContainerTestFactory.class);
        assertEquals(1, defaultFactory.getListenerContainers().size());
        assertEquals(1, customFactory.getListenerContainers().size());
        AzureListenerEndpoint endpoint = defaultFactory.getListenerContainers().get(0).getEndpoint();
        assertEquals(SimpleAzureListenerEndpoint.class, endpoint.getClass(), "Wrong endpoint type");

        AzureListenerEndpointRegistry customRegistry =
            context.getBean("customRegistry", AzureListenerEndpointRegistry.class);
        assertEquals(2, customRegistry.getListenerContainerIds().size(), "Wrong number of containers in the registry");
        assertEquals(2, customRegistry.getListenerContainers().size(), "Wrong number of containers in the registry");
        assertNotNull("Container with custom id on the annotation should be found",
            customRegistry.getListenerContainer("listenerId"));
        assertNotNull("Container created with custom id should be found",
            customRegistry.getListenerContainer("myCustomEndpointId"));
    }

    /**
     * Test for {@link DefaultBean} that does not define the container factory to use as a default is registered with an
     * explicit default.
     */
    public void testExplicitContainerFactoryConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory defaultFactory =
            context.getBean("simpleFactory", AzureListenerContainerTestFactory.class);
        assertEquals(1, defaultFactory.getListenerContainers().size());
    }

    /**
     * Test for {@link DefaultBean} that does not define the container factory to use as a default is registered with
     * the default name.
     */
    public void testDefaultContainerFactoryConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory defaultFactory = context.getBean(
            AzureListenerAnnotationBeanPostProcessor.DEFAULT_AZURE_LISTENER_CONTAINER_FACTORY_BEAN_NAME,
            AzureListenerContainerTestFactory.class);
        assertEquals(1, defaultFactory.getListenerContainers().size());
    }

    /**
     * Test for {@link AzureListenerRepeatableBean} and {@link AzureListenersBean} that validates that the {@code
     *
     * @AzureListener} annotation is repeatable and generate one specific container per annotation.
     */
    public void testAzureListenerRepeatable(ApplicationContext context) {
        AzureListenerContainerTestFactory simpleFactory = context.getBean(
            AzureListenerAnnotationBeanPostProcessor.DEFAULT_AZURE_LISTENER_CONTAINER_FACTORY_BEAN_NAME,
            AzureListenerContainerTestFactory.class);
        assertEquals(2, simpleFactory.getListenerContainers().size());

        MethodAzureListenerEndpoint first =
            (MethodAzureListenerEndpoint) simpleFactory.getListenerContainer("first").getEndpoint();
        assertEquals("first", first.getId());
        assertEquals("myQueue", first.getDestination());
        assertEquals(null, first.getConcurrency());

        MethodAzureListenerEndpoint second =
            (MethodAzureListenerEndpoint) simpleFactory.getListenerContainer("second").getEndpoint();
        assertEquals("second", second.getId());
        assertEquals("anotherQueue", second.getDestination());
        assertEquals("2-10", second.getConcurrency());
    }

    @Component
    static class SampleBean {

        @AzureMessageListener(destination = "myQueue")
        public void defaultHandle(String msg) {
        }

        @AzureMessageListener(containerFactory = "simpleFactory", destination = "myQueue")
        public void simpleHandle(String msg) {
        }
    }

    @Component
    static class FullBean {

        @AzureMessageListener(id = "listener1", containerFactory = "simpleFactory", destination = "queueIn",
            group = "group1", concurrency = "1-10")
        @SendTo("queueOut")
        public String fullHandle(String msg) {
            return "reply";
        }
    }

    @Component
    static class CustomBean {

        @AzureMessageListener(id = "listenerId", containerFactory = "customFactory", destination = "myQueue")
        public void customHandle(String msg) {
        }
    }

    static class DefaultBean {

        @AzureMessageListener(destination = "myQueue")
        public void handleIt(String msg) {
        }
    }

    @Component
    static class AzureListenerRepeatableBean {

        @AzureMessageListener(id = "first", destination = "myQueue")
        @AzureMessageListener(id = "second", destination = "anotherQueue", concurrency = "2-10")
        public void repeatableHandle(String msg) {
        }
    }

    @Component
    static class AzureListenersBean {

        @AzureMessageListeners({ @AzureMessageListener(id = "first", destination = "myQueue"),
            @AzureMessageListener(id = "second", destination = "anotherQueue", concurrency = "2-10") })
        public void repeatableHandle(String msg) {
        }
    }
}
