// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.jproxy;

import java.io.IOException;
import java.util.function.Consumer;

public interface ProxyServer {

    static ProxyServer create(final String hostName, final int port) {
        return new SimpleProxy(hostName, port);
    }

    void start(Consumer<Throwable> onError) throws IOException;

    void stop() throws IOException;
}
