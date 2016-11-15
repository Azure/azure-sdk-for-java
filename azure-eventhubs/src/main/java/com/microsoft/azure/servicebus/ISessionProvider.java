/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.util.function.Consumer;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Session;

interface ISessionProvider
{
	Session getSession(
                final String path,
                final String sessionId,
                final Consumer<Session> onSessionOpen,
                final Consumer<ErrorCondition> onSessionOpenError);
}