// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests scenarios on {@link EventHubAsyncClient}.
 */
@Tag(TestUtils.INTEGRATION)
class EventHubAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final int NUMBER_OF_EVENTS = 5;
    private static final String PARTITION_ID = "1";
    private IntegrationTestEventData testEventData;
    private static final String TEST_CONTENTS = "SSLorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vehicula posuere lobortis. Aliquam finibus volutpat dolor, faucibus pellentesque ipsum bibendum vitae. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut sit amet urna hendrerit, dapibus justo a, sodales justo. Mauris finibus augue id pulvinar congue. Nam maximus luctus ipsum, at commodo ligula euismod ac. Phasellus vitae lacus sit amet diam porta placerat. \nUt sodales efficitur sapien ut posuere. Morbi sed tellus est. Proin eu erat purus. Proin massa nunc, condimentum id iaculis dignissim, consectetur et odio. Cras suscipit sem eu libero aliquam tincidunt. Nullam ut arcu suscipit, eleifend velit in, cursus libero. Ut eleifend facilisis odio sit amet feugiat. Phasellus at nunc sit amet elit sagittis commodo ac in nisi. Fusce vitae aliquam quam. Integer vel nibh euismod, tempus elit vitae, pharetra est. Duis vulputate enim a elementum dignissim. Morbi dictum enim id elit scelerisque, in elementum nulla pharetra. \nAenean aliquet aliquet condimentum. Proin dapibus dui id libero tempus feugiat. Sed commodo ligula a lectus mattis, vitae tincidunt velit auctor. Fusce quis semper dui. Phasellus eu efficitur sem. Ut non sem sit amet enim condimentum venenatis id dictum massa. Nullam sagittis lacus a neque sodales, et ultrices arcu mattis. Aliquam erat volutpat. \nAenean fringilla quam elit, id mattis purus vestibulum nec. Praesent porta eros in dapibus molestie. Vestibulum orci libero, tincidunt et turpis eget, condimentum lobortis enim. Fusce suscipit ante et mauris consequat cursus nec laoreet lorem. Maecenas in sollicitudin diam, non tincidunt purus. Nunc mauris purus, laoreet eget interdum vitae, placerat a sapien. In mi risus, blandit eu facilisis nec, molestie suscipit leo. Pellentesque molestie urna vitae dui faucibus bibendum. \nDonec quis ipsum ultricies, imperdiet ex vel, scelerisque eros. Ut at urna arcu. Vestibulum rutrum odio dolor, vitae cursus nunc pulvinar vel. Donec accumsan sapien in malesuada tempor. Maecenas in condimentum eros. Sed vestibulum facilisis massa a iaculis. Etiam et nibh felis. Donec maximus, sem quis vestibulum gravida, turpis risus congue dolor, pharetra tincidunt lectus nisi at velit.";

    EventHubAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        final Map<String, IntegrationTestEventData> testData = getTestData();
        testEventData = testData.get(PARTITION_ID);
        Assertions.assertNotNull(testEventData, PARTITION_ID + " should have been able to get data for partition.");
    }

    /**
     * Verifies that we can receive messages, and that the receiver continues to fetch messages when the prefetch queue
     * is exhausted.
     */
    @ParameterizedTest
    @EnumSource(value = AmqpTransportType.class)
    void receiveMessage(AmqpTransportType transportType) {
        // Arrange
        final EventHubConsumerAsyncClient consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .transportType(transportType)
            .buildAsyncConsumerClient();

        final Instant lastEnqueued = testEventData.getPartitionProperties().getLastEnqueuedTime();
        final EventPosition startingPosition = EventPosition.fromEnqueuedTime(lastEnqueued);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, startingPosition)
                .take(NUMBER_OF_EVENTS))
                .expectNextCount(NUMBER_OF_EVENTS)
                .expectComplete()
                .verify();
        } finally {
            consumer.close();
        }
    }

    /**
     * Verifies that we can have multiple consumers listening to the same partition + consumer group at the same time.
     */
    @ParameterizedTest
    @EnumSource(value = AmqpTransportType.class)
    void parallelEventHubClients(AmqpTransportType transportType) throws InterruptedException {
        // Arrange
        final int numberOfClients = 3;
        final int numberOfEvents = testEventData.getEvents().size() - 2;
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfClients);
        final EventHubClientBuilder builder = createBuilder()
            .transportType(transportType)
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME);

        final EventHubConsumerAsyncClient[] clients = new EventHubConsumerAsyncClient[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            clients[i] = builder.buildAsyncConsumerClient();
        }

        final long sequenceNumber = testEventData.getPartitionProperties().getLastEnqueuedSequenceNumber();
        final EventPosition position = EventPosition.fromSequenceNumber(sequenceNumber);

        try {

            //@formatter:off
            for (final EventHubConsumerAsyncClient consumer : clients) {
                consumer.receiveFromPartition(PARTITION_ID, position)
                    .filter(partitionEvent -> isMatchingEvent(partitionEvent.getData(), testEventData.getMessageId()))
                    .take(numberOfEvents)
                    .subscribe(partitionEvent -> {
                        EventData event = partitionEvent.getData();
                        logger.info("Event[{}] matched.", event.getSequenceNumber());
                    }, error -> Assertions.fail("An error should not have occurred:" + error.toString()),
                        () -> {
                            long count = countDownLatch.getCount();
                            logger.info("Finished consuming events. Counting down: {}", count);
                            countDownLatch.countDown();
                        });
            }
            //@formatter:on

            // Assert
            // Wait for all the events we sent to be received by each of the consumers.
            Assertions.assertTrue(countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS));

            logger.info("Completed successfully.");
        } finally {
            logger.info("Disposing of subscriptions, consumers and clients.");
            dispose(clients);
        }
    }

    /**
     * Sending with credentials.
     */
    @Test
    void getPropertiesWithCredentials() {
        // Arrange

        // Act & Assert
        try (EventHubAsyncClient client = createBuilder(true)
            .buildAsyncClient()) {
            StepVerifier.create(client.getProperties())
                .consumeRecordedWith(eventHubProperties -> {
                    eventHubProperties.stream().forEach(properties -> {
                        Assertions.assertEquals(getEventHubName(), properties.getName());
                        Assertions.assertEquals(NUMBER_OF_PARTITIONS, properties.getPartitionIds().stream().count());
                    });
                })
                .expectComplete()
                .verify(TIMEOUT);
        }
    }

    @Test
    public void sendAndReceiveEventByAzureNameKeyCredential() {
        ConnectionStringProperties properties = getConnectionStringProperties();
        String fullyQualifiedNamespace = getFullyQualifiedDomainName();
        String sharedAccessKeyName = properties.getSharedAccessKeyName();
        String sharedAccessKey = properties.getSharedAccessKey();
        String eventHubName = getEventHubName();

        final EventData testData = new EventData(TEST_CONTENTS.getBytes(UTF_8));

        EventHubProducerAsyncClient asyncProducerClient = new EventHubClientBuilder()
                .credential(fullyQualifiedNamespace, eventHubName,
                        new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey))
                .buildAsyncProducerClient();
        try {
            StepVerifier.create(
                    asyncProducerClient.createBatch().flatMap(batch -> {
                        assertTrue(batch.tryAdd(testData));
                        return asyncProducerClient.send(batch);
                    })
            ).verifyComplete();
        } finally {
            asyncProducerClient.close();
        }
    }

    @Test
    public void sendAndReceiveEventByAzureSasCredential() {
        Assumptions.assumeTrue(getConnectionString(true) != null,
                "SAS was not set. Can't run test scenario.");

        ConnectionStringProperties properties = getConnectionStringProperties(true);
        String fullyQualifiedNamespace = getFullyQualifiedDomainName();
        String sharedAccessSignature = properties.getSharedAccessSignature();
        String eventHubName = getEventHubName();

        final EventData testData = new EventData(TEST_CONTENTS.getBytes(UTF_8));

        EventHubProducerAsyncClient asyncProducerClient = new EventHubClientBuilder()
                .credential(fullyQualifiedNamespace, eventHubName,
                        new AzureSasCredential(sharedAccessSignature))
                .buildAsyncProducerClient();
        try {
            StepVerifier.create(
                    asyncProducerClient.createBatch().flatMap(batch -> {
                        assertTrue(batch.tryAdd(testData));
                        return asyncProducerClient.send(batch);
                    })
            ).verifyComplete();
        } finally {
            asyncProducerClient.close();
        }
    }
}
