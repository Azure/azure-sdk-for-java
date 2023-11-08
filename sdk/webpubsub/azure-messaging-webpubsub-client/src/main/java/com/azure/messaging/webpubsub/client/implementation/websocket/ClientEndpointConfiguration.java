// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.MessageEncoder;

public final class ClientEndpointConfiguration {

    private final String protocol;

    private final String userAgent;

    private static final MessageEncoder MESSAGE_ENCODER = new MessageEncoder();

    private static final MessageDecoder MESSAGE_DECODER = new MessageDecoder();

    public ClientEndpointConfiguration(String protocol, String userAgent) {
        this.protocol = protocol;
        this.userAgent = userAgent;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public MessageEncoder getMessageEncoder() {
        return MESSAGE_ENCODER;
    }

    public MessageDecoder getMessageDecoder() {
        return MESSAGE_DECODER;
    }
}
