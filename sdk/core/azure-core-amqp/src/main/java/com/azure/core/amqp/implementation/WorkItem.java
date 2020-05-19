// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.amqp.transport.DeliveryState;

public abstract class WorkItem {
    abstract boolean hasBeenRetried();
    abstract int getMessageFormat();
    abstract byte[] getMessage();
    abstract int getEncodedMessageSize();
    abstract void setWaitingForAck();
    abstract void success(DeliveryState delivery);
    abstract void error(Throwable error);
    abstract TimeoutTracker getTimeoutTracker();
    abstract void setLastKnownException(Exception exception);
}
