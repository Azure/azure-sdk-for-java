// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
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
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBodyContentImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBodyImpl;
import com.azure.messaging.servicebus.administration.implementation.models.MessageCountDetailsImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryContentImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeedImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.ResponseLinkImpl;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementError;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementErrorException;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.TitleImpl;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlWriter;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.azure.core.http.policy.AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServiceBusAdministrationAsyncClient}.
 */
class ServiceBusAdministrationAsyncClientTest {
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final String FORWARD_TO_ENTITY = "https://endpoint.servicebus.foo/forward-to-entity";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    @Mock
    private ServiceBusManagementClientImpl serviceClient;
    @Mock
    private EntitiesImpl entitys;
    @Mock
    private RulesImpl rules;
    @Mock
    private SubscriptionsImpl subscriptions;

    private final String queueName = "some-queue";
    private final String subscriptionName = "subscriptionName";
    private final String topicName = "topicName";
    private final String ruleName = "ruleName";
    private final String forwardToEntity = "forward-to-entity";
    private final HttpHeaders httpHeaders = new HttpHeaders().set("foo", "baz");
    private final HttpRequest httpRequest = new HttpRequest(HttpMethod.TRACE, "https://something.com");

    private AutoCloseable mockClosable;
    private ServiceBusAdministrationAsyncClient client;

