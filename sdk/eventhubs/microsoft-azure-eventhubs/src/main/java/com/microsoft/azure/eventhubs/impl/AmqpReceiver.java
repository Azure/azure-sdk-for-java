// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Delivery;

public interface AmqpReceiver extends AmqpLink {
    void onReceiveComplete(Delivery delivery);
}
