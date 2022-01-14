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
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;

import java.time.Duration;
import java.util.function.Consumer;

import static com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;

@ServiceClientBuilder(
    serviceClients = {EventHubBufferedProducerAsyncClient.class, EventHubBufferedProducerClient.class},
    protocol = ServiceClientProtocol.AMQP)
public final class EventHubBufferedProducerClientBuilder {
    private final EventHubClientBuilder builder;
    private final BufferedProducerClientOptions clientOptions = new BufferedProducerClientOptions();

    public EventHubBufferedProducerClientBuilder() {
        builder = new EventHubClientBuilder();
    }

    public EventHubBufferedProducerClientBuilder clientOptions(ClientOptions clientOptions) {
        builder.clientOptions(clientOptions);
        return this;
    }

    public EventHubBufferedProducerClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }

    public EventHubBufferedProducerClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    public EventHubBufferedProducerClientBuilder connectionString(String connectionString, String eventHubName) {
        builder.connectionString(connectionString, eventHubName);
        return this;
    }

    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
                                                            TokenCredential credential) {
        builder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
                                                            AzureNamedKeyCredential credential) {
        builder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
                                                            AzureSasCredential credential) {
        builder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    public EventHubBufferedProducerClientBuilder customEndpointAddress(String customEndpointAddress) {
        builder.customEndpointAddress(customEndpointAddress);
        return this;
    }

    public EventHubBufferedProducerClientBuilder enableIdempotentRetries(boolean enableIdempotentRetries) {
        clientOptions.setEnableIdempotentRetries(enableIdempotentRetries);
        return this;
    }

    public EventHubBufferedProducerClientBuilder maxConcurrentSendsPerPartition(int maxConcurrentSendsPerPartition) {
        clientOptions.setMaxConcurrentSendsPerPartition(maxConcurrentSendsPerPartition);
        return this;
    }

    public EventHubBufferedProducerClientBuilder maxPendingEventCount(int maxPendingEventCount) {
        clientOptions.setMaxPendingEventCount(maxPendingEventCount);
        return this;
    }

    public EventHubBufferedProducerClientBuilder maxWaitTime(Duration maxWaitTime) {
        clientOptions.setMaxWaitTime(maxWaitTime);
        return this;
    }

    public EventHubBufferedProducerClientBuilder onSendBatchFailed(
        Consumer<SendBatchFailedContext> sendFailedContext) {
        clientOptions.setSendFailedContext(sendFailedContext);
        return this;
    }

    public EventHubBufferedProducerClientBuilder onSendBatchSucceeded(
        Consumer<SendBatchSucceededContext> sendSucceededContext) {
        clientOptions.setSendSucceededContext(sendSucceededContext);
        return this;
    }

    public EventHubBufferedProducerClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        builder.proxyOptions(proxyOptions);
        return this;
    }

    public EventHubBufferedProducerClientBuilder retry(AmqpRetryOptions retryOptions) {
        builder.retry(retryOptions);
        return this;
    }

    public EventHubBufferedProducerClientBuilder shareConnection() {
        builder.shareConnection();
        return this;
    }

    public EventHubBufferedProducerClientBuilder transportType(AmqpTransportType transport) {
        builder.transportType(transport);
        return this;
    }

    public EventHubBufferedProducerAsyncClient buildAsyncClient() {
        return new EventHubBufferedProducerAsyncClient(builder, clientOptions);
    }

    public EventHubBufferedProducerClient buildClient() {
        return null;
    }
}