    @BeforeEach
    void beforeEach() {
        mockClosable = MockitoAnnotations.openMocks(this);

        when(serviceClient.getEntities()).thenReturn(entitys);
        when(serviceClient.getRules()).thenReturn(rules);
        String dummyEndpoint = "endpoint.servicebus.foo";
        when(serviceClient.getEndpoint()).thenReturn(dummyEndpoint);
        when(serviceClient.getSubscriptions()).thenReturn(subscriptions);

        client = new ServiceBusAdministrationAsyncClient(serviceClient);
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
        final QueueDescriptionImpl expectedDescription = EntityHelper.getQueueDescription(description);
        final QueueDescriptionEntryImpl expected = new QueueDescriptionEntryImpl()
            .setTitle(new TitleImpl().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContentImpl().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponseAsync(eq(queueName),
            argThat(arg -> createBodyContentEquals(arg, description)), isNull(), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, description))
            .assertNext(e -> assertEquals(updatedName, e.getName()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void createQueueWithResponse() {
        // Arrange
        final String updatedName = "some-new-name";
        final CreateQueueOptions description = new CreateQueueOptions();
        final QueueDescriptionImpl expectedDescription = EntityHelper.getQueueDescription(description);
        final QueueDescriptionEntryImpl expected = new QueueDescriptionEntryImpl()
            .setTitle(new TitleImpl().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContentImpl().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponseAsync(eq(queueName),
            argThat(arg -> createBodyContentEquals(arg, description)), isNull(), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.createQueueWithResponse(queueName, description))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(updatedName, response.getValue().getName());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void createQueueWithForwarding() {
        // Arrange
        final String updatedName = "some-new-name";
        final CreateQueueOptions description = new CreateQueueOptions();
        description.setForwardTo(forwardToEntity);
        description.setForwardDeadLetteredMessagesTo(forwardToEntity);
        final QueueDescriptionImpl expectedDescription = EntityHelper.getQueueDescription(description);
        final QueueDescriptionEntryImpl expected = new QueueDescriptionEntryImpl()
            .setTitle(new TitleImpl().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContentImpl().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponseAsync(eq(queueName),
            argThat(arg -> createBodyContentEquals(arg, description)), isNull(),
            argThat(ctx -> (verifyAdditionalAuthHeaderPresent(ctx,
                SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME, forwardToEntity)
                && verifyAdditionalAuthHeaderPresent(ctx,
                SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME, forwardToEntity)))))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.createQueueWithResponse(queueName, description))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(updatedName, response.getValue().getName());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void deleteQueue() {
        // Arrange
        Response<Object> objectResponse = createObjectResponse(null);
        when(entitys.deleteWithResponseAsync(eq(queueName), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.deleteQueue(queueName))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void deleteQueueWithResponse() {
        // Arrange
        Response<Object> objectResponse = createObjectResponse(null);
        when(entitys.deleteWithResponseAsync(eq(queueName), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.deleteQueueWithResponse(queueName))
            .assertNext(response -> assertResponse(objectResponse, response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void getQueue() {
        // Arrange
        final QueueDescriptionImpl expected = new QueueDescriptionImpl();
        final QueueDescriptionEntryImpl entry = new QueueDescriptionEntryImpl()
            .setTitle(new TitleImpl().setContent(queueName))
            .setContent(new QueueDescriptionEntryContentImpl().setQueueDescription(expected));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(entry));
        when(entitys.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .assertNext(e -> assertEquals(queueName, e.getName()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void getQueueWithResponse() {
        // Arrange
        final String updatedName = "some-new-name";
        final QueueDescriptionImpl expectedDescription = new QueueDescriptionImpl();
        final QueueDescriptionEntryImpl expected = new QueueDescriptionEntryImpl()
            .setTitle(new TitleImpl().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContentImpl().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.getQueueWithResponse(queueName))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(updatedName, response.getValue().getName());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void getQueueRuntimeProperties() throws IOException {
        // Arrange
        final String contents = getContents("QueueDescriptionEntry.xml");
        final ServiceBusManagementSerializer managementSerializer = new ServiceBusManagementSerializer();
        final QueueDescriptionEntryImpl entry = managementSerializer.deserialize(contents, QueueDescriptionEntryImpl.class);

        final String name = "my-test-queue";
        final OffsetDateTime createdAt = OffsetDateTime.parse("2020-06-05T03:55:07.5Z");
        final OffsetDateTime updatedAt = OffsetDateTime.parse("2020-06-05T03:45:07.64Z");
        final OffsetDateTime accessedAt = OffsetDateTime.parse("0001-01-01T00:00:00Z");
        final long sizeInBytes = 2048;
        final long messageCount = 23;
        final MessageCountDetailsImpl expectedCount = new MessageCountDetailsImpl()
            .setActiveMessageCount(5)
            .setDeadLetterMessageCount(3)
            .setScheduledMessageCount(65)
            .setTransferMessageCount(10)
            .setTransferDeadLetterMessageCount(123);

        Response<Object> objectResponse = createObjectResponse(serializeResponse(entry));
        when(entitys.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeProperties(queueName))
            .assertNext(info -> {
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
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void getQueueRuntimePropertiesWithResponse() throws IOException {
        // Arrange
        final String contents = getContents("QueueDescriptionEntry.xml");
        final ServiceBusManagementSerializer managementSerializer = new ServiceBusManagementSerializer();
        final QueueDescriptionEntryImpl entry = managementSerializer.deserialize(contents, QueueDescriptionEntryImpl.class);

        final String name = "my-test-queue";
        final OffsetDateTime createdAt = OffsetDateTime.parse("2020-06-05T03:55:07.5Z");
        final OffsetDateTime updatedAt = OffsetDateTime.parse("2020-06-05T03:45:07.64Z");
        final OffsetDateTime accessedAt = OffsetDateTime.parse("0001-01-01T00:00:00Z");
        final long sizeInBytes = 2048;
        final long messageCount = 23;
        final MessageCountDetailsImpl expectedCount = new MessageCountDetailsImpl()
            .setActiveMessageCount(5)
            .setDeadLetterMessageCount(3)
            .setScheduledMessageCount(65)
            .setTransferMessageCount(10)
            .setTransferDeadLetterMessageCount(123);

        Response<Object> objectResponse = createObjectResponse(serializeResponse(entry));
        when(entitys.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimePropertiesWithResponse(queueName))
            .assertNext(response -> {
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
                assertEquals(expectedCount.getTransferDeadLetterMessageCount(),
                    info.getTransferDeadLetterMessageCount());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * When ServiceBusManagementError is not populated or 'null' in 'ServiceBusManagementErrorException', we get
     * 'ClientAuthenticationException' with error message.
     */
    @ParameterizedTest
    @MethodSource
    void getSubscriptionRuntimePropertiesUnauthorised(String errorMessage, ServiceBusManagementError managementError) {
        // Arrange
        final String topicName = "topicName";
        final String subscriptionName = "subscriptionName";
        final HttpResponse response = mock(HttpResponse.class);
        when(subscriptions.getWithResponseAsync(eq(topicName), eq(subscriptionName), eq(true), any(Context.class)))
            .thenReturn(Mono.error(new ServiceBusManagementErrorException(errorMessage, response, managementError)));
        when(response.getStatusCode()).thenReturn(HTTP_UNAUTHORIZED);

        // Act & Assert
        StepVerifier.create(client.getSubscriptionRuntimeProperties(topicName, subscriptionName))
            .expectErrorMatches(error -> error instanceof ClientAuthenticationException
                && error.getMessage().equals(errorMessage))
            .verify(DEFAULT_TIMEOUT);
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

        when(subscriptions.getWithResponseAsync(eq(topicName), eq(subscriptionName), eq(true), any(Context.class)))
            .thenReturn(Mono.error(new ServiceBusManagementErrorException(message, response, managementError)));

        // Act & Assert
        StepVerifier.create(client.getSubscriptionRuntimeProperties(topicName, subscriptionName))
            .verifyErrorMatches(error -> error instanceof HttpResponseException);
    }

    @Test
    void listQueues() {
        // Arrange
        final int firstEntities = 7;
        final String entityType = "queues";
        final List<QueueDescriptionEntryImpl> firstEntries = IntStream.range(0, 4).mapToObj(number -> {
            final String name = String.valueOf(number);
            final QueueDescriptionImpl description = EntityHelper.getQueueDescription(new CreateQueueOptions());
            final QueueDescriptionEntryContentImpl content = new QueueDescriptionEntryContentImpl()
                .setQueueDescription(description);
            return new QueueDescriptionEntryImpl()
                .setContent(content)
                .setTitle(new TitleImpl().setContent(name));
        }).collect(Collectors.toList());
        final List<ResponseLinkImpl> links = Arrays.asList(
            new ResponseLinkImpl().setRel("self").setHref("foo"),
            new ResponseLinkImpl().setRel("bar").setHref("baz"),
            new ResponseLinkImpl().setRel("next").setHref("https://foo.bar.net?api-version=2021-05&$skip=" + firstEntities)
        );
        final QueueDescriptionFeedImpl firstFeed = new QueueDescriptionFeedImpl()
            .setLink(links)
            .setEntry(firstEntries)
            .setId("first-id");

        final List<QueueDescriptionEntryImpl> secondEntries = IntStream.range(5, 7).mapToObj(number -> {
            final String name = String.valueOf(number);
            final QueueDescriptionImpl description = EntityHelper.getQueueDescription(new CreateQueueOptions());
            final QueueDescriptionEntryContentImpl content = new QueueDescriptionEntryContentImpl()
                .setQueueDescription(description);

            return new QueueDescriptionEntryImpl()
                .setContent(content)
                .setTitle(new TitleImpl().setContent(name));
        }).collect(Collectors.toList());
        final List<ResponseLinkImpl> secondLinks = Arrays.asList(
            new ResponseLinkImpl().setRel("self").setHref("foo"),
            new ResponseLinkImpl().setRel("bar").setHref("baz"));
        final QueueDescriptionFeedImpl secondFeed = new QueueDescriptionFeedImpl()
            .setEntry(secondEntries)
            .setLink(secondLinks)
            .setId("second-id");

        Response<Object> objectResponse = createObjectResponse(serializeResponse(firstFeed));
        when(serviceClient.listEntitiesWithResponseAsync(eq(entityType), eq(0), anyInt(), any(Context.class)))
            .thenReturn(Mono.fromCallable(() -> objectResponse));
        Response<Object> secondObjectResponse = createSecondObjectResponse(serializeResponse(secondFeed));
        when(serviceClient.listEntitiesWithResponseAsync(eq(entityType), eq(firstEntities), anyInt(), any(Context.class)))
            .thenReturn(Mono.fromCallable(() -> secondObjectResponse));

        // Act & Assert
        StepVerifier.create(client.listQueues())
            .expectNextCount(firstEntries.size())
            .expectNextCount(secondEntries.size())
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void updateQueue() {
        // Arrange
        final QueueDescriptionImpl description = new QueueDescriptionImpl();
        final QueueProperties properties = EntityHelper.toModel(description);
        EntityHelper.setQueueName(properties, queueName);

        final String updatedName = "some-new-name";
        final QueueDescriptionImpl expectedDescription = new QueueDescriptionImpl();
        final QueueDescriptionEntryImpl expected = new QueueDescriptionEntryImpl()
            .setTitle(new TitleImpl().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContentImpl().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponseAsync(eq(queueName),
            argThat(arg -> {
                if (!(arg instanceof CreateQueueBodyImpl)) {
                    return false;
                }

                final CreateQueueBodyImpl argument = (CreateQueueBodyImpl) arg;
                return argument.getContent() != null && argument.getContent().getQueueDescription() != null;
            }),
            eq("*"),
            any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.updateQueue(properties))
            .assertNext(e -> assertEquals(updatedName, e.getName()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void updateQueueWithResponse() {
        // Arrange
        final QueueDescriptionImpl description = new QueueDescriptionImpl();
        description.setForwardTo(forwardToEntity);
        final QueueProperties properties = EntityHelper.toModel(description);
        EntityHelper.setQueueName(properties, queueName);

        final String updatedName = "some-new-name";
        final QueueDescriptionImpl expectedDescription = new QueueDescriptionImpl();
        final QueueDescriptionEntryImpl expected = new QueueDescriptionEntryImpl()
            .setTitle(new TitleImpl().setContent(updatedName))
            .setContent(new QueueDescriptionEntryContentImpl().setQueueDescription(expectedDescription));

        Response<Object> objectResponse = createObjectResponse(serializeResponse(expected));
        when(entitys.putWithResponseAsync(eq(queueName),
            argThat(arg -> {
                if (!(arg instanceof CreateQueueBodyImpl)) {
                    return false;
                }

                final CreateQueueBodyImpl argument = (CreateQueueBodyImpl) arg;
                if (argument.getContent() == null || argument.getContent().getQueueDescription() == null) {
                    return false;
                }
                assertEquals(FORWARD_TO_ENTITY, argument.getContent().getQueueDescription().getForwardTo(),
                    "Update queue does not set the forward-to-entity to an absolute URL");
                return true;
            }),
            eq("*"),
            argThat(ctx -> verifyAdditionalAuthHeaderPresent(ctx,
                SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME, forwardToEntity))))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.updateQueueWithResponse(properties))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(updatedName, response.getValue().getName());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void deleteRule() {
        // Arrange
        Response<RuleDescriptionEntryImpl> voidResponse = new SimpleResponse<>(null, 0, null, null);
        when(rules.deleteWithResponseAsync(eq(topicName), eq(subscriptionName), eq(ruleName), any()))
            .thenReturn(Mono.just(voidResponse));

        // Act
        client.deleteRule(topicName, subscriptionName, ruleName).block();

        // Assert
        verify(rules).deleteWithResponseAsync(eq(topicName), eq(subscriptionName), eq(ruleName), any());
    }

    @Test
    void deleteRuleWithResponse() {
        // Arrange
        Response<RuleDescriptionEntryImpl> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(rules.deleteWithResponseAsync(any(), any(), any(), any()))
            .thenReturn(Mono.just(voidResponse));

        // Act & Assert
        StepVerifier.create(client.deleteRuleWithResponse(topicName, subscriptionName, ruleName))
            .assertNext(actual -> assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void deleteSubscription() {
        // Arrange
        Response<SubscriptionDescriptionEntryImpl> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(subscriptions.deleteWithResponseAsync(eq(topicName), eq(subscriptionName), any()))
            .thenReturn(Mono.just(voidResponse));

        // Act
        client.deleteSubscription(topicName, subscriptionName).block();

        // Assert
        verify(subscriptions).deleteWithResponseAsync(eq(topicName), eq(subscriptionName), any());
    }

    @Test
    void deleteSubscriptionWithResponse() {
        // Arrange
        Response<SubscriptionDescriptionEntryImpl> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(subscriptions.deleteWithResponseAsync(any(), any(), any()))
            .thenReturn(Mono.just(voidResponse));

        // Act & Assert
        StepVerifier.create(client.deleteSubscriptionWithResponse(topicName, subscriptionName))
            .assertNext(actual -> assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void deleteTopic() {
        // Arrange
        Response<Object> voidResponse = new SimpleResponse<>(null, 0, null, null);
        when(entitys.deleteWithResponseAsync(any(), any())).thenReturn(Mono.just(voidResponse));

        // Act
        client.deleteTopic(topicName).block();

        // Assert
        verify(entitys).deleteWithResponseAsync(any(), any());
    }

    @Test
    void deleteTopicWithResponse() {
        // Arrange
        Response<Object> voidResponse = new SimpleResponse<>(null, 204, null, null);
        when(entitys.deleteWithResponseAsync(any(), any())).thenReturn(Mono.just(voidResponse));

        // Act & Assert
        StepVerifier.create(client.deleteTopicWithResponse(topicName))
            .assertNext(actual -> assertEquals(actual.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    static Stream<Arguments> getSubscriptionRuntimePropertiesUnauthorised() {
        return Stream.of(
            Arguments.of("Unauthorized access", null),
            Arguments.of("Unauthorized access", new ServiceBusManagementError().setCode(HTTP_UNAUTHORIZED).setDetail("Unauthorized access"))
        );
    }

    /**
     * Gets the corresponding test xml file.
     * @param fileName Name of the xml file.
     *
     * @return String contents of file.
     */
    static String getContents(String fileName) {
        final URL resourceUrl = ServiceBusAdministrationAsyncClient.class.getClassLoader().getResource(".");
        assertNotNull(resourceUrl);

        final File resourceFolder = new File(resourceUrl.getFile(), "xml");
        assertTrue(resourceFolder.exists());

        final Path path = Paths.get(resourceFolder.getPath(), fileName);
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail(String.format("Unable to read file: '  %s'. Error: %s", path.getFileName(), e));
            return null;
        }
    }

    static <T> void assertResponse(Response<Object> expected, Response<T> actual) {
        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        assertEquals(expected.getHeaders(), actual.getHeaders());
        assertEquals(expected.getRequest(), actual.getRequest());
    }

    static boolean createBodyContentEquals(Object requestBody, CreateQueueOptions expected) {
        if (!(requestBody instanceof CreateQueueBodyImpl)) {
            return false;
        }

        final CreateQueueBodyImpl body = (CreateQueueBodyImpl) requestBody;
        final CreateQueueBodyContentImpl content = body.getContent();
        final QueueDescriptionImpl properties = content.getQueueDescription();

        if (properties == null) {
            return false;
        }

        //If forwarding options are enabled, check the value is an absolute URL
        if (!Objects.isNull(properties.getForwardTo())) {
            assertEquals(properties.getForwardTo(), FORWARD_TO_ENTITY);
        }

        if (!Objects.isNull(properties.getForwardDeadLetteredMessagesTo())) {
            assertEquals(properties.getForwardDeadLetteredMessagesTo(), FORWARD_TO_ENTITY);
        }

        return Objects.equals(expected.getAutoDeleteOnIdle(), properties.getAutoDeleteOnIdle())
            && Objects.equals(expected.getDefaultMessageTimeToLive(), properties.getDefaultMessageTimeToLive())
            && Objects.equals(expected.isDeadLetteringOnMessageExpiration(), properties.isDeadLetteringOnMessageExpiration())
            && Objects.equals(expected.getDuplicateDetectionHistoryTimeWindow(),
            properties.getDuplicateDetectionHistoryTimeWindow())
            && Objects.equals(expected.isBatchedOperationsEnabled(), properties.isEnableBatchedOperations())
            && Objects.equals(expected.isPartitioningEnabled(), properties.isEnablePartitioning())
            && Objects.equals(expected.getForwardTo(), properties.getForwardTo())
            && Objects.equals(expected.getForwardDeadLetteredMessagesTo(), properties.getForwardDeadLetteredMessagesTo())
            && Objects.equals(expected.getLockDuration(), properties.getLockDuration())
            && Objects.equals(expected.getMaxDeliveryCount(), properties.getMaxDeliveryCount())
            && Objects.equals(expected.getMaxSizeInMegabytes(), properties.getMaxSizeInMegabytes())
            && Objects.equals(expected.isDuplicateDetectionRequired(), properties.isRequiresDuplicateDetection())
            && Objects.equals(expected.isSessionRequired(), properties.isRequiresSession())
            && Objects.equals(expected.getUserMetadata(), properties.getUserMetadata())
            && "application/xml".equals(content.getType());
    }

    static boolean verifyAdditionalAuthHeaderPresent(Context context, HttpHeaderName requiredHeader, String entity) {
        return context.getData(AZURE_REQUEST_HTTP_HEADERS_KEY).map(headers -> {
            if (!(headers instanceof HttpHeaders)) {
                return false;
            }
            HttpHeaders customHttpHeaders = (HttpHeaders) headers;
            // Attempt to get the required header and validate the value.
            HttpHeader header = customHttpHeaders.get(requiredHeader);
            return header != null && Objects.equals(entity, header.getValue());
        }).orElse(false);
    }

    static String serializeResponse(XmlSerializable<?> response) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             XmlWriter xmlWriter = XmlWriter.toStream(outputStream)) {
            xmlWriter.writeXml(response).flush();
            return outputStream.toString();
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
