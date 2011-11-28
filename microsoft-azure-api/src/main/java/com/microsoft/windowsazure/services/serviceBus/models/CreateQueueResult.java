package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>createQueue</code> operation.
 */
public class CreateQueueResult {

    private QueueInfo value;

    /**
     * Creates an instance of the <code>CreateQueueResult</code> class.
     * 
     * @param value
     *            A {@link QueueInfo} object assigned as the value of the result.
     */
    public CreateQueueResult(QueueInfo value) {
        this.setValue(value);
    }

    /**
     * Specfies the value of the result.
     * 
     * @param value
     *            A {@link QueueInfo} object assigned as the value of the result.
     */
    public void setValue(QueueInfo value) {
        this.value = value;
    }

    /**
     * Returns the value of the result.
     * 
     * @return A {@link QueueInfo} object that represents the value of the result.
     */
    public QueueInfo getValue() {
        return value;
    }

}
