package com.microsoft.windowsazure.services.queue.models;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateAdapter;

@XmlRootElement(name = "QueueMessagesList")
public class ListMessagesResult {
    private List<Entry> entries;

    @XmlElement(name = "QueueMessage")
    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public static class Entry {
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
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
