/*
 * QueueListingDetails.java
 * 
 * Copyright (c) 2011 Microsoft. All rights reserved.
 */
package com.microsoft.windowsazure.services.queue.client;

/**
 * Specifies which details to include when listing the queues in this storage
 * account.
 */
public enum QueueListingDetails {
    /**
     * Specifies including all available details.
     */
    ALL(1),

    /**
     * Specifies including queue metadata.
     */
    METADATA(1),

    /**
     * Specifies including no additional details.
     */
    NONE(0);

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
    QueueListingDetails(final int val) {
        this.value = val;
    }
}
