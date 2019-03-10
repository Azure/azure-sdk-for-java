/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventPosition;
import org.apache.qpid.proton.message.Message;

final class MessageWrapper {
    private final Message message;
    private final EventPosition eventPosition;

    MessageWrapper(Message message, EventPosition eventPosition) {
        this.message = message;
        this.eventPosition = eventPosition;
    }

    Message getMessage() {
        return this.message;
    }

    EventPosition getEventPosition() {
        return this.eventPosition;
    }
}
