// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.properties;

import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;

/**
 *
 */
public class ServiceBusConsumerProperties extends ProcessorProperties {

    private boolean requeueRejected = false;

    private CheckpointMode checkpointMode = CheckpointMode.RECORD;

    /**
     * Get checkpoint mode.
     *
     * @return checkpointMode the checkpoint mode
     */
    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    /**
     * Set checkpoint mode.
     *
     * @param checkpointMode the checkpoint mode
     */
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

    /**
     * Set checkpoint mode.
     *
     * @param requeueRejected the requeue Rejected
     */
    public void setRequeueRejected(boolean requeueRejected) {
        this.requeueRejected = requeueRejected;
    }

}
