package com.microsoft.windowsazure.services.queue.client;

/**
 * Flags for the values to set when updating messages.
 */
public enum MessageUpdateFields {
    /**
     * Set to update the message visibility timeout.
     */
    VISIBILITY(1),

    /**
     * Set to update the message content.
     */
    CONTENT(2);

    /**
     * Returns the value of this enum.
     */
    public int value;

    /**
     * Sets the value of this enum.
     * 
     * @param val
     *            The value being assigned.
     */
    MessageUpdateFields(final int val) {
        this.value = val;
    }
}
