package com.microsoft.azure.services.queue;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "QueueMessagesList")
public class PeekMessagesResult {
    private List<QueueMessage> messages;

    @XmlElement(name = "QueueMessage")
    public List<QueueMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<QueueMessage> messages) {
        this.messages = messages;
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
        public Date getInsertionDate() {
            return insertionDate;
        }

        public void setInsertionDate(Date insertionDate) {
            this.insertionDate = insertionDate;
        }

        @XmlElement(name = "ExpirationTime")
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
