package com.microsoft.windowsazure.services.queue.implementation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "QueueMessage")
public class QueueMessage {
    private String messageText;

    @XmlElement(name = "MessageText")
    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

}
