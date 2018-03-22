/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.message.Message;

import java.util.Map;

public interface ReceiverSettingsProvider {
    Map<Symbol, UnknownDescribedType> getFilter(final Message lastReceivedMessage);

    Map<Symbol, Object> getProperties();

    Symbol[] getDesiredCapabilities();
}
