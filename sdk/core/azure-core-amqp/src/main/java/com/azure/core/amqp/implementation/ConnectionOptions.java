// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.models.ProxyConfiguration;
import com.azure.core.annotation.Immutable;
import com.azure.core.credential.TokenCredential;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;

/**
 * A wrapper class that contains all parameters that are needed to establish a connection to an AMQP message broker.
 */
@Immutable
public class ConnectionOptions {
    private final TokenCredential tokenCredential;
    private final TransportType transport;
    private final RetryOptions retryOptions;
    private final ProxyConfiguration proxyConfiguration;
    private final Scheduler scheduler;
    private final String hostname;
    private final String entityPath;
    private final CBSAuthorizationType authorizationType;

    public ConnectionOptions(String hostname, String entityPath, TokenCredential tokenCredential,
                             CBSAuthorizationType authorizationType, TransportType transport, RetryOptions retryOptions,
                             ProxyConfiguration proxyConfiguration, Scheduler scheduler) {
        this.hostname = Objects.requireNonNull(hostname, "'hostname' is required.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' is required.");
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' is required.");
        this.authorizationType = Objects.requireNonNull(authorizationType, "'authorizationType' is required.");
        this.transport = Objects.requireNonNull(transport, "'transport' is required.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' is required.");
        this.proxyConfiguration = Objects.requireNonNull(proxyConfiguration, "'proxyConfiguration' is required.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' is required.");
    }

    public String getHostname() {
        return hostname;
    }

    public String getEntityPath() {
        return entityPath;
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
