// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.lib.mock;

import com.microsoft.azure.eventhubs.lib.TestBase;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.reactor.Acceptor;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;

/**
 * mock Server (Singleton) designed to test AMQP related features in the javaClient
 */
public class MockServer implements Closeable {
    private final static String HOST_NAME = "127.0.0.1";
    private final static int PORT = 5671;

    private Reactor reactor;
    private Acceptor acceptor;

    private MockServer(BaseHandler handler) throws IOException {
        this.reactor = Proton.reactor();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (TestBase.TEST_LOGGER.isLoggable(Level.FINE)) {
                    TestBase.TEST_LOGGER.log(Level.FINE, "starting reactor instance.");
                }

                reactor.run();
            }
        }).start();

        this.acceptor = this.reactor.acceptor(MockServer.HOST_NAME, MockServer.PORT,
                handler == null ? new ServerTraceHandler() : handler);
    }

    public static MockServer create(BaseHandler handler) throws IOException {
        MockServer server = new MockServer(handler);
        return server;
    }

    @Override
    public void close() {
        if (this.acceptor != null) {
            this.acceptor.close();
        }
    }
}
