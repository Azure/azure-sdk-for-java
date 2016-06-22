/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

/**
 * This exception is thrown when a EventHubReceiver is being disconnected because of one of the
 * following reason:
 * <ul>
 * <li> user attempts to connect a non-epoch receiver to a event hub partition, when there is an epoch receiver connected to the partition.
 * <li> you are using an epoch receiver for a given partition but another epoch receiver with a higher epoch value connects to the same partition.
 * </ul>
 * User should make sure either all code are using non-epoch receivers, or ensure that there is only one epoch receiver processing a given partition 
 * at any given point in time. 
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">http://go.microsoft.com/fwlink/?LinkId=761101</a>
 */
public class ReceiverDisconnectedException extends ServiceBusException
{
	private static final long serialVersionUID = 3385140843418138213L;

	ReceiverDisconnectedException()
	{
		super(false);
	}

	ReceiverDisconnectedException(final String message)
	{
		super(false, message);
	}

	ReceiverDisconnectedException(final Throwable cause)
	{
		super(false, cause);
	}

	ReceiverDisconnectedException(final String message, final Throwable cause)
	{
		super(false, message, cause);
	}
}
