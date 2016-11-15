/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.util.Map;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.message.Message;

public interface IReceiverSettingsProvider
{
	public Map<Symbol, UnknownDescribedType> getFilter(final Message lastReceivedMessage);

	public Map<Symbol, Object> getProperties();
}
