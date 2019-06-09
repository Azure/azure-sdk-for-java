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
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final public class RntbdRequestEncoder extends MessageToByteEncoder {

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
    public boolean acceptOutboundMessage(Object message) {
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
    protected void encode(ChannelHandlerContext context, Object message, ByteBuf out) throws Exception {

        RntbdRequest request = RntbdRequest.from((RntbdRequestArgs)message);
        int start = out.writerIndex();

        try {
            request.encode(out);
        } catch (Throwable error) {
            out.writerIndex(start);
            throw error;
        }

        if (logger.isDebugEnabled()) {
            int length = out.writerIndex() - start;
            logger.debug("{}: ENCODE COMPLETE: length={}, request={}", context.channel(), length, request);
        }
    }
}
