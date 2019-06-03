// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.core.exception.AzureException;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.eventhubs.implementation.ReactorProvider;
import com.azure.eventhubs.implementation.SharedAccessSignatureTokenProvider;
import reactor.core.scheduler.Scheduler;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

/**
 * Builder to create an {@link EventHubClient}.
 */
public class EventHubClientBuilder {
    private ConnectionStringBuilder credentials;
    private TransportType transport;
    private Duration duration;
    private Scheduler scheduler;
    private ProxyConfiguration proxyConfiguration;
    private RetryPolicy retryPolicy;

    /**
     * Creates a new instance with the default transport {@link TransportType#AMQP}.
     */
    public EventHubClientBuilder() {
        transport = TransportType.AMQP;
    }

    /**
     * Sets the credentials.
     *
     * @param credentials Credentials for the EventHubClient.
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder credentials(ConnectionStringBuilder credentials) {
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
     * @param duration Duration for timeout.
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder timeout(Duration duration) {
        this.duration = duration;
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
    public EventHubClientBuilder proxy(ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
        return this;
    }

    /**
     * Sets the retry policy for EventHubClient.
     *
     * @param retryPolicy The retry policy to use.
     * @return The updated EventHubClientBuilder object.
     */
    public EventHubClientBuilder retry(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Creates a new {@link EventHubClient} based on the configuration set in this builder.
     *
     * @return A new {@link EventHubClient} instance.
     * @throws AzureException If the token provider cannot be created for authorizing requests.
     */
    public EventHubClient build() {
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);
        final SharedAccessSignatureTokenProvider tokenProvider;

        try {
            tokenProvider = new SharedAccessSignatureTokenProvider("", "");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AzureException("Could not create token provider.");
        }

        return new EventHubClient(credentials, tokenProvider, provider, handlerProvider, scheduler);
    }

    //TODO (conniey): Remove placeholder when the client is updated.
    public static class ProxyConfiguration {
    }
}
