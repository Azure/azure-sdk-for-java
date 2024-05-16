// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventBatchContextTest {
    private final PartitionContext partitionContext = new PartitionContext("TEST_NAMESPACE",
        "TEST_EVENT_HUB", "TEST_DEFAULT_GROUP", "TEST_TEST_ID");
    private final LastEnqueuedEventProperties lastEnqueuedEventProperties = new LastEnqueuedEventProperties(1035L,
        100L, Instant.ofEpochSecond(1608315301L), Instant.ofEpochSecond(1609315301L));
    private final List<EventData> events = new ArrayList<>();

    @Mock
    private CheckpointStore checkpointStore;
    @Mock
    private EventData eventData1;
    @Mock
    private EventData eventData2;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Asserts non-nullable fields.
     */
    @Test
    void constructorNull() {
        assertThrows(NullPointerException.class, () ->
            new EventBatchContext(null, events, checkpointStore, lastEnqueuedEventProperties));
        assertThrows(NullPointerException.class, () ->
            new EventBatchContext(partitionContext, null, checkpointStore, lastEnqueuedEventProperties));
        assertThrows(NullPointerException.class, () ->
            new EventBatchContext(partitionContext, events, null, lastEnqueuedEventProperties));
    }

    /**
     * Asserts properties on the constructor.
     */
    @Test
    void properties() {
        // Arrange
        events.add(eventData1);
        events.add(eventData2);

        final EventBatchContext context = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);

        // Assert
        assertEquals(partitionContext, context.getPartitionContext());
        assertEquals(events, context.getEvents());
        assertEquals(lastEnqueuedEventProperties, context.getLastEnqueuedEventProperties());
    }

    @Test
    void updateCheckpointAsync() {
        // Arrange
        when(checkpointStore.updateCheckpoint(any(Checkpoint.class)))
            .thenReturn(Mono.empty());

        events.add(eventData1);
        events.add(eventData2);

        final Long sequenceNumber = 10L;
        final Long offset = 15L;
        when(eventData2.getSequenceNumber()).thenReturn(sequenceNumber);
        when(eventData2.getOffset()).thenReturn(offset);

        final EventBatchContext context = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);

        // Act
        StepVerifier.create(context.updateCheckpointAsync())
            .verifyComplete();

        // Assert
        verify(checkpointStore).updateCheckpoint(
            argThat(arg -> partitionContext.getEventHubName().equals(arg.getEventHubName())
                && sequenceNumber.equals(arg.getSequenceNumber())
                && offset.equals(arg.getOffset())));
    }
}
