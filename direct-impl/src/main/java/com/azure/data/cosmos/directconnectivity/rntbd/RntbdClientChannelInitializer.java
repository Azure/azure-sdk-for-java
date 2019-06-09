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

import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.directconnectivity.RntbdTransportClient.Options;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;
import java.util.Objects;

final public class RntbdClientChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private final LogLevel logLevel;
    private final Options options;
    private final RntbdRequestManager requestManager;
    private final SslContext sslContext;
    private final UserAgentContainer userAgent;

    public RntbdClientChannelInitializer(
        UserAgentContainer userAgent,
        SslContext sslContext,
        LogLevel logLevel,
        Options options
    ) {

        Objects.requireNonNull(sslContext, "sslContext");
        Objects.requireNonNull(userAgent, "userAgent");
        Objects.requireNonNull(options, "options");

        this.requestManager = new RntbdRequestManager();
        this.sslContext = sslContext;
        this.userAgent = userAgent;
        this.logLevel = logLevel;
        this.options = options;
    }

    public RntbdRequestManager getRequestManager() {
        return this.requestManager;
    }

    @Override
    protected void initChannel(NioSocketChannel channel) {

        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addFirst(
            new RntbdContextNegotiator(this.requestManager, this.userAgent),
            new RntbdResponseDecoder(),
            new RntbdRequestEncoder(),
            this.requestManager
        );

        if (this.logLevel != null) {
            pipeline.addFirst(new LoggingHandler(this.logLevel));
        }

        final int readerIdleTime = (int)this.options.getReceiveHangDetectionTime().toNanos();
        final int writerIdleTime = (int)this.options.getSendHangDetectionTime().toNanos();
        final SSLEngine sslEngine = this.sslContext.newEngine(channel.alloc());

        pipeline.addFirst(
            // TODO: DANOBLE: Utilize READ/WriteTimeoutHandler for receive/send hang detection
            //  Links:
            //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/331552
            //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/331593
            //  Notes:
            //  First (naive?) attempt caused performance degradation
            //  new WriteTimeoutHandler(writerIdleTime),
            //  new ReadTimeoutHandler(readerIdleTime),
            new SslHandler(sslEngine)
        );
    }
}
