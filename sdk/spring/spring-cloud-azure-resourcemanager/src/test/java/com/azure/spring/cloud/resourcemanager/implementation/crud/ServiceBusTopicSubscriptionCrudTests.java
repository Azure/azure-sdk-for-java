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
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceBusTopicSubscriptionCrudTests extends AbstractResourceCrudTests<ServiceBusSubscription,
    Tuple3<String, String, String>>{

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
        when(serviceBusSubscriptions.getByName(getKey().getT3())).thenThrow(exception);

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

        ServiceBusTopicCrud serviceBusTopicCrud = mock(ServiceBusTopicCrud.class);
        when(serviceBusTopicCrud.get(Tuples.of(NAMESPACE, TOPIC_NAME))).thenThrow(exception);

//        ServiceBusSubscription.DefinitionStages.Blank define = mock(ServiceBusSubscription.DefinitionStages.Blank.class);
//        when(serviceBusSubscriptions.define(SUBSCRIPTION_NAME)).thenReturn(define);
//        when(define.create()).thenThrow(exception);
//
//        ServiceBusSubscription.DefinitionStages.WithCreate create = mock(ServiceBusSubscription.DefinitionStages.WithCreate.class);
//        when(create.create()).thenThrow(exception);
    }

    @Override
    Tuple3<String, String, String> getKey() {
       return Tuples.of(NAMESPACE, TOPIC_NAME, SUBSCRIPTION_NAME);
    }

}
