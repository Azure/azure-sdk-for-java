/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;


/**
 * Describes Boot Diagnostics.
 */
public class BootDiagnostics {
    /**
     * Gets or sets whether VM Agent should be provisioned on the Virtual
     * Machine.
     */
    private Boolean enabled;

    /**
     * Gets or sets the boot diagnostics storage Uri. It should be a valid Uri.
     */
    private String storageUri;

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean getEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get the storageUri value.
     *
     * @return the storageUri value
     */
    public String getStorageUri() {
        return this.storageUri;
    }

    /**
     * Set the storageUri value.
     *
     * @param storageUri the storageUri value to set
     */
    public void setStorageUri(String storageUri) {
        this.storageUri = storageUri;
    }

}
