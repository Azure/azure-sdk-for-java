// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class EventHubClientIntegrationTest extends IntegrationTestBase {
    private EventHubClient client;

    public EventHubClientIntegrationTest() {
        super(new ClientLogger(EventHubClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        client = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .buildClient();
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
        final ConnectionStringProperties connectionProperties = getConnectionStringProperties();

        // Act
        final EventHubProperties properties = client.getProperties();

        // Assert
        Assertions.assertNotNull(properties);
        Assertions.assertEquals(connectionProperties.getEntityPath(), properties.getName());
        Assertions.assertTrue(properties.getCreatedAt().isBefore(Instant.now()));

        Assertions.assertNotNull(properties.getPartitionIds());
        Assertions.assertTrue(properties.getPartitionIds().length > 1);
    }

    /**
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getPartitionProperties() {
        // Arrange
        final ConnectionStringProperties connectionProperties = getConnectionStringProperties();
        final EventHubProperties properties = client.getProperties();
        final String partitionId = properties.getPartitionIds()[0];

        // Act
        final PartitionProperties partitionProperties = client.getPartitionProperties(partitionId);

        // Assert
        Assertions.assertNotNull(partitionProperties);

        Assertions.assertEquals(connectionProperties.getEntityPath(), partitionProperties.getEventHubName());
        Assertions.assertEquals(partitionId, partitionProperties.getId());
    }
}
