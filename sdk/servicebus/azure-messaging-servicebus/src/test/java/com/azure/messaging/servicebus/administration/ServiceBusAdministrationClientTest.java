// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.messaging.servicebus.administration.implementation.EntitiesImpl;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.RulesImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.administration.implementation.SubscriptionsImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryContentImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeedImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.ResponseLinkImpl;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.TitleImpl;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServiceBusAdministrationClient}.
 */
class ServiceBusAdministrationClientTest {
    @Mock
    private ServiceBusManagementClientImpl serviceClient;
    @Mock
    private EntitiesImpl entitys;
    @Mock
    private RulesImpl rules;
    @Mock
    private SubscriptionsImpl subscriptions;
    @Mock
    private ServiceBusManagementSerializer serializer;

    private QueueProperties queuePropertiesResult;

    @Mock
    private QueueDescriptionEntryContentImpl queueDescriptionEntryContent;

    @Mock
    private QueueDescriptionImpl mockQueueDesc;

    @Mock
    private QueueDescriptionEntryImpl queueDescriptionEntry;

    @Mock
    private Response<Object> objectResponse;

    private final Context context = new Context("foo", "bar").addData("baz", "boo");
    private final String queueName = "some-queue";
    private final String subscriptionName = "subscriptionName";
    private final String topicName = "topicName";
    private final String ruleName = "ruleName";
    private ServiceBusAdministrationClient client;
    private final String dummyEndpoint = "endpoint.servicebus.foo";

    private AutoCloseable mockClosable;

