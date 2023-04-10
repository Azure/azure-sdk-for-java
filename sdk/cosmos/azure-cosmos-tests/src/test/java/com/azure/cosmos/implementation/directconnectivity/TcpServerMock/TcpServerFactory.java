// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServerFactory {

    private final static ExecutorService executor = Executors.newFixedThreadPool(10);
    private final static Logger logger = LoggerFactory.getLogger(TcpServerFactory.class);

    public static TcpServer startNewRntbdServer(int port) throws ExecutionException, InterruptedException {
        TcpServer server = new TcpServer(port);

        Promise<Boolean> promise = new DefaultPromise<Boolean>(GlobalEventExecutor.INSTANCE);
        executor.execute(() -> {
            try {
                server.start(promise);
            } catch (InterruptedException e) {
                logger.error("Failed to start server {}", e);
            }
        });

        // only return server when server has started successfully.
        promise.get();
        return server;
    }

    public static void shutdownRntbdServer(TcpServer server) throws ExecutionException, InterruptedException {
        Promise<Boolean> promise = new DefaultPromise<Boolean>(GlobalEventExecutor.INSTANCE);
        executor.execute(() -> server.shutdown(promise));
        // only return when server has shutdown.
        promise.get();
        return;
    }
}
