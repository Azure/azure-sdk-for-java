// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.messaging.servicebus.models.QueueDescription;
import com.azure.messaging.servicebus.models.QueueRuntimeInfo;
import com.azure.messaging.servicebus.models.SubscriptionDescription;
import com.azure.messaging.servicebus.models.SubscriptionRuntimeInfo;
import com.azure.messaging.servicebus.models.TopicDescription;
import com.azure.messaging.servicebus.models.TopicRuntimeInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ServiceBusManagementAsyncClient}.
 */
class ServiceBusManagementAsyncClientIntegrationTest extends TestBase {
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
        return TestBase.getHttpClients().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("test", 10);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());
        final QueueDescription expected = new QueueDescription(queueName)
            .setMaxSizeInMegabytes(500)
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setRequiresSession(true)
            .setRequiresDuplicateDetection(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing");

        // Act & Assert
        StepVerifier.create(client.createQueue(expected))
            .assertNext(actual -> {
                assertEquals(queueName, expected.getName());
                assertEquals(expected.getName(), actual.getName());

                assertEquals(expected.getLockDuration(), actual.getLockDuration());
                assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
                assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.deadLetteringOnMessageExpiration(), actual.deadLetteringOnMessageExpiration());
                assertEquals(expected.enablePartitioning(), actual.enablePartitioning());
                assertEquals(expected.requiresDuplicateDetection(), actual.requiresDuplicateDetection());
                assertEquals(expected.requiresSession(), actual.requiresSession());

                final QueueRuntimeInfo runtimeInfo = new QueueRuntimeInfo(actual);
                assertEquals(0, runtimeInfo.getMessageCount());
                assertEquals(0, runtimeInfo.getSizeInBytes());
                assertNotNull(runtimeInfo.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeInfo.getCreatedAt()));
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueueExistingName(HttpClient httpClient) {
        // Arrange
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-5"
            : TestUtils.getEntityName(TestUtils.getQueueBaseName(), 5);
        final QueueDescription queueDescription = new QueueDescription(queueName);
        final ServiceBusManagementAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueDescription))
            .expectError(ResourceExistsException.class)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic"
            : TestUtils.getTopicName();
        final String subscriptionName = testResourceNamer.randomName("sub", 10);
        final SubscriptionDescription expected = new SubscriptionDescription(topicName, subscriptionName)
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setUserMetadata("some-metadata-for-testing-subscriptions");

        // Act & Assert
        StepVerifier.create(client.createSubscription(expected))
            .assertNext(actual -> {
                assertEquals(topicName, expected.getTopicName());
                assertEquals(subscriptionName, expected.getSubscriptionName());

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
            ? "topic"
            : TestUtils.getTopicName();
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session-1"
            : TestUtils.getEntityName(TestUtils.getSessionSubscriptionBaseName(), 1);
        final ServiceBusManagementAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createSubscription(topicName, subscriptionName))
            .expectError(ResourceExistsException.class)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createTopicWithResponse(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("test", 10);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());
        final TopicDescription expected = new TopicDescription(topicName)
            .setMaxSizeInMegabytes(2048L)
            .setRequiresDuplicateDetection(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing-topic");

        // Act & Assert
        StepVerifier.create(client.createTopicWithResponse(expected))
            .assertNext(response -> {
                assertEquals(201, response.getStatusCode());

                // Assert values on a topic.
                final TopicDescription actual = response.getValue();

                assertEquals(topicName, expected.getName());
                assertEquals(expected.getName(), actual.getName());

                assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.enablePartitioning(), actual.enablePartitioning());
                assertEquals(expected.requiresDuplicateDetection(), actual.requiresDuplicateDetection());

                final TopicRuntimeInfo runtimeInfo = new TopicRuntimeInfo(actual);
                assertEquals(0, runtimeInfo.getSubscriptionCount());
                assertEquals(0, runtimeInfo.getSizeInBytes());
                assertNotNull(runtimeInfo.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeInfo.getCreatedAt()));
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
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
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
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
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
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
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-5"
            : TestUtils.getEntityName(TestUtils.getQueueBaseName(), 5);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .assertNext(queueDescription -> {
                assertEquals(queueName, queueDescription.getName());

                assertFalse(queueDescription.enablePartitioning());
                assertFalse(queueDescription.requiresSession());
                assertNotNull(queueDescription.getLockDuration());

                final QueueRuntimeInfo runtimeInfo = new QueueRuntimeInfo(queueDescription);
                assertNotNull(runtimeInfo.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeInfo.getCreatedAt()));
                assertNotNull(runtimeInfo.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueRuntimeInfo(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-2"
            : TestUtils.getEntityName(TestUtils.getQueueBaseName(), 2);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeInfo(queueName))
            .assertNext(runtimeInfo -> {
                assertEquals(queueName, runtimeInfo.getName());

                assertNotNull(runtimeInfo.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeInfo.getCreatedAt()));
                assertNotNull(runtimeInfo.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic" : TestUtils.getTopicName();
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session-1"
            : TestUtils.getEntityName(TestUtils.getSessionSubscriptionBaseName(), 1);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getSubscription(topicName, subscriptionName))
            .assertNext(description -> {
                assertEquals(topicName, description.getTopicName());
                assertEquals(subscriptionName, description.getSubscriptionName());

                assertTrue(description.requiresSession());
                assertNotNull(description.getLockDuration());

                final SubscriptionRuntimeInfo runtimeInfo = new SubscriptionRuntimeInfo(description);
                assertNotNull(runtimeInfo.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeInfo.getCreatedAt()));
                assertNotNull(runtimeInfo.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic" : TestUtils.getTopicName();
        final String subscriptionName = "subscription-session-not-exist";

        // Act & Assert
        StepVerifier.create(client.getSubscription(topicName, subscriptionName))
            .expectError(ResourceNotFoundException.class)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionRuntimeInfo(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic" : TestUtils.getTopicName();
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-1"
            : TestUtils.getEntityName(TestUtils.getSubscriptionBaseName(), 1);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getSubscriptionRuntimeInfo(topicName, subscriptionName))
            .assertNext(description -> {
                assertEquals(topicName, description.getTopicName());
                assertEquals(subscriptionName, description.getSubscriptionName());

                assertTrue(description.getMessageCount() >= 0);
                assertNotNull(description.getDetails());
                assertNotNull(description.getDetails().getActiveMessageCount());
                assertNotNull(description.getDetails().getScheduledMessageCount());
                assertNotNull(description.getDetails().getTransferDeadLetterMessageCount());
                assertNotNull(description.getDetails().getTransferMessageCount());
                assertNotNull(description.getDetails().getDeadLetterMessageCount());

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
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic"
            : TestUtils.getTopicName();
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

                final TopicRuntimeInfo runtimeInfo = new TopicRuntimeInfo(topicDescription);
                assertNotNull(runtimeInfo.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeInfo.getCreatedAt()));
                assertNotNull(runtimeInfo.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicRuntimeInfo(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic"
            : TestUtils.getTopicName();
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getTopicRuntimeInfo(topicName))
            .assertNext(runtimeInfo -> {
                assertEquals(topicName, runtimeInfo.getName());

                if (interceptorManager.isPlaybackMode()) {
                    assertEquals(22, runtimeInfo.getSubscriptionCount());
                } else {
                    assertTrue(runtimeInfo.getSubscriptionCount() > 1);
                }

                assertNotNull(runtimeInfo.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeInfo.getCreatedAt()));
                assertNotNull(runtimeInfo.getAccessedAt());
                assertTrue(nowUtc.isAfter(runtimeInfo.getAccessedAt()));
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listQueues(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);

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
    void listTopics(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);

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

    private ServiceBusManagementAsyncClient createClient(HttpClient httpClient) {
        final String connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=sb://foo.servicebus.windows.net;SharedAccessKeyName=dummyKey;SharedAccessKey=dummyAccessKey"
            : TestUtils.getConnectionString();
        final ServiceBusManagementClientBuilder builder = new ServiceBusManagementClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .connectionString(connectionString);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.httpClient(httpClient)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy());
        }

        return builder.buildAsyncClient();
    }
}
