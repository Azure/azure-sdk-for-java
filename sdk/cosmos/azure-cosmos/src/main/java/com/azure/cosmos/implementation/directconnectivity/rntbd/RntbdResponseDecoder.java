// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class RntbdResponseDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(RntbdResponseDecoder.class);

    /**
     * Deserialize from an input {@link ByteBuf} to an {@link RntbdResponse} instance.
     * <p>
     * This method is called till it reads no bytes from the {@link ByteBuf} or there is no more data to be read.
     *
     * @param context the {@link ChannelHandlerContext} to which this {@link RntbdResponseDecoder} belongs.
     * @param in the {@link ByteBuf} to which data to be decoded is read.
     * @param out the {@link List} to which decoded messages are added.
     */
    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {

        if (RntbdFramer.canDecodeHead(in)) {

            final RntbdResponse response = RntbdResponse.decode(in);

            if (response != null) {
                logger.debug("{} DECODE COMPLETE: {}", context.channel(), response);
                in.discardReadBytes();
                out.add(response.retain());
            }
        }
    }
}
