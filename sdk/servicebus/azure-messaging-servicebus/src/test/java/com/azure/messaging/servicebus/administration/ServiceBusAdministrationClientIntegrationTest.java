// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusServiceVersion;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.TestUtils;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.messaging.servicebus.models.ServiceBusMessageState;
import com.azure.messaging.servicebus.administration.models.AccessRights;
import com.azure.messaging.servicebus.administration.models.CorrelationRuleFilter;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.EmptyRuleAction;
import com.azure.messaging.servicebus.administration.models.EntityStatus;
import com.azure.messaging.servicebus.administration.models.FalseRuleFilter;
import com.azure.messaging.servicebus.administration.models.MessagingSku;
import com.azure.messaging.servicebus.administration.models.NamespaceProperties;
import com.azure.messaging.servicebus.administration.models.NamespaceType;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SharedAccessAuthorizationRule;
import com.azure.messaging.servicebus.administration.models.SqlRuleAction;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.administration.models.TopicRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.messaging.servicebus.TestUtils.assertAuthorizationRules;
import static com.azure.messaging.servicebus.TestUtils.getEntityName;
import static com.azure.messaging.servicebus.TestUtils.getQueueBaseName;
import static com.azure.messaging.servicebus.TestUtils.getRuleBaseName;
import static com.azure.messaging.servicebus.TestUtils.getSubscriptionBaseName;
import static com.azure.messaging.servicebus.TestUtils.getTopicBaseName;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClientIntegrationTest.configure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests {@link ServiceBusAdministrationClient}.
 */
