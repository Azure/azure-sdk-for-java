/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.message.Message;

public interface IAmqpReceiver extends IAmqpLink
{
	void onReceiveComplete(Delivery delivery);
}
