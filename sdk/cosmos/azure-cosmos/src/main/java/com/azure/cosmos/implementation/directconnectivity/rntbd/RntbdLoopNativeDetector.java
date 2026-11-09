// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.channel.epoll.Epoll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a RntbdLoop instance based on the native transports.
 */
public class RntbdLoopNativeDetector {
    private static final Logger logger = LoggerFactory.getLogger(RntbdLoopNativeDetector.class);
    static final RntbdLoop NATIVE_INSTANCE;
    static final RntbdLoop DEFAULT_INSTANCE;

    static {
        DEFAULT_INSTANCE = new RntbdLoopNIO();

        RntbdLoop nativeLoop;
        try{
            Class.forName("io.netty.channel.epoll.Epoll");
            if (Epoll.isAvailable()) {
                nativeLoop = new RntbdLoopEpoll();
            } else {
                nativeLoop = new RntbdLoopNIO();
            }
        } catch (Exception e) {
            logger.warn("Failed for native transport checking, will fall back to NIO", e);
            nativeLoop = new RntbdLoopNIO();
        }

        NATIVE_INSTANCE = nativeLoop;
    }

    public static RntbdLoop getRntbdLoop(boolean preferTcpNative) {
        return preferTcpNative ? NATIVE_INSTANCE : DEFAULT_INSTANCE;
    }
}
