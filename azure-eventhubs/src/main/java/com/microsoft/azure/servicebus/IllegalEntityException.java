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
 * 
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">http://go.microsoft.com/fwlink/?LinkId=761101</a>
 */
public class IllegalEntityException extends ServiceBusException 
{
	private static final long serialVersionUID = 1842057379278310290L;

	IllegalEntityException()
	{
		super(false);
	}

	public IllegalEntityException(final String message)
	{
		super(false, message);
	}

	public IllegalEntityException(final Throwable cause)
	{
		super(false, cause);
	}

	public IllegalEntityException(final String message, final Throwable cause)
	{
		super(false, message, cause);
	}
}
