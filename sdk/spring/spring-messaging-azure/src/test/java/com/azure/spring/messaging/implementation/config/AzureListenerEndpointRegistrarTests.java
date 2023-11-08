// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.config;

import com.azure.spring.messaging.implementation.endpoint.SimpleAzureListenerTestEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

/**
 *
 */
public class AzureListenerEndpointRegistrarTests {

    private final AzureListenerEndpointRegistrar registrar = new AzureListenerEndpointRegistrar();
    private final AzureListenerEndpointRegistry registry = new AzureListenerEndpointRegistry();
    private final AzureListenerContainerTestFactory containerFactory = new AzureListenerContainerTestFactory();

    @BeforeEach
    public void setup() {
        this.registrar.setEndpointRegistry(this.registry);
        this.registrar.setBeanFactory(new StaticListableBeanFactory());
    }

    @Test
    public void registerNullEndpoint() {
        assertThrows(IllegalArgumentException.class,
            () -> this.registrar.registerEndpoint(null, this.containerFactory));
    }

    @Test
    public void registerNullEndpointId() {
        assertThrows(IllegalArgumentException.class,
            () -> this.registrar.registerEndpoint(new SimpleAzureListenerTestEndpoint(), this.containerFactory));
    }

    @Test
    public void registerEmptyEndpointId() {
        SimpleAzureListenerTestEndpoint endpoint = new SimpleAzureListenerTestEndpoint();
        endpoint.setId("");

        assertThrows(IllegalArgumentException.class,
            () -> this.registrar.registerEndpoint(endpoint, this.containerFactory));
    }

    @Test
    public void registerNullContainerFactoryIsAllowed() {
        SimpleAzureListenerTestEndpoint endpoint = new SimpleAzureListenerTestEndpoint();
        endpoint.setId("some id");
        this.registrar.setContainerFactory(this.containerFactory);
        this.registrar.registerEndpoint(endpoint, null);
        this.registrar.afterPropertiesSet();
        assertNotNull("Container not created", this.registry.getListenerContainer("some id"));
        assertEquals(1, this.registry.getListenerContainersMap().size());
        assertEquals("some id", this.registry.getListenerContainerIds().iterator().next());
    }

    @Test
    public void registerNullContainerFactoryWithNoDefault() {
        SimpleAzureListenerTestEndpoint endpoint = new SimpleAzureListenerTestEndpoint();
        endpoint.setId("some id");
        this.registrar.registerEndpoint(endpoint, null);

        assertThrows(IllegalStateException.class, this.registrar::afterPropertiesSet, endpoint.toString());
    }

    @Test
    public void registerContainerWithoutFactory() {
        SimpleAzureListenerTestEndpoint endpoint = new SimpleAzureListenerTestEndpoint();
        endpoint.setId("myEndpoint");
        this.registrar.setContainerFactory(this.containerFactory);
        this.registrar.registerEndpoint(endpoint);
        this.registrar.afterPropertiesSet();
        assertNotNull("Container not created", this.registry.getListenerContainer("myEndpoint"));
        assertEquals(1, this.registry.getListenerContainersMap().size());
        assertEquals("myEndpoint", this.registry.getListenerContainerIds().iterator().next());
    }

}
