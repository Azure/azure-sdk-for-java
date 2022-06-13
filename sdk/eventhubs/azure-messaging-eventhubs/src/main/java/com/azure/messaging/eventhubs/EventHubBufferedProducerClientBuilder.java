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
import com.azure.core.exception.AzureException;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;

import java.net.URL;
import java.time.Duration;
import java.util.function.Consumer;

import static com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;

@ServiceClientBuilder(
    serviceClients = {EventHubBufferedProducerAsyncClient.class, EventHubBufferedProducerClient.class},
    protocol = ServiceClientProtocol.AMQP)
public final class EventHubBufferedProducerClientBuilder {
    private final EventHubClientBuilder builder;
    private final BufferedProducerClientOptions clientOptions = new BufferedProducerClientOptions();

    /**
     * Creates a new instance with the default transport {@link AmqpTransportType#AMQP}.
     */
    public EventHubBufferedProducerClientBuilder() {
        builder = new EventHubClientBuilder();
    }

    /**
     * Sets the client options.
     *
     * @param clientOptions The client options.
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    public EventHubBufferedProducerClientBuilder clientOptions(ClientOptions clientOptions) {
        builder.clientOptions(clientOptions);
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * If not specified, the default configuration store is used to configure the buffered producer. Use
     * {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to configure the buffered producer.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    public EventHubBufferedProducerClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }

    /**
     * Sets the credential information given a connection string to the Event Hub instance.
     *
     * <p>
     * If the connection string is copied from the Event Hubs namespace, it will likely not contain the name to the
     * desired Event Hub, which is needed. In this case, the name can be added manually by adding {@literal
     * "EntityPath=EVENT_HUB_NAME"} to the end of the connection string. For example, "EntityPath=telemetry-hub".
     * </p>
     *
     * <p>
     * If you have defined a shared access policy directly on the Event Hub itself, then copying the connection string
     * from that Event Hub will result in a connection string that contains the name.
     * </p>
     *
     * @param connectionString The connection string to use for connecting to the Event Hub instance. It is expected
     *     that the Event Hub name and the shared access key properties are contained in this connection string.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     * @throws IllegalArgumentException if {@code connectionString} is null or empty. Or, the {@code
     *     connectionString} does not contain the "EntityPath" key, which is the name of the Event Hub instance.
     * @throws AzureException If the shared access signature token credential could not be created using the
     *     connection string.
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
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     * @throws NullPointerException if {@code connectionString} or {@code eventHubName} is null.
     * @throws IllegalArgumentException if {@code connectionString} or {@code eventHubName} is an empty string. Or,
     *     if the {@code connectionString} contains the Event Hub name.
     * @throws AzureException If the shared access signature token credential could not be created using the
     *     connection string.
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
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty
     *     string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, or {@code credential} is
     *     null.
     */
    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        TokenCredential credential) {
        builder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a> documentation for more details
     * on proper usage of the {@link TokenCredential} type.
     *
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *     Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, or {@code credential} is
     *     null.
     */
    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        AzureNamedKeyCredential credential) {
        builder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a> documentation for more details
     * on proper usage of the {@link TokenCredential} type.
     *
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *     Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, or {@code credential} is
     *     null.
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
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     * @throws IllegalArgumentException if {@code customEndpointAddress} cannot be parsed into a valid {@link URL}.
     */
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

    public EventHubBufferedProducerClientBuilder retryOptions(AmqpRetryOptions retryOptions) {
        builder.retryOptions(retryOptions);
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
