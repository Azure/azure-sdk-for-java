// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2PingFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test utility: counts HTTP/2 PING ACK frames received on the parent H2 channel.
 * Incoming PING ACKs prove that PING frames were sent by reactor-netty and acknowledged by the server.
 * Install on the parent channel (not stream channel) via doOnConnected.
 */
public class Http2PingFrameCounterHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Http2PingFrameCounterHandler.class);
    private final AtomicInteger pingAckCount = new AtomicInteger(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2PingFrame) {
            Http2PingFrame pingFrame = (Http2PingFrame) msg;
            if (pingFrame.ack()) {
                int count = pingAckCount.incrementAndGet();
                logger.info("PING ACK #{} received on channel {}", count, ctx.channel().id().asShortText());
            }
        }
        super.channelRead(ctx, msg);
    }

    public int getPingAckCount() {
        return pingAckCount.get();
    }
}
