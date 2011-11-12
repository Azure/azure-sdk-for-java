package com.microsoft.windowsazure.services.queue.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateAdapter;

@XmlRootElement(name = "QueueMessagesList")
public class PeekMessagesResult {
    private List<QueueMessage> queueMessages = new ArrayList<QueueMessage>();

    @XmlElement(name = "QueueMessage")
    public List<QueueMessage> getQueueMessages() {
        return queueMessages;
    }

    public void setQueueMessages(List<QueueMessage> queueMessages) {
        this.queueMessages = queueMessages;
    }

    public static class QueueMessage {
        private String id;
        private Date insertionDate;
        private Date expirationDate;
        private int dequeueCount;
        private String text;

        @XmlElement(name = "MessageId")
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        @XmlElement(name = "DequeueCount")
        public int getDequeueCount() {
            return dequeueCount;
        }

        public void setDequeueCount(int dequeueCount) {
            this.dequeueCount = dequeueCount;
        }

        @XmlElement(name = "MessageText")
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
