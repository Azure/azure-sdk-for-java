// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core;

import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

public abstract class SendSubscribeWithoutGroupOperationTest<T extends SendOperation & SubscribeOperation>
    extends SendSubscribeOperationTest<T> {

    @Override
    protected void subscribe(String destination, Consumer<Message<?>> consumer, Class<?> payloadType) {
        sendSubscribeOperation.subscribe(destination, consumer, payloadType);
    }

    @Override
    protected void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        sendSubscribeOperation.setCheckpointConfig(checkpointConfig);
    }
}
