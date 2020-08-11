// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder.properties;

/**
 * @author Warren Zhu
 */
public class ServiceBusProducerProperties {
    private boolean sync = false;
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
