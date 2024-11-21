// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.client.traits.AmqpTrait;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.annotation.ServiceClientProtocol;
import com.azure.core.client.traits.AzureNamedKeyCredentialTrait;
import com.azure.core.client.traits.AzureSasCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

import static com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;

/**
 * Builder used to instantiate {@link EventHubBufferedProducerClient} and {@link EventHubBufferedProducerAsyncClient}.
 *
 * <p>
 * To create an instance of {@link EventHubBufferedProducerClient} or {@link EventHubBufferedProducerAsyncClient}, the
 * <b>following fields are required</b>:
 *
 * <ul>
 *     <li>{@link #onSendBatchSucceeded(Consumer)} - A callback when events are successfully published to Event Hubs.
 *     </li>
 *     <li>{@link #onSendBatchFailed(Consumer)} - A callback when a failure publishing to Event Hubs occurs.</li>
 *     <li>Credentials to perform operations against Azure Event Hubs. They can be set by using one of the following
 *      methods:
 *      <ul>
 *           <li>{@link #credential(String, String, TokenCredential) credential(String, String, TokenCredential)} with
 *           the fully qualified namespace, Event Hub name, and a set of credentials authorized to use the Event Hub.
 *           </li>
 *           <li>{@link #credential(TokenCredential)}, {@link #credential(AzureSasCredential)}, or
 *           {@link #credential(AzureNamedKeyCredential)} along with {@link #fullyQualifiedNamespace(String)} and
 *           {@link #eventHubName(String)}. The fully qualified namespace, Event Hub name, and authorized credentials
 *           to use the Event Hub.</li>
 *           <li>{@link #connectionString(String) connectionString(String)} with a connection string to a specific Event
 *           Hub.</li>
 *           <li>{@link #connectionString(String, String) connectionString(String, String)} with an Event Hub
 *           <i>namespace</i> connection string and the Event Hub name.</li>
 *      </ul>
 * </ul>
 *
 * <p>The credential used in the following samples is {@code DefaultAzureCredential} for authentication. It is
 * appropriate for most scenarios, including local development and production environments. Additionally, we recommend
 * using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.  You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Creating an {@link EventHubBufferedProducerAsyncClient}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the asynchronous client
 * {@link EventHubBufferedProducerAsyncClient}.  The {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host
 * name. It is listed under the "Essentials" panel after navigating to the Event Hubs Namespace via Azure Portal. The
 * credential used is {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and
 * development and chooses the credential to used based on its running environment.  The producer is set to publish
 * events every 60 seconds with a buffer size of 1500 events for each partition.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubbufferedproducerasyncclient.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubBufferedProducerAsyncClient client = new EventHubBufferedProducerClientBuilder&#40;&#41;
 *     .credential&#40;&quot;fully-qualified-namespace&quot;, &quot;event-hub-name&quot;, credential&#41;
 *     .onSendBatchSucceeded&#40;succeededContext -&gt; &#123;
 *         System.out.println&#40;&quot;Successfully published events to: &quot; + succeededContext.getPartitionId&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .onSendBatchFailed&#40;failedContext -&gt; &#123;
 *         System.out.printf&#40;&quot;Failed to published events to %s. Error: %s%n&quot;,
 *             failedContext.getPartitionId&#40;&#41;, failedContext.getThrowable&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .maxWaitTime&#40;Duration.ofSeconds&#40;60&#41;&#41;
 *     .maxEventBufferLengthPerPartition&#40;1500&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubbufferedproducerasyncclient.construct -->
 *
 * <p><strong>Sample: Creating an {@link EventHubBufferedProducerClient}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous client
 * {@link EventHubBufferedProducerClient}.  The {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host
 * name. It is listed under the "Essentials" panel after navigating to the Event Hubs Namespace via Azure Portal. The
 * credential used is {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and
 * development and chooses the credential to used based on its running environment.  The producer is set to publish
 * events every 60 seconds with a buffer size of 1500 events for each partition.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubbufferedproducerclient.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubBufferedProducerClient client = new EventHubBufferedProducerClientBuilder&#40;&#41;
 *     .credential&#40;&quot;fully-qualified-namespace&quot;, &quot;event-hub-name&quot;, credential&#41;
 *     .onSendBatchSucceeded&#40;succeededContext -&gt; &#123;
 *         System.out.println&#40;&quot;Successfully published events to: &quot; + succeededContext.getPartitionId&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .onSendBatchFailed&#40;failedContext -&gt; &#123;
 *         System.out.printf&#40;&quot;Failed to published events to %s. Error: %s%n&quot;,
 *             failedContext.getPartitionId&#40;&#41;, failedContext.getThrowable&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubbufferedproducerclient.construct -->
 *
 * @see EventHubBufferedProducerClient
 * @see EventHubBufferedProducerAsyncClient
 */
