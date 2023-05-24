// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.models.MessageCountDetailsImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryContentImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeedImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.ResponseLinkImpl;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementError;
import com.azure.messaging.servicebus.administration.implementation.models.TitleImpl;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.xml.XmlProviders;
import com.azure.xml.XmlWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ServiceBusAdministrationAsyncClient}.
 */
class ServiceBusAdministrationAsyncClientTest {
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final HttpHeaders XML_CONTENT_HEADERS = new HttpHeaders()
        .set(HttpHeaderName.CONTENT_TYPE, "application/xml");

    private static final String FORWARD_TO_ENTITY = "forward-to-entity";

    private final String queueName = "some-queue";

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private static ServiceBusAdministrationAsyncClient createTestClient(HttpClient httpClient) {
        return new ServiceBusAdministrationClientBuilder()
            .endpoint("https://azure.com")
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .buildAsyncClient();
    }

    private static HttpClient getMockHttpClient(Predicate<HttpRequest> requestMatcher, int statusCode,
        String responseBody) {
        byte[] bodyContent = (responseBody == null) ? null : responseBody.getBytes(StandardCharsets.UTF_8);

        return request -> requestMatcher.test(request)
            ? Mono.just(new MockHttpResponse(request, statusCode, XML_CONTENT_HEADERS, bodyContent))
            : Mono.error(new IllegalStateException("Unknown request for mocking."));
    }

    private static Predicate<HttpRequest> getSimpleRequestMatcher(HttpMethod method, String pathWithoutLeadingSlash) {
        return request -> method == request.getHttpMethod()
            && Objects.equals("/" + pathWithoutLeadingSlash, request.getUrl().getPath());
    }

    @Test
    void createQueue() {
        // Arrange
        final String randomQueueName = CoreUtils.randomUuid().toString();
        final String updatedName = "some-new-name";
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + updatedName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\"></QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationAsyncClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.PUT, randomQueueName), 200, expectedXml));

        // Act & Assert
        StepVerifier.create(client.createQueue(randomQueueName, new CreateQueueOptions()))
            .assertNext(e -> assertEquals(updatedName, e.getName()))
            .verifyComplete();
    }

    @Test
    void createQueueWithResponse() {
        // Arrange
        final String randomQueueName = CoreUtils.randomUuid().toString();
        final String updatedName = "some-new-name";
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + updatedName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\"></QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationAsyncClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.PUT, randomQueueName), 200, expectedXml));

        // Act & Assert
        StepVerifier.create(client.createQueueWithResponse(randomQueueName, new CreateQueueOptions()))
            .assertNext(response -> assertEquals(updatedName, response.getValue().getName()))
            .verifyComplete();
    }

    @Test
    void createQueueWithForwarding() {
        // Arrange
        final String randomQueueName = CoreUtils.randomUuid().toString();
        final String updatedName = "some-new-name";
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + updatedName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\"></QueueDescription></content>",
            "</entry>");

        Predicate<HttpRequest> requestMatcher = getSimpleRequestMatcher(HttpMethod.PUT, randomQueueName)
            .and(request -> request.getHeaders().get(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME) != null
                && request.getHeaders().get(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME) != null);

        ServiceBusAdministrationAsyncClient client = createTestClient(getMockHttpClient(requestMatcher, 200,
            expectedXml));

        final CreateQueueOptions createQueueOptions = new CreateQueueOptions()
            .setForwardTo(FORWARD_TO_ENTITY)
            .setForwardDeadLetteredMessagesTo(FORWARD_TO_ENTITY);

