// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.endpoint.SimpleAzureListenerEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Warren Zhu
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
            () -> this.registrar.registerEndpoint(new SimpleAzureListenerEndpoint(), this.containerFactory));
    }

    @Test
    public void registerEmptyEndpointId() {
        SimpleAzureListenerEndpoint endpoint = new SimpleAzureListenerEndpoint();
        endpoint.setId("");

        assertThrows(IllegalArgumentException.class,
            () -> this.registrar.registerEndpoint(endpoint, this.containerFactory));
    }

    @Test
    public void registerNullContainerFactoryIsAllowed() throws Exception {
        SimpleAzureListenerEndpoint endpoint = new SimpleAzureListenerEndpoint();
        endpoint.setId("some id");
        this.registrar.setContainerFactory(this.containerFactory);
        this.registrar.registerEndpoint(endpoint, null);
        this.registrar.afterPropertiesSet();
        assertNotNull("Container not created", this.registry.getListenerContainer("some id"));
        assertEquals(1, this.registry.getListenerContainers().size());
        assertEquals("some id", this.registry.getListenerContainerIds().iterator().next());
    }

    @Test
    public void registerNullContainerFactoryWithNoDefault() throws Exception {
        SimpleAzureListenerEndpoint endpoint = new SimpleAzureListenerEndpoint();
        endpoint.setId("some id");
        this.registrar.registerEndpoint(endpoint, null);

        assertThrows(IllegalStateException.class,
            () -> this.registrar.afterPropertiesSet(), endpoint.toString());
    }

    @Test
    public void registerContainerWithoutFactory() throws Exception {
        SimpleAzureListenerEndpoint endpoint = new SimpleAzureListenerEndpoint();
        endpoint.setId("myEndpoint");
        this.registrar.setContainerFactory(this.containerFactory);
        this.registrar.registerEndpoint(endpoint);
        this.registrar.afterPropertiesSet();
        assertNotNull("Container not created", this.registry.getListenerContainer("myEndpoint"));
        assertEquals(1, this.registry.getListenerContainers().size());
        assertEquals("myEndpoint", this.registry.getListenerContainerIds().iterator().next());
    }

}
