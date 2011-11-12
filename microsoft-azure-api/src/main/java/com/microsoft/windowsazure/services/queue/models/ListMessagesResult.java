package com.microsoft.windowsazure.services.queue.models;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "QueueMessagesList")
public class ListMessagesResult {
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
        private String popReceipt;
        private Date timeNextVisible;
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

        @XmlElement(name = "PopReceipt")
        public String getPopReceipt() {
            return popReceipt;
        }

        public void setPopReceipt(String popReceipt) {
            this.popReceipt = popReceipt;
        }

        @XmlElement(name = "TimeNextVisible")
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
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
