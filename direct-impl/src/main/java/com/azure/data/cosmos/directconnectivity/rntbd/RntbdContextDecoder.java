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

class RntbdContextDecoder extends ByteToMessageDecoder {

    final private static Logger logger = LoggerFactory.getLogger("RntbdContextDecoder");

    /**
     * Deserialize from an input {@link ByteBuf} to an {@link RntbdContext} instance
     * <p>
     * This method fulfills the promise of an {@link RntbdContext} instance. It does not pass the instance down the
     * pipeline.
     *
     * @param context the {@link ChannelHandlerContext} to which this {@link ByteToMessageDecoder} belongs
     * @param in      the {@link ByteBuf} from which to readTree data
     * @param out     the {@link List} to which decoded messages should be added
     * @throws Exception thrown if an error occurs
     */
    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception {

        if (RntbdFramer.canDecodeHead(in)) {

            RntbdContext rntbdContext = RntbdContext.decode(in);
            context.fireUserEventTriggered(rntbdContext);
            in.discardReadBytes();

            logger.debug("{} DECODE COMPLETE: {}", context.channel(), rntbdContext);
        }
    }
}
