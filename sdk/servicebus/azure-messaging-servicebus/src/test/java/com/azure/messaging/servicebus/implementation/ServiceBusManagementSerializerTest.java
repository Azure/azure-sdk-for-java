// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.EntityStatus;
import com.azure.messaging.servicebus.administration.models.MessagingSku;
import com.azure.messaging.servicebus.administration.models.NamespaceProperties;
import com.azure.messaging.servicebus.administration.models.NamespaceType;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeInfo;
import com.azure.messaging.servicebus.administration.models.SubscriptionRuntimeInfo;
import com.azure.messaging.servicebus.implementation.models.CorrelationFilterImpl;
import com.azure.messaging.servicebus.implementation.models.EmptyRuleActionImpl;
import com.azure.messaging.servicebus.implementation.models.FalseFilterImpl;
import com.azure.messaging.servicebus.implementation.models.KeyValueImpl;
import com.azure.messaging.servicebus.implementation.models.MessageCountDetails;
import com.azure.messaging.servicebus.implementation.models.NamespacePropertiesEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntryContent;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.ResponseAuthor;
import com.azure.messaging.servicebus.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.implementation.models.RuleDescription;
import com.azure.messaging.servicebus.implementation.models.RuleDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.RuleDescriptionEntryContent;
import com.azure.messaging.servicebus.implementation.models.RuleDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.SqlFilterImpl;
import com.azure.messaging.servicebus.implementation.models.SqlRuleActionImpl;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescription;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescriptionEntryContent;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.TrueFilterImpl;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
        final String queueName = "my-test-queue";
        final CreateQueueOptions expected = new CreateQueueOptions()
            .setLockDuration(Duration.ofMinutes(5))
            .setMaxSizeInMegabytes(1024)
            .setRequiresDuplicateDetection(true)
            .setRequiresSession(true)
            .setDefaultMessageTimeToLive(Duration.parse("PT3H20M10S"))
            .setDeadLetteringOnMessageExpiration(false)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(10))
            .setMaxDeliveryCount(10)
            .setEnableBatchedOperations(true)
            .setAutoDeleteOnIdle(Duration.ofHours(5))
            .setEnablePartitioning(true);

        // Act
        final QueueDescriptionEntry entry = serializer.deserialize(contents, QueueDescriptionEntry.class);

        // Assert
        assertNotNull(entry);
        assertNotNull(entry.getContent());

        // The entry title is the name of the queue.
        assertTitle(queueName, entry.getTitle());

        final QueueDescription actual = entry.getContent().getQueueDescription();
        assertQueueEquals(expected, EntityStatus.DELETING, actual);
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
        final QueueProperties properties = EntityHelper.toModel(entry.getContent().getQueueDescription());
        final QueueRuntimeInfo actual = new QueueRuntimeInfo(properties);

        // Assert
        assertEquals(sizeInBytes, actual.getSizeInBytes());
        assertEquals(messageCount, actual.getTotalMessageCount());

        assertEquals(createdAt, actual.getCreatedAt());
        assertEquals(updatedAt, actual.getUpdatedAt());
        assertEquals(accessedAt, actual.getAccessedAt());

        assertEquals(expectedCount.getActiveMessageCount(), actual.getActiveMessageCount());
        assertEquals(expectedCount.getDeadLetterMessageCount(), actual.getDeadLetterMessageCount());
        assertEquals(expectedCount.getScheduledMessageCount(), actual.getScheduledMessageCount());
        assertEquals(expectedCount.getTransferMessageCount(), actual.getTransferMessageCount());
        assertEquals(expectedCount.getTransferDeadLetterMessageCount(), actual.getTransferDeadLetterMessageCount());
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

        final String queueName = "q-0";
        final CreateQueueOptions options = new CreateQueueOptions()
            .setLockDuration(Duration.ofMinutes(10))
            .setMaxSizeInMegabytes(102)
            .setRequiresDuplicateDetection(true)
            .setRequiresSession(true)
            .setDefaultMessageTimeToLive(Duration.ofSeconds(10))
            .setDeadLetteringOnMessageExpiration(false)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(10))
            .setMaxDeliveryCount(10)
            .setEnableBatchedOperations(true)
            .setAutoDeleteOnIdle(Duration.ofSeconds(5))
            .setEnablePartitioning(true);
        final QueueDescription queueProperties = EntityHelper.getQueueDescription(options);

        final QueueDescriptionEntry entry1 = new QueueDescriptionEntry()
            .setBase("https://sb-java.servicebus.windows.net/$Resources/queues?api-version=2017-04&enrich=false&$skip=0&$top=5")
            .setId("https://sb-java.servicebus.windows.net/q-0?api-version=2017-04")
            .setTitle(getResponseTitle("q-0"))
            .setPublished(OffsetDateTime.parse("2020-03-05T07:17:04Z"))
            .setUpdated(OffsetDateTime.parse("2020-01-05T07:17:04Z"))
            .setAuthor(new ResponseAuthor().setName("sb-java"))
            .setLink(new ResponseLink().setRel("self").setHref("../q-0?api-version=2017-04"))
            .setContent(new QueueDescriptionEntryContent().setType("application/xml")
                .setQueueDescription(queueProperties));
        final QueueDescriptionEntry entry2 = new QueueDescriptionEntry()
            .setBase("https://sb-java.servicebus.windows.net/$Resources/queues?api-version=2017-04&enrich=false&$skip=0&$top=5")
            .setId("https://sb-java.servicebus.windows.net/q-1?api-version=2017-04")
            .setTitle(getResponseTitle("q-1"))
            .setPublished(OffsetDateTime.parse("2020-06-10T07:16:25Z"))
            .setUpdated(OffsetDateTime.parse("2020-06-15T07:16:25Z"))
            .setAuthor(new ResponseAuthor().setName("sb-java2"))
            .setLink(new ResponseLink().setRel("self").setHref("../q-1?api-version=2017-04"))
            .setContent(new QueueDescriptionEntryContent().setType("application/xml")
                .setQueueDescription(queueProperties));
        final QueueDescriptionEntry entry3 = new QueueDescriptionEntry()
            .setBase("https://sb-java.servicebus.windows.net/$Resources/queues?api-version=2017-04&enrich=false&$skip=0&$top=5")
            .setId("https://sb-java.servicebus.windows.net/q-2?api-version=2017-04")
            .setTitle(getResponseTitle("q-2"))
            .setPublished(OffsetDateTime.parse("2020-06-05T07:17:06Z"))
            .setUpdated(OffsetDateTime.parse("2020-06-05T07:17:06Z"))
            .setAuthor(new ResponseAuthor().setName("sb-java3"))
            .setLink(new ResponseLink().setRel("self").setHref("../q-2?api-version=2017-04"))
            .setContent(new QueueDescriptionEntryContent().setType("application/xml")
                .setQueueDescription(queueProperties));

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

            assertEquals(expected.getId(), actual.getId());
            assertNotNull(actual.getTitle());

            assertResponseTitle(expectedEntry.getTitle(), actualEntry.getTitle());
            assertEquals(expectedEntry.getUpdated(), actualEntry.getUpdated());
            assertEquals(expectedEntry.getPublished(), actualEntry.getPublished());
            assertEquals(expectedEntry.getAuthor().getName(), actualEntry.getAuthor().getName());

            assertQueueEquals(options, EntityStatus.ACTIVE, actualEntry.getContent().getQueueDescription());
        }
    }

    /**
     * Verify we can deserialize XML from a GET namespace request.
     */
    @Test
    void deserializeNamespace() throws IOException {
        // Arrange
        final String contents = getContents("NamespaceEntry.xml");
        final String name = "ShivangiServiceBus";
        final String alias = "MyServiceBusFallback";
        final OffsetDateTime createdTime = OffsetDateTime.parse("2020-04-09T08:38:55.807Z");
        final OffsetDateTime modifiedTime = OffsetDateTime.parse("2020-06-12T06:34:38.383Z");
        final MessagingSku sku = MessagingSku.PREMIUM;
        final NamespaceType namespaceType = NamespaceType.MESSAGING;

        // Act
        final NamespacePropertiesEntry entry = serializer.deserialize(contents, NamespacePropertiesEntry.class);

        // Assert
        assertNotNull(entry);
        assertNotNull(entry.getContent());

        // The entry title is the name of the queue.
        assertTitle(name, entry.getTitle());

        final NamespaceProperties actual = entry.getContent().getNamespaceProperties();
        assertEquals(name, actual.getName());
        assertEquals(alias, actual.getAlias());
        assertEquals(createdTime, actual.getCreatedTime());
        assertEquals(modifiedTime, actual.getModifiedTime());
        assertEquals(sku, actual.getMessagingSku());
        assertEquals(namespaceType, actual.getNamespaceType());
    }

    /**
     * Verify we can deserialize XML from a GET subscription request.
     */
    @Test
    void deserializeSubscription() throws IOException {
        // Arrange
        final String contents = getContents("SubscriptionDescriptionEntry.xml");
        final SubscriptionDescription expected = new SubscriptionDescription()
            .setLockDuration(Duration.ofSeconds(15))
            .setRequiresSession(true)
            .setDefaultMessageTimeToLive(ServiceBusConstants.MAX_DURATION)
            .setDeadLetteringOnMessageExpiration(false)
            .setDeadLetteringOnFilterEvaluationExceptions(true)
            .setEnableBatchedOperations(true)
            .setMaxDeliveryCount(5)
            .setAutoDeleteOnIdle(Duration.ofHours(1).plusMinutes(48));

        // Act
        final SubscriptionDescriptionEntry entry = serializer.deserialize(contents, SubscriptionDescriptionEntry.class);

        // Assert
        assertNotNull(entry);
        assertNotNull(entry.getContent());

        final SubscriptionDescription actual = entry.getContent().getSubscriptionDescription();

        assertSubscriptionEquals(expected, EntityStatus.ACTIVE, actual);
    }

    /**
     * Verify we can deserialize XML from a PUT subscription request.
     */
    @Test
    void deserializeCreateSubscription() throws IOException {
        // Arrange
        final String contents = getContents("CreateSubscriptionEntry.xml");
        final String topicName = "topic";
        final String subscriptionName = "sub46850f";
        final SubscriptionDescription expected = EntityHelper.getSubscriptionDescription(
            new CreateSubscriptionOptions()
                .setAutoDeleteOnIdle(Duration.parse("P10675199DT2H48M5.477S"))
                .setDefaultMessageTimeToLive(Duration.parse("P10675199DT2H48M5.477S"))
                .setRequiresSession(false)
                .setLockDuration(Duration.ofSeconds(45))
                .setMaxDeliveryCount(7));

        // Act
        final SubscriptionDescriptionEntry entry = serializer.deserialize(contents, SubscriptionDescriptionEntry.class);

        // Assert
        assertNotNull(entry);
        assertNotNull(entry.getContent());

        final SubscriptionDescription actual = entry.getContent().getSubscriptionDescription();

        assertSubscriptionEquals(expected, EntityStatus.ACTIVE, actual);
    }

    /**
     * Verify we can deserialize XML from a GET subscription request and create convenience model, {@link
     * SubscriptionRuntimeInfo}.
     */
    @Test
    void deserializeSubscriptionRuntimeInfo() throws IOException {
        final String contents = getContents("SubscriptionDescriptionEntry.xml");

        final OffsetDateTime createdAt = OffsetDateTime.parse("2020-06-22T23:47:54.0131447Z");
        final OffsetDateTime updatedAt = OffsetDateTime.parse("2020-06-22T23:47:20.0131447Z");
        final OffsetDateTime accessedAt = OffsetDateTime.parse("2020-06-22T23:47:54.013Z");
        final int messageCount = 13;
        final MessageCountDetails expectedCount = new MessageCountDetails()
            .setActiveMessageCount(10)
            .setDeadLetterMessageCount(50)
            .setScheduledMessageCount(34)
            .setTransferMessageCount(11)
            .setTransferDeadLetterMessageCount(2);

        // Act
        final SubscriptionDescriptionEntry entry = serializer.deserialize(contents, SubscriptionDescriptionEntry.class);
        final SubscriptionRuntimeInfo actual = new SubscriptionRuntimeInfo(
            EntityHelper.toModel(entry.getContent().getSubscriptionDescription()));

        // Assert
        assertEquals(messageCount, actual.getTotalMessageCount());

        assertEquals(createdAt, actual.getCreatedAt());
        assertEquals(updatedAt, actual.getUpdatedAt());
        assertEquals(accessedAt, actual.getAccessedAt());

        assertEquals(expectedCount.getActiveMessageCount(), actual.getActiveMessageCount());
        assertEquals(expectedCount.getDeadLetterMessageCount(), actual.getDeadLetterMessageCount());
        assertEquals(expectedCount.getScheduledMessageCount(), actual.getScheduledMessageCount());
        assertEquals(expectedCount.getTransferMessageCount(), actual.getTransferMessageCount());
        assertEquals(expectedCount.getTransferDeadLetterMessageCount(), actual.getTransferDeadLetterMessageCount());
    }

    /**
     * Verify we can deserialize feed XML from a list of subscriptions that has a paged response.
     */
    @Test
    void deserializeSubscriptionDescriptionFeed() throws IOException {
        // Arrange
        final String contents = getContents("SubscriptionDescriptionFeed.xml");
        final List<ResponseLink> responseLinks = Collections.singletonList(
            new ResponseLink().setRel("self")
                .setHref("https://sb-java-conniey-5.servicebus.windows.net/topic/Subscriptions?api-version=2017-04&enrich=false&$skip=0&$top=100")
        );

        final String topicName = "topic";
        final String subscriptionName1 = "subscription-0";
        final String subscriptionName2 = "subscription-session-0";
        final String subscriptionName3 = "subscription-session-1";

        final SubscriptionDescription subscription1 = EntityHelper.getSubscriptionDescription(
            new CreateSubscriptionOptions()
                .setLockDuration(Duration.ofSeconds(15))
                .setDefaultMessageTimeToLive(Duration.ofMinutes(5))
                .setMaxDeliveryCount(5)
                .setAutoDeleteOnIdle(Duration.ofDays(1)));
        final SubscriptionDescription subscription2 = EntityHelper.getSubscriptionDescription(
            new CreateSubscriptionOptions()
                .setRequiresSession(true)
                .setLockDuration(Duration.ofSeconds(15))
                .setMaxDeliveryCount(5));
        final SubscriptionDescription subscription3 = EntityHelper.getSubscriptionDescription(
            new CreateSubscriptionOptions()
                .setRequiresSession(true)
                .setLockDuration(Duration.ofSeconds(15))
                .setMaxDeliveryCount(5));
        final List<SubscriptionDescription> expectedDescriptions = Arrays.asList(
            subscription1, subscription2, subscription3);

        final SubscriptionDescriptionEntry entry1 = new SubscriptionDescriptionEntry()
            .setId("https://sb-java-conniey-5.servicebus.windows.net/topic/Subscriptions/subscription-0?api-version=2017-04")
            .setTitle(getResponseTitle(subscriptionName1))
            .setPublished(OffsetDateTime.parse("2020-06-22T23:47:53Z"))
            .setUpdated(OffsetDateTime.parse("2020-06-23T23:47:53Z"))
            .setLink(new ResponseLink().setRel("self").setHref("Subscriptions/subscription-0?api-version=2017-04"))
            .setContent(new SubscriptionDescriptionEntryContent()
                .setType("application/xml")
                .setSubscriptionDescription(subscription1));
        final SubscriptionDescriptionEntry entry2 = new SubscriptionDescriptionEntry()
            .setId("https://sb-java-conniey-5.servicebus.windows.net/topic/Subscriptions/subscription-session-0?api-version=2017-04")
            .setTitle(getResponseTitle(subscriptionName2))
            .setPublished(OffsetDateTime.parse("2020-06-22T23:47:53Z"))
            .setUpdated(OffsetDateTime.parse("2020-05-22T23:47:53Z"))
            .setLink(new ResponseLink().setRel("self").setHref("Subscriptions/subscription-session-0?api-version=2017-04"))
            .setContent(new SubscriptionDescriptionEntryContent()
                .setType("application/xml")
                .setSubscriptionDescription(subscription2));
        final SubscriptionDescriptionEntry entry3 = new SubscriptionDescriptionEntry()
            .setId("https://sb-java-conniey-5.servicebus.windows.net/topic/Subscriptions/subscription-session-1?api-version=2017-04")
            .setTitle(getResponseTitle(subscriptionName3))
            .setPublished(OffsetDateTime.parse("2020-06-22T23:47:54Z"))
            .setUpdated(OffsetDateTime.parse("2020-04-22T23:47:54Z"))
            .setLink(new ResponseLink().setRel("self").setHref("Subscriptions/subscription-session-1?api-version=2017-04"))
            .setContent(new SubscriptionDescriptionEntryContent()
                .setType("application/xml")
                .setSubscriptionDescription(subscription3));

        final Map<String, String> titleMap = new HashMap<>();
        titleMap.put("", "Subscriptions");
        titleMap.put("type", "text");
        final List<SubscriptionDescriptionEntry> entries = Arrays.asList(entry1, entry2, entry3);
        final SubscriptionDescriptionFeed expected = new SubscriptionDescriptionFeed()
            .setId("feed-id")
            .setTitle(titleMap)
            .setUpdated(OffsetDateTime.parse("2020-06-30T11:41:32Z"))
            .setLink(responseLinks)
            .setEntry(entries);
        final int expectedNumberOfEntries = 11;

        // Act
        final SubscriptionDescriptionFeed actual = serializer.deserialize(contents, SubscriptionDescriptionFeed.class);

        // Assert
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

        // We're not going to assert all 13 entries.
        assertTrue(expected.getEntry().size() < actual.getEntry().size());
        assertEquals(expectedNumberOfEntries, actual.getEntry().size());

        for (int i = 0; i < expected.getEntry().size(); i++) {
            final SubscriptionDescriptionEntry expectedEntry = expected.getEntry().get(i);
            final SubscriptionDescriptionEntry actualEntry = actual.getEntry().get(i);

            assertEquals(expected.getId(), actual.getId());
            assertNotNull(actual.getTitle());

            assertResponseTitle(expectedEntry.getTitle(), actualEntry.getTitle());
            assertEquals(expectedEntry.getUpdated(), actualEntry.getUpdated());
            assertEquals(expectedEntry.getPublished(), actualEntry.getPublished());

            final SubscriptionDescription expectedSubscription = expectedDescriptions.get(i);
            assertSubscriptionEquals(expectedSubscription, EntityStatus.ACTIVE,
                actualEntry.getContent().getSubscriptionDescription());
        }
    }

    /**
     * Verify we can deserialize XML from a GET rule.
     */
    @Test
    void deserializeSqlRule() throws IOException {
        // Arrange
        final String contents = getContents("SqlRuleFilter.xml");
        final RuleDescription expectedRule = new RuleDescription()
            .setName("foo")
            .setCreatedAt(OffsetDateTime.parse("2020-08-28T04:32:20.9387321Z"))
            .setAction(new EmptyRuleActionImpl())
            .setFilter(new SqlFilterImpl()
                .setCompatibilityLevel("20")
                .setSqlExpression("type = \"TestType\""));
        final RuleDescriptionEntry expected = new RuleDescriptionEntry()
            .setId("sb://sb-java-conniey-3.servicebus.windows.net/topic-10/Subscriptions/subscription/Rules/foo?api-version=2017-04&enrich=false")
            .setPublished(OffsetDateTime.parse("2020-08-28T04:32:20Z"))
            .setUpdated(OffsetDateTime.parse("2020-08-28T04:34:20Z"))
            .setContent(new RuleDescriptionEntryContent()
                .setRuleDescription(expectedRule)
                .setType("application/xml"));

        // Act
        final RuleDescriptionEntry actual = serializer.deserialize(contents, RuleDescriptionEntry.class);

        // Assert
        assertRuleEntryEquals(expected, actual);
    }

    /**
     * Verify we can deserialize XML from a GET rule that includes an action.
     */
    @Test
    void deserializeSqlRuleWithAction() throws IOException {
        // Arrange
        final String contents = getContents("SqlRuleFilterWithAction.xml");
        final RuleDescription expectedRule = new RuleDescription()
            .setName("foo")
            .setCreatedAt(OffsetDateTime.parse("2020-08-28T04:51:24.9967451Z"))
            .setAction(new SqlRuleActionImpl()
                .setCompatibilityLevel("20")
                .setSqlExpression("set FilterTag = 'true'"))
            .setFilter(new SqlFilterImpl()
                .setCompatibilityLevel("20")
                .setSqlExpression("type = \"TestType\""));
        final RuleDescriptionEntry expected = new RuleDescriptionEntry()
            .setId("https://sb-java-conniey-3.servicebus.windows.net/topic-10/Subscriptions/subscription/Rules/foo?api-version=2017-04")
            .setPublished(OffsetDateTime.parse("2020-08-28T04:51:24Z"))
            .setUpdated(OffsetDateTime.parse("2020-08-28T04:54:24Z"))
            .setContent(new RuleDescriptionEntryContent()
                .setRuleDescription(expectedRule)
                .setType("application/xml"));

        // Act
        final RuleDescriptionEntry actual = serializer.deserialize(contents, RuleDescriptionEntry.class);

        // Assert
        assertRuleEntryEquals(expected, actual);
    }

    /**
     * Verify we can deserialize XML from a GET correlation filter rule that includes an action.
     */
    @Test
    void deserializeCorrelationFilterRule() throws IOException {
        // Arrange
        final String contents = getContents("CorrelationRuleFilter.xml");
        final RuleDescription expectedRule = new RuleDescription()
            .setName("correlation-test")
            .setCreatedAt(OffsetDateTime.parse("2020-08-28T04:32:50.7697024Z"))
            .setAction(new EmptyRuleActionImpl())
            .setFilter(new CorrelationFilterImpl()
                .setLabel("matching-label"));
        final RuleDescriptionEntry expected = new RuleDescriptionEntry()
            .setId("sb://sb-java-conniey-3.servicebus.windows.net/topic-10/Subscriptions/subscription/Rules/correl?api-version=2017-04&enrich=false")
            .setPublished(OffsetDateTime.parse("2020-08-28T04:32:50Z"))
            .setUpdated(OffsetDateTime.parse("2020-08-28T04:34:50Z"))
            .setContent(new RuleDescriptionEntryContent()
                .setRuleDescription(expectedRule)
                .setType("application/xml"));

        // Act
        final RuleDescriptionEntry actual = serializer.deserialize(contents, RuleDescriptionEntry.class);

        // Assert
        assertRuleEntryEquals(expected, actual);
    }

    /**
     * Verify we can deserialize XML from a GET rule that includes an action.
     */
    @Test
    void deserializeRulesFeed() throws IOException {
        // Arrange
        final String contents = getContents("RuleDescriptionFeed.xml");

        final RuleDescription defaultRule = new RuleDescription()
            .setName("$Default")
            .setCreatedAt(OffsetDateTime.parse("2020-08-12T18:48:00.1005312Z"))
            .setAction(new EmptyRuleActionImpl())
            .setFilter(new TrueFilterImpl().setCompatibilityLevel("20").setSqlExpression("1=1"));
        final RuleDescriptionEntry defaultRuleEntry = new RuleDescriptionEntry()
            .setId("https://sb-java-conniey-3.servicebus.windows.net/topic-10/Subscriptions/subscription/rules/$Default?api-version=2017-04")
            .setPublished(OffsetDateTime.parse("2020-08-12T18:48:00Z"))
            .setUpdated(OffsetDateTime.parse("2020-08-12T18:48:00Z"))
            .setContent(new RuleDescriptionEntryContent()
                .setRuleDescription(defaultRule)
                .setType("application/xml"));

        final RuleDescription correlation = new RuleDescription()
            .setName("correl")
            .setCreatedAt(OffsetDateTime.parse("2020-08-28T04:32:50.7697024Z"))
            .setAction(new EmptyRuleActionImpl())
            .setFilter(new CorrelationFilterImpl()
                .setLabel("matching-label"));
        final RuleDescriptionEntry correlationEntry = new RuleDescriptionEntry()
            .setId("https://sb-java-conniey-3.servicebus.windows.net/topic-10/Subscriptions/subscription/rules/correl?api-version=2017-04")
            .setPublished(OffsetDateTime.parse("2020-08-28T04:32:50Z"))
            .setUpdated(OffsetDateTime.parse("2020-08-28T04:32:50Z"))
            .setContent(new RuleDescriptionEntryContent()
                .setRuleDescription(correlation)
                .setType("application/xml"));

        final RuleDescription sqlRule = new RuleDescription()
            .setName("foo")
            .setCreatedAt(OffsetDateTime.parse("2020-08-28T04:51:24.9967451Z"))
            .setAction(new SqlRuleActionImpl()
                .setCompatibilityLevel("20")
                .setSqlExpression("set FilterTag = 'true'"))
            .setFilter(new SqlFilterImpl()
                .setCompatibilityLevel("20")
                .setSqlExpression("type = \"TestType\""));
        final RuleDescriptionEntry sqlRuleEntry = new RuleDescriptionEntry()
            .setId("https://sb-java-conniey-3.servicebus.windows.net/topic-10/Subscriptions/subscription/rules/foo?api-version=2017-04")
            .setPublished(OffsetDateTime.parse("2020-08-28T04:32:20Z"))
            .setUpdated(OffsetDateTime.parse("2020-08-28T04:32:20Z"))
            .setContent(new RuleDescriptionEntryContent()
                .setRuleDescription(sqlRule)
                .setType("application/xml"));

        final List<RuleDescriptionEntry> expectedEntries = Arrays.asList(defaultRuleEntry, correlationEntry, sqlRuleEntry);
        final RuleDescriptionFeed expected = new RuleDescriptionFeed()
            .setEntry(expectedEntries)
            .setId("https://sb-java-conniey-3.servicebus.windows.net/topic-10/Subscriptions/subscription/rules?api-version=2017-04&enrich=false&$skip=0&$top=100")
            .setUpdated(OffsetDateTime.parse("2020-08-28T14:59:16Z"));

        // Act
        final RuleDescriptionFeed actual = serializer.deserialize(contents, RuleDescriptionFeed.class);

        // Assert
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());

        final List<RuleDescriptionEntry> actualEntries = actual.getEntry();
        assertNotNull(actualEntries);
        assertEquals(expectedEntries.size(), actualEntries.size());

        for (int i = 0; i < expected.getEntry().size(); i++) {
            final RuleDescriptionEntry expectedRule = expectedEntries.get(i);
            final RuleDescriptionEntry actualRule = actualEntries.get(i);

            assertRuleEntryEquals(expectedRule, actualRule);
        }
    }

    /**
     * Given a file name, gets the corresponding resource and its contents as a string.
     *
     * @param fileName Name of file to fetch.
     *
     * @return Contents of the file.
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

    private static void assertQueueEquals(CreateQueueOptions expected, EntityStatus expectedStatus,
        QueueDescription actual) {

        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getLockDuration(), actual.getLockDuration());
        assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
        assertEquals(expected.requiresDuplicateDetection(), actual.isRequiresDuplicateDetection());
        assertEquals(expected.requiresSession(), actual.isRequiresSession());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.deadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
        assertEquals(expected.getDuplicateDetectionHistoryTimeWindow(), actual.getDuplicateDetectionHistoryTimeWindow());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.enableBatchedOperations(), actual.isEnableBatchedOperations());

        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.enablePartitioning(), actual.isEnablePartitioning());

        assertEquals(expectedStatus, actual.getStatus());
    }

    private static void assertSubscriptionEquals(SubscriptionDescription expected, EntityStatus expectedStatus,
        SubscriptionDescription actual) {

        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getLockDuration(), actual.getLockDuration());
        assertEquals(expected.isDeadLetteringOnFilterEvaluationExceptions(),
            actual.isDeadLetteringOnFilterEvaluationExceptions());
        assertEquals(expected.isRequiresSession(), actual.isRequiresSession());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.isDeadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.isEnableBatchedOperations(), actual.isEnableBatchedOperations());
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());

        assertEquals(expectedStatus, actual.getStatus());
    }

    private static void assertRuleEntryEquals(RuleDescriptionEntry expected, RuleDescriptionEntry actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());

        if (expected.getContent() == null) {
            assertNull(actual.getContent());
            return;
        }

        assertNotNull(actual.getContent());
        assertEquals(expected.getContent().getType(), actual.getContent().getType());

        final RuleDescription expectedRule = expected.getContent().getRuleDescription();
        final RuleDescription actualRule = actual.getContent().getRuleDescription();
        assertNotNull(actualRule);
        assertRuleEquals(expectedRule, actualRule);
    }

    private static void assertRuleEquals(RuleDescription expected, RuleDescription actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        assertNotNull(actual);
        assertEquals(expected.getName(), actual.getName());

        // Rule action assertions.
        if (expected.getAction() instanceof EmptyRuleActionImpl) {
            assertTrue(actual.getAction() instanceof EmptyRuleActionImpl);
        } else if (expected.getAction() instanceof SqlRuleActionImpl) {
            assertTrue(actual.getAction() instanceof SqlRuleActionImpl);

            final SqlRuleActionImpl expectedAction = (SqlRuleActionImpl) expected.getAction();
            final SqlRuleActionImpl actualAction = (SqlRuleActionImpl) actual.getAction();

            assertEquals(expectedAction.getCompatibilityLevel(), actualAction.getCompatibilityLevel());
            assertEquals(expectedAction.getSqlExpression(), actualAction.getSqlExpression());
            assertEquals(expectedAction.isRequiresPreprocessing(), actualAction.isRequiresPreprocessing());

            assertParameters(expectedAction.getParameters(), actualAction.getParameters());
        }

        // Rule filter assertions.
        if (expected.getFilter() instanceof TrueFilterImpl) {
            assertTrue(actual.getFilter() instanceof TrueFilterImpl);
        } else if (expected.getFilter() instanceof FalseFilterImpl) {
            assertTrue(actual.getFilter() instanceof FalseFilterImpl);
        }

        if (expected.getFilter() instanceof SqlFilterImpl) {
            assertTrue(actual.getFilter() instanceof SqlFilterImpl);

            final SqlFilterImpl expectedFilter = (SqlFilterImpl) expected.getFilter();
            final SqlFilterImpl actualFilter = (SqlFilterImpl) actual.getFilter();

            assertEquals(expectedFilter.getCompatibilityLevel(), actualFilter.getCompatibilityLevel());
            assertEquals(expectedFilter.getSqlExpression(), actualFilter.getSqlExpression());

            assertParameters(expectedFilter.getParameters(), actualFilter.getParameters());
        } else if (expected.getFilter() instanceof CorrelationFilterImpl) {
            assertTrue(actual.getFilter() instanceof CorrelationFilterImpl);

            final CorrelationFilterImpl expectedFilter = (CorrelationFilterImpl) expected.getFilter();
            final CorrelationFilterImpl actualFilter = (CorrelationFilterImpl) actual.getFilter();

            assertEquals(expectedFilter.getCorrelationId(), actualFilter.getCorrelationId());
            assertEquals(expectedFilter.getMessageId(), actualFilter.getMessageId());
            assertEquals(expectedFilter.getTo(), actualFilter.getTo());
            assertEquals(expectedFilter.getReplyTo(), actualFilter.getReplyTo());
            assertEquals(expectedFilter.getReplyToSessionId(), actualFilter.getReplyToSessionId());
            assertEquals(expectedFilter.getSessionId(), actualFilter.getSessionId());
            assertEquals(expectedFilter.getContentType(), actualFilter.getContentType());

            assertParameters(expectedFilter.getProperties(), actualFilter.getProperties());
        }
    }

    private static void assertParameters(List<KeyValueImpl> expected, List<KeyValueImpl> actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());

        final Map<String, KeyValueImpl> actualMap = actual.stream()
            .collect(Collectors.toMap(KeyValueImpl::getKey, Function.identity()));

        for (KeyValueImpl item : expected) {
            final KeyValueImpl removed = actualMap.remove(item.getKey());

            assertNotNull(removed);
            assertEquals(item.getValue(), removed.getValue());
        }

        assertTrue(actualMap.isEmpty());
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
