// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus;

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

    private ServiceBusClientConfig(int prefetchCount, int concurrency, boolean sessionsEnabled) {

        this.prefetchCount = prefetchCount;
        this.concurrency = concurrency;
        this.sessionsEnabled = sessionsEnabled;
    }

    public static ServiceBusClientConfigBuilder builder() {
        return new ServiceBusClientConfigBuilder();
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

    public static class ServiceBusClientConfigBuilder {
        private int prefetchCount = 1;
        private int concurrency = 1;
        private boolean sessionsEnabled = false;

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

        public ServiceBusClientConfig build() {
            return new ServiceBusClientConfig(prefetchCount, concurrency, sessionsEnabled);
        }
    }
}
