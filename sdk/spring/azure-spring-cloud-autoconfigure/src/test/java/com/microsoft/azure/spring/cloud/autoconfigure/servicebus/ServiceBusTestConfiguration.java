// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.impl.ServiceBusNamesapceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class ServiceBusTestConfiguration {
    @Bean
    ResourceManagerProvider resourceManagerProvider() {
        ResourceManagerProvider resourceManagerProvider = mock(ResourceManagerProvider.class);
        ServiceBusNamesapceManager namespaceManager = mock(ServiceBusNamesapceManager.class);
        when(namespaceManager.getOrCreate(anyString())).thenReturn(mock(ServiceBusNamespace.class));
        when(resourceManagerProvider.getServiceBusNamespaceManager()).thenReturn(namespaceManager);
        return resourceManagerProvider;
    }
}
