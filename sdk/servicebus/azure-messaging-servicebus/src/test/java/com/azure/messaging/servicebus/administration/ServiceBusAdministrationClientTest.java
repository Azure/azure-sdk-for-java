// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.servicebus.administration.implementation.EntitiesImpl;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.RulesImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.administration.implementation.SubscriptionsImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryContent;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
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
    private Response<Object> voidResponse;
    @Mock
    private EntitiesImpl entitys;
    @Mock
    private RulesImpl rules;
    @Mock
    private SubscriptionsImpl subscriptions;
    @Mock
    private ServiceBusManagementSerializer serializer;

    @Mock
    QueueProperties queuePropertiesResult;

    @Mock
    QueueDescriptionEntryContent queueDescriptionEntryContent;
    @Mock
    QueueDescription mockQueueDesc;

    @Mock
    QueueDescriptionEntry queueDescriptionEntry;
    @Mock
    Response<Object> objectResponse;

    private final Context context = new Context("foo", "bar").addData("baz", "boo");
    private final String queueName = "some-queue";
    private final String subscriptionName = "subscriptionName";
    private final String topicName = "topicName";
    private final String ruleName = "ruleName";
    private ServiceBusAdministrationClient client;
    private final String dummyEndpoint = "endpoint.servicebus.foo";

    private AutoCloseable mockClosable;
    private ServiceBusAdministrationAsyncClient asyncClient;
    private HashMap<String, String> map = new HashMap<>();

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        mockClosable = MockitoAnnotations.openMocks(this);

        when(serviceClient.getEntities()).thenReturn(entitys);
        when(serviceClient.getEndpoint()).thenReturn(dummyEndpoint);
        when(serviceClient.getSubscriptions()).thenReturn(subscriptions);
        when(serviceClient.getRules()).thenReturn(rules);

        when(queuePropertiesResult.getName()).thenReturn(queueName);
        when(mockQueueDesc.getMaxDeliveryCount()).thenReturn(10);
        when(queueDescriptionEntry.getContent()).thenReturn(queueDescriptionEntryContent);
        map.put("", queueName);
        when(queueDescriptionEntry.getTitle()).thenReturn(map);

        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(queueDescriptionEntry);

        when(objectResponse.getValue()).thenReturn(queueDescriptionEntry);
        when(entitys.<QueueDescriptionEntry>putSyncWithResponse(any(), any(), any(), any())).thenReturn(objectResponse);

        asyncClient = new ServiceBusAdministrationAsyncClient(serviceClient, serializer);
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
        final QueueDescription queueDescription = EntityHelper.getQueueDescription(options);
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
        final QueueDescription queueDescription = EntityHelper.getQueueDescription(options);
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
        when(entitys.deleteSyncWithResponse(eq(queueName), any())).thenReturn(voidResponse);

        // Act
        client.deleteQueue(queueName);

        // Assert
        verify(entitys).deleteSyncWithResponse(eq(queueName), any());
    }

    @Test
    void deleteQueueWithResponse() {
        // Arrange
        when(entitys.deleteSyncWithResponse(any(), any())).thenReturn(voidResponse);
        when(voidResponse.getStatusCode()).thenReturn(HttpResponseStatus.NO_CONTENT.code());

        // Act
        final Response<Void> actual = client.deleteQueueWithResponse(queueName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteRule() {
        // Arrange
        when(rules.deleteSyncWithResponse(eq(topicName), eq(subscriptionName), eq(ruleName), any())).thenReturn(
            voidResponse);

        // Act
        client.deleteRule(topicName, subscriptionName, ruleName);

        // Assert
        verify(rules).deleteSyncWithResponse(eq(topicName), eq(subscriptionName), eq(ruleName), any());
    }

    @Test
    void deleteRuleWithResponse() {
        // Arrange
        when(rules.deleteSyncWithResponse(any(), any(), any(), any())).thenReturn(voidResponse);
        when(voidResponse.getStatusCode()).thenReturn(HttpResponseStatus.NO_CONTENT.code());

        // Act
        final Response<Void> actual = client.deleteRuleWithResponse(topicName, subscriptionName, ruleName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteSubscription() {
        // Arrange
        when(subscriptions.deleteSyncWithResponse(eq(topicName), eq(subscriptionName), any())).thenReturn(voidResponse);
        when(voidResponse.getStatusCode()).thenReturn(HttpResponseStatus.NO_CONTENT.code());

        // Act
        client.deleteSubscription(topicName, subscriptionName);

        // Assert
        verify(subscriptions).deleteSyncWithResponse(eq(topicName), eq(subscriptionName), any());
    }

    @Test
    void deleteSubscriptionWithResponse() {
        // Arrange
        when(subscriptions.deleteSyncWithResponse(any(), any(), any())).thenReturn(voidResponse);
        when(voidResponse.getStatusCode()).thenReturn(HttpResponseStatus.NO_CONTENT.code());

        // Act
        final Response<Void> actual = client.deleteSubscriptionWithResponse(topicName, subscriptionName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteTopic() {
        // Arrange
        when(entitys.deleteSyncWithResponse(any(), any())).thenReturn(voidResponse);

        // Act
        client.deleteTopic(topicName);

        // Assert
        verify(entitys).deleteSyncWithResponse(any(), any());
    }

    @Test
    void deleteTopicWithResponse() {
        // Arrange
        when(entitys.deleteSyncWithResponse(any(), any())).thenReturn(voidResponse);
        when(voidResponse.getStatusCode()).thenReturn(HttpResponseStatus.NO_CONTENT.code());

        // Act
        final Response<Void> actual = client.deleteTopicWithResponse(topicName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void getQueue() {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        when(entitys.<QueueDescriptionEntry>getSyncWithResponse(any(), any(), any())).thenReturn(objectResponse);

        // Act
        final QueueProperties actual = client.getQueue(queueName);

        // Assert
        assertEquals(queuePropertiesResult.getName(), actual.getName());
    }

    @Test
    void getQueueWithResponse() {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        when(entitys.<QueueDescriptionEntry>getSyncWithResponse(any(), any(), any())).thenReturn(objectResponse);

        // Act
        final Response<QueueProperties> actual = client.getQueueWithResponse(queueName, context);

        // Assert
        assertEquals(queuePropertiesResult.getName(), actual.getValue().getName());
    }

    @Test
    void getQueueRuntimeProperties() {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        when(entitys.<QueueDescriptionEntry>getSyncWithResponse(any(), any(), any())).thenReturn(objectResponse);

        // Act
        final QueueRuntimeProperties actual = client.getQueueRuntimeProperties(queueName);

        // Assert
        assertEquals(queuePropertiesResult.getName(), actual.getName());
    }

    @Test
    void getQueueRuntimePropertiesWithResponse() throws IOException {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        when(entitys.<QueueDescriptionEntry>getSyncWithResponse(any(), any(), any())).thenReturn(objectResponse);

        // Act
        final Response<QueueRuntimeProperties> actual = client.getQueueRuntimePropertiesWithResponse(queueName, context);

        // Assert
        assertEquals(queuePropertiesResult.getName(), actual.getValue().getName());
    }

    @Test
    void listQueues() throws IOException {
        // Arrange
        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(mockQueueDesc);
        final QueueDescriptionFeed feed = mock(QueueDescriptionFeed.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionFeed.class))).thenReturn(feed);
        when(feed.getEntry()).thenReturn(Arrays.asList(queueDescriptionEntry));
        when(serviceClient.<QueueDescriptionEntry>listEntitiesSyncWithResponse(any(), any(), any(), any())).thenReturn(
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
        final QueueDescriptionFeed feed = mock(QueueDescriptionFeed.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionFeed.class))).thenReturn(feed);
        when(feed.getEntry()).thenReturn(Arrays.asList(queueDescriptionEntry));
        when(feed.getLink()).thenReturn(Arrays.asList(new ResponseLink().setRel("next").setHref("https://foo.bar.net?api-version=2021-05&$skip=1"))).thenReturn(Arrays.asList(new ResponseLink().setRel("notNext")));
        when(serviceClient.<QueueDescriptionEntry>listEntitiesSyncWithResponse(any(), any(), any(), any())).thenReturn(
            objectResponse);
        final List<QueueProperties> queues = Arrays.asList(queuePropertiesResult);

        final List<QueueProperties> firstPage = queues;
        final List<QueueProperties> secondPage = queues;
        // Act
        final PagedIterable<QueueProperties> queueDescriptions = client.listQueues(context);

        // Assert
        final long size = queueDescriptions.stream().count();
        final long expectedSize = firstPage.size() + secondPage.size();

        assertEquals(expectedSize, size);
    }

    @Test
    void updateQueue() {
        // Arrange
        final CreateQueueOptions options = new CreateQueueOptions()
            .setMaxDeliveryCount(4)
            .setAutoDeleteOnIdle(Duration.ofSeconds(30));
        final QueueDescription queueDescription = EntityHelper.getQueueDescription(options);
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        final QueueProperties expected = EntityHelper.toModel(queueDescription);

        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(queueDescription);
        when(entitys.<QueueDescriptionEntry>putSyncWithResponse(any(), any(), any(), any())).thenReturn(objectResponse);

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
        final QueueDescription queueDescription = EntityHelper.getQueueDescription(options);
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        final QueueProperties expected = EntityHelper.toModel(queueDescription);

        when(queueDescriptionEntryContent.getQueueDescription()).thenReturn(queueDescription);
        when(entitys.<QueueDescriptionEntry>putSyncWithResponse(any(), any(), any(), any())).thenReturn(objectResponse);

        // Act
        final Response<QueueProperties> actual = client.updateQueueWithResponse(description, context);

        // Assert
        QueueProperties actualResult = actual.getValue();
        assertEquals(expected.getMaxDeliveryCount(), actualResult.getMaxDeliveryCount());
        assertEquals(expected.getAutoDeleteOnIdle(), actualResult.getAutoDeleteOnIdle());
    }
}
