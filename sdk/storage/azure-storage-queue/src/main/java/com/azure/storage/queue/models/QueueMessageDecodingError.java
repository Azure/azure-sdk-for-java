// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClient;

import java.util.Objects;

/**
 * Contains information about message that could not be decoded.
 */
@Immutable
public final class QueueMessageDecodingError {
    private final QueueAsyncClient queueAsyncClient;
    private final QueueClient queueClient;
    private final QueueMessageItem queueMessageItem;
    private final PeekedMessageItem peekedMessageItem;
    private final Exception cause;

    /**
     * Creates new {@link QueueMessageDecodingError}.
     * @param queueAsyncClient the {@link QueueAsyncClient} of the queue that has received message.
     * @param queueClient the {@link QueueClient} of the queue that has received message.
     * @param queueMessageItem the {@link QueueMessageItem} that has been received and could not be decoded.
     * @param peekedMessageItem the {@link PeekedMessageItem} that has been peeked and could not be decoded.
     * @param cause the {@link Exception} thrown at decoding attempt if present.
     */
    public QueueMessageDecodingError(
        QueueAsyncClient queueAsyncClient,
        QueueClient queueClient,
        QueueMessageItem queueMessageItem,
        PeekedMessageItem peekedMessageItem, Exception cause) {
        Objects.requireNonNull(queueAsyncClient, "'queueAsyncClient' cannot be null.");
        Objects.requireNonNull(queueClient, "'queueClient' cannot be null.");
        this.queueAsyncClient = queueAsyncClient;
        this.queueClient = queueClient;
        this.queueMessageItem = queueMessageItem;
        this.peekedMessageItem = peekedMessageItem;
        this.cause = cause;
    }

    /**
     * Gets the async queue client that has received message.
     * @return the queue client that has received message.
     */
    public QueueAsyncClient getQueueAsyncClient() {
        return queueAsyncClient;
    }

    /**
     * Gets the queue client that has received message.
     * @return the queue client that has received message.
     */
    public QueueClient getQueueClient() {
        return queueClient;
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

    /**
     * Gets the {@link Exception} thrown at decoding attempt if present.
     * @return the {@link Exception} thrown at decoding attempt if present.
     */
    public Exception getCause() {
        return cause;
    }
}
