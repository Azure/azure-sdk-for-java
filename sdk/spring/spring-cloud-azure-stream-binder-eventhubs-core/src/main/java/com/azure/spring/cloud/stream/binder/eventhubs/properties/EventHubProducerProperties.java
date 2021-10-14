// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.properties;

/**
 * @author Warren Zhu
 */
public class EventHubProducerProperties {
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
    private long sendTimeout = 10000;

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public long getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(long sendTimeout) {
        this.sendTimeout = sendTimeout;
    }
}