@Tag("integration")
@Execution(ExecutionMode.SAME_THREAD)
public class ServiceBusAdministrationClientIntegrationTest extends TestProxyTestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private final AtomicReference<TokenCredential> credentialCached = new AtomicReference<>();
    private final TokenCredential conformanceCredential = new DefaultAzureCredentialBuilder().build();

    //region Create tests

    @Test
    void createQueue() {
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = testResourceNamer.randomName("queue", 10);
        final String forwardToEntityName = getEntityName(getQueueBaseName(), 5);

        final String keyName = "test-rule";
        final List<AccessRights> accessRights = Collections.singletonList(AccessRights.SEND);
        final SharedAccessAuthorizationRule rule = interceptorManager.isPlaybackMode()
            ? new SharedAccessAuthorizationRule(keyName, "REDACTED", "REDACTED", accessRights)
            : new SharedAccessAuthorizationRule(keyName, accessRights);

        final CreateQueueOptions expected = new CreateQueueOptions().setMaxSizeInMegabytes(1024)
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setDuplicateDetectionRequired(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing")
            .setForwardTo(forwardToEntityName)
            .setForwardDeadLetteredMessagesTo(forwardToEntityName);

        expected.getAuthorizationRules().add(rule);

        final QueueProperties actual = client.createQueue(queueName, expected);
        assertEquals(queueName, actual.getName());

        assertEquals(expected.getLockDuration(), actual.getLockDuration());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

        assertEquals(expected.isDeadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
        assertEquals(expected.isPartitioningEnabled(), actual.isPartitioningEnabled());
        assertEquals(expected.isDuplicateDetectionRequired(), actual.isDuplicateDetectionRequired());

        // The URL will be fake, so we can't compare them.
        if (!interceptorManager.isPlaybackMode()) {
            assertEquals(expected.getForwardTo(), actual.getForwardTo());
            assertEquals(expected.getForwardDeadLetteredMessagesTo(), actual.getForwardDeadLetteredMessagesTo());
        }

        assertAuthorizationRules(expected.getAuthorizationRules(), actual.getAuthorizationRules());

        final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(actual);
        assertEquals(0, runtimeProperties.getTotalMessageCount());
        assertEquals(0, runtimeProperties.getSizeInBytes());
        assertNotNull(runtimeProperties.getCreatedAt());
    }

    @Test
    void createQueueWithForwarding() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = testResourceNamer.randomName("test", 10);
        final String forwardToEntityName = getEntityName(TestUtils.getQueueBaseName(), 5);
        final CreateQueueOptions expected = new CreateQueueOptions().setForwardTo(forwardToEntityName)
            .setForwardDeadLetteredMessagesTo(forwardToEntityName);

        // Act
        final Response<QueueProperties> response = client.createQueueWithResponse(queueName, expected, Context.NONE);
        final QueueProperties actual = response.getValue();

        // Assert
        assertEquals(queueName, actual.getName());

        // The URLs will be fake in playback mode.
        if (!interceptorManager.isPlaybackMode()) {
            assertEquals(expected.getForwardTo(), actual.getForwardTo());
            assertEquals(expected.getForwardDeadLetteredMessagesTo(), actual.getForwardDeadLetteredMessagesTo());
        }

        final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(actual);
        assertNotNull(runtimeProperties.getCreatedAt());
    }

    @Test
    void createQueueAuthorizationRules() {
        // Arrange
        final String keyName = "test-rule";
        final List<AccessRights> accessRights = Collections.singletonList(AccessRights.SEND);
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = testResourceNamer.randomName("test", 10);
        final SharedAccessAuthorizationRule rule = interceptorManager.isPlaybackMode()
            ? new SharedAccessAuthorizationRule(keyName, "REDACTED", "REDACTED", accessRights)
            : new SharedAccessAuthorizationRule(keyName, accessRights);

        final CreateQueueOptions expected = new CreateQueueOptions().setMaxSizeInMegabytes(1024)
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setSessionRequired(true)
            .setDuplicateDetectionRequired(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing");

        expected.getAuthorizationRules().add(rule);

        // Act
        final Response<QueueProperties> response = client.createQueueWithResponse(queueName, expected, Context.NONE);
        final QueueProperties actual = response.getValue();

        // Assert
        assertEquals(queueName, actual.getName());

        assertEquals(expected.getLockDuration(), actual.getLockDuration());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

        assertEquals(expected.isDeadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
        assertEquals(expected.isPartitioningEnabled(), actual.isPartitioningEnabled());
        assertEquals(expected.isDuplicateDetectionRequired(), actual.isDuplicateDetectionRequired());
        assertEquals(expected.isSessionRequired(), actual.isSessionRequired());

        final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(actual);
        assertEquals(0, runtimeProperties.getTotalMessageCount());
        assertEquals(0, runtimeProperties.getSizeInBytes());
        assertNotNull(runtimeProperties.getCreatedAt());

        assertAuthorizationRules(expected.getAuthorizationRules(), actual.getAuthorizationRules());
    }

    @Test
    void createTopicWithResponse() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = testResourceNamer.randomName("test", 10);
        final CreateTopicOptions expected = new CreateTopicOptions().setMaxSizeInMegabytes(2048L)
            .setDuplicateDetectionRequired(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing-topic");

        final Response<TopicProperties> response = client.createTopicWithResponse(topicName, expected, null);
        assertEquals(201, response.getStatusCode());

        final TopicProperties actual = response.getValue();

        assertEquals(topicName, actual.getName());
        assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());
        assertEquals(expected.isPartitioningEnabled(), actual.isPartitioningEnabled());
        assertEquals(expected.isDuplicateDetectionRequired(), actual.isDuplicateDetectionRequired());

        final TopicRuntimeProperties runtimeProperties = new TopicRuntimeProperties(actual);
        assertEquals(0, runtimeProperties.getSubscriptionCount());
        assertEquals(0, runtimeProperties.getSizeInBytes());
        assertNotNull(runtimeProperties.getCreatedAt());
    }

    @Test
    void createTopicExistingName() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 3);
        final CreateTopicOptions expected = new CreateTopicOptions().setMaxSizeInMegabytes(2048L)
            .setDuplicateDetectionRequired(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing-topic");

        // Act & Assert
        assertThrows(ResourceExistsException.class,
            () -> client.createTopicWithResponse(topicName, expected, Context.NONE));
    }

    @Test
    void createSubscription() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String forwardToTopic = getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = testResourceNamer.randomName(getSubscriptionBaseName(), 10);
        final CreateSubscriptionOptions expected = new CreateSubscriptionOptions().setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setUserMetadata("some-metadata-for-testing-subscriptions")
            .setForwardTo(forwardToTopic)
            .setForwardDeadLetteredMessagesTo(forwardToTopic);

        final SubscriptionProperties actual = client.createSubscription(topicName, subscriptionName, expected);
        assertEquals(topicName, actual.getTopicName());
        assertEquals(subscriptionName, actual.getSubscriptionName());

        assertEquals(expected.getLockDuration(), actual.getLockDuration());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

        assertEquals(expected.isDeadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
        assertEquals(expected.isSessionRequired(), actual.isSessionRequired());

        // URLs are redacted so they will not match.
        if (!interceptorManager.isPlaybackMode()) {
            assertEquals(expected.getForwardTo(), actual.getForwardTo());
            assertEquals(expected.getForwardDeadLetteredMessagesTo(), actual.getForwardDeadLetteredMessagesTo());
        }
    }

    @Test
    void createRule() {
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = testResourceNamer.randomName("rule", 5);
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();
        final SqlRuleAction action = new SqlRuleAction("SET Label = 'test'");
        final CreateRuleOptions options = new CreateRuleOptions().setAction(action).setFilter(new FalseRuleFilter());

        final RuleProperties actual = client.createRule(topicName, ruleName, subscriptionName, options);
        assertNotNull(actual);
        assertEquals(ruleName, actual.getName());
        assertNotNull(actual.getAction());

        assertInstanceOf(SqlRuleAction.class, actual.getAction());
        assertEquals(action.getSqlExpression(), ((SqlRuleAction) actual.getAction()).getSqlExpression());

        assertNotNull(actual.getFilter());
        assertInstanceOf(FalseRuleFilter.class, actual.getFilter());
    }

    @Test
    void createRuleDefaults() {
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = testResourceNamer.randomName("rule", 7);
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();

        final RuleProperties rule = client.createRule(topicName, subscriptionName, ruleName);
        assertEquals(ruleName, rule.getName());
        assertInstanceOf(TrueRuleFilter.class, rule.getFilter());
        assertInstanceOf(EmptyRuleAction.class, rule.getAction());
    }

    @Test
    void createRuleResponse() {
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = testResourceNamer.randomName("rule", 10);
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();

        final SqlRuleFilter filter = new SqlRuleFilter("sys.To=@MyParameter OR sys.MessageId IS NULL");
        filter.getParameters().put("@MyParameter", "My-Parameter-Value");

        final CreateRuleOptions options = new CreateRuleOptions().setAction(new EmptyRuleAction()).setFilter(filter);

        final Response<RuleProperties> response
            = client.createRuleWithResponse(topicName, subscriptionName, ruleName, options, null);
        assertEquals(201, response.getStatusCode());

        final RuleProperties contents = response.getValue();
        assertNotNull(contents);
        assertEquals(ruleName, contents.getName());

        assertNotNull(contents.getFilter());
        assertInstanceOf(SqlRuleFilter.class, contents.getFilter());

        final SqlRuleFilter actualFilter = (SqlRuleFilter) contents.getFilter();
        assertEquals(filter.getSqlExpression(), actualFilter.getSqlExpression());

        assertNotNull(contents.getAction());
        assertInstanceOf(EmptyRuleAction.class, contents.getAction());
    }

    @Test
    void createQueueExistingName() {
        final String queueName = getEntityName(getQueueBaseName(), 5);
        final CreateQueueOptions options = new CreateQueueOptions();
        final ServiceBusAdministrationClient client = getClient();

        assertThrows(ResourceExistsException.class, () -> client.createQueue(queueName, options),
            "Queue exists exception not thrown when creating a queue with existing name");
    }

    @Test
    void createSubscriptionExistingName() {
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();
        final ServiceBusAdministrationClient client = getClient();

        assertThrows(ResourceExistsException.class, () -> client.createSubscription(topicName, subscriptionName),
            "Queue exists exception not thrown when creating a queue with existing name");
    }

    @Test
    void createSubscriptionWithForwarding() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 3);
        final String subscriptionName = testResourceNamer.randomName("sub", 50);
        final String forwardToTopic = getEntityName(getTopicBaseName(), 4);
        final CreateSubscriptionOptions expected = new CreateSubscriptionOptions().setForwardTo(forwardToTopic)
            .setForwardDeadLetteredMessagesTo(forwardToTopic);

        // Act
        final SubscriptionProperties actual = client.createSubscription(topicName, subscriptionName, expected);

        // Assert
        assertEquals(topicName, actual.getTopicName());
        assertEquals(subscriptionName, actual.getSubscriptionName());

        // URLs are redacted so they will not match.
        if (!interceptorManager.isPlaybackMode()) {
            assertEquals(expected.getForwardTo(), actual.getForwardTo());
            assertEquals(expected.getForwardDeadLetteredMessagesTo(), actual.getForwardDeadLetteredMessagesTo());
        }
    }

    //endregion

    @Test
    void updateRuleResponse() {
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = testResourceNamer.randomName("rule", 15);
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();
        final SqlRuleAction expectedAction = new SqlRuleAction("SET MessageId = 'matching-id'");
        final SqlRuleFilter expectedFilter = new SqlRuleFilter("sys.To = 'telemetry-event'");

        final RuleProperties existingRule = client.createRule(topicName, subscriptionName, ruleName);
        assertNotNull(existingRule);

        existingRule.setAction(expectedAction).setFilter(expectedFilter);

        final RuleProperties rule = client.updateRule(topicName, subscriptionName, existingRule);
        assertNotNull(rule);
        assertEquals(ruleName, rule.getName());

        assertInstanceOf(SqlRuleFilter.class, rule.getFilter());
        assertEquals(expectedFilter.getSqlExpression(), ((SqlRuleFilter) rule.getFilter()).getSqlExpression());

        assertInstanceOf(SqlRuleAction.class, rule.getAction());
        assertEquals(expectedAction.getSqlExpression(), ((SqlRuleAction) rule.getAction()).getSqlExpression());
    }

    //region Get and exists tests

    @Test
    void getNamespace() {
        final ServiceBusAdministrationClient client = getClient();

        final NamespaceProperties namespaceProperties = client.getNamespaceProperties();
        assertEquals(NamespaceType.MESSAGING, namespaceProperties.getNamespaceType());
        if (!interceptorManager.isPlaybackMode()) {
            final String[] split = TestUtils.getFullyQualifiedDomainName(true).split("\\.", 2);
            assertEquals(split[0], namespaceProperties.getName());
            assertNotNull(namespaceProperties.getMessagingSku());
            assertNotNull(namespaceProperties.getCreatedTime());
            assertNotNull(namespaceProperties.getModifiedTime());
        }
    }

    @Test
    @LiveOnly
    void conformanceNamespaceProperties() {
        final NamespaceProperties namespaceProperties = getConformanceClient().getNamespaceProperties();
        assertEquals(NamespaceType.MESSAGING, namespaceProperties.getNamespaceType());
        assertEquals(TestUtils.getFullyQualifiedDomainName(false).split("\\.", 2)[0], namespaceProperties.getName());
        assertNotNull(namespaceProperties.getMessagingSku());
        assertNotNull(namespaceProperties.getCreatedTime());
        assertNotNull(namespaceProperties.getModifiedTime());
    }

    @Test
    @LiveOnly
    void premiumNamespaceHasDedicatedCapacityAndUnpartitionedQueue() {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final NamespaceProperties namespaceProperties = client.getNamespaceProperties();
        assumeTrue(MessagingSku.PREMIUM.equals(namespaceProperties.getMessagingSku()),
            "Dedicated capacity requires a Premium namespace.");
        assertNotNull(namespaceProperties.getMessagingUnits());
        assertTrue(namespaceProperties.getMessagingUnits() > 0);

        final String queueName = testResourceNamer.randomName("premium", 10);
        client.createQueue(queueName);
        try {
            assertFalse(client.getQueue(queueName).isPartitioningEnabled());
        } finally {
            client.deleteQueue(queueName);
        }
    }

    @Test
    @LiveOnly
    void standardPartitionedQueueRoutesMessages() {
        final ServiceBusAdministrationClient client = getConformanceClient();
        assumeTrue(MessagingSku.STANDARD.equals(client.getNamespaceProperties().getMessagingSku()),
            "Partitioned entity routing requires a Standard namespace.");
        final String queueName = testResourceNamer.randomName("partitioned", 10);
        client.createQueue(queueName, new CreateQueueOptions().setPartitioningEnabled(true));
        try {
            assertTrue(client.getQueue(queueName).isPartitioningEnabled());
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
                ServiceBusReceiverClient receiver = builder.receiver().queueName(queueName).buildClient()) {
                sender.sendMessage(new ServiceBusMessage("partitioned").setPartitionKey("route-one"));
                final ServiceBusReceivedMessage message
                    = receiver.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                assertNotNull(message);
                assertEquals("route-one", message.getPartitionKey());
                receiver.complete(message);
            }
        } finally {
            client.deleteQueue(queueName);
        }
    }

    @Test
    @LiveOnly
    void premiumLargeMessageRoundTrips() {
        final ServiceBusAdministrationClient client = getConformanceClient();
        assumeTrue(MessagingSku.PREMIUM.equals(client.getNamespaceProperties().getMessagingSku()),
            "Messages larger than 1 MB require Premium.");
        final String queueName = testResourceNamer.randomName("large", 10);
        client.createQueue(queueName, new CreateQueueOptions().setMaxMessageSizeInKilobytes(4096));
        try {
            assertTrue(client.getQueue(queueName).getMaxMessageSizeInKilobytes() >= 4096);
            final byte[] body = new byte[1024 * 1024 + 1];
            Arrays.fill(body, (byte) 0x5a);
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
                ServiceBusReceiverClient receiver = builder.receiver().queueName(queueName).buildClient()) {
                sender.sendMessage(new ServiceBusMessage(body));
                final ServiceBusReceivedMessage received
                    = receiver.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                assertNotNull(received);
                assertTrue(Arrays.equals(body, received.getBody().toBytes()));
                receiver.complete(received);
            }
        } finally {
            client.deleteQueue(queueName);
        }
    }

    @Test
    @LiveOnly
    void newSubscriptionReceivesViaDefaultRule() {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final String topicName = testResourceNamer.randomName("rules", 10);
        final String subscriptionName = testResourceNamer.randomName("default", 10);
        client.createTopic(topicName);
        try {
            client.createSubscription(topicName, subscriptionName);
            final RuleProperties rule = client.getRule(topicName, subscriptionName, "$Default");
            assertInstanceOf(TrueRuleFilter.class, rule.getFilter());
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().topicName(topicName).buildClient();
                ServiceBusReceiverClient receiver
                    = builder.receiver().topicName(topicName).subscriptionName(subscriptionName).buildClient()) {
                sender.sendMessage(new ServiceBusMessage("default-rule"));
                final ServiceBusReceivedMessage received
                    = receiver.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                assertNotNull(received);
                assertEquals("default-rule", received.getBody().toString());
                receiver.complete(received);
            }
        } finally {
            client.deleteTopic(topicName);
        }
    }

    @Test
    @LiveOnly
    void entityDefaultTtlExpiresMessageToDeadLetterQueue() {
        verifyExpiredMessage(false);
    }

    @Test
    @LiveOnly
    void perMessageTtlExpiresMessageToDeadLetterQueue() {
        verifyExpiredMessage(true);
    }

    @Test
    @LiveOnly
    void expiredMessageRemainsSettleableWhileLocked() throws InterruptedException {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final String queueName = testResourceNamer.randomName("inflight", 10);
        client.createQueue(queueName,
            new CreateQueueOptions().setDeadLetteringOnMessageExpiration(true).setLockDuration(Duration.ofMinutes(1)));
        try {
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
                ServiceBusReceiverClient receiver = builder.receiver().queueName(queueName).buildClient();
                ServiceBusReceiverClient deadLetters
                    = builder.receiver().queueName(queueName).subQueue(SubQueue.DEAD_LETTER_QUEUE).buildClient()) {
                sender.sendMessage(new ServiceBusMessage("in-flight").setTimeToLive(Duration.ofSeconds(10)));
                final ServiceBusReceivedMessage received
                    = receiver.receiveMessages(1, Duration.ofSeconds(20)).stream().findFirst().orElse(null);
                assertNotNull(received, "Message expired before it was locked.");
                final long expiry = received.getExpiresAt().toInstant().toEpochMilli();
                assertTrue(expiry <= System.currentTimeMillis() + Duration.ofSeconds(30).toMillis(),
                    "Message expiration exceeded the configured TTL.");
                assertTrue(received.getLockedUntil().toInstant().toEpochMilli() > expiry,
                    "The message lock must outlive its TTL to prove in-flight expiration.");
                final long delay = expiry - System.currentTimeMillis() + Duration.ofSeconds(2).toMillis();
                if (delay > 0) {
                    Thread.sleep(delay);
                }
                assertTrue(System.currentTimeMillis() > expiry, "Message did not expire while locked.");
                receiver.complete(received);
                assertNull(deadLetters.receiveMessages(1, Duration.ofSeconds(5)).stream().findFirst().orElse(null),
                    "A completed in-flight message must not be dead-lettered after its TTL.");
            }
        } finally {
            client.deleteQueue(queueName);
        }
    }

    @Test
    @LiveOnly
    void prefetchedMessagesAreBufferedByFirstReceiver() throws InterruptedException {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final String queueName = testResourceNamer.randomName("prefetch", 10);
        client.createQueue(queueName, new CreateQueueOptions().setLockDuration(Duration.ofMinutes(2)));
        try {
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
                ServiceBusReceiverClient prefetched
                    = builder.receiver().queueName(queueName).prefetchCount(10).buildClient();
                ServiceBusReceiverClient competing = builder.receiver().queueName(queueName).buildClient()) {
                for (int i = 0; i < 3; i++) {
                    sender.sendMessage(new ServiceBusMessage("buffer-" + i));
                }
                final ServiceBusReceivedMessage first
                    = prefetched.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                assertNotNull(first);
                Thread.sleep(Duration.ofSeconds(5).toMillis());
                assertNull(competing.receiveMessages(1, Duration.ofSeconds(5)).stream().findFirst().orElse(null),
                    "Another receiver obtained a message that should be buffered by prefetch.");
                final List<ServiceBusReceivedMessage> buffered
                    = prefetched.receiveMessages(2, Duration.ofSeconds(10)).stream().collect(Collectors.toList());
                assertEquals(2, buffered.size());
                prefetched.complete(first);
                buffered.forEach(prefetched::complete);
            }
        } finally {
            client.deleteQueue(queueName);
        }
    }

    @Test
    @LiveOnly
    void idleQueueIsDeletedByBroker() throws InterruptedException {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final String queueName = testResourceNamer.randomName("idle", 10);
        client.createQueue(queueName, new CreateQueueOptions().setAutoDeleteOnIdle(Duration.ofMinutes(5)));
        try {
            assertEquals(Duration.ofMinutes(5), client.getQueue(queueName).getAutoDeleteOnIdle());
            final long deadline = System.nanoTime() + Duration.ofMinutes(8).toNanos();
            while (client.getQueueExists(queueName) && System.nanoTime() < deadline) {
                Thread.sleep(Duration.ofSeconds(10).toMillis());
            }
            assertFalse(client.getQueueExists(queueName), "Broker did not delete the idle queue.");
        } finally {
            if (client.getQueueExists(queueName)) {
                client.deleteQueue(queueName);
            }
        }
    }

    @Test
    @LiveOnly
    void excessForwardingHopsMoveMessageToTransferDeadLetterQueue() {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final String[] queues = new String[6];
        for (int i = 0; i < queues.length; i++) {
            queues[i] = testResourceNamer.randomName("hop" + i, 10);
        }
        int created = 0;
        try {
            for (int i = queues.length - 1; i >= 0; i--) {
                client.createQueue(queues[i],
                    i == queues.length - 1
                        ? new CreateQueueOptions()
                        : new CreateQueueOptions().setForwardTo(queues[i + 1]));
                created++;
            }
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().queueName(queues[0]).buildClient()) {
                final List<ServiceBusReceiverClient> transferReceivers = new ArrayList<>();
                try {
                    for (int i = 0; i < queues.length - 1; i++) {
                        transferReceivers.add(builder.receiver()
                            .queueName(queues[i])
                            .subQueue(SubQueue.TRANSFER_DEAD_LETTER_QUEUE)
                            .buildClient());
                    }
                    sender.sendMessage(new ServiceBusMessage("too-many-hops"));
                    final long deadline = System.nanoTime() + Duration.ofMinutes(2).toNanos();
                    ServiceBusReceivedMessage failed = null;
                    while (failed == null && System.nanoTime() < deadline) {
                        for (ServiceBusReceiverClient receiver : transferReceivers) {
                            failed
                                = receiver.receiveMessages(1, Duration.ofSeconds(1)).stream().findFirst().orElse(null);
                            if (failed != null) {
                                assertEquals("MaxTransferHopCountExceeded", failed.getDeadLetterReason());
                                assertEquals("too-many-hops", failed.getBody().toString());
                                receiver.complete(failed);
                                break;
                            }
                        }
                    }
                    assertNotNull(failed, "Excess forwarding did not enter any forwarding source's transfer DLQ.");
                } finally {
                    transferReceivers.forEach(ServiceBusReceiverClient::close);
                }
            }
        } finally {
            for (int i = queues.length - created; i < queues.length; i++) {
                client.deleteQueue(queues[i]);
            }
        }
    }

    @Test
    @LiveOnly
    void filterEvaluationFailureDeadLettersMessage() {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final String topicName = testResourceNamer.randomName("filter", 10);
        final String subscriptionName = testResourceNamer.randomName("errors", 10);
        client.createTopic(topicName);
        try {
            client.createSubscription(topicName, subscriptionName,
                new CreateSubscriptionOptions().setEnableDeadLetteringOnFilterEvaluationExceptions(true));
            client.deleteRule(topicName, subscriptionName, "$Default");
            client.createRule(topicName, "divide-by-zero", subscriptionName,
                new CreateRuleOptions().setFilter(new SqlRuleFilter("1 / divisor > 0")));
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().topicName(topicName).buildClient();
                ServiceBusReceiverClient deadLetters = builder.receiver()
                    .topicName(topicName)
                    .subscriptionName(subscriptionName)
                    .subQueue(SubQueue.DEAD_LETTER_QUEUE)
                    .buildClient()) {
                final ServiceBusMessage invalid = new ServiceBusMessage("invalid-filter");
                invalid.getApplicationProperties().put("divisor", 0);
                sender.sendMessage(invalid);
                final long deadline = System.nanoTime() + Duration.ofMinutes(2).toNanos();
                ServiceBusReceivedMessage failed = null;
                while (failed == null && System.nanoTime() < deadline) {
                    failed = deadLetters.receiveMessages(1, Duration.ofSeconds(5)).stream().findFirst().orElse(null);
                }
                assertNotNull(failed, "The filter evaluation error did not dead-letter the message.");
                assertEquals("invalid-filter", failed.getBody().toString());
                assertEquals("FilterEvaluationException", failed.getDeadLetterReason());
                deadLetters.complete(failed);
            }
        } finally {
            client.deleteTopic(topicName);
        }
    }

    private void verifyExpiredMessage(boolean perMessageTtl) {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final String queueName = testResourceNamer.randomName("expiry", 10);
        final CreateQueueOptions options = new CreateQueueOptions().setDeadLetteringOnMessageExpiration(true);
        if (!perMessageTtl) {
            options.setDefaultMessageTimeToLive(Duration.ofSeconds(10));
        }
        client.createQueue(queueName, options);
        try {
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
                ServiceBusReceiverClient deadLetters
                    = builder.receiver().queueName(queueName).subQueue(SubQueue.DEAD_LETTER_QUEUE).buildClient()) {
                final ServiceBusMessage message = new ServiceBusMessage("expired");
                if (perMessageTtl) {
                    message.setTimeToLive(Duration.ofSeconds(10));
                }
                sender.sendMessage(message);
                final OffsetDateTime deadline = OffsetDateTime.now().plusMinutes(2);
                ServiceBusReceivedMessage expired = null;
                while (expired == null && OffsetDateTime.now().isBefore(deadline)) {
                    expired = deadLetters.receiveMessages(1, Duration.ofSeconds(5)).stream().findFirst().orElse(null);
                }
                assertNotNull(expired, "Expired message was not moved to the dead-letter queue.");
                assertEquals("TTLExpiredException", expired.getDeadLetterReason());
                deadLetters.complete(expired);
            }
        } finally {
            client.deleteQueue(queueName);
        }
    }

    @Test
    @LiveOnly
    void maxDeliveryCountDeadLettersAbandonedMessage() {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final String queueName = testResourceNamer.randomName("delivery", 10);
        client.createQueue(queueName, new CreateQueueOptions().setMaxDeliveryCount(2));
        try {
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
                ServiceBusReceiverClient receiver = builder.receiver().queueName(queueName).buildClient();
                ServiceBusReceiverClient deadLetters
                    = builder.receiver().queueName(queueName).subQueue(SubQueue.DEAD_LETTER_QUEUE).buildClient()) {
                sender.sendMessage(new ServiceBusMessage("redeliver"));
                for (int delivery = 0; delivery < 2; delivery++) {
                    final ServiceBusReceivedMessage received
                        = receiver.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                    assertNotNull(received, "Message was not delivered before max delivery count.");
                    receiver.abandon(received);
                }
                final ServiceBusReceivedMessage deadLetter
                    = deadLetters.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                assertNotNull(deadLetter, "Message was not dead-lettered after max delivery count.");
                assertEquals("MaxDeliveryCountExceeded", deadLetter.getDeadLetterReason());
                deadLetters.complete(deadLetter);
            }
        } finally {
            client.deleteQueue(queueName);
        }
    }

    @Test
    @LiveOnly
    void activeDeferredAndScheduledMessageStates() {
        final ServiceBusAdministrationClient client = getConformanceClient();
        final String queueName = testResourceNamer.randomName("states", 10);
        client.createQueue(queueName);
        try {
            final ServiceBusClientBuilder builder = getConformanceBuilder();
            try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
                ServiceBusReceiverClient receiver = builder.receiver().queueName(queueName).buildClient()) {
                sender.sendMessage(new ServiceBusMessage("active"));
                final ServiceBusReceivedMessage active
                    = receiver.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                assertNotNull(active);
                assertEquals(ServiceBusMessageState.ACTIVE, active.getState());
                receiver.defer(active);

                final ServiceBusReceivedMessage deferred = receiver.receiveDeferredMessage(active.getSequenceNumber());
                assertNotNull(deferred);
                assertEquals(ServiceBusMessageState.DEFERRED, deferred.getState());
                receiver.complete(deferred);

                final long scheduledSequence
                    = sender.scheduleMessage(new ServiceBusMessage("scheduled"), OffsetDateTime.now().plusMinutes(2));
                try {
                    final ServiceBusReceivedMessage scheduled
                        = receiver.peekMessages(1, scheduledSequence).stream().findFirst().orElse(null);
                    assertNotNull(scheduled);
                    assertEquals(scheduledSequence, scheduled.getSequenceNumber());
                    assertEquals(ServiceBusMessageState.SCHEDULED, scheduled.getState());
                } finally {
                    sender.cancelScheduledMessage(scheduledSequence);
                }
            }
        } finally {
            client.deleteQueue(queueName);
        }
    }

    @Test
    void getQueue() {
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = getEntityName(TestUtils.getQueueBaseName(), 5);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        final QueueProperties queueProperties = client.getQueue(queueName);
        assertEquals(queueName, queueProperties.getName());

        assertFalse(queueProperties.isPartitioningEnabled());
        assertFalse(queueProperties.isSessionRequired());
        assertNotNull(queueProperties.getLockDuration());

        final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(queueProperties);
        assertNotNull(runtimeProperties.getCreatedAt());
        assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
        assertNotNull(runtimeProperties.getAccessedAt());
    }

    @Test
    void getQueueDoesNotExist() {
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = getEntityName(TestUtils.getQueueBaseName(), 99);

        assertThrows(ResourceNotFoundException.class, () -> client.getQueue(queueName),
            "Queue exists! But should not. Incorrect getQueue behavior");
    }

    @Test
    void getQueueExists() {
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = getEntityName(TestUtils.getQueueBaseName(), 5);

        assertTrue(client.getQueueExists(queueName));
    }

    @Test
    void getQueueExistsFalse() {
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = getEntityName(TestUtils.getQueueBaseName(), 99);

        assertFalse(client.getQueueExists(queueName));
    }

    @Test
    void getQueueRuntimeProperties() {
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = getEntityName(TestUtils.getQueueBaseName(), 5);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        final QueueRuntimeProperties runtimeProperties = client.getQueueRuntimeProperties(queueName);
        assertEquals(queueName, runtimeProperties.getName());

        assertNotNull(runtimeProperties.getCreatedAt());
        assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
        assertNotNull(runtimeProperties.getAccessedAt());
    }

    @Test
    void getTopic() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        final TopicProperties topicProperties = client.getTopic(topicName);
        assertEquals(topicName, topicProperties.getName());

        assertTrue(topicProperties.isBatchedOperationsEnabled());
        assertFalse(topicProperties.isDuplicateDetectionRequired());
        assertNotNull(topicProperties.getDuplicateDetectionHistoryTimeWindow());
        assertNotNull(topicProperties.getDefaultMessageTimeToLive());
        assertFalse(topicProperties.isPartitioningEnabled());

        final TopicRuntimeProperties runtimeProperties = new TopicRuntimeProperties(topicProperties);
        assertNotNull(runtimeProperties.getCreatedAt());
        assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
        assertNotNull(runtimeProperties.getAccessedAt());
    }

    @Test
    void getTopicDoesNotExist() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = testResourceNamer.randomName("topic", 10);

        assertThrows(ResourceNotFoundException.class, () -> client.getTopic(topicName),
            "Topic exists! But should not. Incorrect getTopic behavior");
    }

    @Test
    void getTopicExists() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);

        assertTrue(client.getTopicExists(topicName));
    }

    @Test
    void getTopicExistsFalse() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = testResourceNamer.randomName(getTopicBaseName(), 10);

        assertFalse(client.getTopicExists(topicName));
    }

    @Test
    void getTopicRuntimeProperties() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        final TopicRuntimeProperties runtimeProperties = client.getTopicRuntimeProperties(topicName);
        assertEquals(topicName, runtimeProperties.getName());

        assertTrue(runtimeProperties.getSubscriptionCount() >= 1);

        assertNotNull(runtimeProperties.getCreatedAt());
        assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
        assertNotNull(runtimeProperties.getAccessedAt());
        assertTrue(nowUtc.isAfter(runtimeProperties.getAccessedAt()));
        assertEquals(0, runtimeProperties.getScheduledMessageCount());
    }

    @Test
    @LiveOnly
    void getTopicFilterCounts() {
        // The topic-level SqlFilterCount / CorrelationFilterCount runtime properties are served by
        // the 2024-05 service API version, so use an explicit V2024_05 client rather than relying on
        // the builder default. The test is live only because the recorded modes are pinned to
        // 2021-05 to match the existing cassettes, and no cassette covers this path yet.
        final ServiceBusAdministrationClient client = getClient(ServiceBusServiceVersion.V2024_05);
        final String topicName = testResourceNamer.randomName("topicfc", 10);
        final String subscriptionName = testResourceNamer.randomName("sub", 10);

        client.createTopic(topicName);
        try {
            client.createSubscription(topicName, subscriptionName);

            // A new subscription carries a default $Default rule (a SQL TrueFilter). Add an
            // explicit SQL rule and a correlation rule so the topic-level counts are non-zero.
            client.createRule(topicName, "sqlRule", subscriptionName,
                new CreateRuleOptions().setFilter(new SqlRuleFilter("1=1")));
            client.createRule(topicName, "correlationRule", subscriptionName,
                new CreateRuleOptions().setFilter(new CorrelationRuleFilter().setCorrelationId("abc")));

            final TopicRuntimeProperties runtimeProperties = client.getTopicRuntimeProperties(topicName);

            // $Default (TrueFilter) + sqlRule = 2 SQL filters; correlationRule = 1 correlation filter.
            assertEquals(2, runtimeProperties.getSqlFilterCount());
            assertEquals(1, runtimeProperties.getCorrelationFilterCount());
        } finally {
            client.deleteTopic(topicName);
        }
    }

    @Test
    void getSubscription() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        final SubscriptionProperties properties = client.getSubscription(topicName, subscriptionName);
        assertEquals(topicName, properties.getTopicName());
        assertEquals(subscriptionName, properties.getSubscriptionName());

        assertFalse(properties.isSessionRequired());
        assertNotNull(properties.getLockDuration());

        final SubscriptionRuntimeProperties runtimeProperties = new SubscriptionRuntimeProperties(properties);
        assertNotNull(runtimeProperties.getCreatedAt());
        assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
        assertNotNull(runtimeProperties.getAccessedAt());
    }

    @Test
    void getSubscriptionDoesNotExist() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = testResourceNamer.randomName(getSubscriptionBaseName(), 10);

        assertThrows(ResourceNotFoundException.class, () -> client.getSubscription(topicName, subscriptionName),
            "Subscription exists! But should not. Incorrect getSubscription behavior");
    }

    @Test
    void getSubscriptionExists() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();

        assertTrue(client.getSubscriptionExists(topicName, subscriptionName));
    }

    @Test
    void getSubscriptionExistsFalse() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = "subscription-session-not-exist";

        // Act
        boolean response = client.getSubscriptionExists(topicName, subscriptionName);

        // Assert
        assertFalse(response);
    }

    @Test
    void getSubscriptionRuntimeProperties() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        final SubscriptionRuntimeProperties properties
            = client.getSubscriptionRuntimeProperties(topicName, subscriptionName);
        assertEquals(topicName, properties.getTopicName());
        assertEquals(subscriptionName, properties.getSubscriptionName());

        assertTrue(properties.getTotalMessageCount() >= 0);
        assertEquals(0, properties.getActiveMessageCount());
        assertEquals(0, properties.getTransferDeadLetterMessageCount());
        assertEquals(0, properties.getTransferMessageCount());
        assertTrue(properties.getDeadLetterMessageCount() >= 0);

        assertNotNull(properties.getCreatedAt());
        assertTrue(nowUtc.isAfter(properties.getCreatedAt()));
        assertNotNull(properties.getAccessedAt());
    }

    @Test
    void getSubscriptionRuntimePropertiesUnauthorizedClient() {
        final String connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=sb://foo.servicebus.windows.net;SharedAccessKeyName=dummyKey;SharedAccessKey=dummyAccessKey"
            : TestUtils.getConnectionString(false);

        final String connectionStringUpdated
            = connectionString.replace("SharedAccessKey=", "SharedAccessKey=fake-key-");

        final ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .connectionString(connectionStringUpdated);

        // Recorded at api-version 2021-05; pin the recorded modes so requests match the recordings.
        if (!interceptorManager.isLiveMode()) {
            builder.serviceVersion(ServiceBusServiceVersion.V2021_05);
        }

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (!interceptorManager.isLiveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        final ServiceBusAdministrationClient client = builder.buildClient();

        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getEntityName(getSubscriptionBaseName(), 2);

        assertThrows(ClientAuthenticationException.class,
            () -> client.getSubscriptionRuntimeProperties(topicName, subscriptionName),
            "Subscription runtime properties accessible by unauthorized client! This should not be possible.");
    }

    @Test
    void getRule() {
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = "$Default";
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();

        final Response<RuleProperties> response
            = client.getRuleWithResponse(topicName, subscriptionName, ruleName, null);
        assertEquals(200, response.getStatusCode());

        final RuleProperties contents = response.getValue();

        assertNotNull(contents);
        assertEquals(ruleName, contents.getName());
        assertNotNull(contents.getFilter());
        assertInstanceOf(SqlRuleFilter.class, contents.getFilter());

        assertNotNull(contents.getAction());
        assertInstanceOf(EmptyRuleAction.class, contents.getAction());
    }

    @Test
    void getRuleDoesNotExist() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = "does-not-exist-rule";
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> client.getRuleWithResponse(topicName, subscriptionName, ruleName, Context.NONE));
    }

    //endregion

    //region Delete tests

    @Test
    void deleteQueue() {
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = testResourceNamer.randomName("queue", 10);

        client.createQueue(queueName);
        client.deleteQueue(queueName);
    }

    @Test
    void deleteQueueDoesNotExist() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = testResourceNamer.randomName("queue", 10);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> client.deleteQueue(queueName));
    }

    @Test
    void deleteRule() {
        final ServiceBusAdministrationClient client = getClient();
        final String ruleName = getEntityName(getRuleBaseName(), 9);
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();
        client.createRule(topicName, subscriptionName, ruleName);

        client.deleteRule(topicName, subscriptionName, ruleName);
    }

    @Test
    void deleteRuleDoesNotExist() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();
        final String ruleName = testResourceNamer.randomName("rule-", 11);
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> client.deleteRule(topicName, subscriptionName, ruleName));
    }

    @Test
    void deleteSubscription() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = testResourceNamer.randomName(getSubscriptionBaseName(), 10);

        client.createSubscription(topicName, subscriptionName);

        client.deleteSubscription(topicName, subscriptionName);
    }

    @Test
    void deleteSubscriptionDoesNotExist() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = testResourceNamer.randomName("topic", 10);
        final String subscriptionName = testResourceNamer.randomName("sub", 7);

        // The topic exists but the subscription does not.
        client.createTopic(topicName);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> client.deleteSubscription(topicName, subscriptionName));
    }

    @Test
    void deleteTopic() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = testResourceNamer.randomName("topic", 10);

        client.createTopic(topicName);
        client.deleteTopic(topicName);
    }

    @Test
    void deleteTopicDoesNotExist() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = testResourceNamer.randomName("topic", 10);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> client.deleteTopic(topicName));
    }

    //endregion

    //region List tests

    @Test
    void listQueues() {
        final ServiceBusAdministrationClient client = getClient();

        PagedIterable<QueueProperties> queueProperties = client.listQueues();
        queueProperties.forEach(queueDescription -> {
            assertNotNull(queueDescription.getName());
            assertTrue(queueDescription.getMaxDeliveryCount() > 0);
            assertSame(EntityStatus.ACTIVE, queueDescription.getStatus());
        });
        assertTrue(queueProperties.stream().findAny().isPresent());
    }

    @Test
    void listTopics() {
        final ServiceBusAdministrationClient client = getClient();

        PagedIterable<TopicProperties> topics = client.listTopics();
        topics.forEach(topicProperties -> {
            assertNotNull(topicProperties.getName());
            assertTrue(topicProperties.isBatchedOperationsEnabled());
            assertFalse(topicProperties.isPartitioningEnabled());
        });
        assertTrue(topics.stream().count() > 1);
    }

    @Test
    void listSubscriptions() {
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 2);

        PagedIterable<SubscriptionProperties> subscriptionProperties = client.listSubscriptions(topicName);
        subscriptionProperties.forEach(subscription -> {
            assertEquals(topicName, subscription.getTopicName());
            assertNotNull(subscription.getSubscriptionName());
        });
        assertTrue(subscriptionProperties.stream().findAny().isPresent());
    }

    @Test
    void listRules() {
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = "$Default";
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();

        PagedIterable<RuleProperties> ruleProperties = client.listRules(topicName, subscriptionName);

        assertTrue(ruleProperties.stream().findAny().isPresent());
        Optional<RuleProperties> ruleOptional
            = ruleProperties.stream().filter(rule1 -> rule1.getName().equals(ruleName)).findFirst();
        assertTrue(ruleOptional.isPresent());
        RuleProperties rule = ruleOptional.get();

        assertEquals(ruleName, rule.getName());
        assertNotNull(rule.getFilter());
        assertInstanceOf(SqlRuleFilter.class, rule.getFilter());
        assertNotNull(rule.getAction());
        assertInstanceOf(EmptyRuleAction.class, rule.getAction());
    }

    //endregion

    private ServiceBusAdministrationClient getConformanceClient() {
        return new ServiceBusAdministrationClientBuilder()
            .credential(TestUtils.getFullyQualifiedDomainName(false), conformanceCredential)
            .buildClient();
    }

    private ServiceBusClientBuilder getConformanceBuilder() {
        return new ServiceBusClientBuilder().credential(TestUtils.getFullyQualifiedDomainName(false),
            conformanceCredential);
    }

    private ServiceBusAdministrationClient getClient() {
        return getClient(null);
    }

    // Builds a client, optionally overriding the api-version that configure() selects. Pass a
    // version only from a @LiveOnly test: configure() pins the recorded modes to 2021-05 to match
    // the existing cassettes, and the api-version participates in playback request matching, so
    // overriding it in a recorded mode breaks playback.
    private ServiceBusAdministrationClient getClient(ServiceBusServiceVersion serviceVersion) {
        final ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        configure(builder, null, interceptorManager, credentialCached);
        if (serviceVersion != null) {
            builder.serviceVersion(serviceVersion);
        }
        return builder.buildClient();
    }
}
