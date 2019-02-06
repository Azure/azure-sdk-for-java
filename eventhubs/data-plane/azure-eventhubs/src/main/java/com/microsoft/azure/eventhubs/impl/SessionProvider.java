/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Session;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SessionProvider {
    Session getSession(
            final String path,
            final Consumer<Session> onSessionOpen,
            final BiConsumer<ErrorCondition, Exception> onSessionOpenError);
}