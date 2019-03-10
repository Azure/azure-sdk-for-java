/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Link;

public interface AmqpConnection {

    /**
     * Host name intended to be used on Amqp Connection Open frame
     * @return host name
     */
    String getHostName();

    void onOpenComplete(Exception exception);

    void onConnectionError(ErrorCondition error);

    void registerForConnectionError(Link link);

    void deregisterForConnectionError(Link link);
}
