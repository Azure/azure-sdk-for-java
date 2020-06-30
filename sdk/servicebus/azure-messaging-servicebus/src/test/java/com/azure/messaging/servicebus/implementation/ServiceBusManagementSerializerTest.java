// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntryContent;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.ResponseAuthor;
import com.azure.messaging.servicebus.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.models.EntityStatus;
import com.azure.messaging.servicebus.models.MessageCountDetails;
import com.azure.messaging.servicebus.models.QueueDescription;
import com.azure.messaging.servicebus.models.QueueRuntimeInfo;
import org.junit.jupiter.api.Test;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class ServiceBusManagementSerializerTest {
    private static final String TITLE_KEY = "";

    private final ServiceBusManagementSerializer serializer = new ServiceBusManagementSerializer();

    /**
     * Verify we can deserialize XML from a GET queue request.
     */
    @Test
    void deserializeQueueDescription() throws IOException {
        // Arrange
        final String contents = getContents("QueueDescriptionEntry.xml");
        final QueueDescription expected = new QueueDescription("my-test-queue")
            .setLockDuration(Duration.ofMinutes(5))
            .setMaxSizeInMegabytes(1024)
            .setRequiresDuplicateDetection(true)
            .setRequiresSession(true)
            .setDefaultMessageTimeToLive(Duration.parse("PT3H20M10S"))
            .setDeadLetteringOnMessageExpiration(false)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(10))
            .setMaxDeliveryCount(10)
            .setEnableBatchedOperations(true)
            .setSupportOrdering(true)
            .setAutoDeleteOnIdle(Duration.ofHours(5))
            .setEnablePartitioning(true);

        final QueueDescription spy = spy(expected);
        when(spy.getStatus()).thenReturn(EntityStatus.DELETING);

        // Act
        final QueueDescriptionEntry entry = serializer.deserialize(contents, QueueDescriptionEntry.class);

        // Assert
        assertNotNull(entry);
        assertNotNull(entry.getContent());

        // The entry title is the name of the queue.
        assertTitle(spy.getName(), entry.getTitle());

        final QueueDescription actual = entry.getContent().getQueueDescription();
        assertQueueDescriptionEquals(spy, actual);
    }

    /**
     * Verify we can deserialize XML from a GET queue request and create convenience model, {@link QueueRuntimeInfo}.
     */
    @Test
    void deserializeQueueRuntimeInfo() throws IOException {
        final String contents = getContents("QueueDescriptionEntry.xml");

        final OffsetDateTime createdAt = OffsetDateTime.parse("2020-06-05T03:55:07.5Z");
        final OffsetDateTime updatedAt = OffsetDateTime.parse("2020-06-05T03:45:07.64Z");
        final OffsetDateTime accessedAt = OffsetDateTime.parse("0001-01-01T00:00:00Z");
        final int sizeInBytes = 2048;
        final int messageCount = 23;
        final MessageCountDetails expectedCount = new MessageCountDetails()
            .setActiveMessageCount(5)
            .setDeadLetterMessageCount(3)
            .setScheduledMessageCount(65)
            .setTransferMessageCount(10)
            .setTransferDeadLetterMessageCount(123);

        // Act
        final QueueDescriptionEntry entry = serializer.deserialize(contents, QueueDescriptionEntry.class);
        final QueueRuntimeInfo actual = new QueueRuntimeInfo(entry.getContent().getQueueDescription());

        // Assert
        assertEquals(sizeInBytes, actual.getSizeInBytes());
        assertEquals(messageCount, actual.getMessageCount());

        assertEquals(createdAt, actual.getCreatedAt());
        assertEquals(updatedAt, actual.getUpdatedAt());
        assertEquals(accessedAt, actual.getAccessedAt());

        final MessageCountDetails details = actual.getDetails();
        assertNotNull(details);

        assertEquals(expectedCount.getActiveMessageCount(), details.getActiveMessageCount());
        assertEquals(expectedCount.getDeadLetterMessageCount(), details.getDeadLetterMessageCount());
        assertEquals(expectedCount.getScheduledMessageCount(), details.getScheduledMessageCount());
        assertEquals(expectedCount.getTransferMessageCount(), details.getTransferMessageCount());
        assertEquals(expectedCount.getTransferDeadLetterMessageCount(), details.getTransferDeadLetterMessageCount());
    }

    /**
     * Verify we can deserialize feed XML from a list queues operation that has a paged response.
     */
    @Test
    void deserializeQueueDescriptionFeedPaged() throws IOException {
        final String contents = getContents("QueueDescriptionFeed-Paged.xml");
        final List<ResponseLink> responseLinks = Arrays.asList(
            new ResponseLink().setRel("self")
                .setHref("https://sb-java.servicebus.windows.net/$Resources/queues?api-version=2017-04&enrich=false&$skip=0&$top=5"),
            new ResponseLink().setRel("next")
                .setHref("https://sb-java.servicebus.windows.net/$Resources/queues?api-version=2017-04&enrich=false&%24skip=5&%24top=5")
        );

        final QueueDescription queueDescription = new QueueDescription("")
            .setLockDuration(Duration.ofMinutes(10))
            .setMaxSizeInMegabytes(102)
            .setRequiresDuplicateDetection(true)
            .setRequiresSession(true)
            .setDefaultMessageTimeToLive(Duration.ofSeconds(10))
            .setDeadLetteringOnMessageExpiration(false)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(10))
            .setMaxDeliveryCount(10)
            .setEnableBatchedOperations(true)
            .setSupportOrdering(true)
            .setAutoDeleteOnIdle(Duration.ofSeconds(5))
            .setEnablePartitioning(true);

            EntityHelper.setSizeInBytes(queueDescription, 0);
            EntityHelper.setMessageCount(queueDescription, 0);

        final QueueDescriptionEntry entry1 = new QueueDescriptionEntry()
            .setBase("https://sb-java.servicebus.windows.net/$Resources/queues?api-version=2017-04&enrich=false&$skip=0&$top=5")
            .setId("https://sb-java.servicebus.windows.net/q-0?api-version=2017-04")
            .setTitle(getResponseTitle("q-0"))
            .setPublished(OffsetDateTime.parse("2020-03-05T07:17:04Z"))
            .setUpdated(OffsetDateTime.parse("2020-01-05T07:17:04Z"))
            .setAuthor(new ResponseAuthor().setName("sb-java"))
            .setLink(new ResponseLink().setRel("self").setHref("../q-0?api-version=2017-04"))
            .setContent(new QueueDescriptionEntryContent().setType("application/xml")
                .setQueueDescription(queueDescription));
        final QueueDescriptionEntry entry2 = new QueueDescriptionEntry()
            .setBase("https://sb-java.servicebus.windows.net/$Resources/queues?api-version=2017-04&enrich=false&$skip=0&$top=5")
            .setId("https://sb-java.servicebus.windows.net/q-1?api-version=2017-04")
            .setTitle(getResponseTitle("q-1"))
            .setPublished(OffsetDateTime.parse("2020-06-10T07:16:25Z"))
            .setUpdated(OffsetDateTime.parse("2020-06-15T07:16:25Z"))
            .setAuthor(new ResponseAuthor().setName("sb-java2"))
            .setLink(new ResponseLink().setRel("self").setHref("../q-1?api-version=2017-04"))
            .setContent(new QueueDescriptionEntryContent().setType("application/xml")
                .setQueueDescription(queueDescription));
        final QueueDescriptionEntry entry3 = new QueueDescriptionEntry()
            .setBase("https://sb-java.servicebus.windows.net/$Resources/queues?api-version=2017-04&enrich=false&$skip=0&$top=5")
            .setId("https://sb-java.servicebus.windows.net/q-2?api-version=2017-04")
            .setTitle(getResponseTitle("q-2"))
            .setPublished(OffsetDateTime.parse("2020-06-05T07:17:06Z"))
            .setUpdated(OffsetDateTime.parse("2020-06-05T07:17:06Z"))
            .setAuthor(new ResponseAuthor().setName("sb-java3"))
            .setLink(new ResponseLink().setRel("self").setHref("../q-2?api-version=2017-04"))
            .setContent(new QueueDescriptionEntryContent().setType("application/xml")
                .setQueueDescription(queueDescription));


        final Map<String, String> titleMap = new HashMap<>();
        titleMap.put("", "Queues");
        titleMap.put("type", "text");
        final List<QueueDescriptionEntry> entries = Arrays.asList(entry1, entry2, entry3);
        final QueueDescriptionFeed expected = new QueueDescriptionFeed()
            .setId("feed-id")
            .setTitle(titleMap)
            .setUpdated(OffsetDateTime.parse("2020-12-05T07:17:21Z"))
            .setLink(responseLinks)
            .setEntry(entries);

        // Act
        final QueueDescriptionFeed actual = serializer.deserialize(contents, QueueDescriptionFeed.class);

        // Assert
        assertQueueFeedEquals(expected, actual);
    }

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

    private static void assertQueueDescriptionEquals(QueueDescription expected, QueueDescription actual) {
        assertEquals(expected.getLockDuration(), actual.getLockDuration());
        assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
        assertEquals(expected.requiresDuplicateDetection(), actual.requiresDuplicateDetection());
        assertEquals(expected.requiresSession(), actual.requiresSession());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.deadLetteringOnMessageExpiration(), actual.deadLetteringOnMessageExpiration());
        assertEquals(expected.getDuplicateDetectionHistoryTimeWindow(), actual.getDuplicateDetectionHistoryTimeWindow());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.enableBatchedOperations(), actual.enableBatchedOperations());

        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getUpdatedAt(), actual.getUpdatedAt());
        assertEquals(expected.supportOrdering(), actual.supportOrdering());
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.enablePartitioning(), actual.enablePartitioning());
    }

    private static void assertQueueEntryEquals(QueueDescriptionEntry expected, QueueDescriptionEntry actual) {
        assertEquals(expected.getId(), actual.getId());
        assertNotNull(actual.getTitle());

        assertResponseTitle(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getUpdated(), actual.getUpdated());
        assertEquals(expected.getPublished(), actual.getPublished());
        assertEquals(expected.getAuthor().getName(), actual.getAuthor().getName());

        assertQueueDescriptionEquals(expected.getContent().getQueueDescription(), actual.getContent().getQueueDescription());
    }

    private static void assertQueueFeedEquals(QueueDescriptionFeed expected, QueueDescriptionFeed actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getUpdated(), actual.getUpdated());

        assertNotNull(actual.getLink());
        assertEquals(expected.getLink().size(), actual.getLink().size());
        for (int i = 0; i < expected.getLink().size(); i++) {
            final ResponseLink expectedLink = expected.getLink().get(i);
            final ResponseLink actualLink = actual.getLink().get(i);

            assertEquals(expectedLink.getRel(), actualLink.getRel());
            assertEquals(expectedLink.getHref(), actualLink.getHref());
        }

        assertNotNull(actual.getEntry());
        assertEquals(expected.getEntry().size(), actual.getEntry().size());
        for (int i = 0; i < expected.getEntry().size(); i++) {
            final QueueDescriptionEntry expectedEntry = expected.getEntry().get(i);
            final QueueDescriptionEntry actualEntry = actual.getEntry().get(i);

            assertQueueEntryEquals(expectedEntry, actualEntry);
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertTitle(String expectedTitle, Object responseTitle) {
        assertTrue(responseTitle instanceof LinkedHashMap);

        final LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) responseTitle;
        assertTrue(map.containsKey(TITLE_KEY));
        assertEquals(expectedTitle, map.get(TITLE_KEY));
    }

    @SuppressWarnings("unchecked")
    private static void assertResponseTitle(Object expectedResponseTitle, Object actualResponseTitle) {
        assertTrue(actualResponseTitle instanceof LinkedHashMap);

        final LinkedHashMap<String, String> actualMap = (LinkedHashMap<String, String>) actualResponseTitle;

        assertTrue(actualMap.containsKey(TITLE_KEY));
        assertTitle(actualMap.get(TITLE_KEY), expectedResponseTitle);
    }

    private static LinkedHashMap<String, String> getResponseTitle(String entityName) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("", entityName);
        map.put("type", "text");
        return map;
    }
}
