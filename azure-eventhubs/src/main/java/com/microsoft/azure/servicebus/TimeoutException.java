/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

public class TimeoutException extends ServiceBusException
{
	private static final long serialVersionUID = 1L;

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
