/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import com.microsoft.azure.servicebus.TransactionContext;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class SendWorkItem<T> extends WorkItem<T>
{
	private byte[] amqpMessage;
	private int messageFormat;
	private int encodedMessageSize;
	private boolean waitingForAck;
	private String deliveryTag;
	private TransactionContext transaction;
	
	public SendWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, String deliveryTag, TransactionContext transaction, CompletableFuture<T> completableFuture, Duration timeout)
	{
		super(completableFuture, timeout);
		this.initialize(amqpMessage, encodedMessageSize, messageFormat, deliveryTag, transaction);
	}

	public SendWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, String deliveryTag, TransactionContext transaction, CompletableFuture<T> completableFuture, TimeoutTracker timeout)
	{
		super(completableFuture, timeout);
		this.initialize(amqpMessage, encodedMessageSize, messageFormat, deliveryTag, transaction);
	}

	private void initialize(byte[] amqpMessage, int encodedMessageSize, int messageFormat, String deliveryTag, TransactionContext transaction)
	{
		this.amqpMessage = amqpMessage;
		this.messageFormat = messageFormat;
		this.encodedMessageSize = encodedMessageSize;
		this.deliveryTag = deliveryTag;
		this.transaction = transaction;
	}

	public byte[] getMessage()
	{
		return this.amqpMessage;
	}

	public int getEncodedMessageSize()
	{
		return this.encodedMessageSize;
	}

	public int getMessageFormat()
	{
		return this.messageFormat;
	}		
	
	public void setWaitingForAck()
	{
		this.waitingForAck = true;
	}
	
	public boolean isWaitingForAck()
	{
		return this.waitingForAck;
	}
	
	public String getDeliveryTag()
	{
	    return this.deliveryTag;
	}
	
	public void setDeliveryTag(String deliveryTag)
	{
	    this.deliveryTag = deliveryTag;
	}

	public TransactionContext getTransaction() { return this.transaction; }

	public void setTransaction(TransactionContext txnId) { this.transaction = txnId; }
}
