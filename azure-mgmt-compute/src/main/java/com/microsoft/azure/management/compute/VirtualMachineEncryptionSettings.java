/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;

/**
 * Type representing encryption settings to be applied to a virtual machine.
 *
 * @param <T> type presenting Windows or Linux specific settings
 */
public abstract class VirtualMachineEncryptionSettings<T extends VirtualMachineEncryptionSettings> {
    protected final String keyVaultId;
    protected final String aadClientId;
    protected final String aadSecret;
    protected DiskVolumeTypes volumeType = DiskVolumeTypes.ALL;
    protected String keyEncryptionKeyURL;
    protected String keyEncryptionKeyVaultId;
    protected String encryptionAlgorithm = "RSA-OAEP";
    protected String passPhrase;

    protected VirtualMachineEncryptionSettings(String keyVaultId,
                                               String aadClientId,
                                               String aadSecret) {
        this.keyVaultId = keyVaultId;
        this.aadClientId = aadClientId;
        this.aadSecret = aadSecret;
    }

    /**
     * @return the operating system type
     */
    public abstract OperatingSystemTypes osType();

    /**
     * @return the AAD application client id to access the key vault
     */
    public String aadClientId() {
        return this.aadClientId;
    }

    /**
     * @return the AAD application client secret to access the key vault
     */
    public String aadSecret() {
        return this.aadSecret;
    }

    /**
     * @return type of the volume to perform encryption operation
     */
    public DiskVolumeTypes volumeType() {
        if (this.volumeType != null) {
            return this.volumeType;
        }
        return DiskVolumeTypes.ALL;
    }

    /**
     * @return resource id of the key vault to store the disk encryption key
     */
    public String keyVaultId() {
        return this.keyVaultId;
    }

    /**
     * @return url to the key vault to store the disk encryption key
     */
    public String keyVaultUrl() {
        String keyVaultName = ResourceUtils.nameFromResourceId(this.keyVaultId);
        return String.format("https://%s.vault.azure.net/", keyVaultName.toLowerCase());
    }

    /**
     * @return resource id of the key vault holding key encryption key (KEK)
     */
    public String keyEncryptionKeyVaultId() {
        return this.keyEncryptionKeyVaultId;
    }

    /**
     * @return key vault url to the key (KEK) to protect (encrypt) the disk-encryption key
     */
    public String keyEncryptionKeyURL() {
        return this.keyEncryptionKeyURL;
    }

    /**
     * @return the algorithm used to encrypt the disk-encryption key
     */
    public String volumeEncryptionKeyEncryptAlgorithm() {
        return this.encryptionAlgorithm;
    }

    /**
     * @return the pass phrase to encrypt Linux OS and data disks
     */
    public String linuxPassPhrase() {
        return this.passPhrase;
    }

    /**
     * Specifies the volume to encrypt.
     *
     * @param volumeType the volume type
     * @return VirtualMachineEncryptionSettings
     */
    public T withVolumeType(DiskVolumeTypes volumeType) {
        this.volumeType = volumeType;
        return (T) this;
    }

    /**
     * Specifies the key vault url to the key for protecting or wrapping the disk-encryption key.
     *
     * @param keyEncryptionKeyKevVaultId resource id of the keyVault storing KEK
     * @param keyEncryptionKeyURL the key (KEK) url
     * @return VirtualMachineEncryptionSettings
     */
    public T withVolumeEncryptionKeyEncrypted(String keyEncryptionKeyKevVaultId, String keyEncryptionKeyURL) {
        this.keyEncryptionKeyVaultId = keyEncryptionKeyKevVaultId;
        this.keyEncryptionKeyURL = keyEncryptionKeyURL;
        return (T) this;
    }

    /**
     * Specifies the algorithm used to encrypt the disk-encryption key.
     *
     * @param encryptionAlgorithm the algorithm
     * @return VirtualMachineEncryptionSettings
     */
    public T withVolumeEncryptionKeyEncryptAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        return (T) this;
    }
}