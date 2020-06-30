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
import com.azure.messaging.servicebus.models.SubscriptionRuntimeInfo;
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
                assertTrue(queueDescription.supportOrdering());
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
    void createQueueExistingName(HttpClient httpClient) {
        // Arrange
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-5"
            : TestUtils.getEntityName(TestUtils.getQueueBaseName(), 5);
        final QueueDescription queueDescription = new QueueDescription(queueName);
        final ServiceBusManagementAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueDescription))
            .consumeErrorWith(error -> {
                assertTrue(error instanceof ResourceExistsException);
            })
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String topicName = TestUtils.getTopicName();
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
        final String topicName = TestUtils.getTopicName();
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
        final String topicName = TestUtils.getTopicName();
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
