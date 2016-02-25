/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

public class ReceiverDisconnectedException extends ServiceBusException
{
	ReceiverDisconnectedException()
	{
		super(false);
	}

	ReceiverDisconnectedException(final String message)
	{
		super(false, message);
	}

	ReceiverDisconnectedException(final Throwable cause)
	{
		super(false, cause);
	}

	ReceiverDisconnectedException(final String message, final Throwable cause)
	{
		super(false, message, cause);
	}
}
