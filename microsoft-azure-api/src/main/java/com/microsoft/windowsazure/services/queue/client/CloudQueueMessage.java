package com.microsoft.windowsazure.services.queue.client;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Represents a message in the Windows Azure Queue service.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public class CloudQueueMessage {

    /**
     * Holds the message ID.
     */
    protected String id;

    /**
     * Holds the message's pop receipt.
     */
    protected String popReceipt;

    /**
     * Holds the time that the message was added to the queue.
     */
    protected Date insertionTime;

    /**
     * Holds the time that the message expires.
     */
    protected Date expirationTime;

    /**
     * Holds the time that the message will next be visible.
     */
    protected Date nextVisibleTime;

    /**
     * Holds the message content.
     */
    protected String messageContent;

    /**
     * Holds the dequeue count.
     */
    protected int dequeueCount;

    /**
     * Holds the message type.
     */
    protected QueueMessageType messageType;

    /**
     * Initializes a new instance of the CloudQueueMessage class(used internaly).
     */
    CloudQueueMessage() {
        // no op
    }

    /**
     * Initializes a new instance of the CloudQueueMessage class.
     * 
     * @param content
     *            The content of the message.
     */
    public CloudQueueMessage(final byte[] content) {
        this.messageContent = Base64.encode(content);
        this.messageType = QueueMessageType.BASE_64_ENCODED;
    }

    /**
     * Initializes a new instance of the CloudQueueMessage class.
     * 
     * @param content
     *            The content of the message.
     */
    public CloudQueueMessage(final String content) {
        this.messageContent = content;
        this.messageType = QueueMessageType.RAW_STRING;
    }

    /**
     * Gets the dequeue count.
     * 
     * @return the dequeue count.
     */
    public final int getDequeueCount() {
        return this.dequeueCount;
    }

    /**
     * Gets the time that the message expires.
     * 
     * @return the time that the message expires.
     */
    public final Date getExpirationTime() {
        return this.expirationTime;
    }

    /**
     * Gets the message ID.
     * 
     * @return the message ID.
     */
    public final String getId() {
        return this.id;
    }

    /**
     * Gets the time that the message was added to the queue.
     * 
     * @return the time that the message was added to the queue.
     */
    public final Date getInsertionTime() {
        return this.insertionTime;
    }

    /**
     * Gets the content of the message, as a byte array.
     * 
     * @return the content of the message.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public final byte[] getMessageContentAsByte() throws StorageException {
        if (Utility.isNullOrEmpty(this.messageContent)) {
            return null;
        }

        if (this.messageType == QueueMessageType.RAW_STRING) {
            try {
                return this.messageContent.getBytes("UTF8");
            } catch (final UnsupportedEncodingException e) {
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        } else {
            return Base64.decode(this.messageContent);
        }
    }

    /**
     * Gets the content of the message, as a string.
     * 
     * @return the content of the message.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public final String getMessageContentAsString() throws StorageException {
        if (Utility.isNullOrEmpty(this.messageContent)) {
            return null;
        }

        if (this.messageType == QueueMessageType.RAW_STRING) {
            return this.messageContent;
        } else {
            try {
                return new String(Base64.decode(this.messageContent), "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }
    }

    /**
     * Gets the content of the message for transfer(internal use only).
     * 
     * @throws StorageException
     */
    protected final String getMessageContentForTransfer(final boolean shouldEncodeMessage) throws StorageException {
        if (this.messageType == QueueMessageType.RAW_STRING) {
            if (shouldEncodeMessage) {
                return Base64.encode(this.getMessageContentAsByte());
            }
        }

        return this.messageContent;
    }

    /**
     * Gets the time that the message will next be visible.
     * 
     * @return the time that the message will next be visible.
     */
    public final Date getNextVisibleTime() {
        return this.nextVisibleTime;
    }

    /**
     * Gets the message's pop receipt.
     * 
     * @return the message's pop receipt.
     */
    public final String getPopReceipt() {
        return this.popReceipt;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public final void setExpirationTime(final Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Sets the content of the message.
     * 
     * @param content
     *            The content of the message.
     */
    public final void setMessageContent(final byte[] content) {
        this.messageContent = Base64.encode(content);
        this.messageType = QueueMessageType.BASE_64_ENCODED;
    }

    /**
     * Sets the content of the message.
     * 
     * @param content
     *            The content of the message.
     */
    public final void setMessageContent(final String content) {
        this.messageContent = content;
        this.messageType = QueueMessageType.RAW_STRING;
    }

    /**
     * @param nextVisibleTime
     *            the nextVisibleTime to set
     */
    public final void setNextVisibleTime(final Date nextVisibleTime) {
        this.nextVisibleTime = nextVisibleTime;
    }
}
