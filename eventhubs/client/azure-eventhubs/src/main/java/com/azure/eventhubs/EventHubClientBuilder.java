// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.TransportType;
import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.eventhubs.implementation.CBSAuthorizationType;
import com.azure.eventhubs.implementation.ClientConstants;
import com.azure.eventhubs.implementation.ConnectionParameters;
import com.azure.eventhubs.implementation.ConnectionStringProperties;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.eventhubs.implementation.ReactorProvider;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Objects;

/**
 * Builder to create an {@link EventHubClient}.
 */
public class EventHubClientBuilder {

    private static final String AZURE_EVENT_HUBS_CONNECTION_STRING = "AZURE_EVENT_HUBS_CONNECTION_STRING";

    private TokenCredential credentials;
    private Configuration configuration;
    private Duration timeout;
    private ProxyConfiguration proxyConfiguration;
    private Retry retry;
    private Scheduler scheduler;
    private TransportType transport;
    private String host;
    private String eventHubPath;

    /**
     * Creates a new instance with the default transport {@link TransportType#AMQP}.
     */
    public EventHubClientBuilder() {
        transport = TransportType.AMQP;
    }

    /**
     * Sets the credential information given a connection string to the Event Hub instance.
     *
     * @param connectionString The connection string to the Event Hub this client wishes to connect to.
     * @return The updated EventHubClientBuilder object.
     * @throws IllegalArgumentException if {@code connectionString} is null or empty.
     * @throws AzureException If the shared access signature token credential could not be created using the connection
     * string.
     */
    public EventHubClientBuilder credentials(String connectionString) {
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new EventHubSharedAccessKeyCredential(properties.sharedAccessKeyName(), properties.sharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AzureException("Could not create the SharedAccessSignatureTokenCredential.", e);
        }

        return credentials(properties.endpoint().getHost(), properties.eventHubPath(), tokenCredential);
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param host The fully qualified host name for the Event Hubs namespace. This is likely to be similar to
     * {@literal {your-namespace}.servicebus.windows.net}.
     * @param eventHubPath The path of the specific Event Hub to connect the client to.
     * @param credentials The token credential to use for authorization. Access controls may be specified by the Event
     * Hubs namespace or the requested Event Hub, depending on Azure configuration.
     * @return The updated EventHubClientBuilder object.
     * @throws IllegalArgumentException if {@code host} or {@code eventHubPath} is null or empty.
     * @throws NullPointerException if {@code credentials} is null.
     */
    public EventHubClientBuilder credentials(String host, String eventHubPath, TokenCredential credentials) {
        if (ImplUtils.isNullOrEmpty(host)) {
            throw new IllegalArgumentException("'host' cannot be null or empty");
        }
        if (ImplUtils.isNullOrEmpty(eventHubPath)) {
            throw new IllegalArgumentException("'eventHubPath' cannot be null or empty.");
        }

        Objects.requireNonNull(credentials);

        this.host = host;
        this.credentials = credentials;
        this.eventHubPath = eventHubPath;
        return this;
    }

    /**
     * Sets the transport type by which all the communication with Azure Event Hubs occurs.
     * Default value is {@link TransportType#AMQP}.
     *
     * @param transport The transport type to use.
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder transportType(TransportType transport) {
        this.transport = transport;
        return this;
    }

    /**
     * Sets the timeout for each connection, link, and session.
     *
     * @param timeout Duration for timeout.
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the scheduler for operations such as connecting to and receiving or sending data to Event Hubs. If none is
     * specified, an elastic pool is used.
     *
     * @param scheduler The scheduler for operations such as connecting to and receiving or sending data to Event Hubs.
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    /**
     * Sets the proxy configuration for EventHubClient.
     *
     * @param proxyConfiguration The proxy configuration to use.
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder proxyConfiguration(ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
        return this;
    }

    /**
     * Sets the retry policy for EventHubClient.
     *
     * @param retry The retry policy to use.
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder retry(Retry retry) {
        this.retry = retry;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link ConfigurationManager#getConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Creates a new {@link EventHubClient} based on the configuration set in this builder.
     * Use the default not null values if the Connection parameters are not provided.
     *
     * @return A new {@link EventHubClient} instance.
     * @throws IllegalStateException if the credentials have not been set using either {@link #credentials(String)}
     * or {@link #credentials(String, String, TokenCredential)}.
     */
    public EventHubClient build() {
        configuration = configuration == null ? ConfigurationManager.getConfiguration().clone() : configuration;

        if (credentials == null) {
            final String connectionString = configuration.get(AZURE_EVENT_HUBS_CONNECTION_STRING);

            if (ImplUtils.isNullOrEmpty(connectionString)) {
                throw new IllegalArgumentException("Credentials have not been set using 'EventHubClientBuilder.credentials(String)'"
                    + "EventHubClientBuilder.credentials(String, String, TokenCredential). And the connection string is"
                    + "not set in the '" + AZURE_EVENT_HUBS_CONNECTION_STRING + "' environment variable.");
            }

            credentials(connectionString);
        }

        if (timeout == null) {
            timeout = Duration.ofSeconds(60);
        }

        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);

        if (retry == null) {
            retry = Retry.getDefaultRetry();
        }

        proxyConfiguration = constructDefaultProxyConfiguration(configuration);

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }

        final CBSAuthorizationType authorizationType = credentials instanceof EventHubSharedAccessKeyCredential
            ? CBSAuthorizationType.SHARED_ACCESS_SIGNATURE
            : CBSAuthorizationType.JSON_WEB_TOKEN;
        final ConnectionParameters parameters = new ConnectionParameters(host, eventHubPath, credentials,
            authorizationType, timeout, transport, retry, proxyConfiguration, scheduler);

        return new EventHubClient(parameters, provider, handlerProvider);
    }

    private ProxyConfiguration constructDefaultProxyConfiguration(Configuration configuration) {
        ProxyAuthenticationType authentication = ProxyAuthenticationType.NONE;
        if (proxyConfiguration != null) {
            authentication = proxyConfiguration.authentication();
        }

        String proxyAddress = configuration.get(BaseConfigurations.HTTP_PROXY);
        Proxy proxy = null;
        if (proxyAddress != null) {
            final String[] hostPort = proxyAddress.split(":");
            if (hostPort.length < 2) {
                throw new IllegalArgumentException("HTTP_PROXY cannot be parsed into a proxy");
            }

            final String host = hostPort[0];
            final int port = Integer.parseInt(hostPort[1]);
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        }

        final String username = configuration.get(ProxyConfiguration.PROXY_USERNAME);
        final String password = configuration.get(ProxyConfiguration.PROXY_PASSWORD);

        return new ProxyConfiguration(authentication, proxy, username, password);
    }
}
