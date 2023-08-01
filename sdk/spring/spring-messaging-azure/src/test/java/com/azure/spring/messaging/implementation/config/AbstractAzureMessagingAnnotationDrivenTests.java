// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.config;

import com.azure.spring.messaging.implementation.endpoint.SimpleAzureListenerTestEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractAzureMessagingAnnotationDrivenTests<T extends MethodAzureListenerEndpoint> {

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

    protected abstract String getDefaultListenerContainerFactoryName();
    /**
     * Test for {@code SampleBean} discovery. If a factory with the default name is set, an endpoint will use it
     * automatically
     */
    public void testSampleConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory defaultFactory =
            context.getBean(getDefaultListenerContainerFactoryName(), AzureListenerContainerTestFactory.class);
        AzureListenerContainerTestFactory simpleFactory =
            context.getBean("simpleFactory", AzureListenerContainerTestFactory.class);
        assertEquals(1, defaultFactory.getListenerContainers().size());
        assertEquals(1, simpleFactory.getListenerContainers().size());
    }

    /**
     * Test for {@code FullBean} discovery. In this case, no default is set because all endpoints provide a default
     * registry. This shows that the default factory is only retrieved if it needs to be.
     */
    @SuppressWarnings("unchecked")
    public void testFullConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory simpleFactory =
            context.getBean("simpleFactory", AzureListenerContainerTestFactory.class);
        assertEquals(1, simpleFactory.getListenerContainers().size());
        T endpoint = (T) simpleFactory.getListenerContainers().get(0).getEndpoint();
        assertEquals("listener1", endpoint.getId());
        assertEquals("queueIn", endpoint.getDestination());
        assertEquals("group1", endpoint.getGroup());
        if (endpoint instanceof AbstractAzureListenerEndpoint) {
            assertEquals("1-10", ((AbstractAzureListenerEndpoint) endpoint).getConcurrency());
        }
    }

    /**
     * Test for {@code CustomBean} and an manually endpoint registered with "myCustomEndpointId". The custom endpoint
     * does not provide any factory so it's registered with the default one
     */
    public void testCustomConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory defaultFactory = context.getBean(getDefaultListenerContainerFactoryName(),
            AzureListenerContainerTestFactory.class);
        AzureListenerContainerTestFactory customFactory =
            context.getBean("customFactory", AzureListenerContainerTestFactory.class);
        assertEquals(1, defaultFactory.getListenerContainers().size());
        assertEquals(1, customFactory.getListenerContainers().size());
        AzureListenerEndpoint endpoint = defaultFactory.getListenerContainers().get(0).getEndpoint();
        assertEquals(SimpleAzureListenerTestEndpoint.class, endpoint.getClass(), "Wrong endpoint type");

        AzureListenerEndpointRegistry customRegistry =
            context.getBean("customRegistry", AzureListenerEndpointRegistry.class);
        assertEquals(2, customRegistry.getListenerContainerIds().size(), "Wrong number of containers in the registry");
        assertEquals(2, customRegistry.getListenerContainersMap().size(), "Wrong number of containers in the registry");
        assertNotNull(customRegistry.getListenerContainer("listenerId"), "Container with custom id on the annotation should be found");
        assertNotNull(customRegistry.getListenerContainer("myCustomEndpointId"), "Container created with custom id should be found");
    }

    /**
     * Test for {@code DefaultBean} that does not define the container factory to use as a default is registered with an
     * explicit default.
     */
    public void testExplicitContainerFactoryConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory defaultFactory =
            context.getBean("simpleFactory", AzureListenerContainerTestFactory.class);
        assertEquals(1, defaultFactory.getListenerContainers().size());
    }

    /**
     * Test for {@code DefaultBean} that does not define the container factory to use as a default is registered with
     * the default name.
     */
    public void testDefaultContainerFactoryConfiguration(ApplicationContext context) {
        AzureListenerContainerTestFactory defaultFactory = context.getBean(getDefaultListenerContainerFactoryName(),
            AzureListenerContainerTestFactory.class);
        assertEquals(1, defaultFactory.getListenerContainers().size());
    }

    /**
     * Test for {@code AzureListenerRepeatableBean} and {@code AzureListenersBean} that validates that the {@code
     *
     * @AzureListener} annotation is repeatable and generate one specific container per annotation.
     */
    @SuppressWarnings("unchecked")
    public void testAzureListenerRepeatable(ApplicationContext context) {
        AzureListenerContainerTestFactory simpleFactory = context.getBean(getDefaultListenerContainerFactoryName(),
            AzureListenerContainerTestFactory.class);
        assertEquals(2, simpleFactory.getListenerContainers().size());

        T first = (T) simpleFactory.getListenerContainer("first").getEndpoint();
        assertEquals("first", first.getId());
        assertEquals("myQueue", first.getDestination());
        if (first instanceof AbstractAzureListenerEndpoint) {
            assertNull(((AbstractAzureListenerEndpoint) first).getConcurrency());
        }


        T second = (T) simpleFactory.getListenerContainer("second").getEndpoint();
        assertEquals("second", second.getId());
        assertEquals("anotherQueue", second.getDestination());
        if (second instanceof AbstractAzureListenerEndpoint) {
            assertEquals("2-10", ((AbstractAzureListenerEndpoint) second).getConcurrency());
        }

    }

}
