// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
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
    private final AmqpTransportType transport;
    private final AmqpRetryOptions retryOptions;
    private final ProxyOptions proxyOptions;
    private final Scheduler scheduler;
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final CbsAuthorizationType authorizationType;

    public ConnectionOptions(String fullyQualifiedNamespace, String entityPath, TokenCredential tokenCredential,
            CbsAuthorizationType authorizationType, AmqpTransportType transport, AmqpRetryOptions retryOptions,
            ProxyOptions proxyOptions, Scheduler scheduler) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' is required.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' is required.");
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' is required.");
        this.authorizationType = Objects.requireNonNull(authorizationType, "'authorizationType' is required.");
        this.transport = Objects.requireNonNull(transport, "'transport' is required.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' is required.");
        this.proxyOptions = Objects.requireNonNull(proxyOptions, "'proxyConfiguration' is required.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' is required.");
    }

    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    public String getEntityPath() {
        return entityPath;
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    public CbsAuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    public AmqpTransportType getTransportType() {
        return transport;
    }

    public AmqpRetryOptions getRetry() {
        return retryOptions;
    }

    public ProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
