// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.Configs;
import io.netty.channel.epoll.Epoll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a RntbdLoop instance based on the native transports.
 */
public class RntbdEventLoopNativeDetector {
    private static final Logger logger = LoggerFactory.getLogger(RntbdEventLoopNativeDetector.class);
    static final RntbdEventLoop INSTANCE;

    static {
        RntbdEventLoop defaultLoop;
        try{
            Class.forName("io.netty.channel.epoll.Epoll");
            if (Epoll.isAvailable() && Configs.useTcpNative()) {
                defaultLoop = new RntbdEventLoopEpoll();
            } else {
                defaultLoop = new RntbdEventLoopNIO();
            }
        } catch (Exception e) {
            logger.warn("Failed for native transport checking, will fall back to NIO", e);
            defaultLoop = new RntbdEventLoopNIO();
        }

        INSTANCE = defaultLoop;
    }
}
