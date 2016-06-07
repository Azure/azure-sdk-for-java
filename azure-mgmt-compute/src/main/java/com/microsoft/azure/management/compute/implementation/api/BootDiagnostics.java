/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;


/**
 * Describes Boot Diagnostics.
 */
public class BootDiagnostics {
    /**
     * Gets or sets whether boot diagnostics should be enabled on the Virtual
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
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the BootDiagnostics object itself.
     */
    public BootDiagnostics withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the storageUri value.
     *
     * @return the storageUri value
     */
    public String storageUri() {
        return this.storageUri;
    }

    /**
     * Set the storageUri value.
     *
     * @param storageUri the storageUri value to set
     * @return the BootDiagnostics object itself.
     */
    public BootDiagnostics withStorageUri(String storageUri) {
        this.storageUri = storageUri;
        return this;
    }

}
