// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class ServiceBusTestConfiguration {
    @Bean
    ServiceBusNamespaceManager namespaceManager() {
        ServiceBusNamespaceManager namespaceManager = mock(ServiceBusNamespaceManager.class);
        when(namespaceManager.getOrCreate(anyString())).thenReturn(mock(ServiceBusNamespace.class));
        return namespaceManager;
    }
}
