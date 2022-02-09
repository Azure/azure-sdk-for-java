// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public final class ServerRntbdContextEncoder extends MessageToByteEncoder<ServerRntbdContext> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ServerRntbdContext msg, ByteBuf out) {

        final int start = out.writerIndex();

        try{
            msg.encode(out);
        } catch(final Throwable error) {
            out.writerIndex(start);
            throw error;
        }
    }
}
