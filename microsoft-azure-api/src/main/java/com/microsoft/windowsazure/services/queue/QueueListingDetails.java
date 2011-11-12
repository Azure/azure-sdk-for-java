package com.microsoft.windowsazure.services.queue;

/**
 * TODO: Make this a bool?
 * 
 * Specifies which details to include when listing the containers in this
 * storage account.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum QueueListingDetails {
    /**
     * Specifies including no additional details.
     */
    NONE(0),

    /**
     * Specifies including container metadata.
     */
    METADATA(1);

    /**
     * Returns the value of this enum.
     */
    int value;

    /**
     * Sets the value of this enum.
     * 
     * @param val
     *            The value being assigned.
     */
    QueueListingDetails(int val) {
        this.value = val;
    }
}
