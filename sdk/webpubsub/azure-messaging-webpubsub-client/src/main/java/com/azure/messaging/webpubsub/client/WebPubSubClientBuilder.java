// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClientBuilder;

@ServiceClientBuilder(serviceClients = {WebPubSubAsyncClient.class})
public class WebPubSubClientBuilder {

    private WebPubSubClientCredential credential;

    public WebPubSubClientBuilder() {
    }

    public WebPubSubClientBuilder credential(WebPubSubClientCredential credential) {
        this.credential = credential;
        return this;
    }

    public WebPubSubClient buildClient() {
        return new WebPubSubClient(this.buildAsyncClient());
    }

    public WebPubSubAsyncClient buildAsyncClient() {
        return new WebPubSubAsyncClient(credential.getClientAccessUriAsync());
    }
}
