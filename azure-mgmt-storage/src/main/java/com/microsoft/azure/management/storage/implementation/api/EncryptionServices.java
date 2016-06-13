/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;


/**
 * The encrypted services.
 */
public class EncryptionServices {
    /**
     * The blob service.
     */
    private EncryptionService blob;

    /**
     * Get the blob value.
     *
     * @return the blob value
     */
    public EncryptionService blob() {
        return this.blob;
    }

    /**
     * Set the blob value.
     *
     * @param blob the blob value to set
     * @return the EncryptionServices object itself.
     */
    public EncryptionServices withBlob(EncryptionService blob) {
        this.blob = blob;
        return this;
    }

}
