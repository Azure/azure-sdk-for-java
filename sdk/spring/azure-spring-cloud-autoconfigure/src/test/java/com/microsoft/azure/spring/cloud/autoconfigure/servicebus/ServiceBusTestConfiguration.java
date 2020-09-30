// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;

@Configuration
public class ServiceBusTestConfiguration {
    @Bean
    ServiceBusNamespaceManager namespaceManager() {
        ServiceBusNamespaceManager namespaceManager = mock(ServiceBusNamespaceManager.class);
        when(namespaceManager.getOrCreate(anyString())).thenReturn(mock(ServiceBusNamespace.class));
        return namespaceManager;
    }
}
