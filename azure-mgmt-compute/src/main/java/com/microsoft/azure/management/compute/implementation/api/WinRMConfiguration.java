/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

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
    public List<WinRMListener> listeners() {
        return this.listeners;
    }

    /**
     * Set the listeners value.
     *
     * @param listeners the listeners value to set
     * @return the WinRMConfiguration object itself.
     */
    public WinRMConfiguration withListeners(List<WinRMListener> listeners) {
        this.listeners = listeners;
        return this;
    }

}
