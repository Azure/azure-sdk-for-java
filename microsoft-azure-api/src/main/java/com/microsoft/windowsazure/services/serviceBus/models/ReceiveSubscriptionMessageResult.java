package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>receiveSubscriptionMessage</code> operation.
 */
public class ReceiveSubscriptionMessageResult {

    private Message value;

    /**
     * Creates an instance of the <code>ReceiveSubscriptionMessageResult</code> class.
     * 
     * @param value
     *            A {@link Message} object assigned as the value of the result.
     */
    public ReceiveSubscriptionMessageResult(Message value) {
        this.setValue(value);
    }

    /**
     * Specifies the value of the result.
     * 
     * @param value
     *            A {@link Message} object assigned as the value of the result.
     */
    public void setValue(Message value) {
        this.value = value;
    }

    /**
     * Returns the value of the result.
     * 
     * @return A {@link Message} object that represents the value of the result.
     */
    public Message getValue() {
        return value;
    }

}
