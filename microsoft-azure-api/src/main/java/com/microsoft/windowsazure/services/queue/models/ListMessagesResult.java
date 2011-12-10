/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.queue.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateAdapter;

@XmlRootElement(name = "QueueMessagesList")
public class ListMessagesResult {
    private List<QueueMessage> queueMessages = new ArrayList<QueueMessage>();

    @XmlElement(name = "QueueMessage")
    public List<QueueMessage> getQueueMessages() {
        return queueMessages;
    }

    public void setQueueMessages(List<QueueMessage> queueMessages) {
        this.queueMessages = queueMessages;
    }

    public static class QueueMessage {
        private String messageId;
        private Date insertionDate;
        private Date expirationDate;
        private String popReceipt;
        private Date timeNextVisible;
        private int dequeueCount;
        private String messageText;

        @XmlElement(name = "MessageId")
        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        @XmlElement(name = "InsertionTime")
        @XmlJavaTypeAdapter(RFC1123DateAdapter.class)
        public Date getInsertionDate() {
            return insertionDate;
        }

        public void setInsertionDate(Date insertionDate) {
            this.insertionDate = insertionDate;
        }

        @XmlElement(name = "ExpirationTime")
        @XmlJavaTypeAdapter(RFC1123DateAdapter.class)
        public Date getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(Date expirationDate) {
            this.expirationDate = expirationDate;
        }

        @XmlElement(name = "PopReceipt")
        public String getPopReceipt() {
            return popReceipt;
        }

        public void setPopReceipt(String popReceipt) {
            this.popReceipt = popReceipt;
        }

        @XmlElement(name = "TimeNextVisible")
        @XmlJavaTypeAdapter(RFC1123DateAdapter.class)
        public Date getTimeNextVisible() {
            return timeNextVisible;
        }

        public void setTimeNextVisible(Date timeNextVisible) {
            this.timeNextVisible = timeNextVisible;
        }

        @XmlElement(name = "DequeueCount")
        public int getDequeueCount() {
            return dequeueCount;
        }

        public void setDequeueCount(int dequeueCount) {
            this.dequeueCount = dequeueCount;
        }

        @XmlElement(name = "MessageText")
        public String getMessageText() {
            return messageText;
        }

        public void setMessageText(String messageText) {
            this.messageText = messageText;
        }
    }
}
