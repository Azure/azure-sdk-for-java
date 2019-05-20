// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.Delivery;

public interface IAmqpSender extends IAmqpLink {
    void onFlow(final int creditIssued);

    void onSendComplete(final Delivery delivery);
}
