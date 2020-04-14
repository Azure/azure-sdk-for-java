// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

/***
 *  This will support one named session or one next available session. If there is no available session, the publisher
 *  will terminate with error.
 */
public class ServiceBusSessionReceiverClient extends ServiceBusReceiverClient{
    /**
     * Creates a synchronous receiver given its asynchronous counterpart.
     * @param asyncClient Asynchronous receiver.
     * @param operationTimeout
     */
    ServiceBusSessionReceiverClient(ServiceBusReceiverAsyncClient asyncClient, Duration operationTimeout) {
        super(asyncClient, operationTimeout);
    }

    public Instant renewSessionLock(MessageLockToken lockToken){return null;};
    public ByteBuffer getSessionState() {return null;}
    public Void setSessionState(ByteBuffer sessionState) {return null;}
    public String getSessionId(){ return null;}
    public Instant getSessionLockedUntil() {return null;}
}
