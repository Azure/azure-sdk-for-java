// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.TransportType;
import com.azure.eventhubs.CredentialInfo;
import com.azure.eventhubs.ProxyConfiguration;
import com.azure.eventhubs.implementation.TokenProvider;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Objects;

/**
 * A wrapper class that contains all parameters that are needed to establish a connection to an Event Hub.
 */
public class ConnectionParameters {
    private final CredentialInfo credentials;
    private final Duration timeout;
    private final TokenProvider tokenProvider;
    private final TransportType transport;
    private final Retry retryPolicy;
    private final ProxyConfiguration proxyConfiguration;
    private final Scheduler scheduler;

    public ConnectionParameters(final CredentialInfo credentials, final Duration timeout, final TokenProvider tokenProvider,
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

    public CredentialInfo credentials() {
        return credentials;
    }

    public Duration timeout() {
        return timeout;
    }

    public TokenProvider tokenProvider() {
        return tokenProvider;
    }

    public TransportType transportType() {
        return transport;
    }

    public Retry retryPolicy() {
        return retryPolicy;
    }

    public ProxyConfiguration proxyConfiguration() {
        return proxyConfiguration;
    }

    public Scheduler scheduler() {
        return scheduler;
    }
}
