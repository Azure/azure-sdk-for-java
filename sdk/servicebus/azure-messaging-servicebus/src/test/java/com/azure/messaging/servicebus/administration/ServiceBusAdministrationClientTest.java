// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.messaging.servicebus.administration.implementation.EntitiesImpl;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.RulesImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.administration.implementation.SubscriptionsImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.administration.implementation.models.MessageCountDetails;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryContent;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementError;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementErrorException;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.Title;
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
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClientTest.assertResponse;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClientTest.createBodyContentEquals;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClientTest.getContents;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClientTest.serializeResponse;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClientTest.verifyAdditionalAuthHeaderPresent;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServiceBusAdministrationClient}.
 */
class ServiceBusAdministrationClientTest {
    private static final String FORWARD_TO_ENTITY = "https://endpoint.servicebus.foo/forward-to-entity";

    @Mock
    private ServiceBusManagementClientImpl serviceClient;
    @Mock
    private EntitiesImpl entitys;
    @Mock
    private RulesImpl rules;
    @Mock
    private SubscriptionsImpl subscriptions;

    private final Context context = new Context("foo", "bar").addData("baz", "boo");
    private final String queueName = "some-queue";
    private final String subscriptionName = "subscriptionName";
    private final String topicName = "topicName";
    private final String ruleName = "ruleName";
    private final String forwardToEntity = "forward-to-entity";
    private final HttpHeaders httpHeaders = new HttpHeaders().set("foo", "baz");
    private final HttpRequest httpRequest = new HttpRequest(HttpMethod.TRACE, "https://something.com");

    private AutoCloseable mockClosable;
    private ServiceBusAdministrationClient client;

    @BeforeEach
    void beforeEach() {
        mockClosable = MockitoAnnotations.openMocks(this);

        when(serviceClient.getEntities()).thenReturn(entitys);
        when(serviceClient.getRules()).thenReturn(rules);
        String dummyEndpoint = "endpoint.servicebus.foo";
        when(serviceClient.getEndpoint()).thenReturn(dummyEndpoint);
        when(serviceClient.getSubscriptions()).thenReturn(subscriptions);

        client = new ServiceBusAdministrationClient(serviceClient);
    }

    private Response<Object> createObjectResponse(String responseString) {
        return new SimpleResponse<>(httpRequest, 202, httpHeaders, responseString);
    }

    private Response<Object> createSecondObjectResponse(String responseString) {
        return new SimpleResponse<>(httpRequest, 430, httpHeaders, responseString);
    }

    @AfterEach
    void afterEach() throws Exception {
        Mockito.framework().clearInlineMock(this);
        mockClosable.close();
    }

    @Test
    void createQueue() {
        // Arrange
        final String updatedName = "some-new-name";
        final CreateQueueOptions description = new CreateQueueOptions();
        final QueueDescription expectedDescription = EntityHelper.getQueueDescription(description);
        final QueueDescriptionEntry expected = new QueueDescriptionEntry().setTitle(new Title().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponse(eq(queueName), argThat(arg -> createBodyContentEquals(arg, description)), isNull(),
            any(Context.class))).thenReturn(objectResponse);

        // Act & Assert
        assertEquals(updatedName, client.createQueue(queueName, description).getName());
    }

    @Test
    void createQueueWithResponse() {
        // Arrange
        final String updatedName = "some-new-name";
        final CreateQueueOptions description = new CreateQueueOptions();
        final QueueDescription expectedDescription = EntityHelper.getQueueDescription(description);
        final QueueDescriptionEntry expected = new QueueDescriptionEntry().setTitle(new Title().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponse(eq(queueName), argThat(arg -> createBodyContentEquals(arg, description)), isNull(),
            any(Context.class))).thenReturn(objectResponse);

        // Act & Assert
        Response<QueueProperties> response = client.createQueueWithResponse(queueName, description, context);
        assertResponse(objectResponse, response);
        assertEquals(updatedName, response.getValue().getName());
    }

