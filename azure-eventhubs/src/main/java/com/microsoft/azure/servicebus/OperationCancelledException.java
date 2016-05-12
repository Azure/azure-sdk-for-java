/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

public class OperationCancelledException extends ServiceBusException
{
	private static final long serialVersionUID = 1L;

	OperationCancelledException()
	{
		super(false);
	}

	public OperationCancelledException(final String message)
	{
		super(false, message);
	}

	OperationCancelledException(final Throwable cause)
	{
		super(false, cause);
	}

	OperationCancelledException(final String message, final Throwable cause)
	{
		super(false, message, cause);
	}
}
