/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

/**
 * This exception is thrown when there is a client side connectivity issue. When receiving this exception user should
 * check client connectivity settings to the service:
 * <ul>
 * <li> Check for any possible proxy settings that can block amqp ports
 * <li> Check for any firewall settings that can block amqp ports
 * <li> Check for any general network connectivity issues, as well as network latency.
 * </ul>
 * <remark>For detail guidline on how to handle service bus exceptions please refer to http://go.microsoft.com/fwlink/?LinkId=761101</remark>
 */
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
