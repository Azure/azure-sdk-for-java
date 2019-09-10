// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.implementation.tracing.Tracer;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncConsumer;
import com.azure.messaging.eventhubs.InMemoryPartitionManager;
import com.azure.messaging.eventhubs.LogPartitionProcessor;
import com.azure.messaging.eventhubs.PartitionManager;
import com.azure.messaging.eventhubs.PartitionProcessor;
import com.azure.messaging.eventhubs.TestUtils;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionOwnership;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link PartitionBasedLoadBalancer}.
 */
public class PartitionBasedLoadBalancerTest {

    private final String eventHubName = "test-event-hub";
    private final String consumerGroupName = "test-consumer-group";

    private List<EventData> eventDataList;
    private PartitionManager partitionManager;

    @Mock
    private EventHubAsyncClient eventHubAsyncClient;

    @Mock
    private EventHubAsyncConsumer eventHubConsumer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        final Date enqueuedTime = Date.from(Instant.now());
        final byte[] contents = "Hello, world".getBytes(StandardCharsets.UTF_8);
        eventDataList = new ArrayList<>();
        IntStream.range(0, 25)
            .forEach(index -> {
                final EventData eventData = TestUtils.getEventData(contents, (long) index, (long) index, enqueuedTime);
                eventDataList.add(eventData);
            });
        this.partitionManager = new InMemoryPartitionManager();
    }

    @After
    public void teardown() {
        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void testSingleEventProcessor() {
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receive())
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> eventDataList.get(index.intValue())));

        PartitionBasedLoadBalancer partitionBasedLoadBalancer = createPartitionLoadBalancer("owner1");

        IntStream.range(0, partitionIds.size()).forEach(index -> {
            partitionBasedLoadBalancer.loadBalance();
            List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
                consumerGroupName).collectList().block();

            assertNotNull(partitionOwnership);
            assertEquals(index + 1, partitionOwnership.size());
            partitionOwnership.forEach(po -> assertEquals("owner1", partitionOwnership.get(0).ownerId()));
            assertEquals(index + 1, partitionOwnership.stream().map(po -> po.partitionId()).distinct().count());
        });
    }

    private void sleep(int secondsToSleep) {
        try {
            TimeUnit.SECONDS.sleep(secondsToSleep);
        } catch (InterruptedException ex) {

        }
    }

    @Test
    public void testTwoEventProcessors() {
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);
        when(eventHubConsumer.receive())
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> eventDataList.get(index.intValue())));

        PartitionBasedLoadBalancer partitionBasedLoadBalancer1 = createPartitionLoadBalancer("owner1");
        PartitionBasedLoadBalancer partitionBasedLoadBalancer2 = createPartitionLoadBalancer("owner2");

        IntStream.range(0, partitionIds.size()).forEach(index -> {
            partitionBasedLoadBalancer1.loadBalance();
            partitionBasedLoadBalancer2.loadBalance();
            List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
                consumerGroupName).collectList().block();
            assertTrue(partitionOwnership.size() <= 3);
            assertEquals(2, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());
        });

        List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
            consumerGroupName).collectList().block();
        // because owner1 runs first, it will have the chance to claim one additional partition
        assertEquals(2, partitionOwnership.stream().filter(po -> "owner1".equals(po.ownerId())).count());
        // after owner1 has 2 partitions and owner2 has 1 partition, owner2 runs again but this time the load
        // is balanced and owner2 should not claim any additional partition
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner2".equals(po.ownerId())).count());
    }

    @Test
    public void testPartitionStealing() {
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receive())
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> eventDataList.get(index.intValue())));

        PartitionBasedLoadBalancer partitionBasedLoadBalancer1 = createPartitionLoadBalancer("owner1");

        // First event processor claims all partitions
        IntStream.range(0, partitionIds.size()).forEach(index -> {
            partitionBasedLoadBalancer1.loadBalance();
            List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
                consumerGroupName).collectList().block();
            assertEquals(index + 1, partitionOwnership.size());
            partitionOwnership.forEach(po -> assertEquals("owner1", partitionOwnership.get(0).ownerId()));
            assertEquals(index + 1,
                partitionOwnership.stream().map(PartitionOwnership::partitionId).distinct().count());
        });

        // Now, second event processor comes online and steals a partition as the number of partitions
        // are not evenly distributed
        PartitionBasedLoadBalancer partitionBasedLoadBalancer2 = createPartitionLoadBalancer("owner2");
        partitionBasedLoadBalancer2.loadBalance();
        List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
            consumerGroupName).collectList().block();
        assertEquals(3, partitionOwnership.size());
        assertEquals(2, partitionOwnership.stream().map(PartitionOwnership::ownerId).distinct().count());
        assertEquals(2, partitionOwnership.stream().filter(po -> po.ownerId().equals("owner1")).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> po.ownerId().equals("owner2")).count());
    }

    @Test
    public void testMoreEventProcessorsThanPartitions() {
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receive())
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> eventDataList.get(index.intValue())));

        List<PartitionBasedLoadBalancer> loadBalancers = new ArrayList<>();
        IntStream.range(0, 4).forEach(index -> loadBalancers.add(createPartitionLoadBalancer("owner" + index)));

        IntStream.range(0, partitionIds.size()).forEach(index -> {
            loadBalancers.forEach(lb -> lb.loadBalance());
            List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
                consumerGroupName).collectList().block();
            assertTrue(partitionOwnership.size() <= 3);
            assertEquals(3, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());
        });

        List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
            consumerGroupName).collectList().block();

        assertEquals(3, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());

        // each should have 1 partition
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner0".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner1".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner2".equals(po.ownerId())).count());
        // owner4 should not be in the list
        assertTrue(partitionOwnership.stream().noneMatch(po -> po.ownerId().equals("owner4")));
    }

    @Test
    public void testEventProcessorInactive() throws Exception {

        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);
        when(eventHubConsumer.receive())
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> eventDataList.get(index.intValue())));
        List<PartitionBasedLoadBalancer> loadBalancers = new ArrayList<>();
        IntStream.range(0, 4).forEach(index -> loadBalancers.add(createPartitionLoadBalancer("owner" + index)));

        IntStream.range(0, partitionIds.size()).forEach(index -> {
            loadBalancers.forEach(lb -> lb.loadBalance());

            List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
                consumerGroupName).collectList().block();
            assertTrue(partitionOwnership.size() <= 3);
            assertEquals(3, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());
        });

        List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
            consumerGroupName).collectList().block();

        assertEquals(3, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());

        // each should have 1 partition
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner0".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner1".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner2".equals(po.ownerId())).count());
        // owner4 should not be in the list
        assertTrue(partitionOwnership.stream().noneMatch(po -> po.ownerId().equals("owner4")));

        sleep(6);
        IntStream.range(0, loadBalancers.size()).forEach(index -> {
            if (index != 1) {
                // run all but 2nd load balancer
                loadBalancers.get(index).loadBalance();
            }
        });

        sleep(6);
        partitionOwnership = partitionManager.listOwnership(eventHubName,
            consumerGroupName).collectList().block();

        assertEquals(3, partitionOwnership.stream().map(PartitionOwnership::ownerId).distinct().count());

        // each should have 1 partition
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner0".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner2".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner3".equals(po.ownerId())).count());
        // owner2 should not be in the list as it was stopped
        assertTrue(partitionOwnership.stream().noneMatch(po -> "owner1".equals(po.ownerId())));
    }

    @Test
    public void testReceiveFailure() throws Exception {
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Arrays.asList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        PartitionProcessor partitionProcessor = mock(PartitionProcessor.class);

        when(partitionProcessor.processEvent(any(PartitionContext.class), any(EventData.class))).thenReturn(Mono.error(new IllegalStateException()));
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);
        when(eventHubConsumer.receive()).thenReturn(Flux.error(new IllegalStateException()));

        PartitionPumpManager partitionPumpManager = new PartitionPumpManager(partitionManager,
            () -> partitionProcessor, EventPosition.earliest(),
            eventHubAsyncClient, tracerProvider);
        PartitionBasedLoadBalancer loadBalancer = new PartitionBasedLoadBalancer(partitionManager,
            eventHubAsyncClient, eventHubName, consumerGroupName, "owner", TimeUnit.SECONDS.toSeconds(5),
            partitionPumpManager);
        loadBalancer.loadBalance();
        sleep(2);
        verify(partitionProcessor, never()).processEvent(any(PartitionContext.class), any(EventData.class));
        verify(partitionProcessor, times(1)).processError(any(PartitionContext.class), any(IllegalStateException.class));
        verify(eventHubConsumer, times(1)).close();
    }

    @Test
    public void testPartitionManagerFailure() throws Exception {
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Arrays.asList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        PartitionManager partitionManager = mock(PartitionManager.class);
        when(partitionManager.listOwnership(any(), any())).thenReturn(Flux.error(new Exception("Listing failed")));
        PartitionProcessor partitionProcessor = mock(PartitionProcessor.class);
        when(partitionProcessor.processEvent(any(PartitionContext.class), any(EventData.class))).thenReturn(Mono.error(new IllegalStateException()));
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        PartitionPumpManager partitionPumpManager = new PartitionPumpManager(partitionManager,
            () -> partitionProcessor, EventPosition.earliest(),
            eventHubAsyncClient, tracerProvider);
        PartitionBasedLoadBalancer loadBalancer = new PartitionBasedLoadBalancer(partitionManager,
            eventHubAsyncClient, eventHubName, consumerGroupName, "owner", TimeUnit.SECONDS.toSeconds(5),
            partitionPumpManager);
        loadBalancer.loadBalance();
        sleep(5);
        verify(eventHubAsyncClient, atLeast(1)).getPartitionIds();
        verify(eventHubAsyncClient, never()).createConsumer(any(), any(), any());
        verify(eventHubConsumer, never()).receive();
        verify(partitionProcessor, never()).processEvent(any(PartitionContext.class), any(EventData.class));
        verify(partitionProcessor, never()).processError(any(PartitionContext.class), any(IllegalStateException.class));
        verify(eventHubConsumer, never()).close();
    }

    @Test
    public void testEventHubClientFailure() throws Exception {
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Arrays.asList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        PartitionProcessor partitionProcessor = mock(PartitionProcessor.class);
        when(partitionProcessor.processEvent(any(PartitionContext.class), any(EventData.class))).thenReturn(Mono.error(new IllegalStateException()));
        List<String> partitionIds = new ArrayList<>();
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        PartitionPumpManager partitionPumpManager = new PartitionPumpManager(partitionManager,
            () -> partitionProcessor, EventPosition.earliest(),
            eventHubAsyncClient, tracerProvider);
        PartitionBasedLoadBalancer loadBalancer = new PartitionBasedLoadBalancer(partitionManager,
            eventHubAsyncClient, eventHubName, consumerGroupName, "owner", TimeUnit.SECONDS.toSeconds(5),
            partitionPumpManager);
        loadBalancer.loadBalance();
        sleep(2);
        verify(eventHubAsyncClient, atLeast(1)).getPartitionIds();
        verify(eventHubAsyncClient, never()).createConsumer(any(), any(), any());
        verify(eventHubConsumer, never()).receive();
        verify(partitionProcessor, never()).processEvent(any(PartitionContext.class), any(EventData.class));
        verify(partitionProcessor, never()).processError(any(PartitionContext.class), any(IllegalStateException.class));
        verify(eventHubConsumer, never()).close();
    }

    @Test
    public void testEmptyOwnerId() {
        // null owner id
        PartitionOwnership claim1 = new PartitionOwnership()
            .eventHubName(eventHubName)
            .consumerGroupName(consumerGroupName)
            .partitionId("1")
            .eTag(UUID.randomUUID().toString())
            .ownerLevel(0)
            .lastModifiedTime(System.currentTimeMillis());
        // owner id is an empty string
        PartitionOwnership claim2 = new PartitionOwnership()
            .eventHubName(eventHubName)
            .consumerGroupName(consumerGroupName)
            .partitionId("2")
            .eTag(UUID.randomUUID().toString())
            .ownerLevel(0)
            .lastModifiedTime(System.currentTimeMillis())
            .ownerId("");
        partitionManager.claimOwnership(claim1, claim2).subscribe();

        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receive())
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> eventDataList.get(index.intValue())));

        PartitionBasedLoadBalancer partitionBasedLoadBalancer = createPartitionLoadBalancer("owner1");

        IntStream.range(0, partitionIds.size()).forEach(index -> {
            partitionBasedLoadBalancer.loadBalance();
        });
        List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership(eventHubName,
            consumerGroupName).collectList().block();
        assertEquals(3, partitionOwnership.size());
        partitionOwnership.forEach(po -> assertEquals("owner1", partitionOwnership.get(0).ownerId()));
        assertEquals(3, partitionOwnership.stream().map(po -> po.partitionId()).distinct().count());
    }

    private PartitionBasedLoadBalancer createPartitionLoadBalancer(String owner) {
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Arrays.asList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        PartitionPumpManager partitionPumpManager = new PartitionPumpManager(partitionManager,
            LogPartitionProcessor::new, EventPosition.earliest(), eventHubAsyncClient, tracerProvider);
        return new PartitionBasedLoadBalancer(partitionManager, eventHubAsyncClient,
            eventHubName, consumerGroupName, owner, TimeUnit.SECONDS.toSeconds(5), partitionPumpManager);
    }
}
