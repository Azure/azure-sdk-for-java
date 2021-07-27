// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

/**
 * The KeyProperties REST model.
 */
public class KeyProperties {

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
