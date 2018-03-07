/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

import java.io.IOException;

public final class ProtonUtil {

    private ProtonUtil() {
    }

    public static Reactor reactor(ReactorHandler reactorHandler) throws IOException {

        final ReactorOptions reactorOptions = new ReactorOptions();
        reactorOptions.setMaxFrameSize(AmqpConstants.MAX_FRAME_SIZE);
        reactorOptions.setEnableSaslByDefault(true);

        final Reactor reactor = Proton.reactor(reactorOptions, reactorHandler);
        reactor.setGlobalHandler(new CustomIOHandler());

        return reactor;
    }
}
