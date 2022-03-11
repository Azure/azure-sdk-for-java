// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;

/**
 *
 */
public class ServiceBusConsumerProperties extends ProcessorProperties {

    private boolean requeueRejected = false;

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
