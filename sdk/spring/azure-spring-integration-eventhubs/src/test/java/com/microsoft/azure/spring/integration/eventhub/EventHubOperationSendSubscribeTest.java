// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.eventhub;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.StartPosition;
import com.microsoft.azure.spring.integration.core.api.reactor.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.support.EventHubTestOperation;
import com.microsoft.azure.spring.integration.test.support.pojo.User;
import com.microsoft.azure.spring.integration.test.support.reactor.SendSubscribeByGroupOperationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubOperationSendSubscribeTest extends SendSubscribeByGroupOperationTest<EventHubOperation> {

    @Mock
    EventContext eventContext;

    @Mock
    PartitionContext partitionContext;

    @Before
    @Override
    public void setUp() {
        when(this.eventContext.updateCheckpointAsync()).thenReturn(Mono.empty());
        when(this.eventContext.getPartitionContext()).thenReturn(this.partitionContext);
        when(this.partitionContext.getPartitionId()).thenReturn(this.partitionId);

        this.sendSubscribeOperation = new EventHubTestOperation(null, () -> eventContext);
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
