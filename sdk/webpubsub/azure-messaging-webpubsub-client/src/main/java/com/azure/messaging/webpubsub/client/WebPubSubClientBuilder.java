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

@ServiceClientBuilder(serviceClients = {WebPubSubAsyncClient.class, WebPubSubClient.class})
public class WebPubSubClientBuilder {

    private final ClientLogger logger = new ClientLogger(WebPubSubClientBuilder.class);

    private WebPubSubClientCredential credential;

    private WebPubSubProtocol webPubSubProtocol = new WebPubSubJsonReliableProtocol();

    private RetryOptions retryOptions = null;

    private boolean autoReconnect = true;

    private boolean autoRestoreGroup = true;

    public WebPubSubClientBuilder() {
    }

    public WebPubSubClientBuilder credential(WebPubSubClientCredential credential) {
        this.credential = Objects.requireNonNull(credential);
        return this;
    }

    public WebPubSubClientBuilder webPubSubProtocol(WebPubSubProtocol webPubSubProtocol) {
        this.webPubSubProtocol = Objects.requireNonNull(webPubSubProtocol);
        return this;
    }

    public WebPubSubClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions);
        return this;
    }

    public WebPubSubClientBuilder autoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    public WebPubSubClientBuilder autoRestoreGroup(boolean autoRestoreGroup) {
        this.autoRestoreGroup = autoRestoreGroup;
        return this;
    }

    public WebPubSubClient buildClient() {
        return new WebPubSubClient(this.buildAsyncClient());
    }

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
