// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;

import java.time.Duration;

/**
 *
 */
public class ServiceBusProducerProperties extends ProducerProperties {
    private boolean sync = false;
    private Duration sendTimeout = Duration.ofMillis(10000);

    /**
     * Check whether is sync.
     *
     * @return true if is sync,false otherwise
     */
    public boolean isSync() {
        return sync;
    }

    /**
     * Set sync.
     *
     * @param sync the sync
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }

    /**
     * Get send time out.
     *
     * @return sendTimeout the send time out
     */
    public Duration getSendTimeout() {
        return sendTimeout;
    }

    /**
     * Set send time out.
     *
     * @param sendTimeout the send time out
     */
    public void setSendTimeout(Duration sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

}
