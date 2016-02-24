/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
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
