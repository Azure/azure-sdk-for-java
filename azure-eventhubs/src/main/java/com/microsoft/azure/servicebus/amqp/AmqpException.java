/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.transport.*;

/**
 * All AmqpExceptions - which EventHub client handles internally. 
 */
public class AmqpException extends Exception
{
	private ErrorCondition errorCondition;

	public AmqpException(ErrorCondition errorCondition)
	{
		super(errorCondition.getDescription());
		this.errorCondition = errorCondition;
	}

	public ErrorCondition getError()
	{
		return this.errorCondition;
	}
}
