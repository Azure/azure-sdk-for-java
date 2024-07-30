// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PartitionBasedLoadBalancer}.
 */
public class PartitionBasedLoadBalancerTest {
    private static final MessageSerializer MESSAGE_SERIALIZER = new EventHubMessageSerializer();
    private static final ClientLogger LOGGER = new ClientLogger(PartitionBasedLoadBalancerTest.class);
    private static final String PARTITION_1 = "1";
    private static final String PARTITION_2 = "2";
    private static final String PARTITION_3 = "3";
    private static final List<String> PARTITION_IDS_3 = Arrays.asList(PARTITION_1, PARTITION_2, PARTITION_3);
    private static final List<String> PARTITION_IDS_2 = Arrays.asList(PARTITION_1, PARTITION_2);

    private static final String FQ_NAMESPACE = "fq-namespace";
    private static final String EVENT_HUB_NAME = "test-event-hub";
    private static final String CONSUMER_GROUP_NAME = "test-consumer-group";

    private static final boolean BATCH_RECEIVE_MODE = false;
    private static final PartitionContext PARTITION_CONTEXT = new PartitionContext(FQ_NAMESPACE, EVENT_HUB_NAME,
        CONSUMER_GROUP_NAME, "bazz");
    private static final EventHubsTracer DEFAULT_TRACER =
        new EventHubsTracer(null, FQ_NAMESPACE, EVENT_HUB_NAME);

    private static final String OWNER_ID_1 = "owner1";
    private static final String OWNER_ID_2 = "owner2";

    private List<EventData> eventDataList;
    private CheckpointStore checkpointStore;

    @Mock
    private EventHubClientBuilder eventHubClientBuilder;

    @Mock
    private EventHubAsyncClient eventHubAsyncClient;

    @Mock
    private EventHubConsumerAsyncClient eventHubConsumer;

    @Mock
    private PartitionProcessor partitionProcessor;

    private AutoCloseable mockCloseable;

    private List<AutoCloseable> toClose;

    private final EventProcessorClientOptions processorOptions = new EventProcessorClientOptions();

    @BeforeEach
    public void setup() {
        toClose = new ArrayList<>();
        mockCloseable = MockitoAnnotations.openMocks(this);

        final Date enqueuedTime = Date.from(Instant.now());
        final byte[] contents = "Hello, world".getBytes(StandardCharsets.UTF_8);
        eventDataList = new ArrayList<>();
        IntStream.range(0, 25)
            .forEach(index -> {
                final EventData eventData = getEventData(contents, (long) index, (long) index, enqueuedTime);
                eventDataList.add(eventData);
            });

        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(EventHubClientBuilder.DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        this.checkpointStore = new SampleCheckpointStore();
    }

    @AfterEach
    public void teardown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }

