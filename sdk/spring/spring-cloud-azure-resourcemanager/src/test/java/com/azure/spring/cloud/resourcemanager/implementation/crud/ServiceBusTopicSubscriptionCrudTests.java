// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespaces;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscriptions;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.resourcemanager.servicebus.models.Topics;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceBusTopicSubscriptionCrudTests extends AbstractResourceCrudTests<ServiceBusSubscription,
    Tuple4<String, String, String, ServiceBusConsumerProperties>> {

    private static final String NAMESPACE = "namespace";
    private static final String TOPIC_NAME = "topic";
    private static final String SUBSCRIPTION_NAME = "subscription";
    private ServiceBusTopicCrud topicCrud;

    private ServiceBusProducerProperties producerProperties = new ServiceBusProducerProperties();
    private ServiceBusConsumerProperties consumerProperties = new ServiceBusConsumerProperties();

    @BeforeEach
    void beforeEach() {
        topicCrud = mock(ServiceBusTopicCrud.class);
        consumerProperties.setDefaultMessageTimeToLive(Duration.ofSeconds(60));
        consumerProperties.setMaxSizeInMegabytes(1024L);

        producerProperties.setDefaultMessageTimeToLive(Duration.ofSeconds(60));
        producerProperties.setMaxSizeInMegabytes(1024L);
        super.beforeEach();
    }

    @Override
    AbstractResourceCrud<ServiceBusSubscription, Tuple4<String, String, String, ServiceBusConsumerProperties>> getResourceCrud() {
        return new ServiceBusTopicSubscriptionCrud(resourceManager, resourceMetadata, this.topicCrud);
    }

    @Override
    Tuple4<String, String, String, ServiceBusConsumerProperties> getKey() {
        return Tuples.of(NAMESPACE, TOPIC_NAME, SUBSCRIPTION_NAME, consumerProperties);
    }

    @Override
    @SuppressWarnings("unchecked")
    void getStubManagementException(int statusCode, String message) {
        Tuple4<String, String, String, ServiceBusConsumerProperties> subscriptionKey = getKey();
        ManagementException exception = createManagementException(statusCode, message);
        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        when(this.topicCrud.get(any(Tuple3.class))).thenReturn(topic);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);
        when(serviceBusSubscriptions.getByName(subscriptionKey.getT3())).thenThrow(exception);
    }

    @Override
    @SuppressWarnings("unchecked")
    void createStubManagementException() {
        ManagementException exception = createManagementException(500, "Create service bus namespace exception");
        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        ServiceBusSubscription.DefinitionStages.Blank define = mock(ServiceBusSubscription.DefinitionStages.Blank.class);

        when(this.topicCrud.getOrCreate(any(Tuple3.class))).thenReturn(topic);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);
        when(serviceBusSubscriptions.define(SUBSCRIPTION_NAME)).thenReturn(define);
        when(define.create()).thenThrow(exception);
    }

    @Test
    @SuppressWarnings("unchecked")
    void topicDoesNotExistReturnNull() {
        when(topicCrud.get(any(Tuple3.class))).thenReturn(null);
        ServiceBusSubscription actualGet = getResourceCrud().get(getKey());
        Assertions.assertNull(actualGet);
    }

    @Test
    @SuppressWarnings("unchecked")
    void topicDoesNotExistsShouldReturnNullTopicAndCreateSub() {
        Tuple4<String, String, String, ServiceBusConsumerProperties> subscriptionKey = getKey();

        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        when(topic.subscriptions()).thenReturn(serviceBusSubscriptions);

        ServiceBusSubscription.DefinitionStages.Blank define =
            mock(ServiceBusSubscription.DefinitionStages.Blank.class);
        when(serviceBusSubscriptions.define(subscriptionKey.getT3())).thenReturn(define);
        ServiceBusSubscription serviceBusSubscription = mock(ServiceBusSubscription.class);
        when(define.create()).thenReturn(serviceBusSubscription);
        when(topicCrud.get(any(Tuple3.class))).thenReturn(null);
        when(topicCrud.getOrCreate(any(Tuple3.class))).thenReturn(topic);

        ServiceBusSubscription actualGet = getResourceCrud().get(subscriptionKey);
        ServiceBusSubscription actualCreate = getResourceCrud().create(subscriptionKey);
        Assertions.assertNull(actualGet);
        Assertions.assertNotNull(actualCreate);
        Assertions.assertEquals(serviceBusSubscription, actualCreate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void topicExistsSubscriptionDoesNotExistShouldReturnNonNullTopicAndCreateSub() {
        Tuple4<String, String, String, ServiceBusConsumerProperties> subscriptionKey = getKey();
        Topic topic = mock(Topic.class);
        ServiceBusSubscriptions serviceBusSubscriptions = mock(ServiceBusSubscriptions.class);
        ServiceBusSubscription subscription = mock(ServiceBusSubscription.class);
        ServiceBusSubscription.DefinitionStages.Blank define =
            mock(ServiceBusSubscription.DefinitionStages.Blank.class);

        when(this.topicCrud.getOrCreate(any(Tuple3.class))).thenReturn(topic);
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

    @Test
    void testTopicOptionPropertiesSetting() {
        AzureResourceManager resourceManager = mock(AzureResourceManager.class);
        AzureResourceMetadata resourceMetadata = mock(AzureResourceMetadata.class);

        ServiceBusNamespaces serviceBusNamespaces = spy(ServiceBusNamespaces.class);
        ServiceBusNamespace serviceBusNamespace = spy(ServiceBusNamespace.class);

        when(resourceManager.serviceBusNamespaces()).thenReturn(serviceBusNamespaces);
        when(serviceBusNamespaces.getByResourceGroup(resourceMetadata.getResourceGroup(), NAMESPACE))
            .thenReturn(serviceBusNamespace);

        Topics topics = mock(Topics.class);
        when(serviceBusNamespace.topics()).thenReturn(topics);

        Topic.DefinitionStages.Blank blank = mock(Topic.DefinitionStages.Blank.class);
        when(topics.define(TOPIC_NAME)).thenReturn(blank);

        Tuple3<String, String, ServiceBusProducerProperties> topicKey = Tuples.of(NAMESPACE, TOPIC_NAME, producerProperties);
        ServiceBusTopicCrud srvBusTopicCrud = new ServiceBusTopicCrud(resourceManager, resourceMetadata);
        when(srvBusTopicCrud.get(topicKey)).thenReturn(null);
        srvBusTopicCrud.getOrCreate(topicKey);

        verify(blank, times(1)).withSizeInMB(producerProperties.getMaxSizeInMegabytes());
        verify(blank, times(1)).withDefaultMessageTTL(producerProperties.getDefaultMessageTimeToLive());
    }
}
