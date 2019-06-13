/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.azure.data.cosmos.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class RntbdResponseDecoder extends ByteToMessageDecoder {

    private static final Logger Logger = LoggerFactory.getLogger(RntbdResponseDecoder.class);

    /**
     * Deserialize from an input {@link ByteBuf} to an {@link RntbdResponse} instance
     * <p>
     * This method is called till it reads no bytes from the {@link ByteBuf} or there is no more data to be readTree.
     *
     * @param context the {@link ChannelHandlerContext} to which this {@link RntbdResponseDecoder} belongs
     * @param in      the {@link ByteBuf} to which data to be decoded is readTree
     * @param out     the {@link List} to which decoded messages are added
     */
    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {

        if (RntbdFramer.canDecodeHead(in)) {

            final RntbdResponse response = RntbdResponse.decode(in);

            if (response != null) {
                Logger.debug("{} DECODE COMPLETE: {}", context.channel(), response);
                in.discardReadBytes();
                out.add(response.retain());
            }
        }
    }
}