        // Act & Assert
        StepVerifier.create(client.createQueueWithResponse(randomQueueName, createQueueOptions))
            .assertNext(response -> assertEquals(updatedName, response.getValue().getName()))
            .verifyComplete();
    }

    @Test
    void deleteQueue() {
        // Arrange
        final String randomQueueName = CoreUtils.randomUuid().toString();

        ServiceBusAdministrationAsyncClient client = createTestClient(
            getMockHttpClient(getSimpleRequestMatcher(HttpMethod.DELETE, randomQueueName), 200, null));

        // Act & Assert
        StepVerifier.create(client.deleteQueue(randomQueueName))
            .verifyComplete();
    }

    @Test
    void deleteQueueWithResponse() {
        // Arrange
        final String randomQueueName = CoreUtils.randomUuid().toString();

        ServiceBusAdministrationAsyncClient client = createTestClient(
            getMockHttpClient(getSimpleRequestMatcher(HttpMethod.DELETE, randomQueueName), 200, null));

        // Act & Assert
        StepVerifier.create(client.deleteQueueWithResponse(randomQueueName))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void getQueue() {
        // Arrange
        final String randomQueueName = CoreUtils.randomUuid().toString();

        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + queueName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\"></QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationAsyncClient client = createTestClient(
            getMockHttpClient(getSimpleRequestMatcher(HttpMethod.GET, randomQueueName), 200, expectedXml));

        // Act & Assert
        StepVerifier.create(client.getQueue(randomQueueName))
            .assertNext(e -> assertEquals(queueName, e.getName()))
            .verifyComplete();
    }

    @Test
    void getQueueWithResponse() {
        // Arrange
        final String randomQueueName = CoreUtils.randomUuid().toString();
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + randomQueueName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\"></QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationAsyncClient client = createTestClient(
            getMockHttpClient(getSimpleRequestMatcher(HttpMethod.GET, randomQueueName), 200, expectedXml));

        // Act & Assert
        StepVerifier.create(client.getQueueWithResponse(randomQueueName))
            .assertNext(response -> assertEquals(randomQueueName, response.getValue().getName()))
            .verifyComplete();
    }

    @Test
    void getQueueRuntimeProperties() {
        // Arrange
        final String randomQueueName = CoreUtils.randomUuid().toString();
        final String expectedXml = getContents();

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

        ServiceBusAdministrationAsyncClient client = createTestClient(
            getMockHttpClient(getSimpleRequestMatcher(HttpMethod.GET, randomQueueName), 200, expectedXml));

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeProperties(randomQueueName))
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
    void getQueueRuntimePropertiesWithResponse() {
        // Arrange
        final String randomQueueName = CoreUtils.randomUuid().toString();
        final String expectedXml = getContents();

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

        ServiceBusAdministrationAsyncClient client = createTestClient(
            getMockHttpClient(getSimpleRequestMatcher(HttpMethod.GET, randomQueueName), 200, expectedXml));

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimePropertiesWithResponse(randomQueueName))
            .assertNext(response -> {
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
            .verifyComplete();
    }

    /**
     * When ServiceBusManagementError is not populated or 'null' in 'ServiceBusManagementErrorException', we get
     * 'ClientAuthenticationException' with error message.
     */
    @ParameterizedTest
    @MethodSource
    void getSubscriptionRuntimePropertiesUnauthorised(String errorMessage, ServiceBusManagementError managementError)
        throws XMLStreamException {
        // Arrange
        final String topicName = CoreUtils.randomUuid().toString();
        final String subscriptionName = CoreUtils.randomUuid().toString();
        final String responseBody;

        if (managementError != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (XmlWriter xmlWriter = XmlProviders.createWriter(outputStream)) {
                managementError.toXml(xmlWriter);
            }

            responseBody = outputStream.toString();
        } else {
            responseBody = errorMessage;
        }

        ServiceBusAdministrationAsyncClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.GET, topicName + "/subscriptions/" + subscriptionName),
            HTTP_UNAUTHORIZED, responseBody));

        // Act & Assert
        StepVerifier.create(client.getSubscriptionRuntimeProperties(topicName, subscriptionName))
            .verifyErrorMatches(error -> error instanceof ClientAuthenticationException
                && error.getMessage().equals(errorMessage));
    }

    @Test
    void listQueues() throws XMLStreamException {
        // Arrange
        final int firstEntities = 7;
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
        ByteArrayOutputStream firstResponse = new ByteArrayOutputStream();
        try (XmlWriter xmlWriter = XmlProviders.createWriter(firstResponse)) {
            firstFeed.toXml(xmlWriter);
        }

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
        ByteArrayOutputStream secondResponse = new ByteArrayOutputStream();
        try (XmlWriter xmlWriter = XmlProviders.createWriter(secondResponse)) {
            secondFeed.toXml(xmlWriter);
        }

        ServiceBusAdministrationAsyncClient client = createTestClient(request -> {
            UrlBuilder urlBuilder = UrlBuilder.parse(request.getUrl());
            if (request.getHttpMethod() != HttpMethod.GET || !"/$Resources/queues".equals(urlBuilder.getPath())) {
                return Mono.error(new IllegalStateException("Unknown request during mocking. Expected list queues path."));
            }

            String skip = urlBuilder.getQuery().get("$skip");
            if ("0".equals(skip)) {
                return Mono.just(new MockHttpResponse(request, 200, XML_CONTENT_HEADERS, firstResponse.toByteArray()));
            } else if ("7".equals(skip)) {
                return Mono.just(new MockHttpResponse(request, 200, XML_CONTENT_HEADERS, secondResponse.toByteArray()));
            } else {
                return Mono.error(new IllegalStateException("Unknown request during mocking. Unknown $skip value: "
                    + skip));
            }
        });

        // Act & Assert
        StepVerifier.create(client.listQueues())
            .expectNextCount(firstEntries.size())
            .expectNextCount(secondEntries.size())
            .verifyComplete();
    }

    @Test
    void updateQueue() {
        // Arrange
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + queueName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">",
            "<MaxDeliveryCount>4</MaxDeliveryCount><AutoDeleteOnIdle>PT30S</AutoDeleteOnIdle>",
            "</QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationAsyncClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.PUT, queueName), 200, expectedXml));

        final QueueDescriptionImpl queueDescription = EntityHelper.getQueueDescription(new CreateQueueOptions());
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        EntityHelper.setQueueName(description, queueName);

        // Act & Assert
        StepVerifier.create(client.updateQueue(description))
            .assertNext(e -> assertEquals(queueName, e.getName()))
            .verifyComplete();
    }

    @Test
    void updateQueueWithResponse() {
        // Arrange
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + queueName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">",
            "<MaxDeliveryCount>4</MaxDeliveryCount><AutoDeleteOnIdle>PT30S</AutoDeleteOnIdle>",
            "</QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationAsyncClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.PUT, queueName), 200, expectedXml));

        final QueueDescriptionImpl queueDescription = EntityHelper.getQueueDescription(new CreateQueueOptions());
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        EntityHelper.setQueueName(description, queueName);

        // Act & Assert
        StepVerifier.create(client.updateQueueWithResponse(description))
            .assertNext(response -> assertEquals(queueName, response.getValue().getName()))
            .verifyComplete();
    }

    static Stream<Arguments> getSubscriptionRuntimePropertiesUnauthorised() {
        return Stream.of(
            // Arguments.of("Unauthorized access", null),
            Arguments.of("Unauthorized access", new ServiceBusManagementError().setCode(HTTP_UNAUTHORIZED).setDetail("Unauthorized access"))
        );
    }

    /**
     * Gets the corresponding test xml file.
     *
     * @return String contents of file.
     */
    private String getContents() {
        final URL resourceUrl = getClass().getClassLoader().getResource(".");
        assertNotNull(resourceUrl);

        final File resourceFolder = new File(resourceUrl.getFile(), "xml");
        assertTrue(resourceFolder.exists());

        final Path path = Paths.get(resourceFolder.getPath(), "QueueDescriptionEntry.xml");
        return assertDoesNotThrow(() -> new String(Files.readAllBytes(path), StandardCharsets.UTF_8),
            () -> String.format("Unable to read file: '%s'.", path.getFileName()));
    }
}
