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
import com.azure.core.util.Context;
import com.azure.messaging.servicebus.TestUtils;
import com.azure.messaging.servicebus.administration.models.AccessRights;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.EmptyRuleAction;
import com.azure.messaging.servicebus.administration.models.EntityStatus;
import com.azure.messaging.servicebus.administration.models.FalseRuleFilter;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.servicebus.TestUtils.assertAuthorizationRules;
import static com.azure.messaging.servicebus.TestUtils.getEntityName;
import static com.azure.messaging.servicebus.TestUtils.getQueueBaseName;
import static com.azure.messaging.servicebus.TestUtils.getRuleBaseName;
import static com.azure.messaging.servicebus.TestUtils.getSubscriptionBaseName;
import static com.azure.messaging.servicebus.TestUtils.getTopicBaseName;
import static com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClientIntegrationTest.configure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ServiceBusAdministrationClient}.
 */
@Tag("integration")
@Execution(ExecutionMode.SAME_THREAD)
public class ServiceBusAdministrationClientIntegrationTest extends TestProxyTestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private final AtomicReference<TokenCredential> credentialCached = new AtomicReference<>();

    //region Create tests

    @Test
    void createQueue() {
        final ServiceBusAdministrationClient client = getClient();
        final String queueName = testResourceNamer.randomName("queue", 10);
        final String forwardToEntityName = getEntityName(getQueueBaseName(), 5);

        final String keyName = "test-rule";
        final List<AccessRights> accessRights = Collections.singletonList(AccessRights.SEND);
        final SharedAccessAuthorizationRule rule = interceptorManager.isPlaybackMode()
            ? new SharedAccessAuthorizationRule(keyName, "REDACTED",
            "REDACTED", accessRights)
            : new SharedAccessAuthorizationRule(keyName, accessRights);

        final CreateQueueOptions expected = new CreateQueueOptions()
            .setMaxSizeInMegabytes(1024)
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
        final CreateQueueOptions expected = new CreateQueueOptions()
            .setForwardTo(forwardToEntityName)
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
            ? new SharedAccessAuthorizationRule(keyName, "REDACTED",
            "REDACTED", accessRights)
            : new SharedAccessAuthorizationRule(keyName, accessRights);

        final CreateQueueOptions expected = new CreateQueueOptions()
            .setMaxSizeInMegabytes(1024)
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
        final CreateTopicOptions expected = new CreateTopicOptions()
            .setMaxSizeInMegabytes(2048L)
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
        final CreateTopicOptions expected = new CreateTopicOptions()
            .setMaxSizeInMegabytes(2048L)
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
        final CreateSubscriptionOptions expected = new CreateSubscriptionOptions()
            .setMaxDeliveryCount(7)
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
        final CreateRuleOptions options = new CreateRuleOptions()
            .setAction(action)
            .setFilter(new FalseRuleFilter());

        final RuleProperties actual = client.createRule(topicName, ruleName, subscriptionName, options);
        assertNotNull(actual);
        assertEquals(ruleName, actual.getName());
        assertNotNull(actual.getAction());

        assertTrue(actual.getAction() instanceof SqlRuleAction);
        assertEquals(action.getSqlExpression(), ((SqlRuleAction) actual.getAction()).getSqlExpression());

        assertNotNull(actual.getFilter());
        assertTrue(actual.getFilter() instanceof FalseRuleFilter);
    }

    @Test
    void createRuleDefaults() {
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = testResourceNamer.randomName("rule", 7);
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();

        final RuleProperties rule = client.createRule(topicName, subscriptionName, ruleName);
        assertEquals(ruleName, rule.getName());
        assertTrue(rule.getFilter() instanceof TrueRuleFilter);
        assertTrue(rule.getAction() instanceof EmptyRuleAction);
    }

    @Test
    void createRuleResponse() {
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = testResourceNamer.randomName("rule", 10);
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();

        final SqlRuleFilter filter = new SqlRuleFilter("sys.To=@MyParameter OR sys.MessageId IS NULL");
        filter.getParameters().put("@MyParameter", "My-Parameter-Value");

        final CreateRuleOptions options = new CreateRuleOptions()
            .setAction(new EmptyRuleAction())
            .setFilter(filter);

        final Response<RuleProperties> response = client.createRuleWithResponse(topicName, subscriptionName,
                                                                                ruleName, options, null);
        assertEquals(201, response.getStatusCode());

        final RuleProperties contents = response.getValue();
        assertNotNull(contents);
        assertEquals(ruleName, contents.getName());

        assertNotNull(contents.getFilter());
        assertTrue(contents.getFilter() instanceof SqlRuleFilter);

        final SqlRuleFilter actualFilter = (SqlRuleFilter) contents.getFilter();
        assertEquals(filter.getSqlExpression(), actualFilter.getSqlExpression());

        assertNotNull(contents.getAction());
        assertTrue(contents.getAction() instanceof EmptyRuleAction);
    }

    @Test
    void createQueueExistingName() {
        final String queueName = getEntityName(getQueueBaseName(), 5);
        final CreateQueueOptions options = new CreateQueueOptions();
        final ServiceBusAdministrationClient client = getClient();

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
            () -> client.createQueue(queueName, options),
            "Queue exists exception not thrown when creating a queue with existing name");
    }

    @Test
    void createSubscriptionExistingName() {
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();
        final ServiceBusAdministrationClient client = getClient();

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
            () -> client.createSubscription(topicName, subscriptionName),
            "Queue exists exception not thrown when creating a queue with existing name");
    }

    @Test
    void createSubscriptionWithForwarding() {
        // Arrange
        final ServiceBusAdministrationClient client = getClient();
        final String topicName = getEntityName(getTopicBaseName(), 3);
        final String subscriptionName = testResourceNamer.randomName("sub", 50);
        final String forwardToTopic = getEntityName(getTopicBaseName(), 4);
        final CreateSubscriptionOptions expected = new CreateSubscriptionOptions()
            .setForwardTo(forwardToTopic)
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

        assertTrue(rule.getFilter() instanceof SqlRuleFilter);
        assertEquals(expectedFilter.getSqlExpression(),
            ((SqlRuleFilter) rule.getFilter()).getSqlExpression());

        assertTrue(rule.getAction() instanceof SqlRuleAction);
        assertEquals(expectedAction.getSqlExpression(),
            ((SqlRuleAction) rule.getAction()).getSqlExpression());
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

        assertThrows(ResourceNotFoundException.class, () ->  client.getSubscription(topicName, subscriptionName),
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

        final SubscriptionRuntimeProperties properties = client.getSubscriptionRuntimeProperties(topicName, subscriptionName);
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

        final String connectionStringUpdated = connectionString.replace("SharedAccessKey=",
            "SharedAccessKey=fake-key-");

        final ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .connectionString(connectionStringUpdated);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (!interceptorManager.isLiveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        final ServiceBusAdministrationClient client = builder.buildClient();

        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getEntityName(getSubscriptionBaseName(), 2);

        ClientAuthenticationException exception = assertThrows(ClientAuthenticationException.class,
            () -> client.getSubscriptionRuntimeProperties(topicName, subscriptionName),
            "Subscription runtime properties accessible by unauthorized client! This should not be possible.");
    }

    @Test
    void getRule() {
        final ServiceBusAdministrationClient client = getClient();

        final String ruleName = "$Default";
        final String topicName = getEntityName(getTopicBaseName(), 2);
        final String subscriptionName = getSubscriptionBaseName();

        final Response<RuleProperties> response = client.getRuleWithResponse(topicName, subscriptionName, ruleName, null);
        assertEquals(200, response.getStatusCode());

        final RuleProperties contents = response.getValue();

        assertNotNull(contents);
        assertEquals(ruleName, contents.getName());
        assertNotNull(contents.getFilter());
        assertTrue(contents.getFilter() instanceof SqlRuleFilter);

        assertNotNull(contents.getAction());
        assertTrue(contents.getAction() instanceof EmptyRuleAction);
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
            assertSame(queueDescription.getStatus(), EntityStatus.ACTIVE);
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
        Optional<RuleProperties> ruleOptional = ruleProperties.stream()
            .filter(rule1 -> rule1.getName().equals(ruleName)).findFirst();
        assertTrue(ruleOptional.isPresent());
        RuleProperties rule = ruleOptional.get();

        assertEquals(ruleName, rule.getName());
        assertNotNull(rule.getFilter());
        assertTrue(rule.getFilter() instanceof SqlRuleFilter);
        assertNotNull(rule.getAction());
        assertTrue(rule.getAction() instanceof EmptyRuleAction);
    }

    //endregion

    private ServiceBusAdministrationClient getClient() {
        final ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        configure(builder, null, interceptorManager, credentialCached);
        return builder.buildClient();
    }
}
