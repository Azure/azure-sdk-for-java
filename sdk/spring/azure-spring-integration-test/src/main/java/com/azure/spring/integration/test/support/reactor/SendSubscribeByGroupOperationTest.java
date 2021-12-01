// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support.reactor;

import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.azure.spring.integration.core.api.reactor.SendOperation;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 *
 * @param <T> The type that extends SendOperation and SubscribeByGroupOperation
 */
public abstract class SendSubscribeByGroupOperationTest<T extends SendOperation & SubscribeByGroupOperation>
    extends SendSubscribeOperationTest<T> {

    /**
     * The consumer group.
     */
    protected String consumerGroup = "group1";

    /**
     *
     * @param destination The destination.
     * @param consumer The consumer.
     * @param payloadType The payloadType.
     */
    @Override
    protected void subscribe(String destination, Consumer<Message<?>> consumer, Class<?> payloadType) {
        sendSubscribeOperation.subscribe(destination, consumerGroup, consumer, payloadType);
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
