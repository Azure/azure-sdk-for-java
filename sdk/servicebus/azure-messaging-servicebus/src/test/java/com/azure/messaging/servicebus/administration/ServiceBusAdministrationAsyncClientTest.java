// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeInfo;
import com.azure.messaging.servicebus.implementation.EntitiesImpl;
import com.azure.messaging.servicebus.implementation.EntityHelper;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.implementation.models.MessageCountDetails;
import com.azure.messaging.servicebus.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntryContent;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.ResponseLink;
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServiceBusAdministrationAsyncClient}.
 */
class ServiceBusAdministrationAsyncClientTest {
    @Mock
    private ServiceBusManagementClientImpl serviceClient;
    @Mock
    private EntitiesImpl entitys;
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

    private ServiceBusAdministrationAsyncClient client;

    ServiceBusAdministrationAsyncClientTest() {
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

        when(serviceClient.getEntities()).thenReturn(entitys);

        client = new ServiceBusAdministrationAsyncClient(serviceClient, serializer);
    }

    @AfterEach
    void afterEach() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void createQueue() throws IOException {
        // Arrange
        final String updatedName = "some-new-name";
        final CreateQueueOptions description = new CreateQueueOptions();
        final QueueDescription expectedDescription = EntityHelper.getQueueDescription(description);
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(getResponseTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(entitys.putWithResponseAsync(eq(queueName),
            argThat(arg -> createBodyContentEquals(arg, description)), isNull(), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, description))
            .assertNext(e -> assertEquals(updatedName, e.getName()))
            .verifyComplete();
    }

    @Test
    void createQueueWithResponse() throws IOException {
        // Arrange
        final String updatedName = "some-new-name";
        final CreateQueueOptions description = new CreateQueueOptions();
        final QueueDescription expectedDescription = EntityHelper.getQueueDescription(description);
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(getResponseTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(entitys.putWithResponseAsync(eq(queueName),
            argThat(arg -> createBodyContentEquals(arg, description)), isNull(), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.createQueueWithResponse(queueName, description))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(updatedName, response.getValue().getName());
            })
            .verifyComplete();
    }

    @Test
    void deleteQueue() {
        // Arrange
        when(entitys.deleteWithResponseAsync(eq(queueName), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        // Act & Assert
        StepVerifier.create(client.deleteQueue(queueName))
            .verifyComplete();
    }

    @Test
    void deleteQueueWithResponse() {
        // Arrange
        when(entitys.deleteWithResponseAsync(eq(queueName), any(Context.class)))
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
            .setTitle(getResponseTitle(queueName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expected));

        when(entitys.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(entry);

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .assertNext(e -> assertEquals(queueName, e.getName()))
            .verifyComplete();
    }

    @Test
    void getQueueWithResponse() throws IOException {
        // Arrange
        final String updatedName = "some-new-name";
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(getResponseTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(entitys.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.getQueueWithResponse(queueName))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(updatedName, response.getValue().getName());
            })
            .verifyComplete();
    }

    @Test
    void getQueueRuntimeInfo() throws IOException {
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
        final MessageCountDetails expectedCount = new MessageCountDetails()
            .setActiveMessageCount(5)
            .setDeadLetterMessageCount(3)
            .setScheduledMessageCount(65)
            .setTransferMessageCount(10)
            .setTransferDeadLetterMessageCount(123);

        when(entitys.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(entry);

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeInfo(queueName))
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
            .verifyComplete();
    }

    @Test
    void getQueueRuntimeInfoWithResponse() throws IOException {
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
        final MessageCountDetails expectedCount = new MessageCountDetails()
            .setActiveMessageCount(5)
            .setDeadLetterMessageCount(3)
            .setScheduledMessageCount(65)
            .setTransferMessageCount(10)
            .setTransferDeadLetterMessageCount(123);

        when(entitys.getWithResponseAsync(eq(queueName), eq(true), any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(entry);

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeInfoWithResponse(queueName))
            .assertNext(response -> {
                assertResponse(objectResponse, response);

                final QueueRuntimeInfo info = response.getValue();
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
            .verifyComplete();
    }

    @Test
    void listQueues() throws IOException {
        // Arrange
        final int firstEntities = 7;
        final String entityType = "queues";
        final List<QueueDescriptionEntry> firstEntries = IntStream.range(0, 4).mapToObj(number -> {
            final String name = String.valueOf(number);
            final QueueDescription description = EntityHelper.getQueueDescription(new CreateQueueOptions());
            final QueueDescriptionEntryContent content = new QueueDescriptionEntryContent()
                .setQueueDescription(description);
            return new QueueDescriptionEntry()
                .setContent(content)
                .setTitle(getResponseTitle(name));
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
            final String name = String.valueOf(number);
            final QueueDescription description = EntityHelper.getQueueDescription(new CreateQueueOptions());
            final QueueDescriptionEntryContent content = new QueueDescriptionEntryContent()
                .setQueueDescription(description);

            return new QueueDescriptionEntry()
                .setContent(content)
                .setTitle(getResponseTitle(name));
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
        final QueueDescription description = new QueueDescription();
        final QueueProperties properties = EntityHelper.toModel(description);
        EntityHelper.setQueueName(properties, queueName);

        final String updatedName = "some-new-name";
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(getResponseTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(entitys.putWithResponseAsync(eq(queueName),
            argThat(arg -> {
                if (!(arg instanceof CreateQueueBody)) {
                    return false;
                }

                final CreateQueueBody argument = (CreateQueueBody) arg;
                return argument.getContent() != null && argument.getContent().getQueueDescription() != null;
            }),
            eq("*"),
            any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.updateQueue(properties))
            .assertNext(e -> assertEquals(updatedName, e.getName()))
            .verifyComplete();
    }

    @Test
    void updateQueueWithResponse() throws IOException {
        // Arrange
        final QueueDescription description = new QueueDescription();
        final QueueProperties properties = EntityHelper.toModel(description);
        EntityHelper.setQueueName(properties, queueName);

        final String updatedName = "some-new-name";
        final QueueDescription expectedDescription = new QueueDescription();
        final QueueDescriptionEntry expected = new QueueDescriptionEntry()
            .setTitle(getResponseTitle(updatedName))
            .setContent(new QueueDescriptionEntryContent().setQueueDescription(expectedDescription));

        when(entitys.putWithResponseAsync(eq(queueName),
            argThat(arg -> {
                if (!(arg instanceof CreateQueueBody)) {
                    return false;
                }

                final CreateQueueBody argument = (CreateQueueBody) arg;
                return argument.getContent() != null && argument.getContent().getQueueDescription() != null;
            }),
            eq("*"),
            any(Context.class)))
            .thenReturn(Mono.just(objectResponse));

        when(serializer.deserialize(responseString, QueueDescriptionEntry.class)).thenReturn(expected);

        // Act & Assert
        StepVerifier.create(client.updateQueueWithResponse(properties))
            .assertNext(response -> {
                assertResponse(objectResponse, response);
                assertEquals(updatedName, response.getValue().getName());
            })
            .verifyComplete();
    }

    /**
     * Gets the corresponding test xml file.
     *
     * @param fileName Name of the xml file.
     *
     * @return String contents of file.
     */
    private String getContents(String fileName) {
        final URL resourceUrl = getClass().getClassLoader().getResource(".");
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

    private static <T> void assertResponse(Response<Object> expected, Response<T> actual) {
        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        assertEquals(expected.getHeaders(), actual.getHeaders());
        assertEquals(expected.getRequest(), actual.getRequest());
    }

    private static boolean createBodyContentEquals(Object requestBody, CreateQueueOptions expected) {
        if (!(requestBody instanceof CreateQueueBody)) {
            return false;
        }

        final CreateQueueBody body = (CreateQueueBody) requestBody;
        final CreateQueueBodyContent content = body.getContent();
        final QueueDescription properties = content.getQueueDescription();

        if (properties == null) {
            return false;
        }

        return equals(expected.getAutoDeleteOnIdle(), properties.getAutoDeleteOnIdle())
            && equals(expected.getDefaultMessageTimeToLive(), properties.getDefaultMessageTimeToLive())
            && equals(expected.deadLetteringOnMessageExpiration(), properties.isDeadLetteringOnMessageExpiration())
            && equals(expected.getDuplicateDetectionHistoryTimeWindow(),
            properties.getDuplicateDetectionHistoryTimeWindow())
            && equals(expected.enableBatchedOperations(), properties.isEnableBatchedOperations())
            && equals(expected.enablePartitioning(), properties.isEnablePartitioning())
            && equals(expected.getForwardTo(), properties.getForwardTo())
            && equals(expected.getForwardDeadLetteredMessagesTo(), properties.getForwardDeadLetteredMessagesTo())
            && equals(expected.getLockDuration(), properties.getLockDuration())
            && equals(expected.getMaxDeliveryCount(), properties.getMaxDeliveryCount())
            && equals(expected.getMaxSizeInMegabytes(), properties.getMaxSizeInMegabytes())
            && equals(expected.requiresDuplicateDetection(), properties.isRequiresDuplicateDetection())
            && equals(expected.requiresSession(), properties.isRequiresSession())
            && equals(expected.getUserMetadata(), properties.getUserMetadata())
            && "application/xml".equals(content.getType());
    }

    private static LinkedHashMap<String, String> getResponseTitle(String entityName) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("", entityName);
        map.put("type", "text");
        return map;
    }

    private static boolean equals(Object expected, Object actual) {
        if (expected == null) {
            return actual == null;
        }

        return expected.equals(actual);
    }
}
