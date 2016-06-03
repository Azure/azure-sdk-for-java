/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a Encryption Settings for a Disk.
 */
public class DiskEncryptionSettings {
    /**
     * Gets or sets the disk encryption key which is a KeyVault Secret.
     */
    @JsonProperty(required = true)
    private KeyVaultSecretReference diskEncryptionKey;

    /**
     * Gets or sets the key encryption key which is KeyVault Key.
     */
    private KeyVaultKeyReference keyEncryptionKey;

    /**
     * Gets or sets whether disk encryption should be enabled on the Virtual
     * Machine.
     */
    private Boolean enabled;

    /**
     * Get the diskEncryptionKey value.
     *
     * @return the diskEncryptionKey value
     */
    public KeyVaultSecretReference diskEncryptionKey() {
        return this.diskEncryptionKey;
    }

    /**
     * Set the diskEncryptionKey value.
     *
     * @param diskEncryptionKey the diskEncryptionKey value to set
     * @return the DiskEncryptionSettings object itself.
     */
    public DiskEncryptionSettings withDiskEncryptionKey(KeyVaultSecretReference diskEncryptionKey) {
        this.diskEncryptionKey = diskEncryptionKey;
        return this;
    }

    /**
     * Get the keyEncryptionKey value.
     *
     * @return the keyEncryptionKey value
     */
    public KeyVaultKeyReference keyEncryptionKey() {
        return this.keyEncryptionKey;
    }

    /**
     * Set the keyEncryptionKey value.
     *
     * @param keyEncryptionKey the keyEncryptionKey value to set
     * @return the DiskEncryptionSettings object itself.
     */
    public DiskEncryptionSettings withKeyEncryptionKey(KeyVaultKeyReference keyEncryptionKey) {
        this.keyEncryptionKey = keyEncryptionKey;
        return this;
    }

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
     * @return the DiskEncryptionSettings object itself.
     */
    public DiskEncryptionSettings withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}
