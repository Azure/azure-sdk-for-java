/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

public class ReplayableWorkItem<T> extends WorkItem<T>
{
	private byte[] amqpMessage;
	private int messageFormat;
	private int encodedMessageSize;

	private Exception lastKnownException;
	private ScheduledFuture<?> timeoutTask;
	
	public ReplayableWorkItem(final byte[] amqpMessage, final int encodedMessageSize, final int messageFormat, final CompletableFuture<T> completableFuture, final Duration timeout)
	{
		super(completableFuture, timeout);
		this.initialize(amqpMessage, encodedMessageSize, messageFormat);
	}

	public ReplayableWorkItem(final byte[] amqpMessage, final int encodedMessageSize, final int messageFormat, final CompletableFuture<T> completableFuture, final TimeoutTracker timeout)
	{
		super(completableFuture, timeout);
		this.initialize(amqpMessage, encodedMessageSize, messageFormat);
	}

	private void initialize(final byte[] amqpMessage, final int encodedMessageSize, final int messageFormat)
	{
		this.amqpMessage = amqpMessage;
		this.messageFormat = messageFormat;
		this.encodedMessageSize = encodedMessageSize;
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

	public Exception getLastKnownException()
	{
		return this.lastKnownException;
	}

	public void setLastKnownException(Exception exception)
	{
		this.lastKnownException = exception;
	}
	
	public ScheduledFuture<?> getTimeoutTask()
	{
		return this.timeoutTask;
	}
	
	public void setTimeoutTask(final ScheduledFuture<?> timeoutTask)
	{
		this.timeoutTask = timeoutTask;
	}
}
