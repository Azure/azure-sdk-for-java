/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

public class IllegalEntityException extends ServiceBusException 
{
	IllegalEntityException()
	{
		super(false);
	}

	IllegalEntityException(final String message)
	{
		super(false, message);
	}

	IllegalEntityException(final Throwable cause)
	{
		super(false, cause);
	}

	IllegalEntityException(final String message, final Throwable cause)
	{
		super(false, message, cause);
	}
}
