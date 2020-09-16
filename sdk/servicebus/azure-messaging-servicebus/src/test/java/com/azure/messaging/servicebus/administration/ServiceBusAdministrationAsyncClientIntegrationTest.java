// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.messaging.servicebus.TestUtils;
import com.azure.messaging.servicebus.administration.models.AccessRights;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.EmptyRuleAction;
import com.azure.messaging.servicebus.administration.models.NamespaceType;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SharedAccessAuthorizationRule;
import com.azure.messaging.servicebus.administration.models.SubscriptionRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.administration.models.TopicRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.messaging.servicebus.TestUtils.assertAuthorizationRules;
import static com.azure.messaging.servicebus.TestUtils.getEntityName;
import static com.azure.messaging.servicebus.TestUtils.getSessionSubscriptionBaseName;
import static com.azure.messaging.servicebus.TestUtils.getSubscriptionBaseName;
import static com.azure.messaging.servicebus.TestUtils.getTopicBaseName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ServiceBusAdministrationAsyncClient}.
 */
@Tag("integration")
class ServiceBusAdministrationAsyncClientIntegrationTest extends TestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    static Stream<Arguments> createHttpClients() {
        return Stream.of(
            Arguments.of(new NettyAsyncHttpClientBuilder().build())
        );
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("test", 10);
        final CreateQueueOptions expected = new CreateQueueOptions()
            .setMaxSizeInMegabytes(1024)
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setRequiresSession(true)
            .setRequiresDuplicateDetection(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing");

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, expected))
            .assertNext(actual -> {
                assertEquals(queueName, actual.getName());

                assertEquals(expected.getLockDuration(), actual.getLockDuration());
                assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
                assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.deadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
                assertEquals(expected.enablePartitioning(), actual.enablePartitioning());
                assertEquals(expected.requiresDuplicateDetection(), actual.requiresDuplicateDetection());
                assertEquals(expected.requiresSession(), actual.requiresSession());

                final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(actual);
                assertEquals(0, runtimeProperties.getTotalMessageCount());
                assertEquals(0, runtimeProperties.getSizeInBytes());
                assertNotNull(runtimeProperties.getCreatedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueueExistingName(HttpClient httpClient) {
        // Arrange
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-5"
            : getEntityName(TestUtils.getQueueBaseName(), 5);
        final CreateQueueOptions options = new CreateQueueOptions();
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, options))
            .expectError(ResourceExistsException.class)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueueAuthorizationRules(HttpClient httpClient) {
        // Arrange
        final String keyName = "test-rule";
        final List<AccessRights> accessRights = Collections.singletonList(AccessRights.SEND);
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("test", 10);
        final SharedAccessAuthorizationRule rule = interceptorManager.isPlaybackMode()
            ? new SharedAccessAuthorizationRule(keyName, "Uobo65ke57pwWehaL9JzGXAK30MZgErqVyn5E+rHl1c=",
            "B4ENtK9Ze1nVMQ1mGdDsy9TuuQuGC4/K8q7OnPl8mn0=", accessRights)
            : new SharedAccessAuthorizationRule(keyName, accessRights);

        final CreateQueueOptions expected = new CreateQueueOptions()
            .setMaxSizeInMegabytes(1024)
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setRequiresSession(true)
            .setRequiresDuplicateDetection(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing");

        expected.getAuthorizationRules().add(rule);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, expected))
            .assertNext(actual -> {
                assertEquals(queueName, actual.getName());

                assertEquals(expected.getLockDuration(), actual.getLockDuration());
                assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
                assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.deadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
                assertEquals(expected.enablePartitioning(), actual.enablePartitioning());
                assertEquals(expected.requiresDuplicateDetection(), actual.requiresDuplicateDetection());
                assertEquals(expected.requiresSession(), actual.requiresSession());

                final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(actual);
                assertEquals(0, runtimeProperties.getTotalMessageCount());
                assertEquals(0, runtimeProperties.getSizeInBytes());
                assertNotNull(runtimeProperties.getCreatedAt());

                assertAuthorizationRules(expected.getAuthorizationRules(), actual.getAuthorizationRules());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-0"
            : getEntityName(getTopicBaseName(), 0);
        final String subscriptionName = testResourceNamer.randomName("sub", 10);
        final CreateSubscriptionOptions expected = new CreateSubscriptionOptions()
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setUserMetadata("some-metadata-for-testing-subscriptions");

        // Act & Assert
        StepVerifier.create(client.createSubscription(topicName, subscriptionName, expected))
            .assertNext(actual -> {
                assertEquals(topicName, actual.getTopicName());
                assertEquals(subscriptionName, actual.getSubscriptionName());

                assertEquals(expected.getLockDuration(), actual.getLockDuration());
                assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.deadLetteringOnMessageExpiration(), actual.deadLetteringOnMessageExpiration());
                assertEquals(expected.requiresSession(), actual.requiresSession());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createSubscriptionExistingName(HttpClient httpClient) {
        // Arrange
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session"
            : getSessionSubscriptionBaseName();
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createSubscription(topicName, subscriptionName))
            .expectError(ResourceExistsException.class)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createTopicWithResponse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("test", 10);
        final CreateTopicOptions expected = new CreateTopicOptions()
            .setMaxSizeInMegabytes(2048L)
            .setRequiresDuplicateDetection(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing-topic");

        // Act & Assert
        StepVerifier.create(client.createTopicWithResponse(topicName, expected))
            .assertNext(response -> {
                assertEquals(201, response.getStatusCode());

                // Assert values on a topic.
                final TopicProperties actual = response.getValue();

                assertEquals(topicName, actual.getName());

                assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.enablePartitioning(), actual.enablePartitioning());
                assertEquals(expected.requiresDuplicateDetection(), actual.requiresDuplicateDetection());

                final TopicRuntimeProperties runtimeProperties = new TopicRuntimeProperties(actual);
                assertEquals(0, runtimeProperties.getSubscriptionCount());
                assertEquals(0, runtimeProperties.getSizeInBytes());
                assertNotNull(runtimeProperties.getCreatedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("sub", 10);

        client.createQueue(queueName).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteQueue(queueName))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("topic", 10);
        final String subscriptionName = testResourceNamer.randomName("sub", 7);

        client.createTopic(topicName).block(TIMEOUT);
        client.createSubscription(topicName, subscriptionName).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteSubscription(topicName, subscriptionName))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteTopic(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("topic", 10);

        client.createTopic(topicName).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteTopic(topicName))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-5"
            : getEntityName(TestUtils.getQueueBaseName(), 5);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .assertNext(queueDescription -> {
                assertEquals(queueName, queueDescription.getName());

                assertFalse(queueDescription.enablePartitioning());
                assertFalse(queueDescription.requiresSession());
                assertNotNull(queueDescription.getLockDuration());

                final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(queueDescription);
                assertNotNull(runtimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
                assertNotNull(runtimeProperties.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getNamespace(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String expectedName;
        if (interceptorManager.isPlaybackMode()) {
            expectedName = "ShivangiServiceBus";
        } else {
            final String[] split = TestUtils.getFullyQualifiedDomainName().split("\\.", 2);
            expectedName = split[0];
        }

        // Act & Assert
        StepVerifier.create(client.getNamespaceProperties())
            .assertNext(properties -> {
                assertEquals(NamespaceType.MESSAGING, properties.getNamespaceType());
                assertEquals(expectedName, properties.getName());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("exist", 10);

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .expectError(ResourceNotFoundException.class)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueExists(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-2"
            : getEntityName(TestUtils.getQueueBaseName(), 2);

        // Act & Assert
        StepVerifier.create(client.getQueueExists(queueName))
            .expectNext(true)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueExistsFalse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("exist", 10);

        // Act & Assert
        StepVerifier.create(client.getQueueExists(queueName))
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueRuntimeProperties(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-2"
            : getEntityName(TestUtils.getQueueBaseName(), 2);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeProperties(queueName))
            .assertNext(RuntimeProperties -> {
                assertEquals(queueName, RuntimeProperties.getName());

                assertNotNull(RuntimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(RuntimeProperties.getCreatedAt()));
                assertNotNull(RuntimeProperties.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getRule(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // There is a single default rule created.
        final String ruleName = "$Default";
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-13"
            : getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.getRuleWithResponse(topicName, subscriptionName, ruleName))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());

                final RuleProperties contents = response.getValue();

                assertNotNull(contents);
                assertEquals(ruleName, contents.getName());
                assertNotNull(contents.getFilter());
                assertTrue(contents.getFilter() instanceof TrueRuleFilter);

                assertNotNull(contents.getAction());
                assertTrue(contents.getAction() instanceof EmptyRuleAction);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic-1" : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session"
            : getSessionSubscriptionBaseName();
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getSubscription(topicName, subscriptionName))
            .assertNext(description -> {
                assertEquals(topicName, description.getTopicName());
                assertEquals(subscriptionName, description.getSubscriptionName());

                assertTrue(description.requiresSession());
                assertNotNull(description.getLockDuration());

                final SubscriptionRuntimeProperties runtimeProperties = new SubscriptionRuntimeProperties(description);
                assertNotNull(runtimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
                assertNotNull(runtimeProperties.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic-1" : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = "subscription-session-not-exist";

        // Act & Assert
        StepVerifier.create(client.getSubscription(topicName, subscriptionName))
            .expectError(ResourceNotFoundException.class)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionExists(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session"
            : getSessionSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.getSubscriptionExists(topicName, subscriptionName))
            .expectNext(true)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionExistsFalse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic-1" : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = "subscription-session-not-exist";

        // Act & Assert
        StepVerifier.create(client.getSubscriptionExists(topicName, subscriptionName))
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionRuntimeProperties(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic-1" : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session"
            : getSessionSubscriptionBaseName();
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getSubscriptionRuntimeProperties(topicName, subscriptionName))
            .assertNext(description -> {
                assertEquals(topicName, description.getTopicName());
                assertEquals(subscriptionName, description.getSubscriptionName());

                assertTrue(description.getTotalMessageCount() >= 0);
                assertEquals(0, description.getActiveMessageCount());
                assertEquals(0, description.getTransferDeadLetterMessageCount());
                assertEquals(0, description.getTransferMessageCount());
                assertTrue(description.getDeadLetterMessageCount() >= 0);

                assertNotNull(description.getCreatedAt());
                assertTrue(nowUtc.isAfter(description.getCreatedAt()));
                assertNotNull(description.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopic(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getTopic(topicName))
            .assertNext(topicDescription -> {
                assertEquals(topicName, topicDescription.getName());

                assertTrue(topicDescription.enableBatchedOperations());
                assertFalse(topicDescription.requiresDuplicateDetection());
                assertNotNull(topicDescription.getDuplicateDetectionHistoryTimeWindow());
                assertNotNull(topicDescription.getDefaultMessageTimeToLive());
                assertFalse(topicDescription.enablePartitioning());

                final TopicRuntimeProperties runtimeProperties = new TopicRuntimeProperties(topicDescription);
                assertNotNull(runtimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
                assertNotNull(runtimeProperties.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("exists", 10);

        // Act & Assert
        StepVerifier.create(client.getTopic(topicName))
            .consumeErrorWith(error -> {
                assertTrue(error instanceof ResourceNotFoundException);

                final ResourceNotFoundException notFoundError = (ResourceNotFoundException) error;
                final HttpResponse response = notFoundError.getResponse();

                assertNotNull(response);
                assertEquals(200, response.getStatusCode());
                StepVerifier.create(response.getBody())
                    .verifyComplete();
            })
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicExists(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);

        // Act & Assert
        StepVerifier.create(client.getTopicExists(topicName))
            .expectNext(true)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicExistsFalse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("exists", 10);

        // Act & Assert
        StepVerifier.create(client.getTopicExists(topicName))
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicRuntimeProperties(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getTopicRuntimeProperties(topicName))
            .assertNext(RuntimeProperties -> {
                assertEquals(topicName, RuntimeProperties.getName());

                if (interceptorManager.isPlaybackMode()) {
                    assertEquals(3, RuntimeProperties.getSubscriptionCount());
                } else {
                    assertTrue(RuntimeProperties.getSubscriptionCount() > 1);
                }

                assertNotNull(RuntimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(RuntimeProperties.getCreatedAt()));
                assertNotNull(RuntimeProperties.getAccessedAt());
                assertTrue(nowUtc.isAfter(RuntimeProperties.getAccessedAt()));
                assertEquals(0, RuntimeProperties.getScheduledMessageCount());

            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listRules(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // There is a single default rule created.
        final String ruleName = "$Default";
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-13"
            : getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.listRules(topicName, subscriptionName))
            .assertNext(response -> {
                assertEquals(ruleName, response.getName());
                assertNotNull(response.getFilter());
                assertTrue(response.getFilter() instanceof TrueRuleFilter);

                assertNotNull(response.getAction());
                assertTrue(response.getAction() instanceof EmptyRuleAction);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listQueues(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.listQueues())
            .assertNext(queueDescription -> {
                assertNotNull(queueDescription.getName());
                assertTrue(queueDescription.enableBatchedOperations());
                assertFalse(queueDescription.requiresDuplicateDetection());
                assertFalse(queueDescription.enablePartitioning());
            })
            .expectNextCount(9)
            .thenCancel()
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listSubscriptions(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);

        // Act & Assert
        StepVerifier.create(client.listSubscriptions(topicName))
            .assertNext(subscription -> {
                assertEquals(topicName, subscription.getTopicName());
                assertNotNull(subscription.getSubscriptionName());
            })
            .expectNextCount(1)
            .thenCancel()
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listTopics(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.listTopics())
            .assertNext(topics -> {
                assertNotNull(topics.getName());
                assertTrue(topics.enableBatchedOperations());
                assertFalse(topics.enablePartitioning());
            })
            .expectNextCount(2)
            .thenCancel()
            .verify();
    }

    private ServiceBusAdministrationAsyncClient createClient(HttpClient httpClient) {
        final String connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=sb://foo.servicebus.windows.net;SharedAccessKeyName=dummyKey;SharedAccessKey=dummyAccessKey"
            : TestUtils.getConnectionString();
        final ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .connectionString(connectionString);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isLiveMode()) {
            builder.httpClient(httpClient)
                .addPolicy(new RetryPolicy());
        } else {
            builder.httpClient(httpClient)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy());
        }

        return builder.buildAsyncClient();
    }
}
