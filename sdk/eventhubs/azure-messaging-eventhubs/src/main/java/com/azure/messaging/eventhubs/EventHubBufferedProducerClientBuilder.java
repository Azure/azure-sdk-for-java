// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.annotation.ServiceClientProtocol;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;

import java.lang.module.Configuration;
import java.time.Duration;
import java.util.function.Consumer;

@ServiceClientBuilder(
    serviceClients = {EventHubBufferedProducerAsyncClient.class, EventHubBufferedProducerClient.class},
    protocol = ServiceClientProtocol.AMQP)
public final class EventHubBufferedProducerClientBuilder {
    private final EventHubClientBuilder clientBuilder;

    public EventHubBufferedProducerClientBuilder() {
        clientBuilder = new EventHubClientBuilder();
    }

    public EventHubBufferedProducerClientBuilder clientOptions(ClientOptions clientOptions) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder configuration(Configuration configuration) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder connectionString(String connectionString) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder connectionString(String connectionString, String eventHubName) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        TokenCredential credential) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        AzureNamedKeyCredential credential) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        AzureSasCredential credential) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder customEndpointAddress(String customEndpointAddress) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder enableIdempotentRetries(boolean enableIdempotentRetries) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder maxoncurrentSendsPerPartition(int maxConcurrentSendsPerPartition) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder maxPendingEventCount(int maxPendingEventCount) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder maxWaitTime(Duration maxWaitTime) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder onSendBatchFailed(
        Consumer<SendBatchFailedContext> sendFailedContext) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder onSendBatchSucceeded(
        Consumer<SendBatchSucceededContext> sendSucceededContext) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder retry(AmqpRetryOptions retryOptions) {
        return this;
    }

    public EventHubBufferedProducerClientBuilder shareConnection() {
        return this;
    }

    public EventHubBufferedProducerClientBuilder transportType(AmqpTransportType transport) {
        return this;
    }

    public EventHubBufferedProducerAsyncClient buildAsyncClient() {
        return null;
    }

    public EventHubBufferedProducerClient buildClient() {
        return null;
    }
}
