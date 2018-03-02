/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Delivery;

public interface AmqpSender extends AmqpLink {
    void onFlow(final int creditIssued);

    void onSendComplete(final Delivery delivery);
}
