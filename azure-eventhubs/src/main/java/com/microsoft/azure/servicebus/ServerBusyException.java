/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

/**
 * Server busy exception is thrown when the current entity's activity has put excessive load onto the service.
 * When encountered this exception user should wait at least 4 seconds before any retry/runtime operations for the said entity again.
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">http://go.microsoft.com/fwlink/?LinkId=761101</a>
 */
public class ServerBusyException extends ServiceBusException 
{
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor for the exception
	 */
	public ServerBusyException()
	{
		super(true);
	}

	ServerBusyException(final String message)
	{
		super(true, message);
	}

	ServerBusyException(final Throwable cause)
	{
		super(true, cause);
	}

	ServerBusyException(final String message, final Throwable cause)
	{
		super(true, message, cause);
	}
}
