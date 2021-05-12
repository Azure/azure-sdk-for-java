// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.test.support.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusQueueOperationDeadLetterQueueTest extends ServiceBusQueueOperationSendSubscribeTest {

    @Test
    public void testSendDeadLetterQueueWithoutManualCheckpointModel() {
        subscribe(destination, m -> sendSubscribeOperation.deadLetter(destination, m, "reason", "desc"), User.class);

        sendSubscribeOperation.sendAsync(destination, userMessage);
        verifyDeadLetterCalledTimes(0);
        verifyCompleteCalledTimes(1);
    }

    @Test
    public void testSendDeadLetterQueueWithManualCheckpointModel() {
        setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());

        subscribe(destination, m -> sendSubscribeOperation.deadLetter(destination, m, "reason", "desc"), User.class);

        sendSubscribeOperation.sendAsync(destination, userMessage);

        verifyDeadLetterCalledTimes(1);
        verifyCompleteCalledTimes(0);
    }

}
