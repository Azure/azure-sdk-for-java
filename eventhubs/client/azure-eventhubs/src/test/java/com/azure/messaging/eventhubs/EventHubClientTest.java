// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestMode;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ConnectionOptions;
import com.azure.messaging.eventhubs.implementation.ConnectionStringProperties;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests scenarios on {@link EventHubClient}.
 */
public class EventHubClientTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";

    private EventHubClient client;
    private EventHubConsumer consumer;
    private ExpectedData data;
    private ReactorHandlerProvider handlerProvider;

    @Rule
    public TestName testName = new TestName();

    public EventHubClientTest() {
        super(new ClientLogger(EventHubClientTest.class));
    }

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        final EventHubConsumerOptions options = new EventHubConsumerOptions().prefetchCount(2);
        consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.earliest(), options);
        data = new ExpectedData(getTestMode(), getConnectionStringProperties());
    }

    @Override
    protected void afterTest() {
        dispose(consumer, client);
    }

    @Ignore("client not closed properly")
    @Test(expected = NullPointerException.class)
    public void nullConstructor() throws NullPointerException {
        client = new EventHubClient(null, null, null);
    }

    /**
     * Verifies that we can get the metadata about an Event Hub
     */
    @Ignore("java.util.concurrent.CancellationException: Disposed")
    @Test
    public void getEventHubProperties() {
        skipIfNotRecordMode();

        // Act & Assert
        StepVerifier.create(client.getProperties())
            .assertNext(properties -> {
                Assert.assertNotNull(properties);
                Assert.assertEquals(data.getProperties().path(), properties.path());
                Assert.assertEquals(data.getProperties().partitionIds().length, properties.partitionIds().length);
            }).verifyComplete();
    }

    /**
     * Verifies that we can get the partition identifiers of an Event Hub.
     */
    @Ignore("java.util.concurrent.CancellationException: Disposed")
    @Test
    public void getPartitionIds() {
        skipIfNotRecordMode();

        // Act & Assert
        StepVerifier.create(client.getPartitionIds())
            .expectNextCount(data.properties.partitionIds().length)
            .verifyComplete();
    }

    /**
     * Verifies that we can get partition information for each of the partitions in an Event Hub.
     */
    @Ignore("client not closed properly")
    @Test
    public void getPartitionProperties() {
        skipIfNotRecordMode();

        // Act & Assert
        for (String partitionId : data.properties.partitionIds()) {
            StepVerifier.create(client.getPartitionProperties(partitionId))
                .assertNext(properties -> {
                    final PartitionProperties expected = data.getPartitionProperties(properties.id());
                    Assert.assertNotNull(expected);
                    Assert.assertEquals(expected.eventHubPath(), properties.eventHubPath());
                    Assert.assertEquals(partitionId, properties.id());
                })
                .verifyComplete();
        }
    }

    /**
     * Verifies that we can make multiple service calls one after the other. This is a typical user scenario when
     * consumers want to create a consumer. 1. Gets information about the Event Hub 2. Queries for partition information
     * about each partition.
     */
    @Ignore
    @Test
    public void getPartitionPropertiesMultipleCalls() {
        skipIfNotRecordMode();

        // Act
        final Flux<PartitionProperties> partitionProperties = client.getPartitionIds()
            .flatMap(partitionId -> client.getPartitionProperties(partitionId));

        // Assert
        StepVerifier.create(partitionProperties)
            .assertNext(properties -> {
                final PartitionProperties expected = data.getPartitionProperties(properties.id());
                Assert.assertNotNull(expected);
                Assert.assertEquals(expected.eventHubPath(), properties.eventHubPath());
            })
            .assertNext(properties -> {
                final PartitionProperties expected = data.getPartitionProperties(properties.id());
                Assert.assertNotNull(expected);
                Assert.assertEquals(expected.eventHubPath(), properties.eventHubPath());
            })
            .verifyComplete();
    }

    /**
     * Verifies that error conditions are handled for fetching Event Hub metadata.
     */
    @Ignore
    @Test
    public void getPartitionPropertiesInvalidToken() throws InvalidKeyException, NoSuchAlgorithmException {
        skipIfNotRecordMode();

        // Arrange
        final ConnectionStringProperties original = getConnectionStringProperties();
        final ConnectionStringProperties invalidCredentials = getCredentials(original.endpoint(), original.eventHubPath(),
            original.sharedAccessKeyName(), "invalid-sas-key-value");
        final TokenCredential badTokenProvider = new EventHubSharedAccessKeyCredential(
            invalidCredentials.sharedAccessKeyName(), invalidCredentials.sharedAccessKey(), Duration.ofSeconds(40));
        final ConnectionOptions connectionOptions = new ConnectionOptions(original.endpoint().getHost(),
            original.eventHubPath(), badTokenProvider, getAuthorizationType(), Duration.ofSeconds(45),
            TransportType.AMQP, Retry.getNoRetry(), ProxyConfiguration.SYSTEM_DEFAULTS, getConnectionOptions().scheduler());
        final EventHubClient client = new EventHubClient(connectionOptions, getReactorProvider(), handlerProvider);

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
    @Ignore("client not closed properly")
    @Test
    public void getPartitionPropertiesNonExistentHub() {
        skipIfNotRecordMode();

        // Arrange
        final ConnectionStringProperties original = getConnectionStringProperties();
        final ConnectionOptions connectionOptions = new ConnectionOptions(original.endpoint().getHost(),
            "invalid-event-hub", getTokenCredential(), getAuthorizationType(), Duration.ofSeconds(45),
            TransportType.AMQP, Retry.getNoRetry(), ProxyConfiguration.SYSTEM_DEFAULTS, getConnectionOptions().scheduler());
        final EventHubClient client = new EventHubClient(connectionOptions, getReactorProvider(), handlerProvider);

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

    /**
     * Verifies that we can create and send a message to an Event Hub partition.
     */
    @Ignore("java.util.concurrent.CancellationException: Disposed")
    @Test
    public void sendMessageToPartition() throws IOException {
        skipIfNotRecordMode();

        // Arrange
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID);
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer(producerOptions)) {
            StepVerifier.create(producer.send(events))
                .verifyComplete();
        }
    }

    /**
     * Verifies that we can create an {@link EventHubProducer} that does not care about partitions and lets the service
     * distribute the events.
     */
    @Ignore("java.util.concurrent.CancellationException: Disposed")
    @Test
    public void sendMessage() throws IOException {
        skipIfNotRecordMode();

        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer()) {
            StepVerifier.create(producer.send(events))
                .verifyComplete();
        }
    }

    @Ignore("can't close consumer: [main] ERROR reactor.core.publisher.Operators - Operator called default onErrorDropped")
    @Test
    public void receiveMessage() {
        skipIfNotRecordMode();

        // Arrange
        final int numberOfEvents = 10;

        // Act & Assert
        StepVerifier.create(consumer.receive().take(numberOfEvents))
            .expectNextCount(numberOfEvents)
            .verifyComplete();
    }

    /**
     * Test for multiple EventHub consumers
     */
    @Ignore
    @Test
    public void parallelEventHubClients() {
        skipIfNotRecordMode();

        final String partitionId = "0";
        final int numberOfClients = 4;
        final int numberOfEvents = 10;

        // Arrange
        final EventHubClient[] ehClients = new EventHubClient[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            ehClients[i] = new EventHubClient(getConnectionOptions(), getReactorProvider(), new ReactorHandlerProvider(getReactorProvider()));
        }

        for (final EventHubClient ehClient : ehClients) {
            // Arrange
            final EventHubConsumer consumer = ehClient.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId, EventPosition.latest());
            final Flux<EventData> events = Flux.range(0, numberOfEvents).map(number -> new EventData("testString".getBytes(UTF_8)));
            final EventHubProducer producer = ehClient.createProducer(new EventHubProducerOptions().partitionId(PARTITION_ID));

            // Act & Assert
            StepVerifier.create(consumer.receive().take(numberOfEvents))
                .then(() -> producer.send(events).block())
                .expectNextCount(numberOfEvents)
                .verifyComplete();

            dispose(producer, consumer, ehClient);
        }
    }

    private static ConnectionStringProperties getCredentials(URI endpoint, String eventHubPath, String sasKeyName, String sasKeyValue) {
        final String connectionString = String.format(Locale.ROOT,
            "Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s;", endpoint.toString(),
            sasKeyName, sasKeyValue, eventHubPath);

        return new ConnectionStringProperties(connectionString);
    }

    /**
     * Holds expected data based on the test-mode.
     */
    private static class ExpectedData {
        private final EventHubProperties properties;
        private final Map<String, PartitionProperties> partitionPropertiesMap;

        ExpectedData(TestMode testMode, ConnectionStringProperties connectionStringProperties) {
            final String eventHubPath;
            final String[] partitionIds;
            switch (testMode) {
                case PLAYBACK:
                    eventHubPath = "test-event-hub";
                    partitionIds = new String[]{"test-1", "test-2"};
                    break;
                case RECORD:
                    eventHubPath = connectionStringProperties.eventHubPath();
                    partitionIds = new String[]{"0", "1"};
                    break;
                default:
                    throw new IllegalArgumentException("Test mode not recognized.");
            }

            this.properties = new EventHubProperties(eventHubPath, Instant.EPOCH, partitionIds);
            this.partitionPropertiesMap = new HashMap<>();

            for (int i = 0; i < partitionIds.length; i++) {
                final String key = String.valueOf(i);

                this.partitionPropertiesMap.put(key, new PartitionProperties(
                    eventHubPath, key, -1, -1,
                    "lastEnqueued", Instant.now(), true));
            }
        }

        EventHubProperties getProperties() {
            return properties;
        }

        PartitionProperties getPartitionProperties(String id) {
            return partitionPropertiesMap.get(id);
        }
    }
}