    @Test
    void createQueueWithForwarding() {
        // Arrange
        final String updatedName = "some-new-name";
        final CreateQueueOptions description = new CreateQueueOptions();
        description.setForwardTo(forwardToEntity);
        description.setForwardDeadLetteredMessagesTo(forwardToEntity);
        final QueueDescription expectedDescription = EntityHelper.getQueueDescription(description);
        final QueueDescriptionEntry expected = new QueueDescriptionEntry().setTitle(new Title().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponse(eq(queueName), argThat(arg -> createBodyContentEquals(arg, description)), isNull(),
            argThat(ctx -> (verifyAdditionalAuthHeaderPresent(ctx, SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                forwardToEntity)
                && verifyAdditionalAuthHeaderPresent(ctx, SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME,
                    forwardToEntity))))).thenReturn(objectResponse);

        // Act & Assert
        Response<QueueProperties> response = client.createQueueWithResponse(queueName, description, context);
        assertResponse(objectResponse, response);
        assertEquals(updatedName, response.getValue().getName());
    }

    @Test
    void deleteQueue() {
        // Arrange
        Response<Object> objectResponse = createObjectResponse(null);
        when(entitys.deleteWithResponse(eq(queueName), any(Context.class))).thenReturn(objectResponse);

        // Act & Assert
        assertDoesNotThrow(() -> client.deleteQueue(queueName));
    }

    @Test
    void deleteQueueWithResponse() {
        // Arrange
        Response<Object> objectResponse = createObjectResponse(null);
        when(entitys.deleteWithResponse(eq(queueName), any(Context.class))).thenReturn(objectResponse);

        // Act & Assert
        assertResponse(objectResponse, client.deleteQueueWithResponse(queueName, context));
    }

    @Test
    void getQueue() {
        // Arrange
        final QueueDescription expected = new QueueDescription();
        final QueueDescriptionEntry entry = new QueueDescriptionEntry().setTitle(new Title().setContent(queueName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expected));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(entry));
        when(entitys.getWithResponse(eq(queueName), eq(true), any(Context.class))).thenReturn(objectResponse);

