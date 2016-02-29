/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

public class ServerBusyException extends ServiceBusException 
{
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
