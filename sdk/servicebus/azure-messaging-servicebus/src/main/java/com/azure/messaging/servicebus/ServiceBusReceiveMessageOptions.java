// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;

/**
 * Option for receiving message.
 */
public class ServiceBusReceiveMessageOptions {
    static final int DEFAULT_MAX_AUTO_RENEWAL_DURATION_SECONDS = 5 * 60; // 300 seconds = 5 minutes total
    static final ReceiveMode DEFAULT_RECEIVE_MODE = ReceiveMode.PEEK_LOCK;
    static final boolean DEFAULT_AUTO_COMPLETE = true;
    static final int DEFAULT_PREFETCH_COUNT = 5;

    private Duration maxAutoRenewDuration =  Duration.ofSeconds(DEFAULT_MAX_AUTO_RENEWAL_DURATION_SECONDS);

    boolean autoComplete;
    ReceiveMode receiveMode;
    private int prefetchCount;

    /**
     * Constructor.
     */
    public ServiceBusReceiveMessageOptions() {
        this.maxAutoRenewDuration = Duration.ofSeconds(DEFAULT_MAX_AUTO_RENEWAL_DURATION_SECONDS);
        this.autoComplete = DEFAULT_AUTO_COMPLETE;
        this.receiveMode = DEFAULT_RECEIVE_MODE;
        this.prefetchCount = DEFAULT_PREFETCH_COUNT;
    }

    /**
     *
     * @param autoComplete the message.
     * @return updated {@link ServiceBusReceiveMessageOptions}.
     */
    public ServiceBusReceiveMessageOptions setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
        return this;
    }

    /**
     *
     * @param receiveMode to receive message in.
     * @return updated {@link ServiceBusReceiveMessageOptions}.
     */
    public ServiceBusReceiveMessageOptions setReceiveMode(ReceiveMode receiveMode) {
        this.receiveMode = receiveMode;
        return this;

    }

    /**
     *
     * @param prefetchCount to receive messages from service bus.
     * @return updated {@link ServiceBusReceiveMessageOptions}.
     */
    public ServiceBusReceiveMessageOptions setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
        return this;
    }

    /**
     *
     * @param maxAutoRenewDuration to renew message lock.
     * @return updated {@link ServiceBusReceiveMessageOptions}.
     */
    public ServiceBusReceiveMessageOptions setMaxAutoRenewDuration(Duration maxAutoRenewDuration) {
        this.maxAutoRenewDuration = maxAutoRenewDuration;
        return this;
    }

    /**
     *
     * @return maximum renew duration.
     */
    public Duration getMaxAutoRenewDuration() {
        return maxAutoRenewDuration;
    }

    /**
     *
     * @return auto commit is on or off.
     */
    public boolean isAutoComplete() {
        return autoComplete;
    }

    /**
     *
     * @return {@link ReceiveMode}
     */
    public ReceiveMode getReceiveMode() {
        return receiveMode;
    }

    /**
     *
     * @return of the messages to receive.
     */
    public int getPrefetchCount() {
        return prefetchCount;
    }
}
