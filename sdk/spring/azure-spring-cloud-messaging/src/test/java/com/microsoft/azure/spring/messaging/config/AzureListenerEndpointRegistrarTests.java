// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.config;

import com.microsoft.azure.spring.messaging.endpoint.SimpleAzureListenerEndpoint;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Warren Zhu
 */
public class AzureListenerEndpointRegistrarTests {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private final AzureListenerEndpointRegistrar registrar = new AzureListenerEndpointRegistrar();
    private final AzureListenerEndpointRegistry registry = new AzureListenerEndpointRegistry();
    private final AzureListenerContainerTestFactory containerFactory = new AzureListenerContainerTestFactory();

    @Before
    public void setup() {
        this.registrar.setEndpointRegistry(this.registry);
        this.registrar.setBeanFactory(new StaticListableBeanFactory());
    }

    @Test
    public void registerNullEndpoint() {
        this.thrown.expect(IllegalArgumentException.class);
        this.registrar.registerEndpoint(null, this.containerFactory);
    }

    @Test
    public void registerNullEndpointId() {
        this.thrown.expect(IllegalArgumentException.class);
        this.registrar.registerEndpoint(new SimpleAzureListenerEndpoint(), this.containerFactory);
    }

    @Test
    public void registerEmptyEndpointId() {
        SimpleAzureListenerEndpoint endpoint = new SimpleAzureListenerEndpoint();
        endpoint.setId("");

        this.thrown.expect(IllegalArgumentException.class);
        this.registrar.registerEndpoint(endpoint, this.containerFactory);
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

        this.thrown.expect(IllegalStateException.class);
        this.thrown.expectMessage(endpoint.toString());
        this.registrar.afterPropertiesSet();
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
