package com.microsoft.windowsazure.services.queue.client;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Represents a message in the Windows Azure Queue service.
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
     * Initializes a new instance of the CloudQueueMessage class (used
     * internally).
     */
    CloudQueueMessage() {
        // no op
    }

    /**
     * Initializes a new instance of the CloudQueueMessage class with an array
     * of <code>byte</code> containing the message.
     * 
     * @param content
     *            The array of <code>byte</code> that contains the message.
     */
    public CloudQueueMessage(final byte[] content) {
        this.setMessageContent(content);
    }

    /**
     * Initializes a new instance of the CloudQueueMessage class with a string
     * containing the message.
     * 
     * @param content
     *            The <code>String</code> that contains the message.
     */
    public CloudQueueMessage(final String content) {
        this.setMessageContent(content);
    }

    /**
     * Gets the dequeue count.
     * 
     * @return The dequeue count.
     */
    public final int getDequeueCount() {
        return this.dequeueCount;
    }

    /**
     * Gets the time that the message expires.
     * 
     * @return The time that the message expires.
     */
    public final Date getExpirationTime() {
        return this.expirationTime;
    }

    /**
     * Gets the message ID.
     * 
     * @return A <code>String</code> that contains the message ID.
     */
    public final String getId() {
        return this.id;
    }

    /**
     * Gets the time that the message was added to the queue.
     * 
     * @return The time that the message was added to the queue.
     */
    public final Date getInsertionTime() {
        return this.insertionTime;
    }

    /**
     * Gets the content of the message as a byte array.
     * 
     * @return An array of <code>byte</code> with the content of the message.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public final byte[] getMessageContentAsByte() throws StorageException {
        if (Utility.isNullOrEmpty(this.messageContent)) {
            return new byte[0];
        }

        if (this.messageType == QueueMessageType.RAW_STRING) {
            try {
                return this.messageContent.getBytes("UTF8");
            }
            catch (final UnsupportedEncodingException e) {
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }
        else {
            return Base64.decode(this.messageContent);
        }
    }

    /**
     * Gets the content of the message as a string.
     * 
     * @return A <code>String</code> with the content of the message.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public final String getMessageContentAsString() throws StorageException {
        if (this.messageType == QueueMessageType.RAW_STRING) {
            return this.messageContent;
        }
        else {
            if (Utility.isNullOrEmpty(this.messageContent)) {
                return null;
            }

            try {
                return new String(Base64.decode(this.messageContent), "UTF-8");
            }
            catch (final UnsupportedEncodingException e) {
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }
    }

    /**
     * Gets the content of the message for transfer (internal use only).
     * 
     * @return A <code>String</code> with the content of the message.
     * 
     * @throws StorageException
     */
    protected final String getMessageContentForTransfer(final boolean shouldEncodeMessage) throws StorageException {
        String result = null;
        if (this.messageType == QueueMessageType.RAW_STRING && shouldEncodeMessage) {
            result = Base64.encode(this.getMessageContentAsByte());

        }
        else {
            result = this.messageContent;
        }

        if (result != null && result.length() > QueueConstants.MAX_MESSAGE_SIZE) {
            throw new IllegalArgumentException(String.format("The message size can't be larger than %s bytes. ",
                    QueueConstants.MAX_MESSAGE_SIZE));
        }

        return result;
    }

    /**
     * Gets the time that the message will next be visible.
     * 
     * @return A <code>java.util.Date</code> with the time that the message will
     *         next be visible.
     */
    public final Date getNextVisibleTime() {
        return this.nextVisibleTime;
    }

    /**
     * Gets the message's pop receipt.
     * 
     * @return A <code>String</code> containing the message's pop receipt.
     */
    public final String getPopReceipt() {
        return this.popReceipt;
    }

    /**
     * Sets the expiration time for the message.
     * 
     * @param expirationTime
     *            The <code>java.util.Date</code> representing the expiration
     *            time to set on the message.
     */
    public final void setExpirationTime(final Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Sets the content of the message as an array of <code>byte</code>.
     * 
     * @param content
     *            The content of the message.
     */
    public final void setMessageContent(final byte[] content) {
        Utility.assertNotNull("content", content);

        this.messageContent = Base64.encode(content);
        this.messageType = QueueMessageType.BASE_64_ENCODED;
    }

    /**
     * Sets the content of the message as a <code>String</code>.
     * 
     * @param content
     *            The content of the message.
     */
    public final void setMessageContent(final String content) {
        this.messageContent = content;
        this.messageType = QueueMessageType.RAW_STRING;
    }

    /**
     * Sets the time for the message to become visible in the queue.
     * 
     * @param nextVisibleTime
     *            The <code>java.util.Date</code> representing the time to set
     *            for the message to be visible.
     */
    public final void setNextVisibleTime(final Date nextVisibleTime) {
        this.nextVisibleTime = nextVisibleTime;
    }
}
