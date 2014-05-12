/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.queue;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a message in the Microsoft Azure Queue service.
 */
public class CloudQueueMessage {

    /**
     * Holds the dequeue count.
     */
    private int dequeueCount;

    /**
     * Holds the time that the message expires.
     */
    private Date expirationTime;

    /**
     * Holds the message ID.
     */
    private String id;

    /**
     * Holds the time that the message was added to the queue.
     */
    private Date insertionTime;

    /**
     * Holds the message content.
     */
    protected String messageContent;

    /**
     * Holds the message type.
     */
    private QueueMessageType messageType;

    /**
     * Holds the time that the message will next be visible.
     */
    private Date nextVisibleTime;

    /**
     * Holds the message's pop receipt.
     */
    private String popReceipt;

    /**
     * Initializes a new instance of the <code>CloudQueueMessage</code> class (used internally).
     */
    protected CloudQueueMessage() {
        // no op
    }

    /**
     * Initializes a new instance of the <code>CloudQueueMessage</code> class with a <code>byte</code>
     * array containing the message.
     * 
     * @param content
     *        A <code>byte</code> array which contains the message.
     */
    public CloudQueueMessage(final byte[] content) {
        this.setMessageContent(content);
    }

    /**
     * Initializes a new instance of the <code>CloudQueueMessage</code> class with a <code>String</code>
     * containing the message.
     * 
     * @param content
     *        A <code>String</code> which contains the message.
     */
    public CloudQueueMessage(final String content) {
        this.setMessageContent(content);
    }

    /**
     * Gets the dequeue count.
     * 
     * @return An <code>int</code> which contains the dequeue count.
     */
    public final int getDequeueCount() {
        return this.dequeueCount;
    }

    /**
     * Gets the time that the message expires.
     * 
     * @return A <code>java.util.Date</code> object which represents the time that the message expires.
     */
    public final Date getExpirationTime() {
        return this.expirationTime;
    }

    /**
     * Gets the message ID.
     * 
     * @return A <code>String</code> which contains the message ID.
     */
    public final String getId() {
        return this.id;
    }

    /**
     * Gets the time the message was added to the queue.
     * 
     * @return A <code>java.util.Date</code> which represents the time the message was added to the queue.
     */
    public final Date getInsertionTime() {
        return this.insertionTime;
    }

    /**
     * Gets the content of the message as a byte array.
     * 
     * @return A <code>byte</code> array which contains the content of the message.
     * 
     * @throws StorageException
     *         If a storage service error occurred.
     */
    public final byte[] getMessageContentAsByte() throws StorageException {
        if (Utility.isNullOrEmpty(this.messageContent)) {
            return new byte[0];
        }

        if (this.messageType == QueueMessageType.RAW_STRING) {
            try {
                return this.messageContent.getBytes(Constants.UTF8_CHARSET);
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
     * @return A <code>String</code> which contains the content of the message.
     * 
     * @throws StorageException
     *         If a storage service error occurred.
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
                return new String(Base64.decode(this.messageContent), Constants.UTF8_CHARSET);
            }
            catch (final UnsupportedEncodingException e) {
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }
    }

    /**
     * Gets the content of the message for transfer (internal use only).
     * 
     * @return A <code>String</code> which contains the content of the message.
     * 
     * @throws StorageException
     *         If a storage service error occurred.
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
            throw new IllegalArgumentException(
                    String.format(SR.INVALID_MESSAGE_LENGTH, QueueConstants.MAX_MESSAGE_SIZE));
        }

        return result;
    }

    /**
     * Gets the message ID.
     * 
     * @return A <code>String</code> which contains the message ID.
     */
    public final String getMessageId() {
        return this.id;
    }

    /**
     * Gets the message type.
     * 
     * @return A <code>QueueMessageType</code> value which represents the message type.
     */
    protected final QueueMessageType getMessageType() {
        return this.messageType;
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
     * @return A <code>String</code> which contains the message's pop receipt.
     */
    public final String getPopReceipt() {
        return this.popReceipt;
    }

    /**
     * Sets the dequeue count.
     * 
     * @param dequeueCount
     *        An <code>int</code> which contains the dequeue count.
     */
    protected final void setDequeueCount(int dequeueCount) {
        this.dequeueCount = dequeueCount;
    }

    /**
     * Sets the expiration time for the message.
     * 
     * @param expirationTime
     *        The <code>java.util.Date</code> which represents the expiration time to set on the message.
     */
    protected final void setExpirationTime(final Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Sets the <code>java.util.Date</code> representing the time the message was added to the queue.
     * 
     * @param insertiontTime
     *        The <code>java.util.Date</code> representing the time the message was added to the queue.
     */
    protected final void setInsertionTime(Date insertionTime) {
        this.insertionTime = insertionTime;
    }

    /**
     * Sets the content of the message as a <code>byte</code> array.
     * 
     * @param content
     *        A <code>byte</code> array which contains the content of the message.
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
     *        A <code>String</code> which contains the content of the message.
     */
    public final void setMessageContent(final String content) {
        this.messageContent = content;
        this.messageType = QueueMessageType.RAW_STRING;
    }

    /**
     * Sets the message ID.
     * 
     * @param id
     *        A <code>String</code> which represents the message ID.
     */
    protected final void setMessageId(String id) {
        this.id = id;
    }

    /**
     * Sets the message's type
     * 
     * @param messageType
     *        A <code>QueueMessageType</code> value which represents the message type.
     */
    protected final void setMessageType(final QueueMessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Sets the time for the message to become visible in the queue.
     * 
     * @param nextVisibleTime
     *        A <code>java.util.Date</code> with the time that the message will
     *        next be visible.
     */
    protected final void setNextVisibleTime(final Date nextVisibleTime) {
        this.nextVisibleTime = nextVisibleTime;
    }

    /**
     * Sets the message's pop receipt.
     * 
     * @param popReceipt
     *        A <code>String</code> which contains the message's pop receipt.
     */
    protected final void setPopReceipt(final String popReceipt) {
        this.popReceipt = popReceipt;
    }
}
