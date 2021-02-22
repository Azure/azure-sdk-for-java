// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.queue.QueueAsyncClient;

import java.util.Objects;

/**
 * Contains information about message that could not be decoded.
 */
@Immutable
public class QueueMessageDecodingFailure {
    private final QueueAsyncClient queueAsyncClient;
    private final QueueMessageItem queueMessageItem;
    private final PeekedMessageItem peekedMessageItem;

    /**
     * Creates new {@link QueueMessageDecodingFailure}.
     * @param queueAsyncClient the {@link QueueAsyncClient} of the queue that has received message.
     * @param queueMessageItem the {@link QueueMessageItem} that has been received and could not be decoded.
     * @param peekedMessageItem the {@link PeekedMessageItem} that has been peeked and could not be decoded.
     */
    public QueueMessageDecodingFailure(
        QueueAsyncClient queueAsyncClient,
        QueueMessageItem queueMessageItem,
        PeekedMessageItem peekedMessageItem) {
        Objects.requireNonNull(queueAsyncClient, "'queueAsyncClient' cannot be null.");
        this.queueAsyncClient = queueAsyncClient;
        this.queueMessageItem = queueMessageItem;
        this.peekedMessageItem = peekedMessageItem;
    }

    /**
     * Gets the queue client that has received message.
     * @return the queue client that has received message.
     */
    public QueueAsyncClient getQueueAsyncClient() {
        return queueAsyncClient;
    }

    /**
     * Gets the {@link QueueMessageItem} that has been received and could not be decoded.
     * The body of the message is as received, i.e. no decoding is attempted.
     * @return the {@link QueueMessageItem} that has been received and could not be decoded.
     */
    public QueueMessageItem getQueueMessageItem() {
        return queueMessageItem;
    }

    /**
     * Gets the {@link PeekedMessageItem} that has been peeked and could not be decoded.
     * The body of the message is as received, i.e. no decoding is attempted.
     * @return the {@link PeekedMessageItem} that has been peeked and could not be decoded.
     */
    public PeekedMessageItem getPeekedMessageItem() {
        return peekedMessageItem;
    }
}