        // Act & Assert
        assertEquals(queueName, client.getQueue(queueName).getName());
    }

    @Test
    void getQueueWithResponse() {
        // Arrange
        final String updatedName = "some-new-name";
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry().setTitle(new Title().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.getWithResponse(eq(queueName), eq(true), any(Context.class))).thenReturn(objectResponse);

        // Act & Assert
        Response<QueueProperties> response = client.getQueueWithResponse(queueName, context);
        assertResponse(objectResponse, response);
        assertEquals(updatedName, response.getValue().getName());
    }

    @Test
    void getQueueRuntimeProperties() throws IOException {
        // Arrange
        final String contents = getContents("QueueDescriptionEntry.xml");
        final ServiceBusManagementSerializer managementSerializer = new ServiceBusManagementSerializer();
        final QueueDescriptionEntry entry = managementSerializer.deserialize(contents, QueueDescriptionEntry.class);

        final String name = "my-test-queue";
        final OffsetDateTime createdAt = OffsetDateTime.parse("2020-06-05T03:55:07.5Z");
        final OffsetDateTime updatedAt = OffsetDateTime.parse("2020-06-05T03:45:07.64Z");
        final OffsetDateTime accessedAt = OffsetDateTime.parse("0001-01-01T00:00:00Z");
        final long sizeInBytes = 2048;
        final long messageCount = 23;
        final MessageCountDetails expectedCount = new MessageCountDetails().setActiveMessageCount(5)
            .setDeadLetterMessageCount(3)
            .setScheduledMessageCount(65)
            .setTransferMessageCount(10)
            .setTransferDeadLetterMessageCount(123);

        Response<Object> objectResponse = createObjectResponse(serializeResponse(entry));
        when(entitys.getWithResponse(eq(queueName), eq(true), any(Context.class))).thenReturn(objectResponse);

        // Act & Assert
        QueueRuntimeProperties info = client.getQueueRuntimeProperties(queueName);
        assertEquals(name, info.getName());
        assertEquals(messageCount, info.getTotalMessageCount());
        assertEquals(sizeInBytes, info.getSizeInBytes());
        assertEquals(createdAt, info.getCreatedAt());
        assertEquals(updatedAt, info.getUpdatedAt());
        assertEquals(accessedAt, info.getAccessedAt());

        assertEquals(expectedCount.getActiveMessageCount(), info.getActiveMessageCount());
        assertEquals(expectedCount.getDeadLetterMessageCount(), info.getDeadLetterMessageCount());
        assertEquals(expectedCount.getScheduledMessageCount(), info.getScheduledMessageCount());
        assertEquals(expectedCount.getTransferMessageCount(), info.getTransferMessageCount());
        assertEquals(expectedCount.getTransferDeadLetterMessageCount(), info.getTransferDeadLetterMessageCount());
    }

    @Test
    void getQueueRuntimePropertiesWithResponse() throws IOException {
        // Arrange
        final String contents = getContents("QueueDescriptionEntry.xml");
        final ServiceBusManagementSerializer managementSerializer = new ServiceBusManagementSerializer();
        final QueueDescriptionEntry entry = managementSerializer.deserialize(contents, QueueDescriptionEntry.class);

        final String name = "my-test-queue";
        final OffsetDateTime createdAt = OffsetDateTime.parse("2020-06-05T03:55:07.5Z");
        final OffsetDateTime updatedAt = OffsetDateTime.parse("2020-06-05T03:45:07.64Z");
        final OffsetDateTime accessedAt = OffsetDateTime.parse("0001-01-01T00:00:00Z");
        final long sizeInBytes = 2048;
        final long messageCount = 23;
        final MessageCountDetails expectedCount = new MessageCountDetails().setActiveMessageCount(5)
            .setDeadLetterMessageCount(3)
            .setScheduledMessageCount(65)
            .setTransferMessageCount(10)
            .setTransferDeadLetterMessageCount(123);

        Response<Object> objectResponse = createObjectResponse(serializeResponse(entry));
        when(entitys.getWithResponse(eq(queueName), eq(true), any(Context.class))).thenReturn(objectResponse);

        // Act & Assert
        Response<QueueRuntimeProperties> response = client.getQueueRuntimePropertiesWithResponse(queueName, context);
        assertResponse(objectResponse, response);

        final QueueRuntimeProperties info = response.getValue();
        assertEquals(name, info.getName());
        assertEquals(messageCount, info.getTotalMessageCount());
        assertEquals(sizeInBytes, info.getSizeInBytes());
        assertEquals(createdAt, info.getCreatedAt());
        assertEquals(updatedAt, info.getUpdatedAt());
        assertEquals(accessedAt, info.getAccessedAt());

        assertEquals(expectedCount.getActiveMessageCount(), info.getActiveMessageCount());
        assertEquals(expectedCount.getDeadLetterMessageCount(), info.getDeadLetterMessageCount());
        assertEquals(expectedCount.getScheduledMessageCount(), info.getScheduledMessageCount());
        assertEquals(expectedCount.getTransferMessageCount(), info.getTransferMessageCount());
        assertEquals(expectedCount.getTransferDeadLetterMessageCount(), info.getTransferDeadLetterMessageCount());
    }

    /**
     * When ServiceBusManagementError is populated in 'ServiceBusManagementErrorException' with no Http status code,
     * we get 'HttpResponseException' with error message.
     * We should always have Http status code populated but this test is to ensure we do not throw NullPointerException.
     */
    @Test
    void getSubscriptionRuntimePropertiesWithNoStatusCode() {
        // Arrange
        final String topicName = "topicName";
        final String subscriptionName = "subscriptionName";
        final String message = "Unauthorized access";
        final HttpResponse response = mock(HttpResponse.class);
        final ServiceBusManagementError managementError = new ServiceBusManagementError();
        managementError.setDetail(message);

        when(subscriptions.getWithResponse(eq(topicName), eq(subscriptionName), eq(true), any(Context.class)))
            .thenThrow(new ServiceBusManagementErrorException(message, response, managementError));

        // Act & Assert
        assertThrows(HttpResponseException.class,
            () -> client.getSubscriptionRuntimeProperties(topicName, subscriptionName));
    }

    @Test
    void listQueues() {
        // Arrange
        final int firstEntities = 7;
        final String entityType = "queues";
        final List<QueueDescriptionEntry> firstEntries = IntStream.range(0, 4).mapToObj(number -> {
            final String name = String.valueOf(number);
            final QueueDescription description = EntityHelper.getQueueDescription(new CreateQueueOptions());
            final QueueDescriptionEntryContent content
                = new QueueDescriptionEntryContent().setQueueDescription(description);
            return new QueueDescriptionEntry().setContent(content).setTitle(new Title().setContent(name));
        }).collect(Collectors.toList());
        final List<ResponseLink> links = Arrays.asList(new ResponseLink().setRel("self").setHref("foo"),
            new ResponseLink().setRel("bar").setHref("baz"), new ResponseLink().setRel("next")
                .setHref("https://foo.bar.net?api-version=2021-05&$skip=" + firstEntities));
        final QueueDescriptionFeed firstFeed
            = new QueueDescriptionFeed().setLink(links).setEntry(firstEntries).setId("first-id");

        final List<QueueDescriptionEntry> secondEntries = IntStream.range(5, 7).mapToObj(number -> {
            final String name = String.valueOf(number);
            final QueueDescription description = EntityHelper.getQueueDescription(new CreateQueueOptions());
            final QueueDescriptionEntryContent content
                = new QueueDescriptionEntryContent().setQueueDescription(description);

            return new QueueDescriptionEntry().setContent(content).setTitle(new Title().setContent(name));
        }).collect(Collectors.toList());
        final List<ResponseLink> secondLinks = Arrays.asList(new ResponseLink().setRel("self").setHref("foo"),
            new ResponseLink().setRel("bar").setHref("baz"));
        final QueueDescriptionFeed secondFeed
            = new QueueDescriptionFeed().setEntry(secondEntries).setLink(secondLinks).setId("second-id");

        Response<Object> objectResponse = createObjectResponse(serializeResponse(firstFeed));
        when(serviceClient.listEntitiesWithResponse(eq(entityType), eq(0), anyInt(), any(Context.class)))
            .thenReturn(objectResponse);
        Response<Object> secondObjectResponse = createSecondObjectResponse(serializeResponse(secondFeed));
        when(serviceClient.listEntitiesWithResponse(eq(entityType), eq(firstEntities), anyInt(), any(Context.class)))
            .thenReturn(secondObjectResponse);

        // Act & Assert
        assertEquals(firstEntries.size() + secondEntries.size(), client.listQueues().stream().count());
    }

    @Test
    void listQueuesWithContext() {
        // Arrange
        final int firstEntities = 7;
        final String entityType = "queues";
        final List<QueueDescriptionEntry> firstEntries = IntStream.range(0, 4).mapToObj(number -> {
            final String name = String.valueOf(number);
            final QueueDescription description = EntityHelper.getQueueDescription(new CreateQueueOptions());
            final QueueDescriptionEntryContent content
                = new QueueDescriptionEntryContent().setQueueDescription(description);
            return new QueueDescriptionEntry().setContent(content).setTitle(new Title().setContent(name));
        }).collect(Collectors.toList());
        final List<ResponseLink> links = Arrays.asList(new ResponseLink().setRel("self").setHref("foo"),
            new ResponseLink().setRel("bar").setHref("baz"), new ResponseLink().setRel("next")
                .setHref("https://foo.bar.net?api-version=2021-05&$skip=" + firstEntities));
        final QueueDescriptionFeed firstFeed
            = new QueueDescriptionFeed().setLink(links).setEntry(firstEntries).setId("first-id");

        final List<QueueDescriptionEntry> secondEntries = IntStream.range(5, 7).mapToObj(number -> {
            final String name = String.valueOf(number);
            final QueueDescription description = EntityHelper.getQueueDescription(new CreateQueueOptions());
            final QueueDescriptionEntryContent content
                = new QueueDescriptionEntryContent().setQueueDescription(description);

            return new QueueDescriptionEntry().setContent(content).setTitle(new Title().setContent(name));
        }).collect(Collectors.toList());
        final List<ResponseLink> secondLinks = Arrays.asList(new ResponseLink().setRel("self").setHref("foo"),
            new ResponseLink().setRel("bar").setHref("baz"));
        final QueueDescriptionFeed secondFeed
            = new QueueDescriptionFeed().setEntry(secondEntries).setLink(secondLinks).setId("second-id");

        Response<Object> objectResponse = createObjectResponse(serializeResponse(firstFeed));
        when(serviceClient.listEntitiesWithResponse(eq(entityType), eq(0), anyInt(), any(Context.class)))
            .thenReturn(objectResponse);
        Response<Object> secondObjectResponse = createSecondObjectResponse(serializeResponse(secondFeed));
        when(serviceClient.listEntitiesWithResponse(eq(entityType), eq(firstEntities), anyInt(), any(Context.class)))
            .thenReturn(secondObjectResponse);

        // Act & Assert
        assertEquals(firstEntries.size() + secondEntries.size(), client.listQueues(context).stream().count());
    }

    @Test
    void updateQueue() {
        // Arrange
        final QueueDescription description = new QueueDescription();
        final QueueProperties properties = EntityHelper.toModel(description);
        EntityHelper.setQueueName(properties, queueName);

        final String updatedName = "some-new-name";
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry().setTitle(new Title().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponse(eq(queueName), argThat(arg -> {
            if (!(arg instanceof CreateQueueBody)) {
                return false;
            }

            final CreateQueueBody argument = (CreateQueueBody) arg;
            return argument.getContent() != null && argument.getContent().getQueueDescription() != null;
        }), eq("*"), any(Context.class))).thenReturn(objectResponse);

        // Act & Assert
        assertEquals(updatedName, client.updateQueue(properties).getName());
    }

    @Test
    void updateQueueWithResponse() {
        // Arrange
        final QueueDescription description = new QueueDescription();
        description.setForwardTo(forwardToEntity);
        final QueueProperties properties = EntityHelper.toModel(description);
        EntityHelper.setQueueName(properties, queueName);

        final String updatedName = "some-new-name";
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry().setTitle(new Title().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponse(eq(queueName), argThat(arg -> {
            if (!(arg instanceof CreateQueueBody)) {
                return false;
            }

            final CreateQueueBody argument = (CreateQueueBody) arg;
            if (argument.getContent() == null || argument.getContent().getQueueDescription() == null) {
                return false;
            }
            assertEquals(FORWARD_TO_ENTITY, argument.getContent().getQueueDescription().getForwardTo(),
                "Update queue does not set the forward-to-entity to an absolute URL");
            return true;
        }), eq("*"), argThat(ctx -> verifyAdditionalAuthHeaderPresent(ctx,
            SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME, forwardToEntity)))).thenReturn(objectResponse);

        // Act & Assert
        Response<QueueProperties> response = client.updateQueueWithResponse(properties, context);
        assertResponse(objectResponse, response);
        assertEquals(updatedName, response.getValue().getName());
    }

    @Test
    void deleteRule() {
        // Arrange
        Response<RuleDescriptionEntry> voidResponse = new SimpleResponse<>(null, 0, null, null);
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
        Response<RuleDescriptionEntry> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(rules.deleteWithResponse(any(), any(), any(), any())).thenReturn(voidResponse);

        // Act
        final Response<Void> actual = client.deleteRuleWithResponse(topicName, subscriptionName, ruleName, context);

        // Assert
        assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    @Test
    void deleteSubscription() {
        // Arrange
        Response<SubscriptionDescriptionEntry> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(subscriptions.deleteWithResponse(eq(topicName), eq(subscriptionName), any())).thenReturn(voidResponse);

        // Act
        client.deleteSubscription(topicName, subscriptionName);

        // Assert
        verify(subscriptions).deleteWithResponse(eq(topicName), eq(subscriptionName), any());
    }

    @Test
    void deleteSubscriptionWithResponse() {
        // Arrange
        Response<SubscriptionDescriptionEntry> voidResponse = new SimpleResponse<>(null, 204, null, null);
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
}
