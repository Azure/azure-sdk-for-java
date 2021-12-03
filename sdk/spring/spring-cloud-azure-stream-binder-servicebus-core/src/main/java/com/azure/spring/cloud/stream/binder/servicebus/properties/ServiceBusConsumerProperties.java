// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.properties;

import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;

/**
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public final class ServiceBusConsumerProperties extends ProcessorProperties {

    private boolean requeueRejected = false;

    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
    }

    /**
     * Controls if the failed messages are routed to the DLQ
     *
     * <p>
     * @return boolean, default : false
     */
    public boolean isRequeueRejected() {
        return requeueRejected;
    }

    public void setRequeueRejected(boolean requeueRejected) {
        this.requeueRejected = requeueRejected;
    }

}
