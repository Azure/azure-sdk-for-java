/**
 * Object]
 */

package com.microsoft.azure.management.website.models;


/**
 * The VirtualDirectory model.
 */
public class VirtualDirectory {
    /**
     * The virtualPath property.
     */
    private String virtualPath;

    /**
     * The physicalPath property.
     */
    private String physicalPath;

    /**
     * Get the virtualPath value.
     *
     * @return the virtualPath value
     */
    public String getVirtualPath() {
        return this.virtualPath;
    }

    /**
     * Set the virtualPath value.
     *
     * @param virtualPath the virtualPath value to set
     */
    public void setVirtualPath(String virtualPath) {
        this.virtualPath = virtualPath;
    }

    /**
     * Get the physicalPath value.
     *
     * @return the physicalPath value
     */
    public String getPhysicalPath() {
        return this.physicalPath;
    }

    /**
     * Set the physicalPath value.
     *
     * @param physicalPath the physicalPath value to set
     */
    public void setPhysicalPath(String physicalPath) {
        this.physicalPath = physicalPath;
    }

}
