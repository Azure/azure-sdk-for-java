// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.servicebus.implementation.QueuesImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntryContent;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.implementation.models.ResponseTitle;
import com.azure.messaging.servicebus.models.QueueDescription;
import com.azure.messaging.servicebus.models.QueueRuntimeInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServiceBusManagementAsyncClient}.
 */
class ServiceBusManagementAsyncClientTest {
    @Mock
    private ServiceBusManagementClientImpl serviceClient;
    @Mock
    private QueuesImpl queuesClient;
    @Mock
    private ServiceBusManagementSerializer serializer;
    @Mock
    private Response<Object> objectResponse;
    @Mock
    private Response<Object> secondObjectResponse;

    private final String queueName = "some-queue";
    private final String responseString = "some-xml-response-string";
    private final String secondResponseString = "second-xml-response";
    private final HttpHeaders httpHeaders = new HttpHeaders().put("foo", "baz");
    private final HttpRequest httpRequest;

    private ServiceBusManagementAsyncClient client;

    ServiceBusManagementAsyncClientTest() {
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
        MockitoAnnotations.initMocks(this);

        when(objectResponse.getValue()).thenReturn(responseString);
        int statusCode = 202;
        when(objectResponse.getStatusCode()).thenReturn(statusCode);
        when(objectResponse.getHeaders()).thenReturn(httpHeaders);
        when(objectResponse.getRequest()).thenReturn(httpRequest);

        when(secondObjectResponse.getValue()).thenReturn(secondResponseString);
        when(secondObjectResponse.getStatusCode()).thenReturn(430);
        when(secondObjectResponse.getHeaders()).thenReturn(httpHeaders);
        when(secondObjectResponse.getRequest()).thenReturn(httpRequest);

        when(serviceClient.getQueues()).thenReturn(queuesClient);

        client = new ServiceBusManagementAsyncClient(serviceClient, serializer);
    }

