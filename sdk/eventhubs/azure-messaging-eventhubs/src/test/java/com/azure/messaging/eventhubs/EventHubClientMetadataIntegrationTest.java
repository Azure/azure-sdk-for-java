// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.EventHubSharedKeyCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests the metadata operations such as fetching partition properties and event hub properties.
 */
@Tag(TestUtils.INTEGRATION)
@Execution(ExecutionMode.SAME_THREAD)
public class EventHubClientMetadataIntegrationTest extends IntegrationTestBase {
    private final List<String> expectedPartitionIds;
    private EventHubAsyncClient client;
    private String eventHubName;

    public EventHubClientMetadataIntegrationTest() {
        super(new ClientLogger(EventHubClientMetadataIntegrationTest.class));

        expectedPartitionIds = IntStream.range(0, NUMBER_OF_PARTITIONS)
            .mapToObj(String::valueOf)
            .collect(Collectors.toList());
    }

    @Override
    protected void beforeTest() {
        client = createBuilder().buildAsyncClient();
        eventHubName = TestUtils.getConnectionStringProperties().getEntityPath();
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    /**
     * Verifies that we can get the metadata about an Event Hub
     */
    @Test
    public void getEventHubProperties() {
        // Act & Assert
        StepVerifier.create(client.getProperties())
            .assertNext(properties -> {
                Assertions.assertNotNull(properties);
                Assertions.assertEquals(eventHubName, properties.getName());
                Assertions.assertEquals(expectedPartitionIds.size(), properties.getPartitionIds().stream().count());
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can get the partition identifiers of an Event Hub.
     */
    @Test
    public void getPartitionIds() {
        // Act & Assert
        StepVerifier.create(client.getPartitionIds())
            .expectNextCount(expectedPartitionIds.size())
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can get partition information for each of the partitions in an Event Hub.
     */
    @Test
    public void getPartitionProperties() {
        // Act & Assert
        for (String partitionId : expectedPartitionIds) {
            StepVerifier.create(client.getPartitionProperties(partitionId))
                .assertNext(properties -> {
                    Assertions.assertEquals(eventHubName, properties.getEventHubName());
                    Assertions.assertEquals(partitionId, properties.getId());
                })
                .expectComplete()
                .verify(TIMEOUT);
        }
    }

    /**
     * Verifies that we can make multiple service calls one after the other. This is a typical user scenario when
     * consumers want to create a consumer. 1. Gets information about the Event Hub 2. Queries for partition information
     * about each partition.
     */
    @Test
    public void getPartitionPropertiesMultipleCalls() {
        // Act
        final Flux<PartitionProperties> partitionProperties = client.getPartitionIds()
            .flatMap(partitionId -> client.getPartitionProperties(partitionId));

        // Assert
        StepVerifier.create(partitionProperties)
            .assertNext(properties -> Assertions.assertEquals(eventHubName, properties.getEventHubName()))
            .assertNext(properties -> Assertions.assertEquals(eventHubName, properties.getEventHubName()))
            .assertNext(properties -> Assertions.assertEquals(eventHubName, properties.getEventHubName()))
            .assertNext(properties -> Assertions.assertEquals(eventHubName, properties.getEventHubName()))
            .assertNext(properties -> Assertions.assertEquals(eventHubName, properties.getEventHubName()))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that error conditions are handled for fetching Event Hub metadata.
     */
    @Test
    public void getPartitionPropertiesInvalidToken() {
        // Arrange
        final ConnectionStringProperties original = TestUtils.getConnectionStringProperties();
        final TokenCredential invalidTokenCredential = new EventHubSharedKeyCredential(
            original.getSharedAccessKeyName(), "invalid-sas-key-value", TIMEOUT);

        // Act & Assert
        try (EventHubAsyncClient invalidClient = createBuilder()
            .credential(original.getEndpoint().getHost(), original.getEntityPath(), invalidTokenCredential)
            .buildAsyncClient()) {
            StepVerifier.create(invalidClient.getProperties())
                .expectErrorSatisfies(error -> {
                    Assertions.assertTrue(error instanceof AmqpException);

                    AmqpException exception = (AmqpException) error;
                    Assertions.assertEquals(AmqpErrorCondition.UNAUTHORIZED_ACCESS, exception.getErrorCondition());
                    Assertions.assertFalse(exception.isTransient());
                    Assertions.assertFalse(CoreUtils.isNullOrEmpty(exception.getMessage()));
                })
                .verify(TIMEOUT);
        }
    }

    /**
     * Verifies that error conditions are handled for fetching partition metadata.
     */
    @Test
    public void getPartitionPropertiesNonExistentHub() {
        // Arrange
        final ConnectionStringProperties original = TestUtils.getConnectionStringProperties();
        final TokenCredential validCredentials = new EventHubSharedKeyCredential(
            original.getSharedAccessKeyName(), original.getSharedAccessKey(), TIMEOUT);

        // Act & Assert
        try (EventHubAsyncClient invalidClient = createBuilder()
            .credential(original.getEndpoint().getHost(), "does-not-exist", validCredentials)
            .buildAsyncClient()) {
            StepVerifier.create(invalidClient.getPartitionIds())
                .expectErrorSatisfies(error -> {
                    Assertions.assertTrue(error instanceof AmqpException);

                    AmqpException exception = (AmqpException) error;
                    Assertions.assertEquals(AmqpErrorCondition.NOT_FOUND, exception.getErrorCondition());
                    Assertions.assertFalse(exception.isTransient());
                    Assertions.assertFalse(CoreUtils.isNullOrEmpty(exception.getMessage()));
                })
                .verify(TIMEOUT);
        }
    }
}
