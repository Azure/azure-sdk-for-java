// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.support.pojo.User;
import com.azure.spring.messaging.core.rx.RxSendSubscribeByGroupOperationTest;
import com.azure.spring.eventhubs.support.StartPosition;
import com.azure.spring.eventhubs.support.RxEventHubTestOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventHubRxOperationSendSubscribeTest extends RxSendSubscribeByGroupOperationTest<EventHubRxOperation> {

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

        this.sendSubscribeOperation = new RxEventHubTestOperation(null, () -> eventContext);
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

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendReceiveWithBatchCheckpointMode() {
        sendSubscribeOperation
            .setCheckpointConfig(CheckpointConfig.builder()
                .checkpointMode(CheckpointMode.BATCH).build());
        sendSubscribeOperation.setStartPosition(StartPosition.EARLIEST);
        Arrays.stream(messages).forEach(m -> sendSubscribeOperation.sendRx(destination, m));
        sendSubscribeOperation.subscribe(destination, consumerGroup, User.class).test()
            .assertValueCount(messages.length).assertNoErrors();
        verifyCheckpointBatchSuccessCalled(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendReceiveWithPartitionCountCheckpointMode() {
        sendSubscribeOperation
            .setCheckpointConfig(CheckpointConfig.builder()
                .checkpointMode(CheckpointMode.PARTITION_COUNT)
                .checkpointCount(1).build());
        sendSubscribeOperation.setStartPosition(StartPosition.EARLIEST);
        Arrays.stream(messages).forEach(m -> sendSubscribeOperation.sendRx(destination, m));
        sendSubscribeOperation.subscribe(destination, consumerGroup, User.class).test()
            .assertValueCount(messages.length).assertNoErrors();
        verifyCheckpointSuccessCalled(messages.length);
    }

    @Test
    public void testHasPartitionIdHeader() {
        sendSubscribeOperation.subscribe(destination, consumerGroup, User.class);
        sendSubscribeOperation.sendRx(destination, userMessage);
    }

}
