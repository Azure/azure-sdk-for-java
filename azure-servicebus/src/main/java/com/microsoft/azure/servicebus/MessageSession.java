// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.net.URI;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class MessageSession extends MessageReceiver implements IMessageSession {
    private String requestedSessionId;
    
    MessageSession(URI namespaceEndpointURI, String entityPath, String requestedSessionId, ClientSettings clientSettings, ReceiveMode receiveMode) {
        super(namespaceEndpointURI, entityPath, clientSettings, receiveMode);
        this.requestedSessionId = requestedSessionId;
    }

    MessageSession(MessagingFactory messagingFactory, String entityPath, String requestedSessionId, ReceiveMode receiveMode) {
        super(messagingFactory, entityPath, receiveMode);
        this.requestedSessionId = requestedSessionId;
    }

    @Override
    protected final boolean isSessionReceiver() {
        return true;
    }

    @Override
    protected boolean isBrowsableSession() {
        return false;
    }

    @Override
    protected String getRequestedSessionId() {
        return this.requestedSessionId;
    }

    @Override
    public Instant getLockedUntilUtc() {
        return this.getInternalReceiver().getSessionLockedUntilUtc();
    }

    @Override
    public void renewSessionLock() throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.renewSessionLockAsync());
    }

    @Override
    public CompletableFuture<Void> renewSessionLockAsync() {
        return this.getInternalReceiver().renewSessionLocksAsync();
    }

    @Override
    public void setState(byte[] sessionState) throws InterruptedException, ServiceBusException {
        Utils.completeFuture(this.setStateAsync(sessionState));
    }

    @Override
    public CompletableFuture<Void> setStateAsync(byte[] sessionState) {
        return this.getInternalReceiver().setSessionStateAsync(sessionState);
    }

    @Override
    public byte[] getState() throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(this.getStateAsync());
    }

    @Override
    public CompletableFuture<byte[]> getStateAsync() {
        return this.getInternalReceiver().getSessionStateAsync();
    }

    @Override
    public String getSessionId() {
        return this.getInternalReceiver().getSessionId();
    }
}
