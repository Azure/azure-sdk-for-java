/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

/**
 * This exception is thrown for the following reasons:
 * <ul>
 * <li> When the entity user attempted to connect does not exist
 * <li> The entity user wants to connect is disabled
 * </ul>
 * <remark>For detail guidline on how to handle service bus exceptions please refer to http://go.microsoft.com/fwlink/?LinkId=761101</remark>
 */
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
