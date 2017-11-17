/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;

/**
 * Type representing encryption configuration to be applied to a virtual machine.
 *
 * @param <T> type presenting Windows or Linux specific settings
 */
public abstract class VirtualMachineEncryptionConfiguration<T extends VirtualMachineEncryptionConfiguration<T>> {
    protected final String keyVaultId;
    protected final String aadClientId;
    protected final String aadSecret;
    protected DiskVolumeType volumeType = DiskVolumeType.ALL;
    protected String keyEncryptionKeyURL;
    protected String keyEncryptionKeyVaultId;
    protected String encryptionAlgorithm = "RSA-OAEP";
    protected String passPhrase;

    /**
     * Creates VirtualMachineEncryptionConfiguration.
     *
     * @param keyVaultId resource ID of the key vault to store the disk encryption key
     * @param aadClientId AAD application client ID to access the key vault
     * @param aadSecret AAD application client secret to access the key vault
     */
    protected VirtualMachineEncryptionConfiguration(String keyVaultId,
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
     * @return the AAD application client ID to access the key vault
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
    public DiskVolumeType volumeType() {
        if (this.volumeType != null) {
            return this.volumeType;
        }
        return DiskVolumeType.ALL;
    }

    /**
     * @return resource ID of the key vault to store the disk encryption key
     */
    public String keyVaultId() {
        return this.keyVaultId;
    }

    /**
     * @return URL to the key vault to store the disk encryption key
     */
    public String keyVaultUrl() {
        String keyVaultName = ResourceUtils.nameFromResourceId(this.keyVaultId);
        return String.format("https://%s.vault.azure.net/", keyVaultName.toLowerCase());
    }

    /**
     * @return resource ID of the Key Vault holding key encryption key (KEK)
     */
    public String keyEncryptionKeyVaultId() {
        return this.keyEncryptionKeyVaultId;
    }

    /**
     * @return key vault URL to the key (KEK) to protect (encrypt) the disk-encryption key
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
     * @return VirtualMachineEncryptionConfiguration
     */
    @SuppressWarnings("unchecked")
    public T withVolumeType(DiskVolumeType volumeType) {
        this.volumeType = volumeType;
        return (T) this;
    }

    /**
     * Specifies the Key Vault URL to the key for protecting or wrapping the disk-encryption key.
     *
     * @param keyEncryptionKeyURL the key (KEK) URL
     * @return VirtualMachineEncryptionConfiguration
     */
    public T withVolumeEncryptionKeyEncrypted(String keyEncryptionKeyURL) {
        return withVolumeEncryptionKeyEncrypted(keyEncryptionKeyURL, null);
    }

    /**
     * Specifies the and key vault Id and a vault URL to the key for protecting or wrapping the disk-encryption key.
     *
     * @param keyEncryptionKeyURL the key (KEK) URL
     * @param keyEncryptionKeyKevVaultId resource ID of the keyVault storing KEK
     * @return VirtualMachineEncryptionConfiguration
     */
    @SuppressWarnings("unchecked")
    public T withVolumeEncryptionKeyEncrypted(String keyEncryptionKeyURL, String keyEncryptionKeyKevVaultId) {
        this.keyEncryptionKeyURL = keyEncryptionKeyURL;
        this.keyEncryptionKeyVaultId = keyEncryptionKeyKevVaultId;
        return (T) this;
    }

    /**
     * Specifies the algorithm used to encrypt the disk-encryption key.
     *
     * @param encryptionAlgorithm the algorithm
     * @return VirtualMachineEncryptionConfiguration
     */
    @SuppressWarnings("unchecked")
    public T withVolumeEncryptionKeyEncryptAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        return (T) this;
    }
}