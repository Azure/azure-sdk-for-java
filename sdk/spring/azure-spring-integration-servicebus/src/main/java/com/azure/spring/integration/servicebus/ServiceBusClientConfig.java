// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;

/**
 * Service bus client related config
 *
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public final class ServiceBusClientConfig {

    private final int prefetchCount;

    private final int concurrency;

    private final boolean sessionsEnabled;

    private final boolean requeueRejected;

    private final int maxConcurrentCalls;

    private ServiceBusReceiveMode serviceBusReceiveMode;

    private ServiceBusClientConfig(int prefetchCount, int concurrency, boolean sessionsEnabled,
                                   boolean requeueRejected, int maxConcurrentCalls, ServiceBusReceiveMode serviceBusReceiveMode) {

        this.prefetchCount = prefetchCount;
        this.concurrency = concurrency;
        this.sessionsEnabled = sessionsEnabled;
        this.requeueRejected = requeueRejected;
        this.maxConcurrentCalls = maxConcurrentCalls;
        this.serviceBusReceiveMode = serviceBusReceiveMode;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public boolean isSessionsEnabled() {
        return sessionsEnabled;
    }

    public boolean isRequeueRejected() {
        return requeueRejected;
    }

    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public ServiceBusReceiveMode getServiceBusReceiveMode() {
        return serviceBusReceiveMode;
    }

    public static ServiceBusClientConfigBuilder builder() {
        return new ServiceBusClientConfigBuilder();
    }

    /**
     * Builder class for {@link ServiceBusClientConfig}.
     */
    public static class ServiceBusClientConfigBuilder {
        private int prefetchCount = 1;
        private int concurrency = 1;
        private boolean sessionsEnabled = false;
        private boolean requeueRejected = false;
        private int maxConcurrentCalls = 1;
        private ServiceBusReceiveMode serviceBusReceiveMode = ServiceBusReceiveMode.PEEK_LOCK;

        public void setRequeueRejected(boolean requeueRejected) {
            this.requeueRejected = requeueRejected;
        }

        public ServiceBusClientConfigBuilder setPrefetchCount(int prefetchCount) {
            this.prefetchCount = prefetchCount;
            return this;
        }

        public ServiceBusClientConfigBuilder setConcurrency(int concurrency) {
            this.concurrency = concurrency;
            return this;
        }

        public ServiceBusClientConfigBuilder setSessionsEnabled(boolean sessionsEnabled) {
            this.sessionsEnabled = sessionsEnabled;
            return this;
        }

        public ServiceBusClientConfigBuilder setMaxConcurrentCalls(int maxConcurrentCalls) {
            this.maxConcurrentCalls = maxConcurrentCalls;
            return this;
        }

        public ServiceBusClientConfigBuilder setServiceBusReceiveMode(ServiceBusReceiveMode serviceBusReceiveMode) {
            this.serviceBusReceiveMode = serviceBusReceiveMode;
            return this;
        }

        public ServiceBusClientConfig build() {
            return new ServiceBusClientConfig(prefetchCount, concurrency, sessionsEnabled, requeueRejected, maxConcurrentCalls, serviceBusReceiveMode);
        }
    }
}
