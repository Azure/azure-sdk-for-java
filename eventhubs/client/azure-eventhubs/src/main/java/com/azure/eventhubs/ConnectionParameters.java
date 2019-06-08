// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.eventhubs.implementation.TokenProvider;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Objects;

/**
 * A wrapper class that contains all parameters that are needed to establish a connection to an Event Hub.
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

    CredentialInfo credentials() {
        return credentials;
    }

    Duration timeout() {
        return timeout;
    }

    TokenProvider tokenProvider() {
        return tokenProvider;
    }

    TransportType transportType() {
        return transport;
    }

    Retry retryPolicy() {
        return retryPolicy;
    }

    ProxyConfiguration proxyConfiguration() {
        return proxyConfiguration;
    }

    Scheduler scheduler() {
        return scheduler;
    }
}
