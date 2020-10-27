package com.azure.cosmos.implementation.directconnectivity.MockServer;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RntbdContextEncoder extends MessageToByteEncoder<RntbdContext> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RntbdContext msg, ByteBuf out) throws Exception {

        final int start = out.writerIndex();

        try{
            msg.encode(out);
        } catch(final Throwable error) {
            out.writerIndex(start);
            throw error;
        }
    }
}