        for (final AutoCloseable closeable : toClose) {
            if (closeable == null) {
                continue;
            }

            try {
                closeable.close();
            } catch (IOException error) {
                LOGGER.log(LogLevel.VERBOSE, () -> "Error closing resource.", error);
            }
        }

        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        this.checkpointStore = null;
        Mockito.framework().clearInlineMock(this);
        Mockito.reset(eventHubClientBuilder, eventHubAsyncClient, eventHubConsumer, partitionProcessor);
    }

    @Test
    public void testSingleEventProcessor() {
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_3));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> {
                final int i = index.intValue() % eventDataList.size();

                return new PartitionEvent(PARTITION_CONTEXT, eventDataList.get(i), null);
            }));

        PartitionBasedLoadBalancer partitionBasedLoadBalancer = createPartitionLoadBalancer(OWNER_ID_1, LoadBalancingStrategy.BALANCED);

        IntStream.range(0, PARTITION_IDS_3.size()).forEach(index -> {
            partitionBasedLoadBalancer.loadBalance();

            StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                    CONSUMER_GROUP_NAME).collectList())
                .assertNext(partitionOwnership -> {
                    assertNotNull(partitionOwnership);
                    assertEquals(index + 1, partitionOwnership.size());

                    partitionOwnership.forEach(po -> assertEquals(OWNER_ID_1, partitionOwnership.get(0).getOwnerId()));
                    assertEquals(index + 1,
                        partitionOwnership.stream().map(po -> po.getPartitionId()).distinct().count());
                })
                .verifyComplete();
        });
    }

    private void sleep(int secondsToSleep) {
        try {
            TimeUnit.SECONDS.sleep(secondsToSleep);
        } catch (InterruptedException ignored) {

        }
    }

    @Test
    public void testTwoEventProcessors() {
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_3));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);
        when(eventHubConsumer.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> {
                int i = index.intValue() % eventDataList.size();
                return new PartitionEvent(PARTITION_CONTEXT, eventDataList.get(i), null);
            }));

        PartitionBasedLoadBalancer partitionBasedLoadBalancer1 = createPartitionLoadBalancer(OWNER_ID_1,
            LoadBalancingStrategy.BALANCED);
        PartitionBasedLoadBalancer partitionBasedLoadBalancer2 = createPartitionLoadBalancer(OWNER_ID_2,
            LoadBalancingStrategy.BALANCED);

        IntStream.range(0, PARTITION_IDS_3.size()).forEach(index -> {
            partitionBasedLoadBalancer1.loadBalance();
            partitionBasedLoadBalancer2.loadBalance();

            StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                    CONSUMER_GROUP_NAME).collectList())
                .assertNext(partitionOwnership -> {

                    assertNotNull(partitionOwnership, "'partitionOwnership' should not be null.");
                    assertTrue(partitionOwnership.size() <= 3);
                    assertEquals(2, partitionOwnership.stream().map(po -> po.getOwnerId()).distinct().count());
                })
                .verifyComplete();
        });

        StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME).collectList())
            .assertNext(ownership -> {
                // because owner1 runs first, it will have the chance to claim one additional partition
                assertEquals(2, ownership.stream().filter(po -> OWNER_ID_1.equals(po.getOwnerId())).count());
                // after owner1 has 2 partitions and owner2 has 1 partition, owner2 runs again but this time the load
                // is balanced and owner2 should not claim any additional partition
                assertEquals(1, ownership.stream().filter(po -> OWNER_ID_2.equals(po.getOwnerId())).count());
            })
            .verifyComplete();
    }

    @Test
    public void testPartitionStealing() {
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_3));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> {
                int i = index.intValue() % eventDataList.size();
                return new PartitionEvent(PARTITION_CONTEXT, eventDataList.get(i), null);
            }));

        PartitionBasedLoadBalancer partitionBasedLoadBalancer1 = createPartitionLoadBalancer(OWNER_ID_1,
            LoadBalancingStrategy.BALANCED);

        // First event processor claims all partitions
        IntStream.range(0, PARTITION_IDS_3.size()).forEach(index -> {
            partitionBasedLoadBalancer1.loadBalance();

            StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                    CONSUMER_GROUP_NAME).collectList())
                .assertNext(partitionOwnership -> {
                    assertEquals(index + 1, partitionOwnership.size());

                    partitionOwnership.forEach(po -> assertEquals(OWNER_ID_1,
                        partitionOwnership.get(0).getOwnerId()));

                    assertEquals(index + 1,
                        partitionOwnership.stream().map(PartitionOwnership::getPartitionId).distinct().count());
                })
                .verifyComplete();

        });

        // Now, second event processor comes online and steals a partition as the number of partitions
        // are not evenly distributed
        PartitionBasedLoadBalancer partitionBasedLoadBalancer2 = createPartitionLoadBalancer(OWNER_ID_2,
            LoadBalancingStrategy.BALANCED);
        partitionBasedLoadBalancer2.loadBalance();

        StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                CONSUMER_GROUP_NAME).collectList())
            .assertNext(partitionOwnership -> {
                assertEquals(3, partitionOwnership.size());
                assertEquals(2, partitionOwnership.stream().map(PartitionOwnership::getOwnerId).distinct().count());
                assertEquals(2, partitionOwnership.stream().filter(po -> po.getOwnerId().equals(OWNER_ID_1)).count());
                assertEquals(1, partitionOwnership.stream().filter(po -> po.getOwnerId().equals(OWNER_ID_2)).count());
            })
            .verifyComplete();
    }

    @Test
    public void testMoreEventProcessorsThanPartitions() {

        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_3));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> {
                int i = index.intValue() % eventDataList.size();
                return new PartitionEvent(PARTITION_CONTEXT, eventDataList.get(i), null);
            }));

        List<PartitionBasedLoadBalancer> loadBalancers = new ArrayList<>();
        IntStream.range(0, 4).forEach(index -> loadBalancers.add(createPartitionLoadBalancer("owner" + index, LoadBalancingStrategy.BALANCED)));

        IntStream.range(0, PARTITION_IDS_3.size()).forEach(index -> {
            loadBalancers.forEach(lb -> lb.loadBalance());
            List<PartitionOwnership> partitionOwnership = checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                CONSUMER_GROUP_NAME).collectList().block();
            assertTrue(partitionOwnership.size() <= 3);
            assertEquals(3, partitionOwnership.stream().map(po -> po.getOwnerId()).distinct().count());
        });

        StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                CONSUMER_GROUP_NAME).collectList())
            .assertNext(partitionOwnership -> {
                assertEquals(3, partitionOwnership.stream().map(po -> po.getOwnerId()).distinct().count());

                // each should have 1 partition
                assertEquals(1, partitionOwnership.stream().filter(po -> "owner0".equals(po.getOwnerId())).count());
                assertEquals(1, partitionOwnership.stream().filter(po -> OWNER_ID_1.equals(po.getOwnerId())).count());
                assertEquals(1, partitionOwnership.stream().filter(po -> OWNER_ID_2.equals(po.getOwnerId())).count());
                // owner4 should not be in the list
                assertTrue(partitionOwnership.stream().noneMatch(po -> po.getOwnerId().equals("owner4")));
            })
            .verifyComplete();
    }

    @Test
    public void testEventProcessorInactive() {
        final String ownerId0 = "owner0";
        final String ownerId4 = "owner4";

        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_3));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);
        when(eventHubConsumer.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> {
                final int i = index.intValue() % eventDataList.size();
                return new PartitionEvent(PARTITION_CONTEXT, eventDataList.get(i), null);
            }));

        List<PartitionBasedLoadBalancer> loadBalancers = new ArrayList<>();
        IntStream.range(0, 4).forEach(index -> loadBalancers.add(createPartitionLoadBalancer("owner" + index,
            LoadBalancingStrategy.BALANCED)));

        IntStream.range(0, PARTITION_IDS_3.size()).forEach(index -> {
            loadBalancers.forEach(lb -> lb.loadBalance());

            List<PartitionOwnership> partitionOwnership = checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                CONSUMER_GROUP_NAME).collectList().block();
            assertTrue(partitionOwnership.size() <= 3);
            assertEquals(3, partitionOwnership.stream().map(po -> po.getOwnerId()).distinct().count());
        });

        StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                CONSUMER_GROUP_NAME).collectList())
            .assertNext(partitionOwnership -> {

                assertEquals(3, partitionOwnership.stream().map(po -> po.getOwnerId()).distinct().count());

                // each should have 1 partition
                assertEquals(1, partitionOwnership.stream().filter(po -> ownerId0.equals(po.getOwnerId())).count());
                assertEquals(1, partitionOwnership.stream().filter(po -> OWNER_ID_1.equals(po.getOwnerId())).count());
                assertEquals(1, partitionOwnership.stream().filter(po -> OWNER_ID_2.equals(po.getOwnerId())).count());
                // owner4 should not be in the list
                assertTrue(partitionOwnership.stream().noneMatch(po -> po.getOwnerId().equals(ownerId4)));
            })
            .verifyComplete();


        sleep(10);
        IntStream.range(0, loadBalancers.size()).forEach(index -> {
            if (index != 1) {
                // run all but 2nd load balancer
                loadBalancers.get(index).loadBalance();
            }
        });

        sleep(10);
        StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                CONSUMER_GROUP_NAME).collectList())
            .assertNext(partitionOwnership -> {
                assertEquals(3, partitionOwnership.stream().map(PartitionOwnership::getOwnerId).distinct().count());

                // each should have 1 partition
                assertEquals(1, partitionOwnership.stream().filter(po -> "owner0".equals(po.getOwnerId())).count());
                assertEquals(1, partitionOwnership.stream().filter(po -> OWNER_ID_2.equals(po.getOwnerId())).count());
                assertEquals(1, partitionOwnership.stream().filter(po -> "owner3".equals(po.getOwnerId())).count());
                // owner2 should not be in the list as it was stopped
                assertTrue(partitionOwnership.stream().noneMatch(po -> OWNER_ID_1.equals(po.getOwnerId())));
            })
            .verifyComplete();
    }

    @Test
    public void testReceiveFailure() {
        doThrow(new IllegalStateException()).when(partitionProcessor).processEvent(any(EventContext.class));
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);
        when(eventHubConsumer.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.error(new IllegalStateException()));

        processorOptions.setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(BATCH_RECEIVE_MODE)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        PartitionPumpManager partitionPumpManager = new PartitionPumpManager(checkpointStore,
            () -> partitionProcessor, eventHubClientBuilder, DEFAULT_TRACER, processorOptions);

        PartitionBasedLoadBalancer loadBalancer = new PartitionBasedLoadBalancer(checkpointStore,
            eventHubAsyncClient, FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME, "owner", TimeUnit.SECONDS.toSeconds(5),
            partitionPumpManager, ec -> {
        }, LoadBalancingStrategy.BALANCED);
        toClose.add(() -> partitionPumpManager.stopAllPartitionPumps());
        loadBalancer.loadBalance();
        sleep(2);
        verify(partitionProcessor, never()).processEvent(any(EventContext.class));
        verify(partitionProcessor, times(1)).processError(any(ErrorContext.class));
        verify(eventHubConsumer, times(1)).close();
    }

    @Test
    public void testCheckpointStoreListOwnershipFailure() {
        CheckpointStore checkpointStore = mock(CheckpointStore.class);
        when(checkpointStore.listOwnership(any(), any(), any())).thenReturn(Flux.error(new Exception("Listing "
            + "failed")));
        doThrow(new IllegalStateException()).when(partitionProcessor).processEvent(any(EventContext.class));
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_2));

        processorOptions.setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(BATCH_RECEIVE_MODE)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        PartitionPumpManager partitionPumpManager = new PartitionPumpManager(checkpointStore, () -> partitionProcessor,
            eventHubClientBuilder, DEFAULT_TRACER, processorOptions);

        PartitionBasedLoadBalancer loadBalancer = new PartitionBasedLoadBalancer(checkpointStore,
            eventHubAsyncClient, FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME, "owner", TimeUnit.SECONDS.toSeconds(5),
            partitionPumpManager, ec -> {
        }, LoadBalancingStrategy.BALANCED);
        toClose.add(() -> partitionPumpManager.stopAllPartitionPumps());
        loadBalancer.loadBalance();
        sleep(5);
        verify(eventHubAsyncClient, atLeast(1)).getPartitionIds();
        verify(eventHubAsyncClient, never()).createConsumer(anyString(), anyInt(), eq(true));
        verify(eventHubConsumer, never())
            .receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class));
        verify(partitionProcessor, never()).processEvent(any(EventContext.class));
        verify(partitionProcessor, never()).processError(any(ErrorContext.class));
        verify(eventHubConsumer, never()).close();
    }

    /**
     * Adds test to ensure we are not calling user's error handler for checkpoint errors that they cannot action.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCheckpointStoreClaimOwnershipFailure() {
        final PartitionOwnership claim1 = getPartitionOwnership(PARTITION_1, OWNER_ID_1);
        final PartitionOwnership claim2 = getPartitionOwnership(PARTITION_2, OWNER_ID_1);

        final CheckpointStore mockCheckpointStore = mock(CheckpointStore.class);
        when(mockCheckpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME))
            .thenAnswer(invocation -> {
                return Flux.just(claim1, claim2);
            });
        when(mockCheckpointStore.claimOwnership(any())).thenReturn(Flux.just(claim1),
            Flux.error(new IllegalStateException("Unable to claim partition.")));
        when(mockCheckpointStore.listCheckpoints(FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME)).thenReturn(Flux.empty());

        doAnswer(invocation -> {
            return null;
        }).when(partitionProcessor).processEvent(any(EventContext.class));
        doAnswer(invocation -> {
            return null;
        }).when(partitionProcessor).processEventBatch(any(EventBatchContext.class));
        doAnswer(invocation -> {
            return null;
        }).when(partitionProcessor).processError(any(ErrorContext.class));

        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_2));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receiveFromPartition(any(), any(), any(ReceiveOptions.class)))
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> {
                int i = index.intValue() % eventDataList.size();
                return new PartitionEvent(PARTITION_CONTEXT, eventDataList.get(i), null);
            }));


        processorOptions.setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(BATCH_RECEIVE_MODE)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager partitionPumpManager = new PartitionPumpManager(mockCheckpointStore,
            () -> partitionProcessor, eventHubClientBuilder, DEFAULT_TRACER, processorOptions);

        toClose.add(() -> partitionPumpManager.stopAllPartitionPumps());
        final PartitionBasedLoadBalancer loadBalancer = new PartitionBasedLoadBalancer(mockCheckpointStore,
            eventHubAsyncClient, FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME, "owner",
            TimeUnit.SECONDS.toSeconds(5),
            partitionPumpManager, ec -> fail("Should not have called error context: " + ec),
            LoadBalancingStrategy.BALANCED);

        // Act
        loadBalancer.loadBalance();
        sleep(5);
        loadBalancer.loadBalance();
        sleep(5);

        // Assert
        verify(eventHubAsyncClient, atLeast(1)).getPartitionIds();
        verify(eventHubAsyncClient).createConsumer(anyString(), anyInt(), eq(true));

        verify(partitionProcessor, atLeastOnce()).processEvent(any(EventContext.class));
        verify(partitionProcessor, never()).processError(any(ErrorContext.class));

        verify(mockCheckpointStore, atLeastOnce()).claimOwnership(any());
    }

    @Test
    public void testEventHubClientFailure() {
        doThrow(new IllegalStateException()).when(partitionProcessor).processEvent(any(EventContext.class));
        List<String> partitionIds = new ArrayList<>();
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));

        processorOptions.setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(BATCH_RECEIVE_MODE)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        PartitionPumpManager partitionPumpManager = new PartitionPumpManager(checkpointStore,
            () -> partitionProcessor, eventHubClientBuilder, DEFAULT_TRACER, processorOptions);

        toClose.add(() -> partitionPumpManager.stopAllPartitionPumps());
        PartitionBasedLoadBalancer loadBalancer = new PartitionBasedLoadBalancer(checkpointStore,
            eventHubAsyncClient, FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME, "owner", TimeUnit.SECONDS.toSeconds(5),
            partitionPumpManager, ec -> {
        }, LoadBalancingStrategy.BALANCED);
        loadBalancer.loadBalance();
        sleep(2);
        verify(eventHubAsyncClient, atLeast(1)).getPartitionIds();
        verify(eventHubAsyncClient, never()).createConsumer(anyString(), anyInt(), eq(true));
        verify(eventHubConsumer, never())
            .receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class));
        verify(partitionProcessor, never()).processEvent(any(EventContext.class));
        verify(partitionProcessor, never()).processError(any(ErrorContext.class));
        verify(eventHubConsumer, never()).close();
    }

    @Test
    public void testEmptyOwnerId() {
        // null owner id
        PartitionOwnership claim1 = new PartitionOwnership()
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP_NAME)
            .setPartitionId("1")
            .setETag(UUID.randomUUID().toString())
            .setLastModifiedTime(System.currentTimeMillis());
        // owner id is an empty string
        PartitionOwnership claim2 = new PartitionOwnership()
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP_NAME)
            .setPartitionId("2")
            .setETag(UUID.randomUUID().toString())
            .setLastModifiedTime(System.currentTimeMillis())
            .setOwnerId("");
        checkpointStore.claimOwnership(Arrays.asList(claim1, claim2)).subscribe();

        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_3));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);
        when(eventHubConsumer.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> {
                final int i = index.intValue() % eventDataList.size();
                return new PartitionEvent(PARTITION_CONTEXT, eventDataList.get(i), null);
            }));

        String ownerName = "owner1";
        PartitionBasedLoadBalancer partitionBasedLoadBalancer = createPartitionLoadBalancer(ownerName,
            LoadBalancingStrategy.BALANCED);

        IntStream.range(0, PARTITION_IDS_3.size()).forEach(index -> {
            partitionBasedLoadBalancer.loadBalance();
        });

        final Set<String> allPartitionIds = new HashSet<>(PARTITION_IDS_3);

        // Act & Assert
        StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME))
            .assertNext(po -> {
                assertEquals(ownerName, po.getOwnerId());
                assertTrue(allPartitionIds.remove(po.getPartitionId()));
            })
            .assertNext(po -> {
                assertEquals(ownerName, po.getOwnerId());
                assertTrue(allPartitionIds.remove(po.getPartitionId()));
            })
            .assertNext(po -> {
                assertEquals(ownerName, po.getOwnerId());
                assertTrue(allPartitionIds.remove(po.getPartitionId()));
            })
            .expectComplete()
            .verify(Duration.ofSeconds(10));

        assertTrue(allPartitionIds.isEmpty(), "Expected it to claim all partitions.");
    }

    @Test
    public void testOwnershipRenewal() {
        PartitionOwnership claim1 = getPartitionOwnership(PARTITION_1, OWNER_ID_1);
        PartitionOwnership claim2 = getPartitionOwnership(PARTITION_2, OWNER_ID_1);
        checkpointStore.claimOwnership(Arrays.asList(claim1, claim2)).collectList().block();

        // Map used to compare what the eTags were updated to.
        final Map<String, String> partitionEtag = new HashMap<>();

        StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                CONSUMER_GROUP_NAME).collectList())
            .assertNext(ownershipList -> {
                assertEquals(2, ownershipList.size());

                for (PartitionOwnership ownership : ownershipList) {
                    final String id = ownership.getPartitionId();
                    final String eTag = ownership.getETag();

                    if (PARTITION_1.equals(id)) {
                        assertEquals(OWNER_ID_1, ownership.getOwnerId());
                        assertEquals(claim1.getETag(), eTag);
                    } else if (PARTITION_2.equals(id)) {
                        assertEquals(OWNER_ID_1, ownership.getOwnerId());
                        assertEquals(claim2.getETag(), eTag);
                    } else {
                        fail("Unexpected partition id: " + id);
                    }

                    assertFalse(partitionEtag.containsKey(id), "This map should be empty.");
                    partitionEtag.put(id, eTag);
                }
            })
            .verifyComplete();

        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_2));

        final PartitionPumpManager partitionPumpManager = getPartitionPumpManager();

        final Scheduler scheduler = Schedulers.newSingle("test");
        final Scheduler scheduler2 = Schedulers.newSingle("test2");
        final PartitionPump pump1 = new PartitionPump(PARTITION_1, eventHubConsumer, scheduler);
        final PartitionPump pump2 = new PartitionPump(PARTITION_2, eventHubConsumer, scheduler2);

        try {
            partitionPumpManager.getPartitionPumps().put(PARTITION_1, pump1);
            partitionPumpManager.getPartitionPumps().put(PARTITION_2, pump2);

            PartitionBasedLoadBalancer partitionBasedLoadBalancer = new PartitionBasedLoadBalancer(checkpointStore,
                eventHubAsyncClient, FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME, OWNER_ID_1,
                TimeUnit.SECONDS.toSeconds(10), partitionPumpManager,
                ec -> {
                }, LoadBalancingStrategy.BALANCED);

            partitionBasedLoadBalancer.loadBalance();

            // after first iteration, both partitions are owned by owner1, so both partitions should be renewed.
            // That is, we expect their previous eTags are not the new ones now.
            StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
                    CONSUMER_GROUP_NAME).collectList())
                .assertNext(ownershipList -> {
                    assertEquals(2, ownershipList.size());

                    for (PartitionOwnership ownership : Collections.unmodifiableList(ownershipList)) {
                        final String oldETag = partitionEtag.get(ownership.getPartitionId());
                        assertNotEquals(oldETag, ownership.getETag());

                        // Update the map values with the latest seen eTag values.
                        partitionEtag.put(ownership.getPartitionId(), ownership.getETag());
                    }
                })
                .verifyComplete();

            // Owner2 steals partition 2
            final String updatedEtag = partitionEtag.get(PARTITION_2);
            final PartitionOwnership newClaim = getPartitionOwnership(PARTITION_2, OWNER_ID_2)
                .setETag(updatedEtag);

            // There should be 1 successful claim.  claim1 contains the older eTag used when initially claiming the
            // partition.  After the load balancing cycle, the eTags have changed.
            StepVerifier.create(checkpointStore.claimOwnership(Arrays.asList(claim1, newClaim)).collectList())
                .assertNext(ownershipList -> {
                    assertEquals(1, ownershipList.size());

                    ownershipList.forEach(ownership -> {
                        partitionEtag.put(ownership.getPartitionId(), ownership.getETag());
                    });
                })
                .verifyComplete();

            // Now, this iteration of load balance on owner1 should renew only partition 1, even if partition pump manager
            // is still processing both partitions.
            partitionBasedLoadBalancer.loadBalance();
            StepVerifier.create(checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP_NAME).collectList())
                .assertNext(ownershipList -> {
                    ownershipList.forEach(ownership -> {
                        // only partition 1's etag should be updated because owner1 renewed the ownership.
                        // partition 2's etag should not be updated
                        if (ownership.getPartitionId().equals(PARTITION_2)) {
                            assertEquals(partitionEtag.get(ownership.getPartitionId()), ownership.getETag());
                            assertEquals(OWNER_ID_2, ownership.getOwnerId());
                        } else {
                            assertNotEquals(partitionEtag.get(ownership.getPartitionId()), ownership.getETag());
                            assertEquals(OWNER_ID_1, ownership.getOwnerId());
                        }
                    });
                })
                .verifyComplete();
        } finally {
            scheduler.dispose();
            scheduler2.dispose();
        }
    }

    private PartitionOwnership getPartitionOwnership(String partitionId, String ownerId) {
        return new PartitionOwnership()
            .setFullyQualifiedNamespace(FQ_NAMESPACE)
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP_NAME)
            .setPartitionId(partitionId)
            .setOwnerId(ownerId)
            .setETag(UUID.randomUUID().toString())
            .setLastModifiedTime(System.currentTimeMillis());
    }

    @Test
    public void testSingleEventProcessorWithGreedyStrategy() {
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS_3));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receiveFromPartition(any(), any(), any(ReceiveOptions.class)))
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> {
                int i = index.intValue() % eventDataList.size();
                return new PartitionEvent(PARTITION_CONTEXT, eventDataList.get(i), null);
            }));

        PartitionBasedLoadBalancer partitionBasedLoadBalancer = createPartitionLoadBalancer(OWNER_ID_1,
            LoadBalancingStrategy.GREEDY);

        // single call to load balance should own all partitions
        partitionBasedLoadBalancer.loadBalance();

        List<PartitionOwnership> partitionOwnership = checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
            CONSUMER_GROUP_NAME).collectList().block();
        assertNotNull(partitionOwnership);
        assertEquals(3, partitionOwnership.size());
        partitionOwnership.forEach(po -> assertEquals("owner1", partitionOwnership.get(0).getOwnerId()));
        assertEquals(3, partitionOwnership.stream().map(po -> po.getPartitionId()).distinct().count());
    }

    @Test
    public void testMultipleEventProcessorsWithGreedyStrategy() {
        List<String> partitionIds = Arrays.asList("1", "2", "3", "4", "5");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyInt(), eq(true))).thenReturn(eventHubConsumer);
        when(eventHubConsumer.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> {
                final int i = index.intValue() % eventDataList.size();
                return new PartitionEvent(PARTITION_CONTEXT, eventDataList.get(i), null);
            }));

        PartitionBasedLoadBalancer partitionBasedLoadBalancer1 = createPartitionLoadBalancer(OWNER_ID_1,
            LoadBalancingStrategy.GREEDY);
        PartitionBasedLoadBalancer partitionBasedLoadBalancer2 = createPartitionLoadBalancer(OWNER_ID_2,
            LoadBalancingStrategy.GREEDY);

        // one execution of load balancer for both instances should result in a 3-2 split
        partitionBasedLoadBalancer1.loadBalance();
        partitionBasedLoadBalancer2.loadBalance();
        List<PartitionOwnership> partitionOwnership = checkpointStore.listOwnership(FQ_NAMESPACE, EVENT_HUB_NAME,
            CONSUMER_GROUP_NAME).collectList().block();
        assertEquals(5, partitionOwnership.size());
        assertEquals(2, partitionOwnership.stream().map(PartitionOwnership::getOwnerId).distinct().count());
        assertTrue(partitionOwnership.stream().filter(po -> po.getOwnerId().equals(OWNER_ID_1)).count() >= 2);
        assertTrue(partitionOwnership.stream().filter(po -> po.getOwnerId().equals("owner2")).count() >= 2);
    }

    private PartitionPumpManager getPartitionPumpManager() {

        processorOptions.setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(BATCH_RECEIVE_MODE)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        PartitionPumpManager pumpManager = new PartitionPumpManager(checkpointStore,
            () -> new PartitionProcessor() {
                @Override
                public void processEvent(EventContext eventContext) {
                    LOGGER.info(
                        "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
                        eventContext.getPartitionContext().getEventHubName(),
                        eventContext.getPartitionContext().getConsumerGroup(),
                        eventContext.getPartitionContext().getPartitionId(),
                        eventContext.getEventData().getSequenceNumber());
                    eventContext.updateCheckpoint();
                }

                @Override
                public void processError(ErrorContext eventProcessingErrorContext) {
                    LOGGER.warning("Error occurred in partition processor for partition {}",
                        eventProcessingErrorContext.getPartitionContext().getPartitionId(),
                        eventProcessingErrorContext.getThrowable());
                }
            }, eventHubClientBuilder, DEFAULT_TRACER, processorOptions);


        toClose.add(() -> pumpManager.stopAllPartitionPumps());
        return pumpManager;
    }

    private PartitionBasedLoadBalancer createPartitionLoadBalancer(String owner, LoadBalancingStrategy loadBalancingStrategy) {
        PartitionPumpManager partitionPumpManager = getPartitionPumpManager();
        return new PartitionBasedLoadBalancer(checkpointStore, eventHubAsyncClient, FQ_NAMESPACE,
            EVENT_HUB_NAME, CONSUMER_GROUP_NAME, owner, TimeUnit.SECONDS.toSeconds(5), partitionPumpManager,
            ec -> {
            }, loadBalancingStrategy);
    }

    /**
     * Creates an EventData with the received properties set.
     */
    private EventData getEventData(byte[] contents, Long sequenceNumber, Long offsetNumber, Date enqueuedTime) {
        final Message message = getMessage(contents, "messageId", sequenceNumber, offsetNumber, enqueuedTime);
        return MESSAGE_SERIALIZER.deserialize(message, EventData.class);
    }
}
