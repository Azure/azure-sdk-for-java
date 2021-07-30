// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.properties;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.eventhubs.implementation.ClientConstants;

/**
 * @author Warren Zhu
 */
public class EventHubProducerProperties {
    /**
     * Whether the producer should act in a synchronous manner with respect to sending messages into destination. If
     * true, the producer will wait for a response from Event Hub after a send operation before sending next message. If
     * false, the producer will keep sending without waiting response
     * <p>
     * Default: false
     */
    private boolean sync;

    /**
     * Effective only if sync is set to true. The amount of time to wait for a response from Event Hub after a send
     * operation, in milliseconds.
     * <p>
     * Default: 10000
     */
    private long sendTimeout = 10000;

    private int prefetchCount = 1;

    private boolean shareConnection = false;

    private String customEndpointAddress;

    private AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(ClientConstants.OPERATION_TIMEOUT);

    private AmqpTransportType transport = AmqpTransportType.AMQP;

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

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public boolean isShareConnection() {
        return shareConnection;
    }

    public void setShareConnection(boolean shareConnection) {
        this.shareConnection = shareConnection;
    }

    public String getCustomEndpointAddress() {
        return customEndpointAddress;
    }

    public void setCustomEndpointAddress(String customEndpointAddress) {
        this.customEndpointAddress = customEndpointAddress;
    }

    public AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }

    public void setRetryOptions(AmqpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
    }

    public AmqpTransportType getTransport() {
        return transport;
    }

    public void setTransport(AmqpTransportType transport) {
        this.transport = transport;
    }

}
