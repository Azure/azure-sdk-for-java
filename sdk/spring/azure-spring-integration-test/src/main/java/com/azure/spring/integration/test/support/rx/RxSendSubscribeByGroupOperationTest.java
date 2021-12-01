// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support.rx;

import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.RxSendOperation;
import com.azure.spring.integration.core.api.RxSubscribeByGroupOperation;
import org.springframework.messaging.Message;
import rx.Observable;

/**
 *
 * @param <T> The type that extends RxSendOperation and RxSubscribeByGroupOperation.
 */
public abstract class RxSendSubscribeByGroupOperationTest<T extends RxSendOperation & RxSubscribeByGroupOperation>
    extends RxSendSubscribeOperationTest<T> {

    /**
     * Consumer group.
     */
    protected String consumerGroup = "group1";

    /**
     *
     * @param destination The destination.
     * @param payloadType The payload type.
     * @return The Observable.
     */
    @Override
    protected Observable<Message<?>> subscribe(String destination, Class<?> payloadType) {
        return sendSubscribeOperation.subscribe(destination, consumerGroup, payloadType);
    }

    /**
     *
     * @param checkpointConfig The checkpointConfig
     */
    @Override
    protected void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        sendSubscribeOperation.setCheckpointConfig(checkpointConfig);
    }

    /**
     *
     * @return The consumerGroup.
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     *
     * @param consumerGroup The consumerGroup.
     */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

}
