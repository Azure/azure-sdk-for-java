// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.TransportType;
import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.exception.AzureException;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.eventhubs.implementation.ConnectionParameters;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.eventhubs.implementation.ReactorProvider;
import com.azure.eventhubs.implementation.SharedAccessSignatureTokenProvider;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

/**
 * Builder to create an {@link EventHubClient}.
 */
public class EventHubClientBuilder {

    private static final String AZURE_EVENT_HUBS_CONNECTION_STRING = "AZURE_EVENT_HUBS_CONNECTION_STRING";

    private CredentialInfo credentials;
    private Configuration configuration;
    private Duration timeout;
    private ProxyConfiguration proxyConfiguration;
    private Retry retry;
    private Scheduler scheduler;
    private TransportType transport;

    /**
     * Creates a new instance with the default transport {@link TransportType#AMQP}.
     */
    public EventHubClientBuilder() {
        transport = TransportType.AMQP;
    }

    /**
     * Sets the credentials information from connection string
     *
     * @param credentials Credentials for the EventHubClient.
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder credentials(CredentialInfo credentials) {
        this.credentials = credentials;
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
     * @throws IllegalArgumentException when 'connectionString' is {@code null} or empty.
     * @throws AzureException If the token provider cannot be created for authorizing requests.
     */
    public EventHubClient build() {
        configuration = configuration == null ? ConfigurationManager.getConfiguration().clone() : configuration;

        if (credentials == null) {
            String connectionString = configuration.get(AZURE_EVENT_HUBS_CONNECTION_STRING);
            if (ImplUtils.isNullOrEmpty(connectionString)) {
                throw new IllegalArgumentException("Connection string is null or empty.");
            }
            credentials = CredentialInfo.from(connectionString);
        }

        if (timeout == null) {
            timeout = Duration.ofSeconds(60);
        }

        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);
        final SharedAccessSignatureTokenProvider tokenProvider;

        try {
            tokenProvider = new SharedAccessSignatureTokenProvider(credentials.sharedAccessKeyName(), credentials.sharedAccessKey());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AzureException("Could not create token provider.");
        }

        if (retry == null) {
            retry = Retry.getDefaultRetry();
        }

        proxyConfiguration = constructDefaultProxyConfiguration(configuration);

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }

        ConnectionParameters connectionParameters = new ConnectionParameters(credentials, timeout, tokenProvider,
            transport, retry, proxyConfiguration, scheduler);

        return new EventHubClient(connectionParameters, provider, handlerProvider);
    }

    private ProxyConfiguration constructDefaultProxyConfiguration(Configuration configuration) {
        ProxyAuthenticationType authentication = ProxyAuthenticationType.NONE;
        if (proxyConfiguration != null) {
            authentication = proxyConfiguration.authentication();
        }

        String proxyAddress = configuration.get(BaseConfigurations.HTTP_PROXY);
        Proxy proxy = null;
        if (proxyAddress != null) {
            String[] hostPort = proxyAddress.split(":");
            if (hostPort.length < 2) {
                throw new IllegalArgumentException("HTTP_PROXY cannot be parsed into a proxy");
            }
            String host = hostPort[0];
            Integer port = Integer.parseInt(hostPort[1]);
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        }

        String username = configuration.get(ProxyConfiguration.PROXY_USERNAME);
        String password = configuration.get(ProxyConfiguration.PROXY_PASSWORD);

        return new ProxyConfiguration(authentication, proxy, username, password);
    }
}