    @AfterEach
    void afterEach() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void createQueue() throws IOException {
        // Arrange
        final String updatedName = "some-new-name";
        final QueueDescription description = new QueueDescription().setName(queueName);
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(new ResponseTitle().setTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(queuesClient.putWithResponseAsync(eq(queueName),
            argThat(arg -> createBodyContentEquals(arg, description)), isNull(), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.createQueue(description))
            .expectNext(expectedDescription)
            .verifyComplete();
    }

    @Test
    void createQueueWithResponse() throws IOException {
        // Arrange
        final String updatedName = "some-new-name";
        final QueueDescription description = new QueueDescription().setName(queueName);
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(new ResponseTitle().setTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(queuesClient.putWithResponseAsync(eq(queueName),
            argThat(arg -> createBodyContentEquals(arg, description)), isNull(), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.createQueueWithResponse(description))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(expectedDescription, response.getValue());
                assertEquals(updatedName, response.getValue().getName());
            })
            .verifyComplete();
    }

    @Test
    void deleteQueue() {
        // Arrange
        when(queuesClient.deleteWithResponseAsync(eq(queueName), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.deleteQueue(queueName))
            .verifyComplete();
    }

    @Test
    void deleteQueueWithResponse() {
        // Arrange
        when(queuesClient.deleteWithResponseAsync(eq(queueName), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.deleteQueueWithResponse(queueName))
            .assertNext(response -> assertResponse(objectResponse, response))
            .verifyComplete();
    }

    @Test
    void getQueue() throws IOException {
        // Arrange
        final QueueDescription expected = new QueueDescription();
        final QueueDescriptionEntry entry = new QueueDescriptionEntry()
            .setTitle(new ResponseTitle().setTitle(queueName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expected));

        when(queuesClient.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(entry);

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .expectNext(expected)
            .verifyComplete();
    }

    @Test
    void getQueueWithResponse() throws IOException {
        // Arrange
        final String updatedName = "some-new-name";
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(new ResponseTitle().setTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));


        when(queuesClient.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.getQueueWithResponse(queueName))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(expectedDescription, response.getValue());
                assertEquals(updatedName, response.getValue().getName());
            })
            .verifyComplete();
    }

    @Test
    void getQueueRuntimeInfo() throws IOException {
        // Arrange
        final QueueDescription expectedDescription = new QueueDescription()
            .setName(queueName)
            .setMessageCount(100)
            .setSizeInBytes(1053)
            .setAccessedAt(OffsetDateTime.of(2020, 10, 6, 12, 1, 20, 300, ZoneOffset.UTC))
            .setCreatedAt(OffsetDateTime.of(2010, 12, 10, 12, 1, 20, 300, ZoneOffset.UTC))
            .setUpdatedAt(OffsetDateTime.of(2019, 4, 25, 7, 1, 20, 300, ZoneOffset.UTC));
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(new ResponseTitle().setTitle(queueName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(queuesClient.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeInfo(queueName))
            .assertNext(info -> {
                assertEquals(expectedDescription.getName(), info.getName());
                assertEquals(Long.valueOf(expectedDescription.getMessageCount()), info.getMessageCount());
                assertEquals(Long.valueOf(expectedDescription.getSizeInBytes()), info.getSizeInBytes());
                assertEquals(expectedDescription.getCreatedAt(), info.getCreatedAt());
                assertEquals(expectedDescription.getUpdatedAt(), info.getUpdatedAt());
                assertEquals(expectedDescription.getAccessedAt(), info.getAccessedAt());
            })
            .verifyComplete();
    }

    @Test
    void getQueueRuntimeInfoWithResponse() throws IOException {
        // Arrange
        final QueueDescription expectedDescription = new QueueDescription()
            .setName(queueName)
            .setMessageCount(100)
            .setSizeInBytes(1053)
            .setAccessedAt(OffsetDateTime.of(2020, 10, 6, 12, 1, 20, 300, ZoneOffset.UTC))
            .setCreatedAt(OffsetDateTime.of(2010, 12, 10, 12, 1, 20, 300, ZoneOffset.UTC))
            .setUpdatedAt(OffsetDateTime.of(2019, 4, 25, 7, 1, 20, 300, ZoneOffset.UTC));
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(new ResponseTitle().setTitle(queueName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(queuesClient.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeInfoWithResponse(queueName))
            .assertNext(response -> {
                assertResponse(objectResponse, response);

                final QueueRuntimeInfo info = response.getValue();
                assertEquals(expectedDescription.getName(), info.getName());
                assertEquals(Long.valueOf(expectedDescription.getMessageCount()), info.getMessageCount());
                assertEquals(Long.valueOf(expectedDescription.getSizeInBytes()), info.getSizeInBytes());
                assertEquals(expectedDescription.getCreatedAt(), info.getCreatedAt());
                assertEquals(expectedDescription.getUpdatedAt(), info.getUpdatedAt());
                assertEquals(expectedDescription.getAccessedAt(), info.getAccessedAt());
            })
            .verifyComplete();
    }

    @Test
    void listQueues() throws IOException {
        // Arrange
        final int firstEntities = 7;
        final String entityType = "queues";
        final List<QueueDescriptionEntry> firstEntries = IntStream.range(0, 4).mapToObj(number -> {
            final String name = String.valueOf(number);
            final QueueDescription description = new QueueDescription();
            final QueueDescriptionEntryContent content = new QueueDescriptionEntryContent()
                .setQueueDescription(description);
            return new QueueDescriptionEntry()
                .setContent(content)
                .setTitle(new ResponseTitle().setTitle(name));
        }).collect(Collectors.toList());
        final List<ResponseLink> links = Arrays.asList(
            new ResponseLink().setRel("self").setHref("foo"),
            new ResponseLink().setRel("bar").setHref("baz"),
            new ResponseLink().setRel("next").setHref("https://foo.bar.net?api-version=2017-04&$skip=" + firstEntities)
        );
        final QueueDescriptionFeed firstFeed = new QueueDescriptionFeed()
            .setLink(links)
            .setEntry(firstEntries)
            .setId("first-id");

        final List<QueueDescriptionEntry> secondEntries = IntStream.range(5, 7).mapToObj(number -> {
            final QueueDescription description = new QueueDescription();
            final QueueDescriptionEntryContent content = new QueueDescriptionEntryContent()
                .setQueueDescription(description);

            return new QueueDescriptionEntry()
                .setContent(content)
                .setTitle(new ResponseTitle().setTitle(String.valueOf(number)));
        }).collect(Collectors.toList());
        final List<ResponseLink> secondLinks = Arrays.asList(
            new ResponseLink().setRel("self").setHref("foo"),
            new ResponseLink().setRel("bar").setHref("baz"));
        final QueueDescriptionFeed secondFeed = new QueueDescriptionFeed()
            .setEntry(secondEntries)
            .setLink(secondLinks)
            .setId("second-id");

        when(serviceClient.listEntitiesWithResponseAsync(eq(entityType), eq(0), anyInt(), any(Context.class)))
            .thenReturn(Mono.fromCallable(() -> objectResponse));
        when(serviceClient.listEntitiesWithResponseAsync(eq(entityType), eq(firstEntities), anyInt(), any(Context.class)))
            .thenReturn(Mono.fromCallable(() -> secondObjectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionFeed.class))
            .thenReturn(firstFeed);
        when(serializer.deserialize(secondResponseString, QueueDescriptionFeed.class))
            .thenReturn(secondFeed);

        // Act & Assert
        StepVerifier.create(client.listQueues())
            .expectNextCount(firstEntries.size())
            .expectNextCount(secondEntries.size())
            .verifyComplete();
    }

    @Test
    void updateQueue() throws IOException {
        // Arrange
        final QueueDescription description = new QueueDescription().setName(queueName);
        final String updatedName = "some-new-name";
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(new ResponseTitle().setTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(queuesClient.putWithResponseAsync(eq(queueName),
            argThat(arg -> createBodyContentEquals(arg, description)), eq("*"), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.updateQueue(description))
            .expectNext(expectedDescription)
            .verifyComplete();
    }

    @Test
    void updateQueueWithResponse() throws IOException {
        // Arrange
        final QueueDescription description = new QueueDescription().setName(queueName);
        final String updatedName = "some-new-name";
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(new ResponseTitle().setTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(queuesClient.putWithResponseAsync(eq(queueName),
            argThat(arg -> createBodyContentEquals(arg, description)), eq("*"), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.updateQueueWithResponse(description))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(expectedDescription, response.getValue());
                assertEquals(updatedName, response.getValue().getName());
            })
            .verifyComplete();
    }

    private static <T> void assertResponse(Response<Object> expected, Response<T> actual) {
        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        assertEquals(expected.getHeaders(), actual.getHeaders());
        assertEquals(expected.getRequest(), actual.getRequest());
    }

    private static boolean createBodyContentEquals(Object requestBody, QueueDescription expected) {
        if (!(requestBody instanceof CreateQueueBody)) {
            return false;
        }

        final CreateQueueBody body = (CreateQueueBody) requestBody;
        final CreateQueueBodyContent content = body.getContent();
        return content != null
            && Objects.equals(expected, content.getQueueDescription())
            && "application/xml".equals(content.getType());
    }
}
