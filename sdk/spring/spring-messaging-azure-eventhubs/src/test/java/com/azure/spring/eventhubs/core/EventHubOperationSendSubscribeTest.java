// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.eventhubs.support.EventHubTestOperation;
import com.azure.spring.eventhubs.support.StartPosition;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.messaging.core.SendSubscribeByGroupOperationTest;
import com.azure.spring.messaging.support.pojo.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventHubOperationSendSubscribeTest extends SendSubscribeByGroupOperationTest<EventHubOperation> {

    @Mock
    EventContext eventContext;

    @Mock
    PartitionContext partitionContext;

    private AutoCloseable closeable;

    @BeforeEach
    @Override
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        when(this.eventContext.updateCheckpointAsync()).thenReturn(Mono.empty());
        when(this.eventContext.getPartitionContext()).thenReturn(this.partitionContext);
        when(this.partitionContext.getPartitionId()).thenReturn(this.partitionId);

        this.sendSubscribeOperation = new EventHubTestOperation(null, () -> eventContext);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Override
    protected void verifyCheckpointSuccessCalled(int times) {
        verify(this.eventContext, times(times)).updateCheckpointAsync();
    }

    @Override
    protected void verifyCheckpointBatchSuccessCalled(int times) {
//        verify(this.eventContext, times(times)).updateCheckpoint();
    }

    @Override
    protected void verifyCheckpointFailureCalled(int times) {

    }

    @Test
    public void testSendReceiveWithBatchCheckpointMode() {
        sendSubscribeOperation
            .setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.BATCH).build());
        sendSubscribeOperation.setStartPosition(StartPosition.EARLIEST);
        messages.forEach(m -> sendSubscribeOperation.sendAsync(destination, m));
        sendSubscribeOperation.subscribe(destination, consumerGroup, this::batchCheckpointHandler, User.class);
        verifyCheckpointBatchSuccessCalled(1);
    }

    private void batchCheckpointHandler(Message<?> message) {
    }

    @Test
    public void testHasPartitionIdHeader() {
        sendSubscribeOperation.subscribe(destination, consumerGroup, this::partitionIdHandler, User.class);
        sendSubscribeOperation.sendAsync(destination, userMessage);
    }

    private void partitionIdHandler(Message<?> message) {
        assertTrue(message.getHeaders().containsKey(AzureHeaders.RAW_PARTITION_ID));
        String partitionId = message.getHeaders().get(AzureHeaders.RAW_PARTITION_ID, String.class);
        assertNotNull(partitionId);
        assertEquals(this.partitionId, partitionId);
    }

    @Override
    protected void verifyCheckpointFailure(Checkpointer checkpointer) {
    }
}
