/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.azure.credentials.AzureTokenCredentials;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Netty OutboundHandler to set authorization header.
 */
public class AzureTokenCredentialsHandler extends ChannelOutboundHandlerAdapter {
    private final AzureTokenCredentials credentials;

    /**
     * Creates AzureTokenCredentialsHandler.
     *
     * @param credentials the credentials
     */
    public AzureTokenCredentialsHandler(AzureTokenCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            String token = credentials.getTokenFromUri(req.uri());
            req.headers().add("Authorization", "Bearer " + token);
        }

        super.write(ctx, msg, promise);
    }
}
