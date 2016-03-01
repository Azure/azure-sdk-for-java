/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import java.util.List;

/**
 * Describes Windows Remote Management configuration of the VM.
 */
public class WinRMConfiguration {
    /**
     * Gets or sets the list of Windows Remote Management listeners.
     */
    private List<WinRMListener> listeners;

    /**
     * Get the listeners value.
     *
     * @return the listeners value
     */
    public List<WinRMListener> getListeners() {
        return this.listeners;
    }

    /**
     * Set the listeners value.
     *
     * @param listeners the listeners value to set
     */
    public void setListeners(List<WinRMListener> listeners) {
        this.listeners = listeners;
    }

}
