// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.useragent.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.netty.resources.LoopResources;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class NettyCompatibilityTests {

    @ParameterizedTest
    @ValueSource(
        strings = {
            "io.netty.channel.MultiThreadIoEventLoopGroup",
            "io.netty.channel.epoll.EpollIoHandler",
            "io.netty.channel.kqueue.KQueueIoHandler" })
    void transportClassesAreAvailable(String className) throws ClassNotFoundException {
        assertThat(Class.forName(className, false, getClass().getClassLoader())).isNotNull();
    }

    @Test
    void nioEventLoopsAreUsable() throws Exception {
        LoopResources loops = LoopResources.create("azure-spring-netty-test", 1, true);
        try {
            assertThat(loops.onClient(false).submit(() -> true).get(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            loops.disposeLater().block(Duration.ofSeconds(10));
        }
    }
}
