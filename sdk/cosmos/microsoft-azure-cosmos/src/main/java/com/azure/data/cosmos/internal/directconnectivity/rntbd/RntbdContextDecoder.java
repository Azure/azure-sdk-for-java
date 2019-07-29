// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class RntbdContextDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(RntbdContextDecoder.class);

    /**
     * Deserialize from an input {@link ByteBuf} to an {@link RntbdContext} instance
     * <p>
     * This method decodes an {@link RntbdContext} or {@link RntbdContextException} instance and fires a user event.
     *
     * @param context the {@link ChannelHandlerContext} to which this {@link RntbdContextDecoder} belongs
     * @param in      the {@link ByteBuf} from which to readTree data
     * @param out     the {@link List} to which decoded messages should be added
     */
    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {

        if (RntbdFramer.canDecodeHead(in)) {

            Object result;

            try {
                final RntbdContext rntbdContext = RntbdContext.decode(in);
                context.fireUserEventTriggered(rntbdContext);
                result = rntbdContext;
            } catch (RntbdContextException error) {
                context.fireUserEventTriggered(error);
                result = error;
            } finally {
                in.discardReadBytes();
            }

            logger.debug("{} DECODE COMPLETE: {}", context.channel(), result);
        }
    }
}
