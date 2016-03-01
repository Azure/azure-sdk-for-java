/**
 * Object]
 */

package com.microsoft.azure.management.website.models;


/**
 * Publishing options for requested profile.
 */
public class CsmPublishingProfileOptions {
    /**
     * Name of the format. Valid values are:
     * FileZilla3
     * WebDeploy -- default
     * Ftp.
     */
    private String format;

    /**
     * Get the format value.
     *
     * @return the format value
     */
    public String getFormat() {
        return this.format;
    }

    /**
     * Set the format value.
     *
     * @param format the format value to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

}
