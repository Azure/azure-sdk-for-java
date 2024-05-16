// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.message.Message;

import java.util.Map;

public interface ReceiverSettingsProvider {
    Map<Symbol, UnknownDescribedType> getFilter(Message lastReceivedMessage);

    Map<Symbol, Object> getProperties();

    Symbol[] getDesiredCapabilities();
}
