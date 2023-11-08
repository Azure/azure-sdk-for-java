// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.implementation.util;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.implementation.models.PeekedMessageItemInternal;
import com.azure.storage.queue.implementation.models.QueueMessageItemInternal;
import com.azure.storage.queue.implementation.models.QueuesGetPropertiesHeaders;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueProperties;

import java.util.Base64;
import java.util.Objects;

public class ModelHelper {
    private static final ClientLogger LOGGER = new ClientLogger(ModelHelper.class);

    private static BinaryData decodeMessageBody(String messageText, QueueMessageEncoding messageEncoding) {
        if (messageText == null) {
            return null;
        }
        switch (messageEncoding) {
            case NONE:
                return BinaryData.fromString(messageText);
            case BASE64:
                try {
                    return BinaryData.fromBytes(Base64.getDecoder().decode(messageText));
                } catch (IllegalArgumentException e) {
                    throw LOGGER.logExceptionAsError(e);
                }
            default:
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Unsupported message encoding=" + messageEncoding));
        }
    }

    public static QueueMessageItem transformQueueMessageItemInternal(
        QueueMessageItemInternal queueMessageItemInternal, QueueMessageEncoding messageEncoding) {
        QueueMessageItem queueMessageItem = new QueueMessageItem()
            .setMessageId(queueMessageItemInternal.getMessageId())
            .setDequeueCount(queueMessageItemInternal.getDequeueCount())
            .setExpirationTime(queueMessageItemInternal.getExpirationTime())
            .setInsertionTime(queueMessageItemInternal.getInsertionTime())
            .setPopReceipt(queueMessageItemInternal.getPopReceipt())
            .setTimeNextVisible(queueMessageItemInternal.getTimeNextVisible());
        BinaryData decodedMessageBody = decodeMessageBody(queueMessageItemInternal.getMessageText(), messageEncoding);
        if (decodedMessageBody != null) {
            queueMessageItem.setBody(decodedMessageBody);
        }
        return queueMessageItem;
    }

    public static PeekedMessageItem transformPeekedMessageItemInternal(
        PeekedMessageItemInternal peekedMessageItemInternal, QueueMessageEncoding messageEncoding) {
        PeekedMessageItem peekedMessageItem = new PeekedMessageItem()
            .setMessageId(peekedMessageItemInternal.getMessageId())
            .setDequeueCount(peekedMessageItemInternal.getDequeueCount())
            .setExpirationTime(peekedMessageItemInternal.getExpirationTime())
            .setInsertionTime(peekedMessageItemInternal.getInsertionTime());
        BinaryData decodedMessage = decodeMessageBody(peekedMessageItemInternal.getMessageText(), messageEncoding);
        if (decodedMessage != null) {
            peekedMessageItem.setBody(decodedMessage);
        }
        return peekedMessageItem;
    }

    public static String encodeMessage(BinaryData message, QueueMessageEncoding messageEncoding) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        switch (messageEncoding) {
            case NONE:
                return message.toString();
            case BASE64:
                return Base64.getEncoder().encodeToString(message.toBytes());
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unsupported message encoding="
                    + messageEncoding));
        }
    }

    public static QueueProperties transformQueueProperties(QueuesGetPropertiesHeaders headers) {
        return new QueueProperties(headers.getXMsMeta(), headers.getXMsApproximateMessagesCount());
    }

}
