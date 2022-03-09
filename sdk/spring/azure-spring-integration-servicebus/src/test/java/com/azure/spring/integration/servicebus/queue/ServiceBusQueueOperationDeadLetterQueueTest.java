// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.test.support.pojo.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class ServiceBusQueueOperationDeadLetterQueueTest extends ServiceBusQueueOperationSendSubscribeTest {

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testSendDeadLetterQueueWithoutManualCheckpointModel() {
        subscribe(destination, m -> sendSubscribeOperation.deadLetter(destination, m, "reason", "desc"), User.class);

        sendSubscribeOperation.sendAsync(destination, userMessage);
        verifyDeadLetterCalledTimes(1);
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
