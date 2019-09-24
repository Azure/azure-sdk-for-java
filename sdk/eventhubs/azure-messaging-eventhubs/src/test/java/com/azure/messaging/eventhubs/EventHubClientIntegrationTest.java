// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class EventHubClientIntegrationTest extends IntegrationTestBase {
    private EventHubClient client;

    @Rule
    public TestName testName = new TestName();

    public EventHubClientIntegrationTest() {
        super(new ClientLogger(EventHubClientIntegrationTest.class));
    }

    @Override
    protected String getTestName() {
        return testName.getMethodName();
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
        Assert.assertNotNull(response);

        final List<String> partitionIds = response.stream().collect(Collectors.toList());
        Assert.assertTrue(partitionIds.size() > 1);
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
        Assert.assertNotNull(properties);
        Assert.assertEquals(connectionProperties.getEntityPath(), properties.getName());
        Assert.assertTrue(properties.getCreatedAt().isBefore(Instant.now()));

        Assert.assertNotNull(properties.getPartitionIds());
        Assert.assertTrue(properties.getPartitionIds().length > 1);
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
        Assert.assertNotNull(partitionProperties);

        Assert.assertEquals(connectionProperties.getEntityPath(), partitionProperties.getEventHubName());
        Assert.assertEquals(partitionId, partitionProperties.getId());
    }
}
