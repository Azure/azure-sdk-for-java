// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.netty.channel.Channel;

import java.io.ByteArrayOutputStream;

/**
 * Holder of information gathered by {@link Netty4ResponseHandler}. Used in {@code NettyHttpClient} to create the final
 * {@link Response}.
 */
public final class ResponseStateInfo {
    private final Channel responseChannel;
    private final boolean channelConsumptionComplete;
    private final int statusCode;
    private final HttpHeaders headers;
    private final ByteArrayOutputStream eagerContent;
    private final ResponseBodyHandling responseBodyHandling;

    ResponseStateInfo(Channel responseChannel, boolean channelConsumptionComplete, int statusCode, HttpHeaders headers,
        ByteArrayOutputStream eagerContent, ResponseBodyHandling responseBodyHandling) {
        this.responseChannel = responseChannel;
        this.channelConsumptionComplete = channelConsumptionComplete;
        this.statusCode = statusCode;
        this.headers = headers;
        this.eagerContent = eagerContent;
        this.responseBodyHandling = responseBodyHandling;
    }

    public Channel getResponseChannel() {
        return responseChannel;
    }

    public boolean isChannelConsumptionComplete() {
        return channelConsumptionComplete;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public ByteArrayOutputStream getEagerContent() {
        return eagerContent;
    }

    public ResponseBodyHandling getResponseBodyHandling() {
        return responseBodyHandling;
    }
}
