/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

public class AuthorizationFailedException extends ServiceBusException
{
	private static final long serialVersionUID = 1L;

	AuthorizationFailedException()
	{
		super(false);
	}

	public AuthorizationFailedException(final String message)
	{
		super(false, message);
	}

	AuthorizationFailedException(final Throwable cause)
	{
		super(false, cause);
	}

	AuthorizationFailedException(final String message, final Throwable cause)
	{
		super(false, message, cause);
	}
}
