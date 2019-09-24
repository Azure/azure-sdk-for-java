// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.models.ProxyConfiguration;
import com.azure.core.credentials.TokenCredential;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;

/**
 * A wrapper class that contains all parameters that are needed to establish a connection to an Event Hub.
 */
public class ConnectionOptions {
    private final TokenCredential tokenCredential;
    private final TransportType transport;
    private final RetryOptions retryOptions;
    private final ProxyConfiguration proxyConfiguration;
    private final Scheduler scheduler;
    private final String host;
    private final String eventHubName;
    private final CBSAuthorizationType authorizationType;

    public ConnectionOptions(String host, String eventHubName, TokenCredential tokenCredential,
                             CBSAuthorizationType authorizationType, TransportType transport, RetryOptions retryOptions,
                             ProxyConfiguration proxyConfiguration, Scheduler scheduler) {
        Objects.requireNonNull(host);
        Objects.requireNonNull(eventHubName);
        Objects.requireNonNull(tokenCredential);
        Objects.requireNonNull(transport);
        Objects.requireNonNull(retryOptions);
        Objects.requireNonNull(proxyConfiguration);
        Objects.requireNonNull(scheduler);

        this.host = host;
        this.eventHubName = eventHubName;
        this.tokenCredential = tokenCredential;
        this.authorizationType = authorizationType;
        this.transport = transport;
        this.retryOptions = retryOptions;
        this.proxyConfiguration = proxyConfiguration;
        this.scheduler = scheduler;
    }

    public String getHost() {
        return host;
    }

    public String getEventHubName() {
        return eventHubName;
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    public CBSAuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    public TransportType getTransportType() {
        return transport;
    }

    public RetryOptions getRetry() {
        return retryOptions;
    }

    public ProxyConfiguration getProxyConfiguration() {
        return proxyConfiguration;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
