// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.credentials.TokenCredential;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
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

    public String host() {
        return host;
    }

    public String eventHubName() {
        return eventHubName;
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

    public RetryOptions retry() {
        return retryOptions;
    }

    public ProxyConfiguration proxyConfiguration() {
        return proxyConfiguration;
    }

    public Scheduler scheduler() {
        return scheduler;
    }
}
