// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.models.MessageCountDetailsImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryContentImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeedImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.ResponseLinkImpl;
import com.azure.messaging.servicebus.administration.implementation.models.TitleImpl;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.xml.XmlProviders;
import com.azure.xml.XmlWriter;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ServiceBusAdministrationClient}.
 */
class ServiceBusAdministrationClientTest {
    private static final HttpHeaders XML_CONTENT_HEADERS = new HttpHeaders()
        .set(HttpHeaderName.CONTENT_TYPE, "application/xml");

    private final Context context = new Context("foo", "bar").addData("baz", "boo");
    private final String queueName = "some-queue";
    private final String subscriptionName = "subscriptionName";
    private final String topicName = "topicName";
    private final String ruleName = "ruleName";

    private static ServiceBusAdministrationClient createTestClient(HttpClient httpClient) {
        return new ServiceBusAdministrationClientBuilder()
            .endpoint("https://azure.com")
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .buildClient();
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
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + queueName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">",
            "<MaxDeliveryCount>4</MaxDeliveryCount><AutoDeleteOnIdle>PT30S</AutoDeleteOnIdle>",
            "</QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.PUT, queueName), 200, expectedXml));

        // Act
        final QueueProperties actual = client.createQueue(queueName, new CreateQueueOptions());

        // Assert
        assertEquals(Duration.ofSeconds(30), actual.getAutoDeleteOnIdle());
        assertEquals(4, actual.getMaxDeliveryCount());
    }

    @Test
    void createQueueWithResponse() {
        // Arrange
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + queueName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">",
            "<MaxDeliveryCount>4</MaxDeliveryCount><AutoDeleteOnIdle>PT30S</AutoDeleteOnIdle>",
            "</QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.PUT, queueName), 200, expectedXml));

        // Act
        final Response<QueueProperties> actual = client.createQueueWithResponse(queueName, new CreateQueueOptions(),
            context);

        // Assert
        QueueProperties actualResult = actual.getValue();
        assertEquals(Duration.ofSeconds(30), actualResult.getAutoDeleteOnIdle());
        assertEquals(4, actualResult.getMaxDeliveryCount());
    }

