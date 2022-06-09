// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.ProgressReporter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.FileRegion;
import io.netty.handler.codec.http.LastHttpContent;

public class RequestProgressHandler extends ChannelOutboundHandlerAdapter {
    /**
     * Name of the handler when it is added into a ChannelPipeline.
     */
    public static final String HANDLER_NAME = "azureRequestProgressHandler";

    private final ProgressReporter progressReporter;

    public RequestProgressHandler(ProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {

        if (msg instanceof ByteBuf) {
            progressReporter.reportProgress(((ByteBuf) msg).readableBytes());
        } else if (msg instanceof LastHttpContent) {
            progressReporter.reportProgress(((LastHttpContent) msg).content().readableBytes());
        } else if (msg instanceof FileRegion) {
            progressReporter.reportProgress(((FileRegion) msg).count());
        }

        ctx.write(msg, promise.unvoid());
    }
}
