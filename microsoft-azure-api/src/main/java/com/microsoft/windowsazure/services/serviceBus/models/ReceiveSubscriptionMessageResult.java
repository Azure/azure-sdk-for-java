package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>receiveSubscriptionMessage</code> operation.
 */
public class ReceiveSubscriptionMessageResult {

    private BrokeredMessage value;

    /**
     * Creates an instance of the <code>ReceiveSubscriptionMessageResult</code> class.
     * 
     * @param value
     *            A {@link BrokeredMessage} object assigned as the value of the result.
     */
    public ReceiveSubscriptionMessageResult(BrokeredMessage value) {
        this.setValue(value);
    }

    /**
     * Specifies the value of the result.
     * 
     * @param value
     *            A {@link BrokeredMessage} object assigned as the value of the result.
     */
    public void setValue(BrokeredMessage value) {
        this.value = value;
    }

    /**
     * Returns the value of the result.
     * 
     * @return A {@link BrokeredMessage} object that represents the value of the result.
     */
    public BrokeredMessage getValue() {
        return value;
    }

}
