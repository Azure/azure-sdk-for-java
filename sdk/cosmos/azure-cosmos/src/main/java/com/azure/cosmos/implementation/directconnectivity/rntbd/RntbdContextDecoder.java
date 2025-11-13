// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

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

        // BREADCRUMB: Track buffer at decode entry
        if (logger.isTraceEnabled()) {
            in.touch("RntbdContextDecoder.decode: entry");
            logger.trace("{} RntbdContextDecoder.decode: ByteBuf refCnt={}, readableBytes={}", 
                context.channel(), in.refCnt(), in.readableBytes());
        }

        if (RntbdFramer.canDecodeHead(in)) {

            Object result;

            try {
                final RntbdContext rntbdContext = RntbdContext.decode(in);
                context.fireUserEventTriggered(rntbdContext);
                result = rntbdContext;
                
                if (logger.isTraceEnabled()) {
                    logger.trace("{} RntbdContextDecoder: decoded RntbdContext successfully", context.channel());
                }
            } catch (RntbdContextException error) {
                context.fireUserEventTriggered(error);
                result = error;
                
                if (logger.isTraceEnabled()) {
                    logger.trace("{} RntbdContextDecoder: caught RntbdContextException", context.channel(), error);
                }
            } finally {
                // BREADCRUMB: Track buffer before discard
                if (logger.isTraceEnabled()) {
                    in.touch("RntbdContextDecoder.decode: before discardReadBytes in finally block");
                    logger.trace("{} RntbdContextDecoder: discarding read bytes in finally block", context.channel());
                }
                in.discardReadBytes();
            }

            logger.debug("{} DECODE COMPLETE: {}", context.channel(), result);
        } else if (logger.isTraceEnabled()) {
            logger.trace("{} RntbdContextDecoder: cannot decode head yet, readableBytes={}", 
                context.channel(), in.readableBytes());
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // BREADCRUMB: Track exceptions that might lead to leaked buffers
        logger.warn("{} RntbdContextDecoder.exceptionCaught: {}", ctx.channel(), cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }
}