@ServiceClientBuilder(
    serviceClients = {EventHubBufferedProducerAsyncClient.class, EventHubBufferedProducerClient.class},
    protocol = ServiceClientProtocol.AMQP)
public final class EventHubBufferedProducerClientBuilder implements
    TokenCredentialTrait<EventHubBufferedProducerClientBuilder>,
    AzureNamedKeyCredentialTrait<EventHubBufferedProducerClientBuilder>,
    ConnectionStringTrait<EventHubBufferedProducerClientBuilder>,
    AzureSasCredentialTrait<EventHubBufferedProducerClientBuilder>,
    AmqpTrait<EventHubBufferedProducerClientBuilder>,
    ConfigurationTrait<EventHubBufferedProducerClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(EventHubBufferedProducerClientBuilder.class);

    private final EventHubClientBuilder builder;
    private final BufferedProducerClientOptions clientOptions = new BufferedProducerClientOptions();
    private final PartitionResolver partitionResolver = new PartitionResolver();
    private AmqpRetryOptions retryOptions;

    /**
     * Creates a new instance with the following defaults:
     * <ul>
     *     <li>{@link #maxEventBufferLengthPerPartition(int)} is 1500</li>
     *     <li>{@link #transportType(AmqpTransportType)} is {@link AmqpTransportType#AMQP}</li>
     *     <li>{@link #maxConcurrentSendsPerPartition(int)} is 1</li>
     *     <li>{@link #maxConcurrentSends(int)} is 1</li>
     *     <li>{@link #maxWaitTime(Duration)} is 30 seconds</li>
     * </ul>
     */
    public EventHubBufferedProducerClientBuilder() {
        builder = new EventHubClientBuilder();
    }

    /**
     * Sets the client options.
     *
     * @param clientOptions The client options.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    @Override
    public EventHubBufferedProducerClientBuilder clientOptions(ClientOptions clientOptions) {
        builder.clientOptions(clientOptions);
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * If not specified, the default configuration store is used to configure the buffered producer. Use {@link
     * Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to configure the buffered producer.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    @Override
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
     *
     * @throws IllegalArgumentException if {@code connectionString} is null or empty. Or, the {@code
     *     connectionString} does not contain the "EntityPath" key, which is the name of the Event Hub instance.
     * @throws AzureException If the shared access signature token credential could not be created using the
     *     connection string.
     */
    @Override
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
     *
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
     *
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty
     *     string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, or {@code credential}
     *     is null.
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
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *     Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     *
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, or {@code credential}
     *     is null.
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
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *     Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     *
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, or {@code credential}
     *     is null.
     */
    public EventHubBufferedProducerClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        AzureSasCredential credential) {
        builder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param credential The shared access name and key credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     * @throws NullPointerException if {@code credentials} is null.
     */
    @Override
    public EventHubBufferedProducerClientBuilder credential(AzureNamedKeyCredential credential) {
        builder.credential(credential);
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param credential The shared access signature credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     * @throws NullPointerException if {@code credentials} is null.
     */
    @Override
    public EventHubBufferedProducerClientBuilder credential(AzureSasCredential credential) {
        builder.credential(credential);
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *     Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     * @throws NullPointerException if {@code credentials} is null.
     */
    @Override
    public EventHubBufferedProducerClientBuilder credential(TokenCredential credential) {
        builder.credential(credential);
        return this;
    }

    /**
     * Sets a custom endpoint address when connecting to the Event Hubs service. This can be useful when your network
     * does not allow connecting to the standard Azure Event Hubs endpoint address, but does allow connecting through an
     * intermediary. For example: {@literal https://my.custom.endpoint.com:55300}.
     *
     * <p>If no port is specified, the default port for the {@link #transportType(AmqpTransportType) transport type} is
     * used.</p>
     *
     * @param customEndpointAddress The custom endpoint address.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     *
     * @throws IllegalArgumentException if {@code customEndpointAddress} cannot be parsed into a valid {@link URL}.
     */
    public EventHubBufferedProducerClientBuilder customEndpointAddress(String customEndpointAddress) {
        builder.customEndpointAddress(customEndpointAddress);
        return this;
    }

    /**
     * Indicates whether events should be published using idempotent semantics for retries. If enabled, retries during
     * publishing will attempt to avoid duplication with a minor cost to throughput.  Duplicates are still possible but
     * the chance of them occurring is much lower when idempotent retries are enabled.
     *
     * <p>
     * It is important to note that enabling idempotent retries does not guarantee exactly-once semantics.  The existing
     * Event Hubs at-least-once delivery contract still applies and event duplication is unlikely, but possible.
     * </p>
     *
     * @param enableIdempotentRetries {@code true} to enable idempotent retries, {@code false} otherwise.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    EventHubBufferedProducerClientBuilder enableIdempotentRetries(boolean enableIdempotentRetries) {
        clientOptions.setEnableIdempotentRetries(enableIdempotentRetries);
        return this;
    }

    /**
     * Sets the fully qualified name for the Event Hubs namespace.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     *
     * @return The updated object.
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} is an empty string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace} is null.
     */
    public EventHubBufferedProducerClientBuilder fullyQualifiedNamespace(String fullyQualifiedNamespace) {
        builder.fullyQualifiedNamespace(fullyQualifiedNamespace);
        return this;
    }

    /**
     * Sets the name of the Event Hub to connect the client to.
     *
     * @param eventHubName The name of the Event Hub to connect the client to.

     * @return The updated object.
     * @throws IllegalArgumentException if {@code eventHubName} is an empty string.
     * @throws NullPointerException if {@code eventHubName} is null.
     */
    public EventHubBufferedProducerClientBuilder eventHubName(String eventHubName) {
        builder.eventHubName(eventHubName);
        return this;
    }

    /**
     * The total number of batches that may be sent concurrently, across all partitions.  This limit takes precedence
     * over the value specified in {@link #maxConcurrentSendsPerPartition(int) maxConcurrentSendsPerPartition}, ensuring
     * this maximum is respected.  When batches for the same partition are published concurrently, the ordering of
     * events is not guaranteed.  If the order events are published must be maintained, {@link
     * #maxConcurrentSendsPerPartition(int) maxConcurrentSendsPerPartition} should not exceed 1.
     *
     * <p>
     * By default, this will be set to the number of processors available in the host environment.
     * </p>
     *
     * @param maxConcurrentSends The total number of batches that may be sent concurrently.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    EventHubBufferedProducerClientBuilder maxConcurrentSends(int maxConcurrentSends) {
        clientOptions.setMaxConcurrentSends(maxConcurrentSends);
        return this;
    }

    /**
     * The number of batches that may be sent concurrently for a given partition.  This option is superseded by the
     * value specified for {@link #maxConcurrentSends(int) maxConcurrrentSends}, ensuring that limit is respected.
     *
     * @param maxConcurrentSendsPerPartition The number of batches that may be sent concurrently for a given
     *     partition.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    EventHubBufferedProducerClientBuilder maxConcurrentSendsPerPartition(int maxConcurrentSendsPerPartition) {
        clientOptions.setMaxConcurrentSendsPerPartition(maxConcurrentSendsPerPartition);
        return this;
    }

    /**
     * The total number of events that can be buffered for publishing at a given time for a given partition.  Once this
     * capacity is reached, more events can enqueued by calling the {@code enqueueEvent} methods on either {@link
     * EventHubBufferedProducerClient} or {@link EventHubBufferedProducerAsyncClient}.
     *
     * The default limit is 1500 queued events for each partition.
     *
     * @param maxEventBufferLengthPerPartition Total number of events that can be buffered for publishing at a given
     *     time.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    public EventHubBufferedProducerClientBuilder maxEventBufferLengthPerPartition(int maxEventBufferLengthPerPartition) {
        clientOptions.maxEventBufferLengthPerPartition(maxEventBufferLengthPerPartition);
        return this;
    }

    /**
     * The amount of time to wait for a batch to be built with events in the buffer before publishing a partially full
     * batch.
     *
     * @param maxWaitTime The amount of time to wait.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    public EventHubBufferedProducerClientBuilder maxWaitTime(Duration maxWaitTime) {
        clientOptions.setMaxWaitTime(maxWaitTime);
        return this;
    }

    /**
     * The callback to invoke when publishing a set of events fails.
     *
     * @param sendFailedContext The callback to invoke.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    public EventHubBufferedProducerClientBuilder onSendBatchFailed(
        Consumer<SendBatchFailedContext> sendFailedContext) {
        clientOptions.setSendFailedContext(sendFailedContext);
        return this;
    }

    /**
     * The callback to invoke when publishing a set of events succeeds.
     *
     * @param sendSucceededContext The callback to invoke when publishing a ste of events succeeds.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    public EventHubBufferedProducerClientBuilder onSendBatchSucceeded(
        Consumer<SendBatchSucceededContext> sendSucceededContext) {
        clientOptions.setSendSucceededContext(sendSucceededContext);
        return this;
    }

    /**
     * Sets the proxy configuration to use for the buffered producer. When a proxy is configured, {@link
     * AmqpTransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyOptions The proxy configuration to use.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    @Override
    public EventHubBufferedProducerClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        builder.proxyOptions(proxyOptions);
        return this;
    }

    /**
     * Sets the retry policy for the producer client. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry policy to use.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    @Override
    public EventHubBufferedProducerClientBuilder retryOptions(AmqpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        builder.retryOptions(retryOptions);
        return this;
    }

    /**
     * Sets the transport type by which all the communication with Azure Event Hubs occurs. Default value is {@link
     * AmqpTransportType#AMQP}.
     *
     * @param transport The transport type to use.
     *
     * @return The updated {@link EventHubBufferedProducerClientBuilder} object.
     */
    @Override
    public EventHubBufferedProducerClientBuilder transportType(AmqpTransportType transport) {
        builder.transportType(transport);
        return this;
    }

    /**
     * Builds a new instance of the async buffered producer client.
     *
     * @return A new instance of {@link EventHubBufferedProducerAsyncClient}.
     *
     * @throws NullPointerException if {@link #onSendBatchSucceeded(Consumer)}, {@link
     *     #onSendBatchFailed(Consumer)}, or {@link #maxWaitTime(Duration)} are null.
     * @throws IllegalArgumentException if {@link #maxConcurrentSends(int)}, {@link
     *     #maxConcurrentSendsPerPartition(int)}, or {@link #maxEventBufferLengthPerPartition(int)} are less than 1.
     */
    public EventHubBufferedProducerAsyncClient buildAsyncClient() {

        if (Objects.isNull(clientOptions.getSendSucceededContext())) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'onSendBatchSucceeded' cannot be null."));
        }

        if (Objects.isNull(clientOptions.getSendFailedContext())) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'onSendBatchFailed' cannot be null."));
        }

        if (Objects.isNull(clientOptions.getMaxWaitTime())) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'maxWaitTime' cannot be null."));
        }

        if (clientOptions.getMaxEventBufferLengthPerPartition() < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'maxEventBufferLengthPerPartition' cannot be less than 1."));
        }

        if (clientOptions.getMaxConcurrentSends() < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'maxConcurrentSends' cannot be less than 1."));
        }

        if (clientOptions.getMaxConcurrentSendsPerPartition() < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'maxConcurrentSendsPerPartition' cannot be less than 1."));
        }

        final AmqpRetryOptions options = retryOptions == null
            ? EventHubClientBuilder.DEFAULT_RETRY
            : retryOptions;

        return new EventHubBufferedProducerAsyncClient(builder, clientOptions, partitionResolver, options, builder.createTracer());
    }

    /**
     * Builds a new instance of the buffered producer client.
     *
     * @return A new instance of {@link EventHubBufferedProducerClient}.
     */
    public EventHubBufferedProducerClient buildClient() {
        final AmqpRetryOptions options = retryOptions == null
            ? EventHubClientBuilder.DEFAULT_RETRY
            : retryOptions;

        return new EventHubBufferedProducerClient(buildAsyncClient(), options.getTryTimeout());
    }
}
