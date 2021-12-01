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

    private final int maxConcurrentSessions;

    private final boolean enableAutoComplete;

    private final ServiceBusReceiveMode serviceBusReceiveMode;

    private ServiceBusClientConfig(int prefetchCount, int concurrency, boolean sessionsEnabled,
                                   boolean requeueRejected, int maxConcurrentCalls, int maxConcurrentSessions,
                                   ServiceBusReceiveMode serviceBusReceiveMode, boolean enableAutoComplete) {

        this.prefetchCount = prefetchCount;
        this.concurrency = concurrency;
        this.sessionsEnabled = sessionsEnabled;
        this.requeueRejected = requeueRejected;
        this.maxConcurrentCalls = maxConcurrentCalls;
        this.serviceBusReceiveMode = serviceBusReceiveMode;
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.enableAutoComplete = enableAutoComplete;
    }

    /**
     *
     * @return The prefetch count.
     */
    public int getPrefetchCount() {
        return prefetchCount;
    }

    /**
     *
     * @return The concurrency
     */
    public int getConcurrency() {
        return concurrency;
    }

    /**
     *
     * @return true if sessions enabled.
     */
    public boolean isSessionsEnabled() {
        return sessionsEnabled;
    }

    /**
     *
     * @return true if requeue rejected.
     */
    public boolean isRequeueRejected() {
        return requeueRejected;
    }

    /**
     *
     * @return the max concurrent calls.
     */
    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    /**
     *
     * @return True if enable auto complete.
     */
    public boolean isEnableAutoComplete() {
        return enableAutoComplete;
    }

    /**
     * The mas concurrent sessions.
     * @return The max concurrent sessions.
     */
    public int getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    /**
     *
     * @return Service bus receive mode.
     */
    public ServiceBusReceiveMode getServiceBusReceiveMode() {
        return serviceBusReceiveMode;
    }

    /**
     *
     * @return Service bus client config builder.
     */
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
        private int maxConcurrentSessions = 1;
        private boolean enableAutoComplete = false;
        private ServiceBusReceiveMode serviceBusReceiveMode = ServiceBusReceiveMode.PEEK_LOCK;

        /**
         *
         * @param requeueRejected whether requeue rejected.
         */
        public void setRequeueRejected(boolean requeueRejected) {
            this.requeueRejected = requeueRejected;
        }

        /**
         *
         * @param prefetchCount The prefetch count.
         * @return The ServiceBusClientConfigBuilder.
         */
        public ServiceBusClientConfigBuilder setPrefetchCount(int prefetchCount) {
            this.prefetchCount = prefetchCount;
            return this;
        }

        /**
         *
         * @param concurrency The concurrency.
         * @return The ServiceBusClientConfigBuilder.
         */
        public ServiceBusClientConfigBuilder setConcurrency(int concurrency) {
            this.concurrency = concurrency;
            return this;
        }

        /**
         *
         * @param sessionsEnabled Whether sessions is enabled.
         * @return The ServiceBusClientConfigBuilder.
         */
        public ServiceBusClientConfigBuilder setSessionsEnabled(boolean sessionsEnabled) {
            this.sessionsEnabled = sessionsEnabled;
            return this;
        }

        /**
         *
         * @param maxConcurrentCalls Mak concurrent calls.
         * @return The ServiceBusClientConfigBuilder.
         */
        public ServiceBusClientConfigBuilder setMaxConcurrentCalls(int maxConcurrentCalls) {
            this.maxConcurrentCalls = maxConcurrentCalls;
            return this;
        }

        /**
         *
         * @param maxConcurrentSessions The max concurrent sessions.
         * @return The ServiceBusClientConfigBuilder.
         */
        public ServiceBusClientConfigBuilder setMaxConcurrentSessions(int maxConcurrentSessions) {
            this.maxConcurrentSessions = maxConcurrentSessions;
            return this;
        }

        /**
         *
         * @param serviceBusReceiveMode The service bus receive mode.
         * @return The ServiceBusClientConfigBuilder.
         */
        public ServiceBusClientConfigBuilder setServiceBusReceiveMode(ServiceBusReceiveMode serviceBusReceiveMode) {
            this.serviceBusReceiveMode = serviceBusReceiveMode;
            return this;
        }

        /**
         *
         * @param enableAutoComplete Whether enable auto complete.
         * @return The ServiceBusClientConfigBuilder.
         */
        public ServiceBusClientConfigBuilder setEnableAutoComplete(boolean enableAutoComplete) {
            this.enableAutoComplete = enableAutoComplete;
            return this;
        }

        /**
         *
         * @return The ServiceBusClientConfig.
         */
        public ServiceBusClientConfig build() {
            return new ServiceBusClientConfig(prefetchCount, concurrency, sessionsEnabled, requeueRejected, maxConcurrentCalls, maxConcurrentSessions, serviceBusReceiveMode, enableAutoComplete);
        }
    }
}
