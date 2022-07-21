// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.provisioning;

import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroup;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroups;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaces;
import com.azure.resourcemanager.eventhubs.models.EventHubs;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.crud.EventHubNamespaceCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.EventHubsConsumerGroupCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.EventHubsCrud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuples;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultEventHubsProvisionerTests {

    private AzureResourceManager resourceManager;
    private AzureResourceMetadata resourceMetadata;
    private TestDefaultEventHubsProvisioner provisioner;
    private ManagementException exception;

    private EventHubNamespaces eventHubNamespaces;
    private EventHubNamespace eventHubNamespace;

    private static final String NAMESPACE = "namespace";
    private static final String EVENTHUB_NAME = "eventhub-name";
    private static final String CONSUMER_GROUP_NAME = "consumer-group-name";

    @BeforeEach
    void beforeEach() {
        resourceManager = mock(AzureResourceManager.class);
        resourceMetadata = mock(AzureResourceMetadata.class);
        when(resourceMetadata.getResourceGroup()).thenReturn("test-rg");
        when(resourceMetadata.getRegion()).thenReturn("eastasia");
        provisioner = spy(new TestDefaultEventHubsProvisioner(resourceManager, resourceMetadata));
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(404);
        exception = new ManagementException("ResourceNotFound", response);

        eventHubNamespaces = spy(EventHubNamespaces.class);
        eventHubNamespace = spy(EventHubNamespace.class);
    }

    @Test
    void provisionExceptionWhenNamespaceNotExist() {
        when(resourceManager.eventHubNamespaces()).thenThrow(exception).thenReturn(eventHubNamespaces);
        when(eventHubNamespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), NAMESPACE))
            .thenReturn(eventHubNamespace);

        EventHubNamespace.DefinitionStages.Blank define = mock(EventHubNamespace.DefinitionStages.Blank.class);
        when(eventHubNamespaces.define(NAMESPACE)).thenReturn(define);
        EventHubNamespace.DefinitionStages.WithGroup group = mock(EventHubNamespace.DefinitionStages.WithGroup.class);
        when(define.withRegion(resourceMetadata.getRegion())).thenReturn(group);
        EventHubNamespace.DefinitionStages.WithCreate create = mock(EventHubNamespace.DefinitionStages.WithCreate.class);
        when(group.withExistingResourceGroup(resourceMetadata.getResourceGroup())).thenReturn(create);

        provisioner.provisionNamespace(NAMESPACE);
        verify(create, times(1)).create();
    }

    @Test
    void provisionEventHubWhenEventHubNotExist() {
        provisionNamespace();
        EventHubs eventHubs = mock(EventHubs.class);
        EventHub eventHub = mock(EventHub.class);

        when((resourceManager).eventHubs()).thenReturn(eventHubs);
        when(eventHubs.getByName(resourceMetadata.getResourceGroup(), NAMESPACE, EVENTHUB_NAME))
            .thenReturn(null, eventHub);

        EventHub.DefinitionStages.Blank define = mock(EventHub.DefinitionStages.Blank.class);
        when(eventHubs.define(EVENTHUB_NAME)).thenReturn(define);

        EventHub.DefinitionStages.WithCaptureProviderOrCreate create = mock(EventHub.DefinitionStages.WithCaptureProviderOrCreate.class);
        when(define.withExistingNamespace(eventHubNamespace)).thenReturn(create);

        provisioner.provisionEventHub(NAMESPACE, EVENTHUB_NAME);
        verify(create, times(1)).create();
    }

    @Test
    void provisionConsumerGroupWhenConsumerGroupNotExist() {
        provisionNamespace();
        EventHubs eventHubs = mock(EventHubs.class);
        EventHub eventHub = mock(EventHub.class);
        when((resourceManager).eventHubs()).thenReturn(eventHubs);
        when(eventHubs.getByName(resourceMetadata.getResourceGroup(), NAMESPACE, EVENTHUB_NAME)).thenReturn(eventHub);

        EventHubConsumerGroups groups = mock(EventHubConsumerGroups.class);
        when(eventHubs.consumerGroups()).thenReturn(groups);

        EventHubConsumerGroup group = mock(EventHubConsumerGroup.class);
        when(groups.getByName(resourceMetadata.getResourceGroup(),
            NAMESPACE, EVENTHUB_NAME, CONSUMER_GROUP_NAME)).thenReturn(null, group);

        EventHubConsumerGroup.DefinitionStages.Blank define = mock(EventHubConsumerGroup.DefinitionStages.Blank .class);
        when(groups.define(CONSUMER_GROUP_NAME)).thenReturn(define);

        EventHubConsumerGroup.DefinitionStages.WithCreate create = mock(EventHubConsumerGroup.DefinitionStages.WithCreate.class);
        when(define.withExistingEventHub(eventHub)).thenReturn(create);

        provisioner.provisionConsumerGroup(NAMESPACE, EVENTHUB_NAME, CONSUMER_GROUP_NAME);
        verify(create, times(1)).create();
    }

    private void provisionNamespace() {
        when(resourceManager.eventHubNamespaces()).thenReturn(eventHubNamespaces);
        when(eventHubNamespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), NAMESPACE))
            .thenReturn(eventHubNamespace);
    }

    static class TestDefaultEventHubsProvisioner extends DefaultEventHubsProvisioner {

        private final EventHubNamespaceCrud namespaceCrud;
        private final EventHubsCrud eventHubsCrud;
        private final EventHubsConsumerGroupCrud consumerGroupCrud;

        /**
         * Creates a new instance of {@link DefaultEventHubsProvisioner}.
         *
         * @param azureResourceManager the azure resource manager
         * @param azureResourceMetadata the azure resource metadata
         */
        TestDefaultEventHubsProvisioner(AzureResourceManager azureResourceManager,
                                                AzureResourceMetadata azureResourceMetadata) {
            super(azureResourceManager, azureResourceMetadata);

            this.namespaceCrud = new EventHubNamespaceCrud(azureResourceManager, azureResourceMetadata);
            this.eventHubsCrud = new EventHubsCrud(azureResourceManager, azureResourceMetadata);
            this.consumerGroupCrud = new EventHubsConsumerGroupCrud(azureResourceManager, azureResourceMetadata);
        }

        @Override
        public void provisionNamespace(String namespace) {
            this.namespaceCrud.getOrCreate(namespace);
        }

        @Override
        public void provisionEventHub(String namespace, String eventHub) {
            this.eventHubsCrud.getOrCreate(Tuples.of(namespace, eventHub));
        }

        @Override
        public void provisionConsumerGroup(String namespace, String eventHub, String consumerGroup) {
            this.consumerGroupCrud.getOrCreate(Tuples.of(namespace, eventHub, consumerGroup));
        }
    }
}
