// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Session;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SessionProvider {
    Session getSession(
            String path,
            Consumer<Session> onSessionOpen,
            BiConsumer<ErrorCondition, Exception> onSessionOpenError);
}
