// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscriptions;
import com.azure.resourcemanager.servicebus.models.Topic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusTopicSubscriptionCrudTests extends AbstractResourceCrudTests<ServiceBusSubscription,
    Tuple3<String, String, String>> {

    private static final String NAMESPACE = "namespace";
    private static final String TOPIC_NAME = "topic";
    private static final String SUBSCRIPTION_NAME = "subscription";
    private ServiceBusTopicCrud topicCrud;

    @BeforeEach
    void beforeEach() {
        topicCrud = mock(ServiceBusTopicCrud.class);
        super.beforeEach();
    }

    @Override
    AbstractResourceCrud<ServiceBusSubscription, Tuple3<String, String, String>> getResourceCrud() {
        return new ServiceBusTopicSubscriptionCrud(resourceManager, resourceMetadata, this.topicCrud);
    }

    @Override
    Tuple3<String, String, String> getKey() {
        return Tuples.of(NAMESPACE, TOPIC_NAME, SUBSCRIPTION_NAME);
    }

    @Override
    void getStubManagementException(int statusCode, String message) {
        ManagementException exception = createManagementException(statusCode, message);
        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);

        Tuple3<String, String, String> subscriptionKey = getKey();

        when(this.topicCrud.get(Tuples.of(subscriptionKey.getT1(), subscriptionKey.getT2()))).thenReturn(topic);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);
        when(serviceBusSubscriptions.getByName(subscriptionKey.getT3())).thenThrow(exception);
    }

    @Override
    void createStubManagementException() {
        ManagementException exception = createManagementException(500, "Create service bus namespace exception");
        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        ServiceBusSubscription.DefinitionStages.Blank define = mock(ServiceBusSubscription.DefinitionStages.Blank.class);

        Tuple3<String, String, String> subscriptionKey = getKey();

        when(this.topicCrud.getOrCreate(Tuples.of(subscriptionKey.getT1(), subscriptionKey.getT2()))).thenReturn(topic);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);
        when(serviceBusSubscriptions.define(SUBSCRIPTION_NAME)).thenReturn(define);
        when(define.create()).thenThrow(exception);
    }

    @Test
    void topicDoesNotExistReturnNull() {
        when(topicCrud.get(Tuples.of(NAMESPACE, TOPIC_NAME))).thenReturn(null);
        ServiceBusSubscription actualGet = getResourceCrud().get(getKey());
        Assertions.assertNull(actualGet);
    }

    @Test
    void topicDoesNotExistsShouldReturnNullTopicAndCreateSub() {
        Tuple3<String, String, String> subscriptionKey = getKey();
        Tuple2<String, String> topicKey = Tuples.of(subscriptionKey.getT1(), subscriptionKey.getT2());

        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);

        ServiceBusSubscription.DefinitionStages.Blank define =
            mock(ServiceBusSubscription.DefinitionStages.Blank.class);
        when(serviceBusSubscriptions.define(subscriptionKey.getT3())).thenReturn(define);
        ServiceBusSubscription serviceBusSubscription = mock(ServiceBusSubscription.class);
        when(define.create()).thenReturn(serviceBusSubscription);
        when(topicCrud.get(topicKey)).thenReturn(null);
        when(topicCrud.getOrCreate(topicKey)).thenReturn(topic);

        ServiceBusSubscription actualGet = getResourceCrud().get(subscriptionKey);
        ServiceBusSubscription actualCreate = getResourceCrud().create(subscriptionKey);
        Assertions.assertNull(actualGet);
        Assertions.assertNotNull(actualCreate);
        Assertions.assertEquals(serviceBusSubscription, actualCreate);
    }

    @Test
    void topicExistsSubscriptionDoesNotExistShouldReturnNonNullTopicAndCreateSub() {
        Tuple3<String, String, String> subscriptionKey = getKey();
        Tuple2<String, String> topicKey = Tuples.of(subscriptionKey.getT1(), subscriptionKey.getT2());

        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        ServiceBusSubscription subscription = mock(ServiceBusSubscription.class);
        ServiceBusSubscription.DefinitionStages.Blank define =
            mock(ServiceBusSubscription.DefinitionStages.Blank.class);

        when(this.topicCrud.getOrCreate(topicKey)).thenReturn(topic);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);
        when(serviceBusSubscriptions.getByName(subscriptionKey.getT3())).thenReturn(null);
        when(serviceBusSubscriptions.define(subscriptionKey.getT3())).thenReturn(define);
        when(define.create()).thenReturn(subscription);

        ServiceBusSubscription actualGet = getResourceCrud().get(subscriptionKey);
        ServiceBusSubscription actualCreate = getResourceCrud().create(subscriptionKey);

        Assertions.assertNull(actualGet);
        Assertions.assertNotNull(actualCreate);
        Assertions.assertEquals(subscription, actualCreate);
    }

}
