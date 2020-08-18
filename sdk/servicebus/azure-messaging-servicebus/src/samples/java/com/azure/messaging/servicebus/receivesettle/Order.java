package com.azure.messaging.servicebus.receivesettle;

/**
 * Order entity simulation.
 */
public class Order {
    private String id;
    private String content;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    @Override
    public String toString() {
        return "Order{" +
            "id='" + id + '\'' +
            ", content='" + content + '\'' +
            '}';
    }
}
