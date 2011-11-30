package com.microsoft.windowsazure.services.blob.client;

/**
 * Specifies which details to include when listing the containers in this storage account.
 * 
 */
public enum ContainerListingDetails {
    /**
     * Specifies including all available details.
     */
    ALL(1),

    /**
     * Specifies including container metadata.
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
    ContainerListingDetails(final int val) {
        this.value = val;
    }
}
