// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServerFactory {

    private final static ExecutorService executor = Executors.newFixedThreadPool(10);
    private final static Logger logger = LoggerFactory.getLogger(TcpServerFactory.class);

    public static TcpServer startNewRntbdServer(int port) {
        TcpServer server = new TcpServer(port);
        executor.execute(() -> {
            try {
                server.start();
            } catch (InterruptedException e) {
                logger.error("Failed to start server {}", e);
            }
        });

        // Change to only return server when server started.
        return server;
    }

    public static void shutdownRntbdServer(TcpServer server) {
        executor.execute(server::shutdown);
    }
}
