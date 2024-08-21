// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link EventHubBufferedProducerAsyncClient}.
 */
@Isolated
@Tag(TestUtils.INTEGRATION)
@Execution(ExecutionMode.SAME_THREAD)
public class EventHubBufferedProducerAsyncClientIntegrationTest extends IntegrationTestBase {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        .withLocale(Locale.US)
        .withZone(ZoneId.of("America/Los_Angeles"));
    private EventHubBufferedProducerAsyncClient producer;
    private EventHubClient hubClient;
    private String[] partitionIds;
    private final Map<String, PartitionProperties> partitionPropertiesMap = new HashMap<>();

    public EventHubBufferedProducerAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubBufferedProducerAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        this.hubClient = toClose(createBuilder().buildClient());

        List<String> allIds = new ArrayList<>();
        final EventHubProperties properties = hubClient.getProperties();

        properties.getPartitionIds().forEach(id -> {
            allIds.add(id);

            final PartitionProperties partitionProperties = hubClient.getPartitionProperties(id);
            partitionPropertiesMap.put(id, partitionProperties);
        });

        this.partitionIds = allIds.toArray(new String[0]);

        assertFalse(partitionPropertiesMap.isEmpty(), "'partitionPropertiesMap' should have values.");
    }

    /**
     * Checks that we can publish round-robin.
     *
     * @throws InterruptedException If the semaphore cannot be awaited.
     */
    @Test
    public void publishRoundRobin() throws InterruptedException {
        // Arrange
        final CountDownLatch countDownLatch = new CountDownLatch(partitionPropertiesMap.size());
        final AtomicBoolean anyFailures = new AtomicBoolean(false);

        final Duration maxWaitTime = Duration.ofSeconds(5);
        final int queueSize = 10;
        final EventHubClientBuilder builder = createBuilder();

        producer = toClose(new EventHubBufferedProducerClientBuilder()
            .credential(builder.getFullyQualifiedNamespace(), builder.getEventHubName(), builder.getCredentials())
            .retryOptions(builder.getRetryOptions())
            .onSendBatchFailed(failed -> {
                anyFailures.set(true);
                fail("Exception occurred while sending messages." + failed.getThrowable());
            })
            .onSendBatchSucceeded(succeeded -> {
                countDownLatch.countDown();
            })
            .maxEventBufferLengthPerPartition(queueSize)
            .maxWaitTime(maxWaitTime)
            .buildAsyncClient());

        // Creating 2x number of events, we expect that each partition will get at least one of these events.
        final int numberOfEvents = partitionPropertiesMap.size() * 2;
        final List<EventData> eventsToPublish = IntStream.range(0, numberOfEvents)
            .mapToObj(index -> new EventData(String.valueOf(index)))
            .collect(Collectors.toList());

        // Waiting for at least maxWaitTime because events will get published by then.
        StepVerifier.create(producer.enqueueEvents(eventsToPublish))
            .assertNext(integer -> {
                assertEquals(0, integer, "Do not expect anymore events in queue.");
            })
            .thenAwait(maxWaitTime)
            .expectComplete()
            .verify(TIMEOUT);

        assertTrue(countDownLatch.await(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS), "Did not get enough messages.");

        // Assert
        final Map<String, PartitionProperties> propertiesAfterMap = producer.getEventHubProperties()
            .flatMapMany(properties -> {
                return Flux.fromIterable(properties.getPartitionIds())
                    .flatMap(id -> producer.getPartitionProperties(id));
            })
            .collectMap(properties -> properties.getId(), Function.identity())
            .block(TIMEOUT);

        assertNotNull(propertiesAfterMap, "'partitionPropertiesMap' should not be null");

        assertFalse(anyFailures.get(), "Should not have encountered any failures.");
        assertTrue(countDownLatch.await(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS),
            "Should have sent x batches where x is the number of partitions.");

        // Check that the offsets have increased because we have published some events.
        assertPropertiesUpdated(partitionPropertiesMap, propertiesAfterMap);
    }

    /**
     * Checks that sending an iterable with multiple partition keys is successful.
     */
    @Test
    public void publishWithPartitionKeys() throws InterruptedException {
        // Arrange
        final int numberOfEvents = partitionPropertiesMap.size() * 4;

        final AtomicBoolean anyFailures = new AtomicBoolean(false);
        final List<SendBatchSucceededContext> succeededContexts = new ArrayList<>();
        final CountDownLatch eventCountdown = new CountDownLatch(numberOfEvents);

        final Duration maxWaitTime = Duration.ofSeconds(15);
        final int queueSize = 10;

        final EventHubClientBuilder builder = createBuilder();
        producer = new EventHubBufferedProducerClientBuilder()
            .credential(builder.getFullyQualifiedNamespace(), builder.getEventHubName(), builder.getCredentials())
            .retryOptions(builder.getRetryOptions())
            .onSendBatchFailed(failed -> {
                anyFailures.set(true);
                fail("Exception occurred while sending messages." + failed.getThrowable());
            })
            .onSendBatchSucceeded(succeeded -> {
                succeededContexts.add(succeeded);
                succeeded.getEvents().forEach(e -> eventCountdown.countDown());
            })
            .maxEventBufferLengthPerPartition(queueSize)
            .maxWaitTime(maxWaitTime)
            .buildAsyncClient();

        final Random randomInterval = new Random(10);
        final Map<String, List<String>> expectedPartitionIdsMap = new HashMap<>();
        final PartitionResolver resolver = new PartitionResolver();

        final List<Mono<Integer>> publishEventMono = IntStream.range(0, numberOfEvents)
            .mapToObj(index -> {
                final String partitionKey = "partition-" + index;
                final EventData eventData = new EventData(partitionKey);
                final SendOptions sendOptions = new SendOptions().setPartitionKey(partitionKey);
                final int delay = randomInterval.nextInt(20);

                final String expectedPartitionId = resolver.assignForPartitionKey(partitionKey, partitionIds);

                expectedPartitionIdsMap.compute(expectedPartitionId, (key, existing) -> {
                    if (existing == null) {
                        List<String> events = new ArrayList<>();
                        events.add(partitionKey);
                        return events;
                    } else {
                        existing.add(partitionKey);
                        return existing;
                    }
                });

                return Mono.delay(Duration.ofSeconds(delay)).then(producer.enqueueEvent(eventData, sendOptions)
                    .doFinally(signal  -> logger.log(LogLevel.VERBOSE,
                        () -> String.format("\t[%s] %s Published event.%n", expectedPartitionId,
                            formatter.format(Instant.now())))));
            }).collect(Collectors.toList());

        // Waiting for at least maxWaitTime because events will get published by then.
        StepVerifier.create(Mono.when(publishEventMono))
            .expectComplete()
            .verify(TIMEOUT);

        final boolean await = eventCountdown.await(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

        assertFalse(anyFailures.get(), "Should not have encountered any failures.");
        assertFalse(succeededContexts.isEmpty(), "Should have successfully sent some messages.");

        for (SendBatchSucceededContext context : succeededContexts) {
            final List<String> expected = expectedPartitionIdsMap.get(context.getPartitionId());
            assertNotNull(expected, "Did not find any expected for partitionId: " + context.getPartitionId());

            context.getEvents().forEach(eventData -> {
                final boolean success = expected.removeIf(key -> key.equals(eventData.getBodyAsString()));
                assertTrue(success, "Unable to find key " + eventData.getBodyAsString()
                    + " in partition id: " + context.getEvents());
            });
        }

        expectedPartitionIdsMap.forEach((key, value) -> {
            assertTrue(value.isEmpty(), key + ": There should be no more partition keys. "
                + String.join(",", value));
        });

        final Map<String, PartitionProperties> finalProperties = getPartitionProperties();
        assertPropertiesUpdated(partitionPropertiesMap, finalProperties);
    }

    private Map<String, PartitionProperties> getPartitionProperties() {
        final EventHubProperties properties1 = this.hubClient.getProperties();
        final Map<String, PartitionProperties> result = new HashMap<>();

        properties1.getPartitionIds().forEach(id -> {
            final PartitionProperties props = hubClient.getPartitionProperties(id);
            result.put(id, props);
        });

        return result;
    }

    private static void assertPropertiesUpdated(Map<String, PartitionProperties> initial,
        Map<String, PartitionProperties> afterwards) {

        // Check that the offsets have increased because we have published some events.
        initial.forEach((key, before) -> {
            final PartitionProperties after = afterwards.get(key);

            assertNotNull(after, "did not get properties for key: " + key);
            assertEquals(before.getEventHubName(), after.getEventHubName());
            assertEquals(before.getId(), after.getId());

            assertTrue(after.getLastEnqueuedTime().isAfter(before.getLastEnqueuedTime()),
                "Last enqueued time should be newer");

            assertTrue(before.getLastEnqueuedSequenceNumber() < after.getLastEnqueuedSequenceNumber(),
                "Sequence number should be greater.");
        });
    }
}
