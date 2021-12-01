// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.core.api.SubscribeOperation;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 *
 * @param <T> The type that extends SendOperation and SubscribeOperation
 */
public abstract class SendSubscribeWithoutGroupOperationTest<T extends SendOperation & SubscribeOperation>
    extends SendSubscribeOperationTest<T> {

    /**
     *
     * @param destination The destination.
     * @param consumer The consumer.
     * @param payloadType The payloadType.
     */
    @Override
    protected void subscribe(String destination, Consumer<Message<?>> consumer, Class<?> payloadType) {
        sendSubscribeOperation.subscribe(destination, consumer, payloadType);
    }

    /**
     *
     * @param checkpointConfig The checkpointConfig.
     */
    @Override
    protected void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        sendSubscribeOperation.setCheckpointConfig(checkpointConfig);
    }
}
