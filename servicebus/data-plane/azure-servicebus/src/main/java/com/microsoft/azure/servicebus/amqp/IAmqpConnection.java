// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Link;

public interface IAmqpConnection {
    String getHostName();

    void onConnectionOpen();

    void onConnectionError(ErrorCondition error);

    void registerForConnectionError(Link link);

    void deregisterForConnectionError(Link link);
}
