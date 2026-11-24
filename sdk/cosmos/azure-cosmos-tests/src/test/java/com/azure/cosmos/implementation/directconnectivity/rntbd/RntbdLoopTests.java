// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SingleThreadIoEventLoop;
import io.netty.channel.epoll.Epoll;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RntbdLoopTests {

    @Test(groups = { "unit" })
    public void nioChannelRegistration() throws Exception {
        RntbdLoop loop = RntbdLoopNativeDetector.getRntbdLoop(false);
        assertThat(loop.getName()).isEqualTo("nio");
        verifyChannelRegistration(loop);
    }

    @Test(groups = { "unit" })
    public void preferredTransportChannelRegistration() throws Exception {
        RntbdLoop loop = RntbdLoopNativeDetector.getRntbdLoop(true);
        assertThat(loop.getName()).isEqualTo(Epoll.isAvailable() ? "epoll" : "nio");
        verifyChannelRegistration(loop);
    }

    @Test(groups = { "unit" })
    public void epollChannelRegistration() throws Exception {
        if (!Epoll.isAvailable()) {
            throw new SkipException("Epoll is not available on this platform", Epoll.unavailabilityCause());
        }
        verifyChannelRegistration(new RntbdLoopEpoll());
    }

    private static void verifyChannelRegistration(RntbdLoop loop) throws Exception {
        String threadName = "rntbd-loop-test-" + loop.getName();
        EventLoopGroup group = loop.newEventLoopGroup(1, new DefaultThreadFactory(threadName));
        Channel channel = null;
        try {
            assertThat(group).isInstanceOf(MultiThreadIoEventLoopGroup.class);
            assertThat(((MultiThreadIoEventLoopGroup) group).executorCount()).isEqualTo(1);
            channel = new Bootstrap()
                .group(group)
                .channel(loop.getChannelClass())
                .handler(new ChannelInboundHandlerAdapter())
                .register().sync().channel();

            assertThat(channel.isRegistered()).isTrue();
            assertThat(channel).isInstanceOf(loop.getChannelClass());
            assertThat(channel.eventLoop()).isInstanceOf(SingleThreadIoEventLoop.class);
            assertThat(RntbdUtils.tryGetExecutorTaskQueueSize(channel.eventLoop())).isGreaterThanOrEqualTo(0);
            assertThat(channel.eventLoop().submit(() -> Thread.currentThread().getName()).get(10, TimeUnit.SECONDS))
                .startsWith(threadName);
            assertThat(channel.eventLoop().schedule(() -> true, 1, TimeUnit.MILLISECONDS).get(10, TimeUnit.SECONDS))
                .isTrue();
        } finally {
            if (channel != null) {
                channel.close().sync();
            }
            group.shutdownGracefully(0, 5, TimeUnit.SECONDS).sync();
        }
    }
}
