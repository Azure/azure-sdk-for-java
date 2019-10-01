// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RntbdRequestEncoder extends MessageToByteEncoder {

    private static final Logger logger = LoggerFactory.getLogger(RntbdRequestEncoder.class);

    /**
     * Returns {@code true} if the given message is an @{link RntbdRequest} instance
     * <p>
     * If {@code false} this message should be passed to the next {@link ChannelOutboundHandler} in the pipeline.
     *
     * @param message the message to encode
     * @return {@code true}, if the given message is an an {@link RntbdRequest} instance; otherwise @{false}
     */
    @Override
    public boolean acceptOutboundMessage(final Object message) {
        return message instanceof RntbdRequestArgs;
    }

    /**
     * Encode a message into a {@link ByteBuf}
     * <p>
     * This method will be called for each message that can be written by this encoder.
     *
     * @param context the {@link ChannelHandlerContext} which this {@link MessageToByteEncoder} belongs encode
     * @param message the message to encode
     * @param out     the {@link ByteBuf} into which the encoded message will be written
     */
    @Override
    protected void encode(final ChannelHandlerContext context, final Object message, final ByteBuf out) throws Exception {

        final RntbdRequest request = RntbdRequest.from((RntbdRequestArgs)message);
        final int start = out.writerIndex();

        try {
            request.encode(out);
        } catch (final Throwable error) {
            out.writerIndex(start);
            throw error;
        }

        if (logger.isDebugEnabled()) {
            final int length = out.writerIndex() - start;
            logger.debug("{}: ENCODE COMPLETE: length={}, request={}", context.channel(), length, request);
        }
    }
}
