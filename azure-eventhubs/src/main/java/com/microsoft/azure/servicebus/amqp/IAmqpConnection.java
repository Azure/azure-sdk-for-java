/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Link;

public interface IAmqpConnection
{
	void onOpenComplete(Exception exception);
	
	void onConnectionError(ErrorCondition error);
	
	void registerForConnectionError(Link link);
	
	void deregisterForConnectionError(Link link);
}
