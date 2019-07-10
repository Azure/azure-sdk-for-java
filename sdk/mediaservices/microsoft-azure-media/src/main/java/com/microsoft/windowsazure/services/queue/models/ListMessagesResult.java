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
package com.microsoft.windowsazure.services.queue.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.core.RFC1123DateAdapter;

/**
 * A wrapper class for the result returned from a Queue Service REST API
 * operation to get a list of messages. This is returned by calls to
 * implementations of {@link com.microsoft.windowsazure.services.queue.QueueContract#listMessages(String)} and
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#listMessages(String, ListMessagesOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179474.aspx">Get
 * Messages</a> documentation on MSDN for details of the underlying Queue
 * Service REST API operation.
 */
@XmlRootElement(name = "QueueMessagesList")
public class ListMessagesResult {
    private List<QueueMessage> queueMessages = new ArrayList<QueueMessage>();

    /**
     * Gets the list of queue messages returned by a {@link com.microsoft.windowsazure.services.queue.QueueContract}
     * <em>.listMessages</em> request. The queue messages returned have their
     * visibility timeout set to allow for processing by the client. The client
     * must delete the messages once processing is complete, or they will become
     * visible in the queue when the visibility timeout period is over.
     * 
     * @return A {@link List} of {@link QueueMessage} instances representing the
     *         messages returned by the request.
     */
    @XmlElement(name = "QueueMessage")
    public List<QueueMessage> getQueueMessages() {
        return queueMessages;
    }

    /**
     * Reserved for internal use. Sets the list of queue messages returned by a
     * {@link com.microsoft.windowsazure.services.queue.QueueContract} <em>.listMessages</em> request. This method is
     * invoked by the API as part of the response generation from the Queue
     * Service REST API operation to set the value from the queue message list
     * returned by the server.
     * 
     * @param queueMessages
     *            A {@link List} of {@link QueueMessage} instances representing
     *            the messages returned by the request.
     */
    public void setQueueMessages(List<QueueMessage> queueMessages) {
        this.queueMessages = queueMessages;
    }

    /**
     * Represents a message in the queue returned by the server. A
     * {@link QueueMessage} instance contains a copy of the queue message data
     * in the storage service as of the time the message was requested.
     */
    public static class QueueMessage {
        private String messageId;
        private Date insertionDate;
        private Date expirationDate;
        private String popReceipt;
        private Date timeNextVisible;
        private int dequeueCount;
        private String messageText;

        /**
         * Gets the message ID for the message in the queue. The message ID is a
         * value that is opaque to the client that must be used along with the
         * pop receipt to validate an update message or delete message
         * operation.
         * 
         * @return A {@link String} containing the message ID.
         */
        @XmlElement(name = "MessageId")
        public String getMessageId() {
            return messageId;
        }

        /**
         * Reserved for internal use. Sets the value of the message ID for the
         * queue message. This method is invoked by the API as part of the
         * response generation from the Queue Service REST API operation to set
         * the value with the message ID returned by the server.
         * 
         * @param messageId
         *            A {@link String} containing the message ID.
         */
        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        /**
         * Gets the {@link Date} when this message was added to the queue.
         * 
         * @return The {@link Date} when this message was added to the queue.
         */
        @XmlElement(name = "InsertionTime")
        @XmlJavaTypeAdapter(RFC1123DateAdapter.class)
        public Date getInsertionDate() {
            return insertionDate;
        }

        /**
         * Reserved for internal use. Sets the value of the insertion time for
         * the queue message. This method is invoked by the API as part of the
         * response generation from the Queue Service REST API operation to set
         * the value with the insertion time returned by the server.
         * 
         * @param insertionDate
         *            The {@link Date} when this message was added to the queue.
         */
        public void setInsertionDate(Date insertionDate) {
            this.insertionDate = insertionDate;
        }

        /**
         * Gets the {@link Date} when this message will expire and be
         * automatically removed from the queue.
         * 
         * @return The {@link Date} when this message will expire.
         */
        @XmlElement(name = "ExpirationTime")
        @XmlJavaTypeAdapter(RFC1123DateAdapter.class)
        public Date getExpirationDate() {
            return expirationDate;
        }

        /**
         * Reserved for internal use. Sets the value of the expiration time for
         * the queue message. This method is invoked by the API as part of the
         * response generation from the Queue Service REST API operation to set
         * the value with the expiration time returned by the server.
         * 
         * @param expirationDate
         *            The {@link Date} when this message will expire.
         */
        public void setExpirationDate(Date expirationDate) {
            this.expirationDate = expirationDate;
        }

        /**
         * Gets the pop receipt value for the queue message. The pop receipt is
         * a value that is opaque to the client that must be used along with the
         * message ID to validate an update message or delete message operation.
         * 
         * @return A {@link String} containing the pop receipt value for the
         *         queue message.
         */
        @XmlElement(name = "PopReceipt")
        public String getPopReceipt() {
            return popReceipt;
        }

        /**
         * Reserved for internal use. Sets the value of the pop receipt for the
         * queue message. This method is invoked by the API as part of the
         * response generation from the Queue Service REST API operation to set
         * the value with the pop receipt returned by the server.
         * 
         * @param popReceipt
         *            A {@link String} containing the pop receipt value for the
         *            queue message.
         */
        public void setPopReceipt(String popReceipt) {
            this.popReceipt = popReceipt;
        }

        /**
         * Gets the {@link Date} when this message will become visible in the
         * queue.
         * 
         * @return The {@link Date} when this message will become visible in the
         *         queue.
         */
        @XmlElement(name = "TimeNextVisible")
        @XmlJavaTypeAdapter(RFC1123DateAdapter.class)
        public Date getTimeNextVisible() {
            return timeNextVisible;
        }

        /**
         * Reserved for internal use. Sets the value of the time the message
         * will become visible. This method is invoked by the API as part of the
         * response generation from the Queue Service REST API operation to set
         * the value with the time next visible returned by the server.
         * 
         * @param timeNextVisible
         *            The {@link Date} when this message will become visible in
         *            the queue.
         */
        public void setTimeNextVisible(Date timeNextVisible) {
            this.timeNextVisible = timeNextVisible;
        }

        /**
         * Gets the number of times this queue message has been retrieved with a
         * list messages operation.
         * 
         * @return The number of times this queue message has been retrieved.
         */
        @XmlElement(name = "DequeueCount")
        public int getDequeueCount() {
            return dequeueCount;
        }

        /**
         * Reserved for internal use. Sets the value of the dequeue count of the
         * message. This method is invoked by the API as part of the response
         * generation from the Queue Service REST API operation to set the value
         * with the queue message dequeue count returned by the server.
         * 
         * @param dequeueCount
         *            The number of times this queue message has been retrieved.
         */
        public void setDequeueCount(int dequeueCount) {
            this.dequeueCount = dequeueCount;
        }

        /**
         * Gets the {@link String} containing the content of the queue message.
         * 
         * @return A {@link String} containing the content of the queue message.
         */
        @XmlElement(name = "MessageText")
        public String getMessageText() {
            return messageText;
        }

        /**
         * Reserved for internal use. Sets the {@link String} containing the
         * content of the message. This method is invoked by the API as part of
         * the response generation from the Queue Service REST API operation to
         * set the value with the queue message content returned by the server.
         * 
         * @param messageText
         *            A {@link String} containing the content of the message.
         */
        public void setMessageText(String messageText) {
            this.messageText = messageText;
        }
    }
}
