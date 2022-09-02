// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespaces;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscriptions;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.resourcemanager.servicebus.models.Topics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusTopicSubscriptionCrudTests extends AbstractResourceCrudTests<ServiceBusSubscription,
    Tuple3<String, String, String>> {

    private static final String NAMESPACE = "namespace";
    private static final String TOPIC_NAME = "topic";
    private static final String SUBSCRIPTION_NAME = "subscription";

    @Override
    AbstractResourceCrud<ServiceBusSubscription, Tuple3<String, String, String>> getResourceCrud() {
        return new ServiceBusTopicSubscriptionCrud(resourceManager, resourceMetadata);
    }

    @Override
    void getStubManagementException(int statusCode, String message) {
        ServiceBusNamespaces namespaces = mock(ServiceBusNamespaces.class);
        ServiceBusNamespace namespace = mock(ServiceBusNamespace.class);

        when(resourceManager.serviceBusNamespaces()).thenReturn(namespaces);
        ManagementException exception = getManagementException(statusCode, message);
        when(namespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), getKey().getT1()))
            .thenReturn(namespace);

        Topics topics = mock(Topics.class);
        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        when(namespace.topics()).thenReturn(topics);
        when(topics.getByName(getKey().getT2())).thenReturn(topic);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);
        when(serviceBusSubscriptions.getByName(getKey().getT2())).thenThrow(exception);
    }

    @Override
    void createStubManagementException() {
        ServiceBusNamespaces namespaces = mock(ServiceBusNamespaces.class);
        ServiceBusNamespace namespace = mock(ServiceBusNamespace.class);
        when(resourceManager.serviceBusNamespaces()).thenReturn(namespaces);
        ManagementException exception = getManagementException(500, "Create service bus namespace exception");
        when(namespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), getKey().getT1()))
            .thenReturn(namespace);

        Topics topics = mock(Topics.class);
        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        when(namespace.topics()).thenReturn(topics);
        when(topics.getByName(getKey().getT2())).thenReturn(topic);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);
        when(serviceBusSubscriptions.getByName(getKey().getT3())).thenThrow(exception);

        ServiceBusSubscription.DefinitionStages.Blank define = mock(ServiceBusSubscription.DefinitionStages.Blank.class);
        when(serviceBusSubscriptions.define(SUBSCRIPTION_NAME)).thenReturn(define);
        when(define.create()).thenThrow(exception);
    }

    @Override
    Tuple3<String, String, String> getKey() {
        return Tuples.of(NAMESPACE, TOPIC_NAME, SUBSCRIPTION_NAME);
    }

    @Test
    void topicDoesNotExistReturnNull() {
        ServiceBusTopicCrud topicCrud = mock(ServiceBusTopicCrud.class);
        ServiceBusTopicSubscriptionCrud topicSubCrud = new ServiceBusTopicSubscriptionCrud(this.resourceManager,
            this.resourceMetadata, topicCrud);

        when(topicCrud.get(Tuples.of(NAMESPACE, TOPIC_NAME))).thenReturn(null);

        ServiceBusSubscription actualGet = topicSubCrud.get(getKey());
        Assertions.assertNull(actualGet);
    }

    @Test
    void topicDoesNotExistReturnNullToCreate() {
        ServiceBusTopicCrud topicCrud = mock(ServiceBusTopicCrud.class);
        ServiceBusTopicSubscriptionCrud topicSubCrud = new ServiceBusTopicSubscriptionCrud(this.resourceManager,
            this.resourceMetadata, topicCrud);

        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);

        ServiceBusSubscription.DefinitionStages.Blank define =
            mock(ServiceBusSubscription.DefinitionStages.Blank.class);
        when(serviceBusSubscriptions.define(SUBSCRIPTION_NAME)).thenReturn(define);
        ServiceBusSubscription serviceBusSubscription = mock(ServiceBusSubscription.class);
        when(define.create()).thenReturn(serviceBusSubscription);
        when(topicCrud.get(Tuples.of(NAMESPACE, TOPIC_NAME))).thenReturn(null);
        when(topicCrud.getOrCreate(Tuples.of(NAMESPACE, TOPIC_NAME))).thenReturn(topic);

        ServiceBusSubscription actualGet = topicSubCrud.get(getKey());
        ServiceBusSubscription actualCreate = topicSubCrud.create(getKey());
        Assertions.assertNull(actualGet);
        Assertions.assertNotNull(actualCreate);
        Assertions.assertEquals(serviceBusSubscription, actualCreate);
    }

    @Test
    void topicExistSubscriptionDoesNotExist() {
        ServiceBusNamespaceCrud namespaceCrud = mock(ServiceBusNamespaceCrud.class);
        ServiceBusTopicCrud topicCrud = new ServiceBusTopicCrud(this.resourceManager,
            this.resourceMetadata);
        ServiceBusTopicSubscriptionCrud topicSubCrud = new ServiceBusTopicSubscriptionCrud(this.resourceManager,
            this.resourceMetadata, topicCrud);

        ServiceBusNamespaces namespaces = mock(ServiceBusNamespaces.class);
        ServiceBusNamespace namespace = mock(ServiceBusNamespace.class);
        when(resourceManager.serviceBusNamespaces()).thenReturn(namespaces);
        when(namespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), getKey().getT1()))
            .thenReturn(namespace);

        Topics topics = mock(Topics.class);
        when(namespace.topics()).thenReturn(topics);
        Topic topic = mock(Topic.class);
        when(topics.getByName(getKey().getT2())).thenReturn(topic);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);

        Topic.DefinitionStages.Blank define = mock(Topic.DefinitionStages.Blank.class);
        when(topics.define(TOPIC_NAME)).thenReturn(define);
        when(define.create()).thenReturn(topic);
        when(namespaceCrud.get(NAMESPACE)).thenReturn(namespace);
        when(namespaceCrud.getOrCreate(NAMESPACE)).thenReturn(namespace);

        Topic actualGetTopic = topicCrud.get(getKey());
        Topic actualCreateTopic = topicCrud.create(getKey());
        Assertions.assertNotNull(actualGetTopic);
        Assertions.assertNotNull(actualCreateTopic);

        ServiceBusSubscription actualGetSub = topicSubCrud.get(getKey());
        Assertions.assertNull(actualGetSub);
    }

}
