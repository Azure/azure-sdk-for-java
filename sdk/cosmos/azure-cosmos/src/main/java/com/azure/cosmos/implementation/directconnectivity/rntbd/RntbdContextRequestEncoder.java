// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RntbdContextRequestEncoder extends MessageToByteEncoder<RntbdContextRequest> {

    private static final Logger Logger = LoggerFactory.getLogger(RntbdContextRequestEncoder.class);

    /**
     * Returns {@code true} if the given message is an {@link RntbdContextRequest} message.
     * <p>
     * If {@code false} this message should be passed to the next {@link io.netty.channel.ChannelHandlerContext} in the
     * pipeline.
     *
     * @param message the message to encode.
     *
     * @return {@code true}, if the given message is an an @{link RntbdContextRequest} instance; {@code false}
     * otherwise.
     */
    @Override
    public boolean acceptOutboundMessage(final Object message) {
        return message.getClass() == RntbdContextRequest.class;
    }

    /**
     * Encode an {@link RntbdContextRequest} message into a {@link ByteBuf}.
     * <p>
     * This method will be called for each written message that can be handled by this encoder.
     *
     * @param context the {@link ChannelHandlerContext} to which this {@link MessageToByteEncoder} belongs.
     * @param message the message to encode.
     * @param out the {@link ByteBuf} into which the encoded message will be written.
     *
     * @throws IllegalStateException is thrown if an error occurs.
     */
    @Override
    protected void encode(
        final ChannelHandlerContext context,
        final RntbdContextRequest message,
        final ByteBuf out) throws IllegalStateException {

        out.markWriterIndex();

        try {
            message.encode(out);
        } catch (final IllegalStateException error) {
            out.resetWriterIndex();
            throw error;
        }

        Logger.debug("{}: ENCODE COMPLETE: message={}", context.channel(), message);
    }
}
