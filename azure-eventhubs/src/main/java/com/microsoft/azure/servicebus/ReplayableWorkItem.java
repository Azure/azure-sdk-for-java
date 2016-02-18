package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.message.Message;

public class ReplayableWorkItem<T> extends WorkItem<T>
{
	final private byte[] amqpMessage;
	final private int messageFormat;
	final private int encodedMessageSize;
	
	private Exception lastKnownException;
	
	public ReplayableWorkItem(final byte[] amqpMessage, final int encodedMessageSize, final int messageFormat, final CompletableFuture<T> completableFuture, final Duration timeout)
	{
		super(completableFuture, timeout);
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
}
