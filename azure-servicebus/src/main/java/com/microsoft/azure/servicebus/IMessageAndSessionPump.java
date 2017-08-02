// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

interface IMessageAndSessionPump
{
	public void registerMessageHandler(IMessageHandler handler) throws InterruptedException, ServiceBusException;
	
	public void registerMessageHandler(IMessageHandler handler, MessageHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException;
	
	public void registerSessionHandler(ISessionHandler handler) throws InterruptedException, ServiceBusException;
	
	public void registerSessionHandler(ISessionHandler handler, SessionHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException;
	
    void abandon(UUID lockToken) throws InterruptedException, ServiceBusException;

    void abandon(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;
    
    CompletableFuture<Void> abandonAsync(UUID lockToken);

    CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify);
    
    void complete(UUID lockToken) throws InterruptedException, ServiceBusException;
    
    CompletableFuture<Void> completeAsync(UUID lockToken);
    
//    void defer(UUID lockToken) throws InterruptedException, ServiceBusException;
//
//    void defer(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> deferAsync(UUID lockToken);

    CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify);
    
    void deadLetter(UUID lockToken) throws InterruptedException, ServiceBusException;

    void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) throws InterruptedException, ServiceBusException;
    
    void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> deadLetterAsync(UUID lockToken);

    CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription);
    
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify);
    
    int getPrefetchCount();
    
    void setPrefetchCount(int prefetchCount) throws ServiceBusException;
}
