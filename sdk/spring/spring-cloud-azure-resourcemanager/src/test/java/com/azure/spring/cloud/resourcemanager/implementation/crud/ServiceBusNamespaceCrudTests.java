// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespaces;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceBusNamespaceCrudTests extends AbstractResourceCrudTests<ServiceBusNamespace, String> {

    private static final String NAMESPACE = "namespace";

    @Override
    AbstractResourceCrud<ServiceBusNamespace, String> getResourceCrud() {
        return new ServiceBusNamespaceCrud(resourceManager, resourceMetadata);
    }

    @Override
    void getStubManagementException(int statusCode, String message) {
        ServiceBusNamespaces namespaces = mock(ServiceBusNamespaces.class);
        when(resourceManager.serviceBusNamespaces()).thenReturn(namespaces);
        ManagementException exception = getManagementException(statusCode, message);
        when(namespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), getKey()))
            .thenThrow(exception);
    }

    @Override
    void createStubManagementException() {
        ServiceBusNamespaces namespaces = mock(ServiceBusNamespaces.class);
        when(resourceManager.serviceBusNamespaces()).thenReturn(namespaces);
        ManagementException exception = getManagementException(500, "Create service bus namespace exception");

        ServiceBusNamespace.DefinitionStages.Blank define = mock(ServiceBusNamespace.DefinitionStages.Blank.class);
        when(namespaces.define(NAMESPACE)).thenReturn(define);

        ServiceBusNamespace.DefinitionStages.WithGroup group = mock(ServiceBusNamespace.DefinitionStages.WithGroup.class);
        when(define.withRegion(resourceMetadata.getRegion())).thenReturn(group);

        ServiceBusNamespace.DefinitionStages.WithCreate write = mock(ServiceBusNamespace.DefinitionStages.WithCreate.class);
        when(group.withExistingResourceGroup(resourceMetadata.getResourceGroup())).thenReturn(write);
        when(write.create()).thenThrow(exception);
    }

    @Override
    String getKey() {
        return NAMESPACE;
    }
}
