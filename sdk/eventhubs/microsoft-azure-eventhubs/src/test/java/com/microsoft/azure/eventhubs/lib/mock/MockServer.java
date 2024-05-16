// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.lib.mock;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.reactor.Acceptor;
import org.apache.qpid.proton.reactor.Reactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Mock Server (Singleton) designed to test AMQP related features in the Java client.
 */
public final class MockServer implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(MockServer.class);

    private static final String HOST_NAME = "127.0.0.1";
    private static final int PORT = 5671;

    private Reactor reactor;
    private Acceptor acceptor;

    private MockServer(BaseHandler handler) throws IOException {
        this.reactor = Proton.reactor();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (logger.isTraceEnabled()) {
                    logger.trace("starting reactor instance.");
                }

                reactor.run();
            }
        }).start();

        this.acceptor = this.reactor.acceptor(MockServer.HOST_NAME, MockServer.PORT,
                handler == null ? new ServerTraceHandler() : handler);
    }

    public static MockServer create(BaseHandler handler) throws IOException {
        return new MockServer(handler);
    }

    @Override
    public void close() {
        if (this.acceptor != null) {
            this.acceptor.close();
        }
    }
}
