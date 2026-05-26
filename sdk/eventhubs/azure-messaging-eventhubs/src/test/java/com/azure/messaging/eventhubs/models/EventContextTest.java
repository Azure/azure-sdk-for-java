// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link EventContext}.
 */
class EventContextTest {
    private final PartitionContext partitionContext
        = new PartitionContext("TEST_NAMESPACE", "TEST_EVENT_HUB", "TEST_DEFAULT_GROUP", "TEST_PARTITION_ID");

    @Mock
    private CheckpointStore checkpointStore;
    @Mock
    private EventData eventData;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Verifies that updateCheckpointAsync sets offsetString on the checkpoint.
     * Regression test for https://github.com/Azure/azure-sdk-for-java/issues/46752
     * where only setOffset(Long) was called, causing BlobCheckpointStore to store
     * null offset because it reads getOffsetString().
     */
    @Test
    void updateCheckpointAsyncSetsOffsetString() {
        // Arrange
        final Long sequenceNumber = 10L;
        final Long offset = 15L;
        final String offsetString = "15";

        when(eventData.getSequenceNumber()).thenReturn(sequenceNumber);
        when(eventData.getOffset()).thenReturn(offset);
        when(eventData.getOffsetString()).thenReturn(offsetString);
        when(checkpointStore.updateCheckpoint(any(Checkpoint.class))).thenReturn(Mono.empty());

        final EventContext context = new EventContext(partitionContext, eventData, checkpointStore, null);

        // Act
        StepVerifier.create(context.updateCheckpointAsync()).verifyComplete();

        // Assert - offsetString must be set on the checkpoint passed to the store
        ArgumentCaptor<Checkpoint> captor = ArgumentCaptor.forClass(Checkpoint.class);
        verify(checkpointStore).updateCheckpoint(captor.capture());

        Checkpoint captured = captor.getValue();
        assertEquals(partitionContext.getFullyQualifiedNamespace(), captured.getFullyQualifiedNamespace());
        assertEquals(partitionContext.getEventHubName(), captured.getEventHubName());
        assertEquals(partitionContext.getConsumerGroup(), captured.getConsumerGroup());
        assertEquals(partitionContext.getPartitionId(), captured.getPartitionId());
        assertEquals(sequenceNumber, captured.getSequenceNumber());
        assertEquals(offset, captured.getOffset());
        assertNotNull(captured.getOffsetString(), "offsetString must not be null - BlobCheckpointStore depends on it");
        assertEquals(offsetString, captured.getOffsetString());
    }
}
