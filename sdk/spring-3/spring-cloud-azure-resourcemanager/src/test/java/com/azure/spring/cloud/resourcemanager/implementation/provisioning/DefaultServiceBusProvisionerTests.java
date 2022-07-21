// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.provisioning;

import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.Queues;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespaces;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscriptions;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.resourcemanager.servicebus.models.Topics;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusQueueCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusTopicCrud;
import com.azure.spring.cloud.resourcemanager.implementation.crud.ServiceBusTopicSubscriptionCrud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuples;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultServiceBusProvisionerTests {

    private AzureResourceManager resourceManager;
    private AzureResourceMetadata resourceMetadata;
    private TestDefaultServiceBusProvisioner provisioner;
    private ManagementException exception;

    private ServiceBusNamespaces serviceBusNamespaces;
    private ServiceBusNamespace serviceBusNamespace;

    private static final String NAMESPACE = "namespace";
    private static final String QUEUE_NAME = "queue-name";
    private static final String TOPIC_NAME = "topic-name";
    private static final String SUBSCRIPTION_NAME = "subscription-name";

    @BeforeEach
    void beforeEach() {
        resourceManager = mock(AzureResourceManager.class);
        resourceMetadata = mock(AzureResourceMetadata.class);
        when(resourceMetadata.getResourceGroup()).thenReturn("test-rg");
        when(resourceMetadata.getRegion()).thenReturn("eastasia");
        provisioner = spy(new TestDefaultServiceBusProvisioner(resourceManager, resourceMetadata));
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(404);
        exception = new ManagementException("ResourceNotFound", response);

        serviceBusNamespaces = spy(ServiceBusNamespaces.class);
        serviceBusNamespace = spy(ServiceBusNamespace.class);
    }

    @Test
    void provisionExceptionWhenNamespaceNotExist() {
        when(resourceManager.serviceBusNamespaces()).thenThrow(exception);
        assertThrows(IllegalArgumentException.class, () ->
            provisioner.provisionQueue(NAMESPACE, QUEUE_NAME));
        assertThrows(IllegalArgumentException.class, () ->
            provisioner.provisionTopic(NAMESPACE, TOPIC_NAME));
        assertThrows(IllegalArgumentException.class, () ->
            provisioner.provisionSubscription(NAMESPACE, TOPIC_NAME, SUBSCRIPTION_NAME));
    }

    @Test
    void provisionQueueWhenQueueNameNotExist() {
        provisionNamespace();
        Queues queues = mock(Queues.class);
        Queue queue = mock(Queue.class);
        when((serviceBusNamespace).queues()).thenReturn(queues);
        when(queues.getByName(QUEUE_NAME)).thenReturn(null, queue);

        Queue.DefinitionStages.Blank queueStage = mock(Queue.DefinitionStages.Blank.class);
        when(queues.define(QUEUE_NAME)).thenReturn(queueStage);

        provisioner.provisionQueue(NAMESPACE, QUEUE_NAME);
        verify(queueStage, times(1)).create();
    }

    @Test
    void provisionTopicWhenTopicNameNotExist() {
        provisionNamespace();
        Topics topics = mock(Topics.class);
        Topic topic = mock(Topic.class);
        when((serviceBusNamespace).topics()).thenReturn(topics);
        when(topics.getByName(TOPIC_NAME)).thenReturn(null, topic);

        Topic.DefinitionStages.Blank topicStage = mock(Topic.DefinitionStages.Blank .class);
        when(topics.define(TOPIC_NAME)).thenReturn(topicStage);

        provisioner.provisionTopic(NAMESPACE, TOPIC_NAME);
        verify(topicStage, times(1)).create();
    }

    @Test
    void provisionSubscriptionWhenSubscriptionNotExist() {
        provisionNamespace();
        Topics topics = mock(Topics.class);
        Topic topic = mock(Topic.class);
        when(serviceBusNamespace.topics()).thenReturn(topics);
        when(topics.getByName(TOPIC_NAME)).thenReturn(topic);

        ServiceBusSubscriptions subscriptions = mock(ServiceBusSubscriptions.class);
        ServiceBusSubscription subscription = mock(ServiceBusSubscription.class);
        when(topic.subscriptions()).thenReturn(subscriptions);
        when(subscriptions.getByName(SUBSCRIPTION_NAME)).thenReturn(null, subscription);

        ServiceBusSubscription.DefinitionStages.Blank  subscriptionStage = mock(ServiceBusSubscription.DefinitionStages.Blank.class);
        when(subscriptions.define(SUBSCRIPTION_NAME)).thenReturn(subscriptionStage);

        provisioner.provisionSubscription(NAMESPACE, TOPIC_NAME, SUBSCRIPTION_NAME);
        verify(subscriptionStage, times(1)).create();
    }

    private void provisionNamespace() {
        when(resourceManager.serviceBusNamespaces()).thenReturn(serviceBusNamespaces);
        when(serviceBusNamespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), NAMESPACE))
            .thenReturn(serviceBusNamespace);
    }

    static class TestDefaultServiceBusProvisioner extends DefaultServiceBusProvisioner {

        private final ServiceBusQueueCrud queueCrud;
        private final ServiceBusTopicCrud topicCrud;
        private final ServiceBusTopicSubscriptionCrud subscriptionCrud;

        /**
         * Creates a new instance of {@link DefaultServiceBusProvisioner}.
         *
         * @param azureResourceManager the azure resource manager
         * @param azureResourceMetadata the azure resource metadata
         */
        TestDefaultServiceBusProvisioner(AzureResourceManager azureResourceManager,
                                                AzureResourceMetadata azureResourceMetadata) {
            super(azureResourceManager, azureResourceMetadata);

            this.queueCrud = new ServiceBusQueueCrud(azureResourceManager, azureResourceMetadata);
            this.topicCrud = new ServiceBusTopicCrud(azureResourceManager, azureResourceMetadata);
            this.subscriptionCrud = new ServiceBusTopicSubscriptionCrud(azureResourceManager, azureResourceMetadata);
        }

        @Override
        public void provisionQueue(String namespace, String queue) {
            this.queueCrud.getOrCreate(Tuples.of(namespace, queue));
        }

        @Override
        public void provisionTopic(String namespace, String topic) {
            this.topicCrud.getOrCreate(Tuples.of(namespace, topic));
        }

        @Override
        public void provisionSubscription(String namespace, String topic, String subscription) {
            this.subscriptionCrud.getOrCreate(Tuples.of(namespace, topic, subscription));
        }
    }
}
