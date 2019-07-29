// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

import java.io.IOException;

public final class ProtonUtil {

    private ProtonUtil() {
    }

    public static Reactor reactor(final ReactorHandler reactorHandler, final int maxFrameSize, final String name) throws IOException {

        final ReactorOptions reactorOptions = new ReactorOptions();
        reactorOptions.setMaxFrameSize(maxFrameSize);
        reactorOptions.setEnableSaslByDefault(true);

        final Reactor reactor = Proton.reactor(reactorOptions, reactorHandler);
        reactor.setGlobalHandler(new CustomIOHandler(name));

        return reactor;
    }
}
