// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.properties;

/**
 * @author Warren Zhu
 */
public class ServiceBusProducerProperties {
    private boolean sync = false;
    private long sendTimeout = 10000;

    /**
     *
     * @return True if is sync.
     */
    public boolean isSync() {
        return sync;
    }

    /**
     *
     * @param sync Whether is sync.
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }

    /**
     *
     * @return Send time out.
     */
    public long getSendTimeout() {
        return sendTimeout;
    }

    /**
     *
     * @param sendTimeout Send time out.
     */
    public void setSendTimeout(long sendTimeout) {
        this.sendTimeout = sendTimeout;
    }
}
