// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaces;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubNamespaceCrudTests extends AbstractResourceCrudTests<EventHubNamespace, String> {

    private static final String NAMESPACE = "namespace";

    @Override
    AbstractResourceCrud<EventHubNamespace, String> getResourceCrud() {
        return new EventHubNamespaceCrud(resourceManager, resourceMetadata);
    }

    @Override
    void getStubManagementException(int statusCode, String message) {
        EventHubNamespaces namespaces = mock(EventHubNamespaces.class);
        when(resourceManager.eventHubNamespaces()).thenReturn(namespaces);
        ManagementException exception = getManagementException(statusCode, message);
        when(namespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), getKey()))
            .thenThrow(exception);
    }

    @Override
    void createStubManagementException() {
        EventHubNamespaces namespaces = mock(EventHubNamespaces.class);
        when(resourceManager.eventHubNamespaces()).thenReturn(namespaces);
        ManagementException exception = getManagementException(500, "Create event hubs namespace exception");

        EventHubNamespace.DefinitionStages.Blank define = mock(EventHubNamespace.DefinitionStages.Blank.class);
        when(namespaces.define(NAMESPACE)).thenReturn(define);
        EventHubNamespace.DefinitionStages.WithGroup group = mock(EventHubNamespace.DefinitionStages.WithGroup.class);
        when(define.withRegion(resourceMetadata.getRegion())).thenReturn(group);
        EventHubNamespace.DefinitionStages.WithCreate create = mock(EventHubNamespace.DefinitionStages.WithCreate.class);
        when(group.withExistingResourceGroup(resourceMetadata.getResourceGroup())).thenReturn(create);
        when(create.create()).thenThrow(exception);
    }

    @Override
    String getKey() {
        return NAMESPACE;
    }
}
