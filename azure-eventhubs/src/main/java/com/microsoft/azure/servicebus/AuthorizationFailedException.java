/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import com.microsoft.azure.eventhubs.EventHubClient;

/**
 * Authorization failed exception is thrown when error is encountered during authorizing user's permission to run the intended operations.
 * When encountered this exception user should check whether the token/key provided in the connection string (e.g. one passed to 
 * {@link EventHubClient#createFromConnectionString(String)}) is valid, and has correct execution right for the intended operations (e.g. 
 * Receive call will need Listen claim associated with the key/token).
 * <remark>For detail guidline on how to handle service bus exceptions please refer to http://go.microsoft.com/fwlink/?LinkId=761101</remark>
 */
public class AuthorizationFailedException extends ServiceBusException
{
	private static final long serialVersionUID = 1L;

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
