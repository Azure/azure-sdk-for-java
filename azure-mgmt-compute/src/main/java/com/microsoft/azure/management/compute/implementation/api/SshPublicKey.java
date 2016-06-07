/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;


/**
 * Contains information about SSH certificate public key and the path on the
 * Linux VM where the public key is placed.
 */
public class SshPublicKey {
    /**
     * Gets or sets the full path on the created VM where SSH public key is
     * stored. If the file already exists, the specified key is appended to
     * the file.
     */
    private String path;

    /**
     * Gets or sets Certificate public key used to authenticate with VM
     * through SSH.The certificate must be in Pem format with or without
     * headers.
     */
    private String keyData;

    /**
     * Get the path value.
     *
     * @return the path value
     */
    public String path() {
        return this.path;
    }

    /**
     * Set the path value.
     *
     * @param path the path value to set
     * @return the SshPublicKey object itself.
     */
    public SshPublicKey withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Get the keyData value.
     *
     * @return the keyData value
     */
    public String keyData() {
        return this.keyData;
    }

    /**
     * Set the keyData value.
     *
     * @param keyData the keyData value to set
     * @return the SshPublicKey object itself.
     */
    public SshPublicKey withKeyData(String keyData) {
        this.keyData = keyData;
        return this;
    }

}
