// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.protocol.WebPubSubJsonReliableProtocol;
import com.azure.messaging.webpubsub.client.protocol.WebPubSubProtocol;

import java.util.Objects;

/**
 * The builder of WebPubSub client.
 */
@ServiceClientBuilder(serviceClients = {WebPubSubAsyncClient.class, WebPubSubClient.class})
public class WebPubSubClientBuilder {

    private final ClientLogger logger = new ClientLogger(WebPubSubClientBuilder.class);

    private WebPubSubClientCredential credential;

    private WebPubSubProtocol webPubSubProtocol = new WebPubSubJsonReliableProtocol();

    private RetryOptions retryOptions = null;

    private boolean autoReconnect = true;

    private boolean autoRestoreGroup = true;

    /**
     * Creates a new instance of WebPubSubClientBuilder.
     */
    public WebPubSubClientBuilder() {
    }

    /**
     * Sets the credential as the provider for client access URI.
     *
     * @param credential the credential as the provider for client access URI.
     * @return itself.
     */
    public WebPubSubClientBuilder credential(WebPubSubClientCredential credential) {
        this.credential = Objects.requireNonNull(credential);
        return this;
    }

    /**
     * Sets the protocol.
     *
     * @param webPubSubProtocol the protocol.
     * @return itself.
     */
    public WebPubSubClientBuilder webPubSubProtocol(WebPubSubProtocol webPubSubProtocol) {
        this.webPubSubProtocol = Objects.requireNonNull(webPubSubProtocol);
        return this;
    }

    /**
     * Sets the retry options when sending messages.
     *
     * @param retryOptions the retry options.
     * @return itself.
     */
    public WebPubSubClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions);
        return this;
    }

    /**
     * Sets whether automatically reconnect after disconnect.
     *
     * @param autoReconnect whether automatically reconnect after disconnect.
     * @return itself.
     */
    public WebPubSubClientBuilder autoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    /**
     * Sets whether automatically restore joined groups after reconnect.
     *
     * @param autoRestoreGroup whether automatically restore joined groups after reconnect.
     * @return itself.
     */
    public WebPubSubClientBuilder autoRestoreGroup(boolean autoRestoreGroup) {
        this.autoRestoreGroup = autoRestoreGroup;
        return this;
    }

    /**
     * Builds the client.
     *
     * @return the client.
     */
    public WebPubSubClient buildClient() {
        return new WebPubSubClient(this.buildAsyncClient());
    }

    /**
     * Builds the asynchronous client.
     *
     * @return the asynchronous client.
     */
    public WebPubSubAsyncClient buildAsyncClient() {
        RetryStrategy retryStrategy;
        if (retryOptions != null) {
            if (retryOptions.getExponentialBackoffOptions() != null) {
                retryStrategy = new ExponentialBackoff(retryOptions.getExponentialBackoffOptions());
            } else if (retryOptions.getFixedDelayOptions() != null) {
                retryStrategy = new FixedDelay(retryOptions.getFixedDelayOptions());
            } else {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("'retryOptions' didn't define any retry strategy options"));
            }
        } else {
            retryStrategy = new ExponentialBackoff();
        }
        return new WebPubSubAsyncClient(
            credential.getClientAccessUriAsync(), webPubSubProtocol, retryStrategy, autoReconnect, autoRestoreGroup);
    }
}
