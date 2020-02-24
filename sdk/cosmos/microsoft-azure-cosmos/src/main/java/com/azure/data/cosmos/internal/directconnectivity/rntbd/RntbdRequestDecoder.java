// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public final class RntbdRequestDecoder extends ByteToMessageDecoder {
    /**
     * Prepare for decoding an @{link RntbdRequest} or fire a channel readTree event to pass the input message along
     *
     * @param context the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param message the message to be decoded
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) throws Exception {

        if (message instanceof ByteBuf) {

            final ByteBuf in = (ByteBuf) message;
            final int resourceOperationType = in.getInt(in.readerIndex() + Integer.BYTES);

            if (resourceOperationType != 0) {
                super.channelRead(context, message);
                return;
            }
        }

        context.fireChannelRead(message);
    }

    /**
     * Decode the input {@link ByteBuf} to an RntbdRequest instance
     * <p>
     * This method will be called till either the input {@link ByteBuf} has nothing to readTree after return from this
     * method or till nothing was readTree from the input {@link ByteBuf}.
     *
     * @param context the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in      the {@link ByteBuf} from which to readTree data
     * @param out     the {@link List} to which decoded messages should be added
     * @throws IllegalStateException thrown if an error occurs
     */
    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) throws IllegalStateException {

        final RntbdRequest request;
        in.markReaderIndex();

        try {
            request = RntbdRequest.decode(in);
        } catch (final IllegalStateException error) {
            in.resetReaderIndex();
            throw error;
        }

        in.discardReadBytes();
        out.add(request);
    }
}
