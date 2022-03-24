// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import com.azure.core.http.HttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.concurrent.CompletableFuture;

import static com.azure.core.http.netty.implementation.simple.SimpleNettyConstants.REQUEST_CONTEXT_KEY;

public class SimpleChannelHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        SimpleRequestContext requestContext = ctx.channel().attr(REQUEST_CONTEXT_KEY).get();
        if (msg instanceof io.netty.handler.codec.http.HttpResponse) {
            io.netty.handler.codec.http.HttpResponse response = (io.netty.handler.codec.http.HttpResponse) msg;

            requestContext.setStatusCode(response.status().code());

            com.azure.core.http.HttpHeaders coreHeaders = new com.azure.core.http.HttpHeaders();

            if (!response.headers().isEmpty()) {
                for (String name: response.headers().names()) {
                    for (String value: response.headers().getAll(name)) {
                        coreHeaders.add(name, value);
                    }
                }
            }

            requestContext.setHttpHeaders(coreHeaders);
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            requestContext.getBodyCollector().collect(content.content());

            if (content instanceof LastHttpContent) {
                CompletableFuture<HttpResponse> responseFuture = requestContext.getResponseFuture();
                ctx.channel().attr(REQUEST_CONTEXT_KEY).set(null);

                requestContext.getChannelPool().release(ctx.channel());

                responseFuture.complete(new SimpleNettyResponse(requestContext));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        SimpleRequestContext requestContext = ctx.channel().attr(REQUEST_CONTEXT_KEY).get();
        ctx.channel().attr(REQUEST_CONTEXT_KEY).set(null);
        requestContext.getResponseFuture().completeExceptionally(cause);
        ctx.close();

        requestContext.getChannelPool().release(ctx.channel());
    }
}
