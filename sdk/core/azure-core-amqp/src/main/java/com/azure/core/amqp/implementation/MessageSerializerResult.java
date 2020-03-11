// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import java.util.ArrayList;
import java.util.List;

/***
 * This will hole additional data which can not be stored in AMQP Message.
 * Example last  serial number.
 */
public class MessageSerializerResult<T> {
    List<T> messages = null;
    long lastSequenceNumber;

    /**
     * Ctor
     * @param listOfMessages after serialization.
     * @param lastSequenceNumber serial number of the last message.
     */
    public MessageSerializerResult(List<T> listOfMessages, long lastSequenceNumber) {

        this.messages = listOfMessages;
        this.lastSequenceNumber = lastSequenceNumber;
        // Make sure array is not null.
        if (messages == null) {
            messages = new ArrayList<>();
        }
    }

    public List<T> getMessages() {
        return messages;
    }

    public long getLastSequenceNumber() {
        return lastSequenceNumber;
    }
}
