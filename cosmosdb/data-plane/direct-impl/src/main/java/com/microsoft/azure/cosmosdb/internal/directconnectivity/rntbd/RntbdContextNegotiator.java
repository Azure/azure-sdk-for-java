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

package com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd;

import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import com.microsoft.azure.cosmosdb.internal.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CombinedChannelDuplexHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class RntbdContextNegotiator extends CombinedChannelDuplexHandler<RntbdContextDecoder, RntbdContextRequestEncoder> {

    private static final Logger logger = LoggerFactory.getLogger(RntbdContextNegotiator.class);
    private final RntbdRequestManager manager;
    private final UserAgentContainer userAgent;

    private volatile boolean pendingRntbdContextRequest = true;

    public RntbdContextNegotiator(final RntbdRequestManager manager, final UserAgentContainer userAgent) {

        super(new RntbdContextDecoder(), new RntbdContextRequestEncoder());

        checkNotNull(manager, "manager");
        checkNotNull(userAgent, "userAgent");

        this.manager = manager;
        this.userAgent = userAgent;
    }

    /**
     * Called once a write operation is made. The write operation will write the messages through the
     * {@link ChannelPipeline}. Those are then ready to be flushed to the actual {@link Channel} once
     * {@link Channel#flush()} is called
     *
     * @param context the {@link ChannelHandlerContext} for which the write operation is made
     * @param message the message to write
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void write(
        final ChannelHandlerContext context, final Object message, final ChannelPromise promise
    ) throws Exception {

        checkArgument(message instanceof ByteBuf, "message: %s", message.getClass());
        final ByteBuf out = (ByteBuf)message;

        if (this.manager.hasRntbdContext()) {
            context.writeAndFlush(out, promise);
        } else {
            if (this.pendingRntbdContextRequest) {
                // Thread safe: netty guarantees that no channel handler methods are called concurrently
                this.startRntbdContextRequest(context);
                this.pendingRntbdContextRequest = false;
            }
            this.manager.pendWrite(out, promise);
        }
    }

    // region Privates

    private void startRntbdContextRequest(final ChannelHandlerContext context) throws Exception {

        logger.debug("{} START CONTEXT REQUEST", context.channel());

        final Channel channel = context.channel();
        final RntbdContextRequest request = new RntbdContextRequest(Utils.randomUUID(), this.userAgent);
        final CompletableFuture<RntbdContextRequest> contextRequestFuture = this.manager.getRntbdContextRequestFuture();

        super.write(context, request, channel.newPromise().addListener((ChannelFutureListener)future -> {

            if (future.isSuccess()) {
                contextRequestFuture.complete(request);
                return;
            }

            if (future.isCancelled()) {
                contextRequestFuture.cancel(true);
                return;
            }

            contextRequestFuture.completeExceptionally(future.cause());
        }));
    }

    // endregion
}
