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
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

import static com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;

/**
 * This class provides a fluent builder API to aid the instantiation of {@link EventHubBufferedProducerAsyncClient},
 * {@link EventHubBufferedProducerClient}.
 */
@ServiceClientBuilder(
    serviceClients = {EventHubBufferedProducerAsyncClient.class, EventHubBufferedProducerClient.class},
    protocol = ServiceClientProtocol.AMQP)
public final class EventHubBufferedProducerClientBuilder {
    private final EventHubClientBuilder builder;
    private final BufferedProducerClientOptions clientOptions = new BufferedProducerClientOptions();
    private AmqpRetryOptions retryOptions;
    private static final AmqpRetryOptions DEFAULT_RETRY = new AmqpRetryOptions()
        .setTryTimeout(ClientConstants.OPERATION_TIMEOUT);

    /**
     * Creates a new instance of {@link EventHubBufferedProducerClientBuilder}.
     */
    public EventHubBufferedProducerClientBuilder() {
        builder = new EventHubClientBuilder();
    }

    /**
     * Sets the buffered producer client options.
     * @param clientOptions {@link ClientOptions}
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder clientOptions(ClientOptions clientOptions) {
        builder.clientOptions(clientOptions);
        return this;
    }

    /**
     * Sets {@link EventHubClientBuilder} configuration. See {@link EventHubClientBuilder#configuration(Configuration)}
     * @param configuration {@link Configuration}
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }

    /**
     * Sets the credential information given a connection string to the Event Hub instance.
     * @param connectionString The connection string to use for connecting to the Event Hub instance. It is expected
     *     that the Event Hub name and the shared access key properties are contained in this connection string.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    /**
     * Sets the credential information given a connection string to the Event Hubs namespace and name to a specific
     * Event Hub instance.
     *
     * @param connectionString The connection string to use for connecting to the Event Hubs namespace; it is
     *     expected that the shared access key properties are contained in this connection string, but not the Event Hub
     *     name.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder connectionString(String connectionString, String eventHubName) {
        builder.connectionString(connectionString, eventHubName);
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *     Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
                                                            TokenCredential credential) {
        builder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The shared access name and key credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
                                                            AzureNamedKeyCredential credential) {
        builder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The shared access signature credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
                                                            AzureSasCredential credential) {
        builder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets a custom endpoint address when connecting to the Event Hubs service. This can be useful when your network
     * does not allow connecting to the standard Azure Event Hubs endpoint address, but does allow connecting through
     * an intermediary. For example: {@literal https://my.custom.endpoint.com:55300}.
     * <p>
     * If no port is specified, the default port for the {@link #transportType(AmqpTransportType) transport type} is
     * used.
     *
     * @param customEndpointAddress The custom endpoint address.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder customEndpointAddress(String customEndpointAddress) {
        builder.customEndpointAddress(customEndpointAddress);
        return this;
    }

    /**
     * Sets the {@link ClientOptions} of {@link EventHubBufferedProducerClientBuilder}
     * @param enableIdempotentRetries enable idempotent retries
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder enableIdempotentRetries(boolean enableIdempotentRetries) {
        clientOptions.setEnableIdempotentRetries(enableIdempotentRetries);
        return this;
    }

    /**
     * Sets send event max concurrent per partition.
     * @param maxConcurrentSendsPerPartition max concurrent.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder maxConcurrentSendsPerPartition(int maxConcurrentSendsPerPartition) {
        clientOptions.setMaxConcurrentSendsPerPartition(maxConcurrentSendsPerPartition);
        return this;
    }

    /**
     * Sets max pending events that stored in client.
     * @param maxPendingEventCount if the count of pending events greater than maxPendingEventCount, client will send all events.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder maxPendingEventCount(int maxPendingEventCount) {
        clientOptions.setMaxPendingEventCount(maxPendingEventCount);
        return this;
    }

    /**
     * Sets max wait time for sending event.
     * @param maxWaitTime If send events to event hub reach the max wait time, send operation will complete immediately.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder maxWaitTime(Duration maxWaitTime) {
        clientOptions.setMaxWaitTime(maxWaitTime);
        return this;
    }

    /**
     * Sets consumer for events that failed to send to service.
     * @param sendFailedContext All failed events will be accepted by the consumer.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder onSendBatchFailed(
        Consumer<SendBatchFailedContext> sendFailedContext) {
        clientOptions.setSendFailedContext(sendFailedContext);
        return this;
    }

    /**
     * Set consumer for events that send to service successfully.
     * @param sendSucceededContext All successful events will be accepted by the consumer.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder onSendBatchSucceeded(
        Consumer<SendBatchSucceededContext> sendSucceededContext) {
        clientOptions.setSendSucceededContext(sendSucceededContext);
        return this;
    }

    /**
     * Sets the proxy configuration to use for {@link EventHubClientBuilder} to build {@link EventHubProducerAsyncClient}. When a proxy is configured, {@link
     * AmqpTransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyOptions The proxy configuration to use.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        builder.proxyOptions(proxyOptions);
        return this;
    }

    /**
     * Sets the retry policy for {@link EventHubClientBuilder} to build {@link EventHubProducerAsyncClient}. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry policy to use.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder retry(AmqpRetryOptions retryOptions) {
        builder.retry(retryOptions);
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Toggles the builder to use the same connection for producers or consumers that are built from this instance. By
     * default, a new connection is constructed and used created for each Event Hub consumer or producer created.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder shareConnection() {
        builder.shareConnection();
        return this;
    }

    /**
     * Sets the transport type by which all the communication with Azure Event Hubs occurs. Default value is {@link
     * AmqpTransportType#AMQP}.
     *
     * @param transport The transport type to use.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object
     */
    public EventHubBufferedProducerClientBuilder transportType(AmqpTransportType transport) {
        builder.transportType(transport);
        return this;
    }

    /**
     * Create a new {@link EventHubProducerAsyncClient} based on the options set on this builder.
     *
     * @return A new {@link EventHubBufferedProducerAsyncClient} with options.
     */
    public EventHubBufferedProducerAsyncClient buildAsyncClient() {
        return new EventHubBufferedProducerAsyncClient(builder, clientOptions);
    }

    /**
     * Create a new {@link EventHubBufferedProducerClient} based on the options set on this builder.
     *
     * @return A new {@link EventHubBufferedProducerClient} with options.
     */
    public EventHubBufferedProducerClient buildClient() {
        if (Objects.isNull(retryOptions)) {
            retryOptions = DEFAULT_RETRY;
        }
        return new EventHubBufferedProducerClient(new EventHubBufferedProducerAsyncClient(builder, clientOptions), retryOptions.getTryTimeout());
    }
}
