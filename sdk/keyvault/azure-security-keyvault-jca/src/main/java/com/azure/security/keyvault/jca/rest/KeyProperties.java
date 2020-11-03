// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.rest;

import java.io.Serializable;

/**
 * The KeyProperties REST model.
 */
public class KeyProperties implements Serializable {
    
    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores if the key is exportable.
     */
    private boolean exportable;

    /**
     * Is the key exportable.
     *
     * @return true if exportable, false otherwise.
     */
    public boolean isExportable() {
        return exportable;
    }

    /**
     * Set the key to be exportable.
     *
     * @param exportable the exportable flag.
     */
    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }
}
