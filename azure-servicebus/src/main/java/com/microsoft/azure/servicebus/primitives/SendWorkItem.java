/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class SendWorkItem<T> extends WorkItem<T>
{
	private byte[] amqpMessage;
	private int messageFormat;
	private int encodedMessageSize;
	private boolean waitingForAck;
	private String deliveryTag;
	
	public SendWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, String deliveryTag, CompletableFuture<T> completableFuture, Duration timeout)
	{
		super(completableFuture, timeout);
		this.initialize(amqpMessage, encodedMessageSize, messageFormat, deliveryTag);
	}

	public SendWorkItem(byte[] amqpMessage, int encodedMessageSize, int messageFormat, String deliveryTag, CompletableFuture<T> completableFuture, TimeoutTracker timeout)
	{
		super(completableFuture, timeout);
		this.initialize(amqpMessage, encodedMessageSize, messageFormat, deliveryTag);
	}

	private void initialize(byte[] amqpMessage, int encodedMessageSize, int messageFormat, String deliveryTag)
	{
		this.amqpMessage = amqpMessage;
		this.messageFormat = messageFormat;
		this.encodedMessageSize = encodedMessageSize;
		this.deliveryTag = deliveryTag;
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
}
