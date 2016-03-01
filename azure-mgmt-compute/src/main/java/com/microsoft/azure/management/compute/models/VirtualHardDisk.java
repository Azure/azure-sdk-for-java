/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;


/**
 * Describes the uri of a disk.
 */
public class VirtualHardDisk {
    /**
     * Gets or sets the virtual hard disk's uri. It should be a valid Uri to a
     * virtual hard disk.
     */
    private String uri;

    /**
     * Get the uri value.
     *
     * @return the uri value
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Set the uri value.
     *
     * @param uri the uri value to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

}