    @BeforeEach
    void beforeEach() throws IOException {
        mockClosable = MockitoAnnotations.openMocks(this);

        when(serviceClient.getEntities()).thenReturn(entitys);
        when(serviceClient.getEndpoint()).thenReturn(dummyEndpoint);
        when(serviceClient.getSubscriptions()).thenReturn(subscriptions);
        when(serviceClient.getRules()).thenReturn(rules);

        queuePropertiesResult = EntityHelper.toModel(new QueueDescriptionImpl());
        EntityHelper.setQueueName(queuePropertiesResult, queueName);
        when(mockQueueDesc.getMaxDeliveryCount()).thenReturn(10);
        when(queueDescriptionEntry.getContent()).thenReturn(queueDescriptionEntryContent);
        when(queueDescriptionEntry.getTitle()).thenReturn(new TitleImpl().setContent(queueName));

        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntryImpl.class))).thenReturn(queueDescriptionEntry);

        when(objectResponse.getValue()).thenReturn(queueDescriptionEntry);
        when(entitys.<QueueDescriptionEntryImpl>putWithResponse(any(), any(), any(), any())).thenReturn(objectResponse);

        client = new ServiceBusAdministrationClient(serviceClient, serializer);
    }

    @AfterEach
    void afterEach() throws Exception {
        Mockito.framework().clearInlineMock(this);
        mockClosable.close();
    }

    @Test
    void createQueue() {
        // Arrange
        final CreateQueueOptions description = new CreateQueueOptions()
            .setMaxDeliveryCount(10)
            .setAutoDeleteOnIdle(Duration.ofSeconds(10));

        final CreateQueueOptions options = new CreateQueueOptions()
            .setMaxDeliveryCount(4)
            .setAutoDeleteOnIdle(Duration.ofSeconds(30));
        final QueueDescriptionImpl queueDescription = EntityHelper.getQueueDescription(options);
        final QueueProperties expected = EntityHelper.toModel(queueDescription);
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(queueDescription);

        // Act
        final QueueProperties actual = client.createQueue(queueName, description);

        // Assert
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
    }

    @Test
    void createQueueWithResponse() {
        // Arrange
        final CreateQueueOptions description = new CreateQueueOptions()
            .setMaxDeliveryCount(10)
            .setAutoDeleteOnIdle(Duration.ofSeconds(10));

        final String expectedName = "queue-name-2";
        final CreateQueueOptions options = new CreateQueueOptions()
            .setMaxDeliveryCount(4)
            .setAutoDeleteOnIdle(Duration.ofSeconds(30));
        final QueueDescriptionImpl queueDescription = EntityHelper.getQueueDescription(options);
        final QueueProperties expected = EntityHelper.toModel(queueDescription);

        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(queueDescription);

        // Act
        final Response<QueueProperties> actual = client.createQueueWithResponse(queueName, description, context);

        // Assert
        QueueProperties actualResult = actual.getValue();
        assertEquals(expected.getAutoDeleteOnIdle(), actualResult.getAutoDeleteOnIdle());
        assertEquals(expected.getMaxDeliveryCount(), actualResult.getMaxDeliveryCount());
    }

    @Test
    void deleteQueue() {
        // Arrange
        Response<Object> voidResponse = new SimpleResponse<>(null, 0, null, null);
        when(entitys.deleteWithResponse(eq(queueName), any())).thenReturn(voidResponse);

        // Act
        client.deleteQueue(queueName);

        // Assert
        verify(entitys).deleteWithResponse(eq(queueName), any());
    }

    @Test
    void deleteQueueWithResponse() {
        // Arrange
        Response<Object> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(entitys.deleteWithResponse(any(), any())).thenReturn(voidResponse);

        // Act
        final Response<Void> actual = client.deleteQueueWithResponse(queueName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteRule() {
        // Arrange
        Response<RuleDescriptionEntryImpl> voidResponse = new SimpleResponse<>(null, 0, null, null);
        when(rules.deleteWithResponse(eq(topicName), eq(subscriptionName), eq(ruleName), any()))
            .thenReturn(voidResponse);

        // Act
        client.deleteRule(topicName, subscriptionName, ruleName);

        // Assert
        verify(rules).deleteWithResponse(eq(topicName), eq(subscriptionName), eq(ruleName), any());
    }

    @Test
    void deleteRuleWithResponse() {
        // Arrange
        Response<RuleDescriptionEntryImpl> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(rules.deleteWithResponse(any(), any(), any(), any())).thenReturn(voidResponse);

        // Act
        final Response<Void> actual = client.deleteRuleWithResponse(topicName, subscriptionName, ruleName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteSubscription() {
        // Arrange
        Response<SubscriptionDescriptionEntryImpl> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(subscriptions.deleteWithResponse(eq(topicName), eq(subscriptionName), any())).thenReturn(voidResponse);

        // Act
        client.deleteSubscription(topicName, subscriptionName);

        // Assert
        verify(subscriptions).deleteWithResponse(eq(topicName), eq(subscriptionName), any());
    }

    @Test
    void deleteSubscriptionWithResponse() {
        // Arrange
        Response<SubscriptionDescriptionEntryImpl> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(subscriptions.deleteWithResponse(any(), any(), any())).thenReturn(voidResponse);

        // Act
        final Response<Void> actual = client.deleteSubscriptionWithResponse(topicName, subscriptionName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteTopic() {
        // Arrange
        Response<Object> voidResponse = new SimpleResponse<>(null, 0, null, null);
        when(entitys.deleteWithResponse(any(), any())).thenReturn(voidResponse);

        // Act
        client.deleteTopic(topicName);

        // Assert
        verify(entitys).deleteWithResponse(any(), any());
    }

    @Test
    void deleteTopicWithResponse() {
        // Arrange
        Response<Object> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(entitys.deleteWithResponse(any(), any())).thenReturn(voidResponse);

        // Act
        final Response<Void> actual = client.deleteTopicWithResponse(topicName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void getQueue() {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        when(entitys.<QueueDescriptionEntryImpl>getWithResponse(any(), any(), any())).thenReturn(objectResponse);

        // Act
        final QueueProperties actual = client.getQueue(queueName);

        // Assert
        assertEquals(queuePropertiesResult.getName(), actual.getName());
    }

    @Test
    void getQueueWithResponse() {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        when(entitys.<QueueDescriptionEntryImpl>getWithResponse(any(), any(), any())).thenReturn(objectResponse);

        // Act
        final Response<QueueProperties> actual = client.getQueueWithResponse(queueName, context);

        // Assert
        assertEquals(queuePropertiesResult.getName(), actual.getValue().getName());
    }

    @Test
    void getQueueRuntimeProperties() {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        when(entitys.<QueueDescriptionEntryImpl>getWithResponse(any(), any(), any())).thenReturn(objectResponse);

        // Act
        final QueueRuntimeProperties actual = client.getQueueRuntimeProperties(queueName);

        // Assert
        assertEquals(queuePropertiesResult.getName(), actual.getName());
    }

    @Test
    void getQueueRuntimePropertiesWithResponse() {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        when(entitys.<QueueDescriptionEntryImpl>getWithResponse(any(), any(), any())).thenReturn(objectResponse);

        // Act
        final Response<QueueRuntimeProperties> actual = client.getQueueRuntimePropertiesWithResponse(queueName, context);

        // Assert
        assertEquals(queuePropertiesResult.getName(), actual.getValue().getName());
    }

    @Test
    void listQueues() throws IOException {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        final QueueDescriptionFeedImpl feed = mock(QueueDescriptionFeedImpl.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionFeedImpl.class))).thenReturn(feed);
        when(feed.getEntry()).thenReturn(Collections.singletonList(queueDescriptionEntry));
        when(serviceClient.<QueueDescriptionEntryImpl>listEntitiesWithResponse(any(), any(), any(), any())).thenReturn(
            objectResponse);
        final List<QueueProperties> queues = Arrays.asList(queuePropertiesResult);

        // Act
        final PagedIterable<QueueProperties> queueDescriptions = client.listQueues();

        // Assert
        final long size = queueDescriptions.stream().count();
        assertEquals(queues.size(), size);
    }

    @Test
    void listQueuesWithContext() throws IOException {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        final QueueDescriptionFeedImpl feed = mock(QueueDescriptionFeedImpl.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionFeedImpl.class))).thenReturn(feed);
        when(feed.getEntry()).thenReturn(Collections.singletonList(queueDescriptionEntry));
        when(feed.getLink()).thenReturn(Collections.singletonList(new ResponseLinkImpl().setRel("next").setHref("https://foo.bar.net?api-version=2021-05&$skip=1"))).thenReturn(Arrays.asList(new ResponseLinkImpl().setRel("notNext")));
        when(serviceClient.<QueueDescriptionEntryImpl>listEntitiesWithResponse(any(), any(), any(), any())).thenReturn(
            objectResponse);
        final List<QueueProperties> queues = Arrays.asList(queuePropertiesResult);

        // Act
        final PagedIterable<QueueProperties> queueDescriptions = client.listQueues(context);

        // Assert
        final long size = queueDescriptions.stream().count();
        final long expectedSize = queues.size() + queues.size();

        assertEquals(expectedSize, size);
    }

    @Test
    void updateQueue() {
        // Arrange
        final CreateQueueOptions options = new CreateQueueOptions()
            .setMaxDeliveryCount(4)
            .setAutoDeleteOnIdle(Duration.ofSeconds(30));
        final QueueDescriptionImpl queueDescription = EntityHelper.getQueueDescription(options);
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        final QueueProperties expected = EntityHelper.toModel(queueDescription);

        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(queueDescription);
        when(entitys.<QueueDescriptionEntryImpl>putWithResponse(any(), any(), any(), any())).thenReturn(objectResponse);

        // Act
        final QueueProperties actual = client.updateQueue(description);

        // Assert
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
    }

    @Test
    void updateQueueWithResponse() {
        // Arrange
        final CreateQueueOptions options = new CreateQueueOptions()
            .setMaxDeliveryCount(4)
            .setAutoDeleteOnIdle(Duration.ofSeconds(30));
        final QueueDescriptionImpl queueDescription = EntityHelper.getQueueDescription(options);
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        final QueueProperties expected = EntityHelper.toModel(queueDescription);

        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(queueDescription);
        when(entitys.<QueueDescriptionEntryImpl>putWithResponse(any(), any(), any(), any())).thenReturn(objectResponse);

        // Act
        final Response<QueueProperties> actual = client.updateQueueWithResponse(description, context);

        // Assert
        QueueProperties actualResult = actual.getValue();
        assertEquals(expected.getMaxDeliveryCount(), actualResult.getMaxDeliveryCount());
        assertEquals(expected.getAutoDeleteOnIdle(), actualResult.getAutoDeleteOnIdle());
    }
}
