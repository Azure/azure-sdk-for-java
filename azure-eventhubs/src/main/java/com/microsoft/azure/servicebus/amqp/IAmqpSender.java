/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.transport.*;

public interface IAmqpSender extends IAmqpLink
{
	void onSendComplete(final byte[] deliveryTag, final DeliveryState outcome);
}
