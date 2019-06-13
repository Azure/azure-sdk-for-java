// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.TransportType;
import com.azure.core.credentials.TokenCredential;
import com.azure.eventhubs.ProxyConfiguration;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Objects;

/**
 * A wrapper class that contains all parameters that are needed to establish a connection to an Event Hub.
 */
public class ConnectionParameters {
    private final Duration timeout;
    private final TokenCredential tokenCredential;
    private final TransportType transport;
    private final Retry retryPolicy;
    private final ProxyConfiguration proxyConfiguration;
    private final Scheduler scheduler;
    private final String host;
    private final String eventHubPath;
    private final CBSAuthorizationType authorizationType;

    public ConnectionParameters(String host, String eventHubPath, TokenCredential tokenCredential,
                                CBSAuthorizationType authorizationType, Duration timeout, TransportType transport,
                                Retry retryPolicy, ProxyConfiguration proxyConfiguration, Scheduler scheduler) {
        Objects.requireNonNull(host);
        Objects.requireNonNull(eventHubPath);
        Objects.requireNonNull(timeout);
        Objects.requireNonNull(tokenCredential);
        Objects.requireNonNull(transport);
        Objects.requireNonNull(retryPolicy);
        Objects.requireNonNull(proxyConfiguration);
        Objects.requireNonNull(scheduler);

        this.host = host;
        this.eventHubPath = eventHubPath;
        this.timeout = timeout;
        this.tokenCredential = tokenCredential;
        this.authorizationType = authorizationType;
        this.transport = transport;
        this.retryPolicy = retryPolicy;
        this.proxyConfiguration = proxyConfiguration;
        this.scheduler = scheduler;
    }

    public String host() {
        return host;
    }

    public String eventHubPath() {
        return eventHubPath;
    }

    public Duration timeout() {
        return timeout;
    }

    public TokenCredential tokenCredential() {
        return tokenCredential;
    }

    public CBSAuthorizationType authorizationType() {
        return authorizationType;
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
