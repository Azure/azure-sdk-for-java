// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ConnectionStringProperties;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Tests the metadata operations such as fetching partition properties and event hub properties.
 */
public class EventHubClientMetadataIntegrationTest extends IntegrationTestBase {
    private final String[] expectedPartitionIds = new String[]{"0", "1"};
    private EventHubAsyncClient client;
    private String eventHubName;

    public EventHubClientMetadataIntegrationTest() {
        super(new ClientLogger(EventHubClientMetadataIntegrationTest.class));

        eventHubName = getConnectionStringProperties().getEventHubName();
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        client = createBuilder().buildAsyncClient();
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
                Assert.assertNotNull(properties);
                Assert.assertEquals(eventHubName, properties.getName());
                Assert.assertEquals(expectedPartitionIds.length, properties.getPartitionIds().length);
            }).verifyComplete();
    }

    /**
     * Verifies that we can get the partition identifiers of an Event Hub.
     */
    @Test
    public void getPartitionIds() {
        // Act & Assert
        StepVerifier.create(client.getPartitionIds())
            .expectNextCount(expectedPartitionIds.length)
            .verifyComplete();
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
                    Assert.assertEquals(eventHubName, properties.getEventHubName());
                    Assert.assertEquals(partitionId, properties.getId());
                })
                .verifyComplete();
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
            .assertNext(properties -> Assert.assertEquals(eventHubName, properties.getEventHubName()))
            .assertNext(properties -> Assert.assertEquals(eventHubName, properties.getEventHubName()))
            .verifyComplete();
    }

    /**
     * Verifies that error conditions are handled for fetching Event Hub metadata.
     */
    @Test
    public void getPartitionPropertiesInvalidToken() throws InvalidKeyException, NoSuchAlgorithmException {
        // Arrange
        final ConnectionStringProperties original = getConnectionStringProperties();
        final TokenCredential invalidTokenCredential = new EventHubSharedAccessKeyCredential(
            original.getSharedAccessKeyName(), "invalid-sas-key-value", TIMEOUT);
        final EventHubAsyncClient invalidClient = createBuilder()
            .credential(original.getEndpoint().getHost(), original.getEventHubName(), invalidTokenCredential)
            .buildAsyncClient();

        // Act & Assert
        StepVerifier.create(invalidClient.getProperties())
            .expectErrorSatisfies(error -> {
                Assert.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assert.assertEquals(ErrorCondition.UNAUTHORIZED_ACCESS, exception.getErrorCondition());
                Assert.assertFalse(exception.isTransient());
                Assert.assertFalse(ImplUtils.isNullOrEmpty(exception.getMessage()));
            })
            .verify();
    }

    /**
     * Verifies that error conditions are handled for fetching partition metadata.
     */
    @Test
    public void getPartitionPropertiesNonExistentHub() throws InvalidKeyException, NoSuchAlgorithmException {
        // Arrange
        final ConnectionStringProperties original = getConnectionStringProperties();
        final TokenCredential validCredentials = new EventHubSharedAccessKeyCredential(
            original.getSharedAccessKeyName(), original.getSharedAccessKey(), TIMEOUT);
        final EventHubAsyncClient invalidClient = createBuilder()
            .credential(original.getEndpoint().getHost(), "does-not-exist", validCredentials)
            .buildAsyncClient();

        // Act & Assert
        StepVerifier.create(invalidClient.getPartitionIds())
            .expectErrorSatisfies(error -> {
                Assert.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assert.assertEquals(ErrorCondition.NOT_FOUND, exception.getErrorCondition());
                Assert.assertFalse(exception.isTransient());
                Assert.assertFalse(ImplUtils.isNullOrEmpty(exception.getMessage()));
            })
            .verify();
    }
}
