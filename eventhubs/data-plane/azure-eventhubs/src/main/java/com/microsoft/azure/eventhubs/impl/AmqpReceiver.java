/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Delivery;

public interface AmqpReceiver extends AmqpLink {
    void onReceiveComplete(Delivery delivery);
}
