/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

public class EPHConfigurationException extends RuntimeException
{
	private static final long serialVersionUID = 7042035017304134956L;

	EPHConfigurationException()
	{
		super();
	}

	EPHConfigurationException(String message, Exception cause)
	{
		super(message, cause);
	}
	
	EPHConfigurationException(final String message)
	{
		super(message);
	}

	EPHConfigurationException(final Throwable cause)
	{
		super(cause);
	}
}
