package com.microsoft.windowsazure.services.queue;

import java.util.Date;

public class UpdateMessageResult {
    private String popReceipt;
    private Date timeNextVisible;

    public String getPopReceipt() {
        return popReceipt;
    }

    public void setPopReceipt(String popReceipt) {
        this.popReceipt = popReceipt;
    }

    public Date getTimeNextVisible() {
        return timeNextVisible;
    }

    public void setTimeNextVisible(Date timeNextVisible) {
        this.timeNextVisible = timeNextVisible;
    }
}
