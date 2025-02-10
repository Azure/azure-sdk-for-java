// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tests metadata operations with synchronous {@link EventHubClient}.
 */
@Tag(TestUtils.INTEGRATION)
@Execution(ExecutionMode.SAME_THREAD)
public class EventHubClientIntegrationTest extends IntegrationTestBase {
    private EventHubClient client;

    public EventHubClientIntegrationTest() {
        super(new ClientLogger(EventHubClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        client = createBuilder().buildClient();
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    /**
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getPartitionIds() {
        // Act
        final IterableStream<String> response = client.getPartitionIds();

        // Assert
        Assertions.assertNotNull(response);

        final List<String> partitionIds = response.stream().collect(Collectors.toList());
        Assertions.assertTrue(partitionIds.size() > 1);
    }

    /**
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getMetadata() {
        // Arrange
        final String eventHubName = TestUtils.getEventHubName();

        // Act
        final EventHubProperties properties = client.getProperties();

        // Assert
        Assertions.assertNotNull(properties);
        Assertions.assertEquals(eventHubName, properties.getName());
        Assertions.assertTrue(properties.getCreatedAt().isBefore(Instant.now()));

        Assertions.assertNotNull(properties.getPartitionIds());
        Assertions.assertTrue(properties.getPartitionIds().stream().count() > 1);
    }

    /**
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getPartitionProperties() {
        // Arrange
        final EventHubProperties properties = client.getProperties();
        final Optional<String> firstPartition = properties.getPartitionIds().stream().findFirst();

        Assertions.assertTrue(firstPartition.isPresent(), "Expect at least one partition returned.");
        final String partitionId = firstPartition.get();

        // Act
        final PartitionProperties partitionProperties = client.getPartitionProperties(partitionId);

        // Assert
        Assertions.assertNotNull(partitionProperties);

        final String eventHubName = TestUtils.getEventHubName();

        Assertions.assertEquals(eventHubName, partitionProperties.getEventHubName());
        Assertions.assertEquals(partitionId, partitionProperties.getId());
    }
}
