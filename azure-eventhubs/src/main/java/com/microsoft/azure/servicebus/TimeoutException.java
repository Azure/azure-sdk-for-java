/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

/**
 * This exception is thrown when the operation has exceeded the predetermined time limit.
 * User should check connectivity is healthy between client process and service.
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">http://go.microsoft.com/fwlink/?LinkId=761101</a>
 */
public class TimeoutException extends ServiceBusException
{
	private static final long serialVersionUID = -3505469991851121512L;

	/**
	 * Default constructor for exception type.
	 */
	public TimeoutException()
	{
		super(true);
	}

	public TimeoutException(final String message)
	{
		super(true, message);
	}

	public TimeoutException(final Throwable cause)
	{
		super(true, cause);
	}

	public TimeoutException(final String message, final Throwable cause)
	{
		super(true, message, cause);
	}
}
