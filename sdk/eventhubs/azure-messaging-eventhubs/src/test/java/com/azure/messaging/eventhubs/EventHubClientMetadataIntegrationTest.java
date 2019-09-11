// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ConnectionOptions;
import com.azure.messaging.eventhubs.implementation.ConnectionStringProperties;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Locale;

/**
 * Tests the metadata operations such as fetching partition properties and event hub properties.
 */
public class EventHubClientMetadataIntegrationTest extends IntegrationTestBase {
    private final String[] expectedPartitionIds = new String[]{"0", "1"};
    private EventHubAsyncClient client;
    private ReactorHandlerProvider handlerProvider;
    private String eventHubName;

    public EventHubClientMetadataIntegrationTest() {
        super(new ClientLogger(EventHubClientMetadataIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        eventHubName = getConnectionOptions().getEventHubName();
        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());

        client = new EventHubAsyncClient(getConnectionOptions(), getReactorProvider(), handlerProvider, tracerProvider);
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
        final ConnectionStringProperties invalidCredentials = getCredentials(original.getEndpoint(), original.getEventHubName(),
            original.getSharedAccessKeyName(), "invalid-sas-key-value");
        final TokenCredential badTokenProvider = new EventHubSharedAccessKeyCredential(
            invalidCredentials.getSharedAccessKeyName(), invalidCredentials.getSharedAccessKey(), TIMEOUT);
        final ConnectionOptions connectionOptions = new ConnectionOptions(original.getEndpoint().getHost(),
            original.getEventHubName(), badTokenProvider, getAuthorizationType(), TransportType.AMQP, RETRY_OPTIONS,
            ProxyConfiguration.SYSTEM_DEFAULTS, getConnectionOptions().getScheduler());
        final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());

        final EventHubAsyncClient client = new EventHubAsyncClient(connectionOptions, getReactorProvider(), handlerProvider, tracerProvider);

        // Act & Assert
        StepVerifier.create(client.getProperties())
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
    public void getPartitionPropertiesNonExistentHub() {
        // Arrange
        final ConnectionStringProperties original = getConnectionStringProperties();
        final ConnectionOptions connectionOptions = new ConnectionOptions(original.getEndpoint().getHost(),
            "invalid-event-hub", getTokenCredential(), getAuthorizationType(), TransportType.AMQP,
            RETRY_OPTIONS, ProxyConfiguration.SYSTEM_DEFAULTS, getConnectionOptions().getScheduler());
        final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());

        final EventHubAsyncClient client = new EventHubAsyncClient(connectionOptions, getReactorProvider(), handlerProvider, tracerProvider);

        // Act & Assert
        StepVerifier.create(client.getPartitionIds())
            .expectErrorSatisfies(error -> {
                Assert.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assert.assertEquals(ErrorCondition.NOT_FOUND, exception.getErrorCondition());
                Assert.assertFalse(exception.isTransient());
                Assert.assertFalse(ImplUtils.isNullOrEmpty(exception.getMessage()));
            })
            .verify();
    }

    private static ConnectionStringProperties getCredentials(URI endpoint, String eventHubName, String sasKeyName,
                                                             String sasKeyValue) {
        final String connectionString = String.format(Locale.ROOT,
            "Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s;", endpoint.toString(),
            sasKeyName, sasKeyValue, eventHubName);

        return new ConnectionStringProperties(connectionString);
    }
}
