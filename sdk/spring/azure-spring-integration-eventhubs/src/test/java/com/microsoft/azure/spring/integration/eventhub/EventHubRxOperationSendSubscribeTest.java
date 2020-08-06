// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.eventhub;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.StartPosition;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubRxOperation;
import com.microsoft.azure.spring.integration.eventhub.support.RxEventHubTestOperation;
import com.microsoft.azure.spring.integration.test.support.pojo.User;
import com.microsoft.azure.spring.integration.test.support.rx.RxSendSubscribeByGroupOperationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubRxOperationSendSubscribeTest extends RxSendSubscribeByGroupOperationTest<EventHubRxOperation> {

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

        this.sendSubscribeOperation = new RxEventHubTestOperation(null, () -> eventContext);
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
