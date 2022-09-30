// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
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
import java.net.MalformedURLException;
import java.net.URL;
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
    private Response<QueueProperties> queueDescriptionResponse;
    @Mock
    private Response<QueueRuntimeProperties> queueRuntimePropertiesResponse;
    @Mock
    private Response<Object> response;
    @Mock
    private PagedResponse<QueueProperties> pagedResponse;
    @Mock
    private PagedResponse<QueueProperties> continuationPagedResponse;
    @Mock
    private EntitiesImpl entitys;

    @Mock
    private RulesImpl rules;
    @Mock
    private SubscriptionsImpl subscriptions;
    @Mock
    private ServiceBusManagementSerializer serializer;
    @Mock
    private Response<Object> objectResponse;
    @Mock
    private Response<Object> secondObjectResponse;

    private final Context context = new Context("foo", "bar").addData("baz", "boo");
    private final String queueName = "some-queue";
    private final String subscriptionName = "subscriptionName";
    private final String topicName = "topicName";
    private final String ruleName = "ruleName";
    private ServiceBusAdministrationClient client;
    private final String responseString = "some-xml-response-string";
    private final String secondResponseString = "second-xml-response";
    private final String dummyEndpoint = "endpoint.servicebus.foo";
    private final String forwardToEntity = "forward-to-entity";
    private final HttpHeaders httpHeaders = new HttpHeaders().put("foo", "baz");
    private final HttpRequest httpRequest;

    private AutoCloseable mockClosable;
    private ServiceBusAdministrationAsyncClient asyncClient;

    ServiceBusAdministrationClientTest() {
        try {
            httpRequest = new HttpRequest(HttpMethod.TRACE, new URL("https://something.com"));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not form URL.", e);
        }
    }

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void beforeEach() {
        mockClosable = MockitoAnnotations.openMocks(this);

        when(objectResponse.getValue()).thenReturn(responseString);
        int statusCode = 202;
        when(objectResponse.getStatusCode()).thenReturn(statusCode);
        when(objectResponse.getHeaders()).thenReturn(httpHeaders);
        when(objectResponse.getRequest()).thenReturn(httpRequest);

        when(secondObjectResponse.getValue()).thenReturn(secondResponseString);
        when(secondObjectResponse.getStatusCode()).thenReturn(430);
        when(secondObjectResponse.getHeaders()).thenReturn(httpHeaders);
        when(secondObjectResponse.getRequest()).thenReturn(httpRequest);

        when(response.getStatusCode()).thenReturn(HttpResponseStatus.NO_CONTENT.code());
        when(response.getRequest()).thenReturn(null);
        when(response.getRequest()).thenReturn(null);

        when(serviceClient.getEntities()).thenReturn(entitys);
        when(serviceClient.getEndpoint()).thenReturn(dummyEndpoint);
        when(serviceClient.getSubscriptions()).thenReturn(subscriptions);
        when(serviceClient.getRules()).thenReturn(rules);

        asyncClient = new ServiceBusAdministrationAsyncClient(serviceClient, serializer);
        client = new ServiceBusAdministrationClient(serviceClient, serializer);
    }

    @AfterEach
    void afterEach() throws Exception {
        Mockito.framework().clearInlineMock(this);
        mockClosable.close();
    }

    @Test
    void createQueue() throws IOException {
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

        final QueueProperties result = mock(QueueProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription mockDesc = mock(QueueDescription.class);
        when(mockDesc.getMaxDeliveryCount()).thenReturn(10);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(queueDescription);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(response1.getValue()).thenReturn(result1);
        when(entitys.<QueueDescriptionEntry>putSyncWithResponse(any(), any(), any(), any())).thenReturn(response1);

        // Act
        final QueueProperties actual = client.createQueue(queueName, description);

        // Assert
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
    }

    @Test
    void createQueueWithResponse() throws IOException {
        // Arrange
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

        final QueueProperties result = mock(QueueProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription mockDesc = mock(QueueDescription.class);
        when(mockDesc.getMaxDeliveryCount()).thenReturn(10);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(queueDescription);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(response1.getValue()).thenReturn(result1);
        when(entitys.<QueueDescriptionEntry>putSyncWithResponse(any(), any(), any(), any())).thenReturn(response1);

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
        when(entitys.deleteSyncWithResponse(eq(queueName), any())).thenReturn(response);

        // Act
        client.deleteQueue(queueName);

        // Assert
        verify(entitys).deleteSyncWithResponse(eq(queueName), any());
    }

    @Test
    void deleteQueueWithResponse() {
        // Arrange
        when(entitys.deleteSyncWithResponse(any(), any())).thenReturn(response);

        // Act
        final Response<Void> actual = client.deleteQueueWithResponse(queueName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteRule() {
        // Arrange
        when(rules.deleteSyncWithResponse(eq(topicName), eq(subscriptionName), eq(ruleName), any())).thenReturn(response);

        // Act
        client.deleteRule(topicName, subscriptionName, ruleName);

        // Assert
        verify(rules).deleteSyncWithResponse(eq(topicName), eq(subscriptionName), eq(ruleName), any());
    }

    @Test
    void deleteRuleWithResponse() {
        // Arrange
        when(rules.deleteSyncWithResponse(any(), any(), any(), any())).thenReturn(response);

        // Act
        final Response<Void> actual = client.deleteRuleWithResponse(topicName, subscriptionName, ruleName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteSubscription() {
        // Arrange
        when(subscriptions.deleteSyncWithResponse(eq(topicName), eq(subscriptionName), any())).thenReturn(response);

        // Act
        client.deleteSubscription(topicName, subscriptionName);

        // Assert
        verify(subscriptions).deleteSyncWithResponse(eq(topicName), eq(subscriptionName), any());
    }

    @Test
    void deleteSubscriptionWithResponse() {
        // Arrange
        when(subscriptions.deleteSyncWithResponse(any(), any(), any())).thenReturn(response);

        // Act
        final Response<Void> actual = client.deleteSubscriptionWithResponse(topicName, subscriptionName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteTopic() {
        // Arrange
        when(entitys.deleteSyncWithResponse(any(), any())).thenReturn(response);

        // Act
        client.deleteTopic(topicName);

        // Assert
        verify(entitys).deleteSyncWithResponse(any(), any());
    }

    @Test
    void deleteTopicWithResponse() {
        // Arrange
        when(entitys.deleteSyncWithResponse(any(), any())).thenReturn(response);

        // Act
        final Response<Void> actual = client.deleteTopicWithResponse(topicName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void getQueue() throws IOException {
        // Arrange
        final QueueProperties result = mock(QueueProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription description = mock(QueueDescription.class);
        when(description.getMessageCount()).thenReturn(1);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(description);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(response1.getValue()).thenReturn(result1);
        when(entitys.<QueueDescriptionEntry>getSyncWithResponse(any(), any(), any())).thenReturn(response1);

        // Act
        final QueueProperties actual = client.getQueue(queueName);

        // Assert
        assertEquals(result.getName(), actual.getName());
    }

    @Test
    void getQueueWithResponse() throws IOException {
        // Arrange
        final QueueProperties result = mock(QueueProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription description = mock(QueueDescription.class);
        when(description.getMessageCount()).thenReturn(1);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(description);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(response1.getValue()).thenReturn(result1);
        when(entitys.<QueueDescriptionEntry>getSyncWithResponse(any(), any(), any())).thenReturn(response1);

        // Act
        final Response<QueueProperties> actual = client.getQueueWithResponse(queueName, context);

        // Assert
        assertEquals(result.getName(), actual.getValue().getName());
    }

    @Test
    void getQueueRuntimeProperties() throws IOException {
        // Arrange
        final QueueRuntimeProperties result = mock(QueueRuntimeProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription description = mock(QueueDescription.class);
        when(description.getMessageCount()).thenReturn(1);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(description);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(response1.getValue()).thenReturn(result1);
        when(entitys.<QueueDescriptionEntry>getSyncWithResponse(any(), any(), any())).thenReturn(response1);

        // Act
        final QueueRuntimeProperties actual = client.getQueueRuntimeProperties(queueName);

        // Assert
        assertEquals(result.getName(), actual.getName());
    }

    @Test
    void getQueueRuntimePropertiesWithResponse() throws IOException {
        // Arrange
        final QueueRuntimeProperties result = mock(QueueRuntimeProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription description = mock(QueueDescription.class);
        when(description.getMessageCount()).thenReturn(1);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(description);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(response1.getValue()).thenReturn(result1);
        when(entitys.<QueueDescriptionEntry>getSyncWithResponse(any(), any(), any())).thenReturn(response1);

        // Act
        final Response<QueueRuntimeProperties> actual = client.getQueueRuntimePropertiesWithResponse(queueName, context);

        // Assert
        assertEquals(result.getName(), actual.getValue().getName());
    }

    @Test
    void listQueues() throws IOException {
        // Arrange
        final QueueProperties result = mock(QueueProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription description = mock(QueueDescription.class);
        when(description.getMessageCount()).thenReturn(1);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(description);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);
        final QueueDescriptionFeed feed = mock(QueueDescriptionFeed.class);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(serializer.deserialize(anyString(), eq(QueueDescriptionFeed.class))).thenReturn(feed);
        when(feed.getEntry()).thenReturn(Arrays.asList(result1));
        when(response1.getValue()).thenReturn(result1);
        when(serviceClient.<QueueDescriptionEntry>listEntitiesSyncWithResponse(any(), any(), any(), any())).thenReturn(response1);
        final List<QueueProperties> queues = Arrays.asList(result);

        // Act
        final PagedIterable<QueueProperties> queueDescriptions = client.listQueues();

        // Assert
        final long size = queueDescriptions.stream().count();
        assertEquals(queues.size(), size);
    }

    @Test
    void listQueuesWithContext() throws IOException {
        // Arrange
        final QueueProperties result = mock(QueueProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription description = mock(QueueDescription.class);
        when(description.getMessageCount()).thenReturn(1);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(description);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);
        final QueueDescriptionFeed feed = mock(QueueDescriptionFeed.class);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(serializer.deserialize(anyString(), eq(QueueDescriptionFeed.class))).thenReturn(feed);
        when(feed.getEntry()).thenReturn(Arrays.asList(result1));
        when(feed.getLink()).thenReturn(Arrays.asList(new ResponseLink().setRel("next").setHref("https://foo.bar.net?api-version=2021-05&$skip=1"))).thenReturn(Arrays.asList(new ResponseLink().setRel("notNext")));
        when(response1.getValue()).thenReturn(result1);
        when(serviceClient.<QueueDescriptionEntry>listEntitiesSyncWithResponse(any(), any(), any(), any())).thenReturn(response1);
        final List<QueueProperties> queues = Arrays.asList(result);

        final String continuationToken = "foo";
        final String lastToken = "last";
        final List<QueueProperties> firstPage = queues;
        final List<QueueProperties> secondPage = queues;

        when(pagedResponse.getElements()).thenReturn(new IterableStream<>(firstPage));
        when(pagedResponse.getValue()).thenReturn(firstPage);
        when(pagedResponse.getStatusCode()).thenReturn(200);
        when(pagedResponse.getHeaders()).thenReturn(new HttpHeaders());
        when(pagedResponse.getContinuationToken()).thenReturn(continuationToken);

        when(continuationPagedResponse.getElements()).thenReturn(new IterableStream<>(secondPage));
        when(continuationPagedResponse.getValue()).thenReturn(firstPage);
        when(continuationPagedResponse.getStatusCode()).thenReturn(200);
        when(continuationPagedResponse.getHeaders()).thenReturn(new HttpHeaders());
        when(continuationPagedResponse.getContinuationToken()).thenReturn(lastToken);

        // Act
        final PagedIterable<QueueProperties> queueDescriptions = client.listQueues(context);

        // Assert
        final long size = queueDescriptions.stream().count();
        final long expectedSize = firstPage.size() + secondPage.size();

        assertEquals(expectedSize, size);
    }

    @Test
    void updateQueue() throws IOException {
        // Arrange
        final CreateQueueOptions options = new CreateQueueOptions()
            .setMaxDeliveryCount(4)
            .setAutoDeleteOnIdle(Duration.ofSeconds(30));
        final QueueDescription queueDescription = EntityHelper.getQueueDescription(options);
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        final QueueProperties expected = EntityHelper.toModel(queueDescription);

        final QueueProperties result = mock(QueueProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription mockDesc = mock(QueueDescription.class);
        when(mockDesc.getMaxDeliveryCount()).thenReturn(1);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(queueDescription);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(response1.getValue()).thenReturn(result1);
        when(entitys.<QueueDescriptionEntry>putSyncWithResponse(any(), any(), any(), any())).thenReturn(response1);

        // Act
        final QueueProperties actual = client.updateQueue(description);

        // Assert
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
    }

    @Test
    void updateQueueWithResponse() throws IOException {
        // Arrange
        final CreateQueueOptions options = new CreateQueueOptions()
            .setMaxDeliveryCount(4)
            .setAutoDeleteOnIdle(Duration.ofSeconds(30));
        final QueueDescription queueDescription = EntityHelper.getQueueDescription(options);
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        final QueueProperties expected = EntityHelper.toModel(queueDescription);

        final QueueProperties result = mock(QueueProperties.class);
        when(result.getName()).thenReturn(queueName);
        final QueueDescriptionEntryContent content = mock(QueueDescriptionEntryContent.class);
        final QueueDescription mockDesc = mock(QueueDescription.class);
        when(mockDesc.getMaxDeliveryCount()).thenReturn(1);
        final QueueDescriptionEntry result1 = mock(QueueDescriptionEntry.class);
        when(result1.getContent()).thenReturn(content);
        when(content.getQueueDescription()).thenReturn(queueDescription);
        HashMap<String, String> map = new HashMap<>();
        map.put("", queueName);
        when(result1.getTitle()).thenReturn(map);

        final Response<Object> response1 = mock(Response.class);
        when(serializer.deserialize(anyString(), eq(QueueDescriptionEntry.class))).thenReturn(result1);

        when(response1.getValue()).thenReturn(result1);
        when(entitys.<QueueDescriptionEntry>putSyncWithResponse(any(), any(), any(), any())).thenReturn(response1);

        // Act
        final Response<QueueProperties> actual = client.updateQueueWithResponse(description, context);

        // Assert
        QueueProperties actualResult = actual.getValue();
        assertEquals(expected.getMaxDeliveryCount(), actualResult.getMaxDeliveryCount());
        assertEquals(expected.getAutoDeleteOnIdle(), actualResult.getAutoDeleteOnIdle());
    }
}
