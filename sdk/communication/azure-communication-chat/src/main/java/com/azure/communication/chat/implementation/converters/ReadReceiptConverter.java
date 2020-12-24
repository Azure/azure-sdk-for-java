// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.chat.models.ReadReceipt;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ReadReceipt} and
 * {@link ReadReceipt}.
 */
public final class ReadReceiptConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.ReadReceipt} to {@link ReadReceipt}.
     */
    public static ReadReceipt convert(com.azure.communication.chat.implementation.models.ReadReceipt obj) {
        if (obj == null) {
            return null;
        }

        ReadReceipt readReceipt = new ReadReceipt()
            .setSender(new CommunicationUserIdentifier(obj.getSenderId()))
            .setChatMessageId(obj.getChatMessageId())
            .setReadOn(obj.getReadOn());

        return readReceipt;
    }

    private ReadReceiptConverter() {
    }
}
