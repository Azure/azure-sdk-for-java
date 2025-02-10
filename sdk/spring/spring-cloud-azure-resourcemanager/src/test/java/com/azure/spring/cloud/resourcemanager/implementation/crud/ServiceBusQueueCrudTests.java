// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.Queues;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespaces;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusQueueProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceBusQueueCrudTests extends AbstractResourceCrudTests<Queue, Tuple2<String, String>, ServiceBusQueueProperties> {

    private AzureResourceManager resourceManager;
    private AzureResourceMetadata resourceMetadata;

    private ServiceBusNamespaces serviceBusNamespaces;
    private ServiceBusNamespace serviceBusNamespace;
    private static final String NAMESPACE = "namespace";
    private static final String QUEUE_NAME = "queue";

    private final ServiceBusQueueProperties queueProperties = new ServiceBusQueueProperties();


    @BeforeEach
    void beforeEach() {
        resourceManager = mock(AzureResourceManager.class);
        resourceMetadata = mock(AzureResourceMetadata.class);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(404);

        serviceBusNamespaces = spy(ServiceBusNamespaces.class);
        serviceBusNamespace = spy(ServiceBusNamespace.class);

        queueProperties.setDefaultMessageTimeToLive(Duration.ofSeconds(60));
        queueProperties.setMaxSizeInMegabytes(1024L);
        super.beforeEach();
    }

    @Override
    AbstractResourceCrud<Queue, Tuple2<String, String>, ServiceBusQueueProperties> getResourceCrud() {
        return new ServiceBusQueueCrud(this.resourceManager, this.resourceMetadata);
    }

    @Override
    void getStubManagementException(int statusCode, String message) {
        Tuple2<String, String> queueKey = getKey();
        stubServiceBusNamespace();
        ManagementException exception = createManagementException(statusCode, message);
        Queues queues = mock(Queues.class);
        when(this.serviceBusNamespace.queues()).thenReturn(queues);
        when(queues.getByName(queueKey.getT2())).thenThrow(exception);
    }

    @Override
    void createStubManagementException() {
        ManagementException exception = createManagementException(500, "Create service bus namespace exception");

        stubServiceBusNamespace();

        Queues queues = mock(Queues.class);
        when(serviceBusNamespace.queues()).thenReturn(queues);

        Queue.DefinitionStages.Blank blank = mock(Queue.DefinitionStages.Blank.class);
        when(queues.define(QUEUE_NAME)).thenReturn(blank);

        when(blank.create()).thenThrow(exception);
    }

    private void stubServiceBusNamespace() {
        when(resourceManager.serviceBusNamespaces()).thenReturn(serviceBusNamespaces);
        when(serviceBusNamespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), NAMESPACE))
            .thenReturn(serviceBusNamespace);
    }

    @Override
    Tuple2<String, String> getKey() {
        return Tuples.of(NAMESPACE, QUEUE_NAME);
    }

    @Override
    ServiceBusQueueProperties getCreationProperties() {
        return queueProperties;
    }

    @Test
    void testQueueOptionPropertiesSetting() {
        stubServiceBusNamespace();

        Queues queues = mock(Queues.class);
        when(serviceBusNamespace.queues()).thenReturn(queues);

        Queue.DefinitionStages.Blank blank = mock(Queue.DefinitionStages.Blank.class);
        when(queues.define(QUEUE_NAME)).thenReturn(blank);

        Tuple2<String, String> queueKey = Tuples.of(NAMESPACE, QUEUE_NAME);
        when(getResourceCrud().get(queueKey)).thenReturn(null);
        getResourceCrud().getOrCreate(queueKey, queueProperties);

        verify(blank, times(1)).withSizeInMB(queueProperties.getMaxSizeInMegabytes());
        verify(blank, times(1)).withDefaultMessageTTL(queueProperties.getDefaultMessageTimeToLive());
    }
}
