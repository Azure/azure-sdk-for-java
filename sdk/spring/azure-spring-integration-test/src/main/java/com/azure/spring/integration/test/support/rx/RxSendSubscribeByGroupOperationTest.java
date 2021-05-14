// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support.rx;

import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.RxSendOperation;
import com.azure.spring.integration.core.api.RxSubscribeByGroupOperation;
import org.springframework.messaging.Message;
import rx.Observable;

public abstract class RxSendSubscribeByGroupOperationTest<T extends RxSendOperation & RxSubscribeByGroupOperation>
    extends RxSendSubscribeOperationTest<T> {

    protected String consumerGroup = "group1";

    @Override
    protected Observable<Message<?>> subscribe(String destination, Class<?> payloadType) {
        return sendSubscribeOperation.subscribe(destination, consumerGroup, payloadType);
    }

    @Override
    protected void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        sendSubscribeOperation.setCheckpointConfig(checkpointConfig);
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

}
