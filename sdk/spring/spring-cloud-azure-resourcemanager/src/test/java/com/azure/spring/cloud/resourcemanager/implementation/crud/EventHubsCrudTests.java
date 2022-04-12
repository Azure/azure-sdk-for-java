// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaces;
import com.azure.resourcemanager.eventhubs.models.EventHubs;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubsCrudTests extends AbstractResourceCrudTests<EventHub, Tuple2<String, String>> {

    private static final String NAMESPACE = "namespace";
    private static final String EVENTHUB_NAME = "eventhub-name";

    @Override
    AbstractResourceCrud<EventHub, Tuple2<String, String>> getResourceCrud() {
        return new EventHubsCrud(resourceManager, resourceMetadata);
    }

    @Override
    void getStubManagementException(int statusCode, String message) {
        EventHubNamespaces namespaces = mock(EventHubNamespaces.class);
        EventHubNamespace eventHubNamespace = mock(EventHubNamespace.class);

        when(resourceManager.eventHubNamespaces()).thenReturn(namespaces);
        ManagementException exception = getManagementException(statusCode, message);
        when(namespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), getKey().getT1()))
            .thenReturn(eventHubNamespace);

        EventHubs eventHubs = mock(EventHubs.class);
        when((resourceManager).eventHubs()).thenReturn(eventHubs);
        when(eventHubs.getByName(resourceMetadata.getResourceGroup(),
            NAMESPACE, EVENTHUB_NAME)).thenThrow(exception);
    }

    @Override
    void createStubManagementException() {
        EventHubNamespaces namespaces = mock(EventHubNamespaces.class);
        EventHubNamespace eventHubNamespace = mock(EventHubNamespace.class);
        ManagementException exception = getManagementException(500, "Create event hubs namespace exception");
        when(resourceManager.eventHubNamespaces()).thenReturn(namespaces);
        when(namespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), getKey().getT1()))
            .thenReturn(eventHubNamespace);

        EventHubs eventHubs = mock(EventHubs.class);
        EventHub eventHub = mock(EventHub.class);
        when((resourceManager).eventHubs()).thenReturn(eventHubs);
        when(eventHubs.getByName(resourceMetadata.getResourceGroup(),
            NAMESPACE, EVENTHUB_NAME)).thenReturn(null, eventHub);

        EventHub.DefinitionStages.Blank define = mock(EventHub.DefinitionStages.Blank.class);
        when(eventHubs.define(EVENTHUB_NAME)).thenReturn(define);

        EventHub.DefinitionStages.WithCaptureProviderOrCreate create = mock(EventHub.DefinitionStages.WithCaptureProviderOrCreate.class);
        when(define.withExistingNamespace(eventHubNamespace)).thenReturn(create);
        when(create.create()).thenThrow(exception);
    }

    @Override
    Tuple2<String, String> getKey() {
        return Tuples.of(NAMESPACE, EVENTHUB_NAME);
    }
}
