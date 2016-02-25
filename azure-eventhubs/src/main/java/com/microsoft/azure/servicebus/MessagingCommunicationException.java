/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

public class MessagingCommunicationException extends ServiceBusException
{
	MessagingCommunicationException()
	{
		super(true);
	}
	
	MessagingCommunicationException(final String message)
	  {
	    super(true, message);
	  }
	
	MessagingCommunicationException(final Throwable cause)
	  {
	    super(true, cause);
	  }
	
	MessagingCommunicationException(final String message, final Throwable cause)
	  {
	    super(true, message, cause);
	  }
}
