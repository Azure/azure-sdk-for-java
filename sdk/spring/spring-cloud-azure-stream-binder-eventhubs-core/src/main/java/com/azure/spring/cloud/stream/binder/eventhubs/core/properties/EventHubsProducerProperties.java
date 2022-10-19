// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.properties;

import com.azure.spring.messaging.eventhubs.core.properties.ProducerProperties;

import java.time.Duration;

/**
 *
 */
public class EventHubsProducerProperties extends ProducerProperties {
    /**
     * Whether the producer should act in a synchronous manner with respect to sending messages into destination.
     * If true, the producer will wait for a response from Event Hub after a send operation before sending next message.
     * If false, the producer will keep sending without waiting response
     * <p>
     * Default: false
     */
    private boolean sync;

    /**
     * Effective only if sync is set to true.
     * The amount of time to wait for a response from Event Hub after a send operation, in milliseconds.
     * <p>
     * Default: 10000
     */
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
