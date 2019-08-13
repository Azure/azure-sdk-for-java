// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumer;
import com.azure.messaging.eventhubs.InMemoryPartitionManager;
import com.azure.messaging.eventhubs.LogPartitionProcessor;
import com.azure.messaging.eventhubs.PartitionManager;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;

public class PartitionLoadBalancerStrategyTest {

    @Mock
    private EventHubAsyncClient eventHubAsyncClient;

    @Mock
    private EventHubConsumer eventHubConsumer;

    private List<EventData> eventDataList;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        eventDataList = new ArrayList<>();
        IntStream.range(0, 25)
            .forEach(index -> {
                EventData eventData = Mockito.mock(EventData.class);
                when(eventData.sequenceNumber()).thenReturn((long) index);
                when(eventData.offset()).thenReturn(String.valueOf(index));
                eventDataList.add(eventData);
            });
    }

    @Test
    public void testSingleEventProcessor() {
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receive())
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> eventDataList.get(index.intValue())));

        PartitionManager partitionManager = new InMemoryPartitionManager();
        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner1",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        IntStream.range(0, partitionIds.size()).forEach(index -> {
            partitionLoadBalancerStrategy.runOnce();
            List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership("",
                EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME).collectList().block();
            assertEquals(index + 1, partitionOwnership.size());
            partitionOwnership.forEach(po -> assertEquals("owner1", partitionOwnership.get(0).ownerId()));
            assertEquals(index + 1, partitionOwnership.stream().map(po -> po.partitionId()).distinct().count());
        });
    }

    @Test
    public void testTwoEventProcessors() {
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receive())
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> eventDataList.get(index.intValue())));

        PartitionManager partitionManager = new InMemoryPartitionManager();

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy1 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner1",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy2 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner2",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        IntStream.range(0, partitionIds.size()).forEach(index -> {
            partitionLoadBalancerStrategy1.runOnce();
            partitionLoadBalancerStrategy2.runOnce();
            List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership("",
                EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME).collectList().block();
            assertTrue(partitionOwnership.size() <= 3);
            assertEquals(2, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());
        });

        List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership("",
            EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME).collectList().block();
        // because owner1 runs first, it will have the chance to claim one additional partition
        assertEquals(2, partitionOwnership.stream().filter(po -> "owner1".equals(po.ownerId())).count());
        // after owner1 has 2 partitions and owner2 has 1 partition, owner2 runs again but this time the load
        // is balanced and owner2 should not claim any additional partition
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner2".equals(po.ownerId())).count());
    }

    @Test
    public void testMoreEventProcessorsThanPartitions() {
        List<String> partitionIds = Arrays.asList("1", "2", "3");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(partitionIds));
        when(eventHubAsyncClient.createConsumer(anyString(), anyString(), any(EventPosition.class), any(
            EventHubConsumerOptions.class))).thenReturn(eventHubConsumer);

        when(eventHubConsumer.receive())
            .thenReturn(Flux.interval(Duration.ofSeconds(1)).map(index -> eventDataList.get(index.intValue())));

        PartitionManager partitionManager = new InMemoryPartitionManager();

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy1 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner1",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy2 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner2",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy3 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner3",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy4 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner4",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        IntStream.range(0, partitionIds.size()).forEach(index -> {
            partitionLoadBalancerStrategy1.runOnce();
            partitionLoadBalancerStrategy2.runOnce();
            partitionLoadBalancerStrategy3.runOnce();
            partitionLoadBalancerStrategy4.runOnce();

            List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership("",
                EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME).collectList().block();
            assertTrue(partitionOwnership.size() <= 3);
            assertEquals(3, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());
        });

        List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership("",
            EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME).collectList().block();

        assertEquals(3, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());

        // each should have 1 partition
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner1".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner2".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner3".equals(po.ownerId())).count());
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

        PartitionManager partitionManager = new InMemoryPartitionManager();

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy1 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner1",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy2 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner2",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy3 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner3",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        PartitionLoadBalancerStrategy partitionLoadBalancerStrategy4 = new PartitionLoadBalancerStrategy(
            partitionManager, eventHubAsyncClient,
            "", EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, "owner4",
            (LogPartitionProcessor::new), EventPosition.earliest(), TimeUnit.SECONDS.toSeconds(5));

        IntStream.range(0, partitionIds.size()).forEach(index -> {
            partitionLoadBalancerStrategy1.runOnce();
            partitionLoadBalancerStrategy2.runOnce();
            partitionLoadBalancerStrategy3.runOnce();
            partitionLoadBalancerStrategy4.runOnce();

            List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership("",
                EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME).collectList().block();
            assertTrue(partitionOwnership.size() <= 3);
            assertEquals(3, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());
        });

        List<PartitionOwnership> partitionOwnership = partitionManager.listOwnership("",
            EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME).collectList().block();

        assertEquals(3, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());

        // each should have 1 partition
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner1".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner2".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner3".equals(po.ownerId())).count());
        // owner4 should not be in the list
        assertTrue(partitionOwnership.stream().noneMatch(po -> po.ownerId().equals("owner4")));

        // Stop event processor 2
        partitionLoadBalancerStrategy2.stopAllPartitionPumps();

        TimeUnit.SECONDS.sleep(6);
        IntStream.range(0, partitionIds.size()).forEach(index -> {
            partitionLoadBalancerStrategy1.runOnce();
            partitionLoadBalancerStrategy3.runOnce();
            partitionLoadBalancerStrategy4.runOnce();
        });

        TimeUnit.SECONDS.sleep(3);
        partitionOwnership = partitionManager.listOwnership("",
            EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME).collectList().block();

        assertEquals(3, partitionOwnership.stream().map(po -> po.ownerId()).distinct().count());

        // each should have 1 partition
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner1".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner3".equals(po.ownerId())).count());
        assertEquals(1, partitionOwnership.stream().filter(po -> "owner4".equals(po.ownerId())).count());
        // owner2 should not be in the list as it was stopped
        assertTrue(partitionOwnership.stream().noneMatch(po -> po.ownerId().equals("owner2")));
    }
}
