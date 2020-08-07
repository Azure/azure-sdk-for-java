// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.config;

import com.microsoft.azure.spring.messaging.endpoint.MethodAzureListenerEndpoint;
import com.microsoft.azure.spring.messaging.endpoint.SimpleAzureListenerEndpoint;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Warren Zhu
 */
public class AzureListenerEndpointRegistryTests {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private final AzureListenerEndpointRegistry registry = new AzureListenerEndpointRegistry();
    private final AzureListenerContainerTestFactory containerFactory = new AzureListenerContainerTestFactory();

    @Test
    public void createWithNullEndpoint() {
        thrown.expect(IllegalArgumentException.class);
        registry.registerListenerContainer(null, containerFactory);
    }

    @Test
    public void createWithNullEndpointId() {
        thrown.expect(IllegalArgumentException.class);
        registry.registerListenerContainer(new MethodAzureListenerEndpoint(), containerFactory);
    }

    @Test
    public void createWithNullContainerFactory() {
        thrown.expect(IllegalArgumentException.class);
        registry.registerListenerContainer(createEndpoint("foo", "myDestination"), null);
    }

    @Test
    public void createWithDuplicateEndpointId() {
        registry.registerListenerContainer(createEndpoint("test", "queue"), containerFactory);

        thrown.expect(IllegalStateException.class);
        registry.registerListenerContainer(createEndpoint("test", "queue"), containerFactory);
    }

    private SimpleAzureListenerEndpoint createEndpoint(String id, String destinationName) {
        SimpleAzureListenerEndpoint endpoint = new SimpleAzureListenerEndpoint();
        endpoint.setId(id);
        endpoint.setDestination(destinationName);
        return endpoint;
    }

}
