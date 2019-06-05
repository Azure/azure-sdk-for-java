// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.eventhubs.implementation.TokenProvider;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Objects;

/**
 * A wrap class that contains all parameters that need to establish a connection to an event hub.
 */
class ConnectionParameters {
    private final CredentialInfo credentials;
    private final Duration timeout;
    private final TokenProvider tokenProvider;
    private final TransportType transport;
    private final Retry retryPolicy;
    private final ProxyConfiguration proxyConfiguration;
    private final Scheduler scheduler;

    ConnectionParameters(final CredentialInfo credentials, final Duration timeout, final TokenProvider tokenProvider,
                         final TransportType transport, final Retry retryPolicy,
                         final ProxyConfiguration proxyConfiguration, final Scheduler scheduler) {
        Objects.requireNonNull(credentials);
        Objects.requireNonNull(timeout);
        Objects.requireNonNull(tokenProvider);
        Objects.requireNonNull(transport);
        Objects.requireNonNull(retryPolicy);
        Objects.requireNonNull(proxyConfiguration);
        Objects.requireNonNull(scheduler);

        this.credentials = credentials;
        this.timeout = timeout;
        this.tokenProvider = tokenProvider;
        this.transport = transport;
        this.retryPolicy = retryPolicy;
        this.proxyConfiguration = proxyConfiguration;
        this.scheduler = scheduler;
    }

    CredentialInfo getCredentials() {
        return credentials;
    }

    Duration getTimeout() {
        return timeout;
    }

    TokenProvider getTokenProvider() {
        return tokenProvider;
    }

    TransportType getTransportType() {
        return transport;
    }

    Retry getRetryPolicy() {
        return retryPolicy;
    }

    ProxyConfiguration getProxyConfiguration() {
        return proxyConfiguration;
    }

    Scheduler getScheduler() {
        return scheduler;
    }
}
