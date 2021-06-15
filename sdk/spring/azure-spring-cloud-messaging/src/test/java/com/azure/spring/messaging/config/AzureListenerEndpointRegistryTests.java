// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.endpoint.MethodAzureListenerEndpoint;
import com.azure.spring.messaging.endpoint.SimpleAzureListenerEndpoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Warren Zhu
 */
public class AzureListenerEndpointRegistryTests {

    private final AzureListenerEndpointRegistry registry = new AzureListenerEndpointRegistry();
    private final AzureListenerContainerTestFactory containerFactory = new AzureListenerContainerTestFactory();

    @Test
    public void createWithNullEndpoint() {
        assertThrows(IllegalArgumentException.class,
            () -> registry.registerListenerContainer(null, containerFactory));
    }

    @Test
    public void createWithNullEndpointId() {
        assertThrows(IllegalArgumentException.class,
            () -> registry.registerListenerContainer(new MethodAzureListenerEndpoint(), containerFactory));
    }

    @Test
    public void createWithNullContainerFactory() {
        assertThrows(IllegalArgumentException.class,
            () -> registry.registerListenerContainer(createEndpoint("foo", "myDestination"), null));
    }

    @Test
    public void createWithDuplicateEndpointId() {
        registry.registerListenerContainer(createEndpoint("test", "queue"), containerFactory);

        assertThrows(IllegalStateException.class,
            () -> registry.registerListenerContainer(createEndpoint("test", "queue"), containerFactory));
    }

    private SimpleAzureListenerEndpoint createEndpoint(String id, String destinationName) {
        SimpleAzureListenerEndpoint endpoint = new SimpleAzureListenerEndpoint();
        endpoint.setId(id);
        endpoint.setDestination(destinationName);
        return endpoint;
    }

}
