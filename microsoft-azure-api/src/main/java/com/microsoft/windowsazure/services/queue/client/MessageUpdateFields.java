package com.microsoft.windowsazure.services.queue.client;

/**
 * 
 * Represents a set of flags for updating messages.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum MessageUpdateFields {
    /**
     * Update the message visibility timeout.
     */
    VISIBILITY(1),

    /**
     * Update the message content.
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