    @Test
    void deleteQueue() {
        // Arrange
        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.DELETE, queueName), 200, null));

        // Act
        assertDoesNotThrow(() -> client.deleteQueue(queueName));
    }

    @Test
    void deleteQueueWithResponse() {
        // Arrange
        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.DELETE, queueName), 200, null));

        // Act
        Response<Void> response = assertDoesNotThrow(() -> client.deleteQueueWithResponse(queueName, context));
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void deleteRule() {
        // Arrange
        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(getSimpleRequestMatcher(
            HttpMethod.DELETE, topicName + "/subscriptions/" + subscriptionName + "/rules/" + ruleName), 200,
            "<entry xmlns=\"http://www.w3.org/2005/Atom\"></entry>"));

        // Act
        assertDoesNotThrow(() -> client.deleteRule(topicName, subscriptionName, ruleName));
    }

    @Test
    void deleteRuleWithResponse() {
        // Arrange
        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(getSimpleRequestMatcher(
            HttpMethod.DELETE, topicName + "/subscriptions/" + subscriptionName + "/rules/" + ruleName), 200,
            "<entry xmlns=\"http://www.w3.org/2005/Atom\"></entry>"));

        // Act
        final Response<Void> actual = client.deleteRuleWithResponse(topicName, subscriptionName, ruleName, context);

        // Assert
        assertEquals(200, actual.getStatusCode());
    }

    @Test
    void deleteSubscription() {
        // Arrange
        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(getSimpleRequestMatcher(
            HttpMethod.DELETE, topicName + "/subscriptions/" + subscriptionName), 200,
            "<entry xmlns=\"http://www.w3.org/2005/Atom\"></entry>"));

        // Act
        assertDoesNotThrow(() -> client.deleteSubscription(topicName, subscriptionName));
    }

    @Test
    void deleteSubscriptionWithResponse() {
        // Arrange
        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(getSimpleRequestMatcher(
            HttpMethod.DELETE, topicName + "/subscriptions/" + subscriptionName), 200,
            "<entry xmlns=\"http://www.w3.org/2005/Atom\"></entry>"));

        // Act
        final Response<Void> actual = client.deleteSubscriptionWithResponse(topicName, subscriptionName, context);

        // Assert
        assertEquals(200, actual.getStatusCode());
    }

    @Test
    void deleteTopic() {
        // Arrange
        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.DELETE, topicName), 200, null));

        // Act
        assertDoesNotThrow(() -> client.deleteTopic(topicName));
    }

    @Test
    void deleteTopicWithResponse() {
        // Arrange
        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.DELETE, topicName), 200, null));

        // Act
        final Response<Void> actual = client.deleteTopicWithResponse(topicName, context);

        // Assert
        assertEquals(200, actual.getStatusCode());
    }

    @Test
    void getQueue() {
        // Arrange
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + queueName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\"></QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.GET, queueName), 200, expectedXml));

        // Act
        final QueueProperties actual = client.getQueue(queueName);

        // Assert
        assertEquals(queueName, actual.getName());
    }

    @Test
    void getQueueWithResponse() {
        // Arrange
        final String expectedXml = String.join("",
            "<entry xmlns=\"http://www.w3.org/2005/Atom\">",
            "<title type=\"text\">" + queueName + "</title>",
            "<content><QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\"></QueueDescription></content>",
            "</entry>");

        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.GET, queueName), 200, expectedXml));

        // Act
        final Response<QueueProperties> actual = client.getQueueWithResponse(queueName, context);

        // Assert
        assertEquals(queueName, actual.getValue().getName());
    }

    @Test
    void getQueueRuntimeProperties() {
        // Arrange
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

        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.GET, queueName), 200, expectedXml));

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
    void getQueueRuntimePropertiesWithResponse() {
        // Arrange
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

        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.GET, queueName), 200, expectedXml));

        // Act & Assert
        QueueRuntimeProperties info = client.getQueueRuntimePropertiesWithResponse(queueName, context).getValue();

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

        ServiceBusAdministrationClient client = createTestClient(request -> {
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
        Iterator<PagedResponse<QueueProperties>> pages = client.listQueues().iterableByPage().iterator();
        assertEquals(firstEntries.size(), pages.next().getValue().size());
        assertEquals(secondEntries.size(), pages.next().getValue().size());
    }

    @Test
    void listQueuesWithContext() throws XMLStreamException {
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

        ServiceBusAdministrationClient client = createTestClient(request -> {
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
        Iterator<PagedResponse<QueueProperties>> pages = client.listQueues(context).iterableByPage().iterator();
        assertEquals(firstEntries.size(), pages.next().getValue().size());
        assertEquals(secondEntries.size(), pages.next().getValue().size());
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

        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.PUT, queueName), 200, expectedXml));

        final QueueDescriptionImpl queueDescription = EntityHelper.getQueueDescription(new CreateQueueOptions());
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        EntityHelper.setQueueName(description, queueName);

        // Act
        final QueueProperties actual = client.updateQueue(description);

        // Assert
        assertEquals(4, actual.getMaxDeliveryCount());
        assertEquals(Duration.ofSeconds(30), actual.getAutoDeleteOnIdle());
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

        ServiceBusAdministrationClient client = createTestClient(getMockHttpClient(
            getSimpleRequestMatcher(HttpMethod.PUT, queueName), 200, expectedXml));

        final QueueDescriptionImpl queueDescription = EntityHelper.getQueueDescription(new CreateQueueOptions());
        final QueueProperties description = EntityHelper.toModel(queueDescription);
        EntityHelper.setQueueName(description, queueName);

        // Act
        final Response<QueueProperties> actual = client.updateQueueWithResponse(description, context);

        // Assert
        QueueProperties actualResult = actual.getValue();
        assertEquals(4, actualResult.getMaxDeliveryCount());
        assertEquals(Duration.ofSeconds(30), actualResult.getAutoDeleteOnIdle());
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
