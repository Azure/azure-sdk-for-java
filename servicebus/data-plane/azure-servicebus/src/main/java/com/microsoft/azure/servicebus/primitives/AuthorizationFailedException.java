/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

/**
 * Authorization failed exception is thrown when error is encountered during authorizing user's permission to run the intended operations.
 * When encountered this exception user should check whether the token/key provided in the connection string is valid, and has correct 
 * execution right for the intended operations (e.g. Receive call will need Listen claim associated with the key/token).
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">http://go.microsoft.com/fwlink/?LinkId=761101</a>
 * @since 1.0
 */
public class AuthorizationFailedException extends ServiceBusException
{
	private static final long serialVersionUID = 5384872132102860710L;

	AuthorizationFailedException()
	{
		super(false);
	}

	/**
	 * Constructor for the exception class
	 * @param message the actual error message detailing the reason for the failure
	 */
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
