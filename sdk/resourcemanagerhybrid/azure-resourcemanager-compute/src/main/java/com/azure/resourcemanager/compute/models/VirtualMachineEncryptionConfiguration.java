// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;

import java.util.Locale;
import java.util.Objects;

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
    protected String keyEncryptionKeyUrl;
    protected String keyEncryptionKeyVaultId;
    protected String encryptionAlgorithm = "RSA-OAEP";
    protected String passPhrase;
    protected final AzureEnvironment azureEnvironment;
    protected final String vaultUri;

    /**
     * Creates VirtualMachineEncryptionConfiguration.
     *
     * @param keyVaultId resource ID of the KeyVault to store the disk encryption key
     * @param vaultUri URI of the key vault data-plane endpoint
     * @param aadClientId AAD application client ID to access the KeyVault
     * @param aadSecret AAD application client secret to access the KeyVault
     * @param azureEnvironment Azure environment
     */
    protected VirtualMachineEncryptionConfiguration(String keyVaultId, String vaultUri,
                                                    String aadClientId, String aadSecret,
                                                    AzureEnvironment azureEnvironment) {
        this.keyVaultId =
            Objects
                .requireNonNull(
                    keyVaultId,
                    "KeyVaultId parameter holding resource id of the KeyVault to store disk encryption key is"
                        + " required.");
        this.aadClientId =
            Objects
                .requireNonNull(
                    aadClientId, "aadClientId parameter holding AAD client id to access the KeyVault is required.");
        this.aadSecret =
            Objects
                .requireNonNull(
                    aadSecret, "aadSecret parameter holding AAD secret to access the KeyVault is required.");
        this.vaultUri = vaultUri;
        this.azureEnvironment = azureEnvironment;
    }

    /**
     * Creates VirtualMachineEncryptionConfiguration.
     *
     * @param keyVaultId resource ID of the KeyVault to store the disk encryption key
     * @param vaultUri URI of the key vault data-plane endpoint
     * @param azureEnvironment Azure environment
     * */
    protected VirtualMachineEncryptionConfiguration(String keyVaultId, String vaultUri,
                                                    AzureEnvironment azureEnvironment) {
        Objects
            .requireNonNull(
                keyVaultId,
                "KeyVaultId parameter holding resource id of the keyVault to store disk encryption key is required.");
        this.keyVaultId = keyVaultId;
        this.aadClientId = null;
        this.aadSecret = null;
        this.vaultUri = vaultUri;
        this.azureEnvironment = azureEnvironment;
    }

    /** @return the operating system type */
    public abstract OperatingSystemTypes osType();

    /** @return the AAD application client ID to access the key vault */
    public String aadClientId() {
        return this.aadClientId;
    }

    /** @return the AAD application client secret to access the key vault */
    public String aadSecret() {
        return this.aadSecret;
    }

    /** @return type of the volume to perform encryption operation */
    public DiskVolumeType volumeType() {
        if (this.volumeType != null) {
            return this.volumeType;
        }
        return DiskVolumeType.ALL;
    }

    /** @return resource ID of the key vault to store the disk encryption key */
    public String keyVaultId() {
        return this.keyVaultId;
    }

    /** @return URL to the key vault to store the disk encryption key */
    public String keyVaultUrl() {
        if (vaultUri != null) {
            return vaultUri;
        }

        String keyVaultDnsSuffix =
            (azureEnvironment == null || CoreUtils.isNullOrEmpty(azureEnvironment.getKeyVaultDnsSuffix()))
                ? AzureEnvironment.AZURE.getKeyVaultDnsSuffix()
                : azureEnvironment.getKeyVaultDnsSuffix();
        String keyVaultName = ResourceUtils.nameFromResourceId(this.keyVaultId);
        return String.format("https://%1$s%2$s", keyVaultName.toLowerCase(Locale.ROOT), keyVaultDnsSuffix);
    }

    /** @return resource ID of the Key Vault holding key encryption key (KEK) */
    public String keyEncryptionKeyVaultId() {
        return this.keyEncryptionKeyVaultId;
    }

    /** @return key vault URL to the key (KEK) to protect (encrypt) the disk-encryption key */
    public String keyEncryptionKeyUrl() {
        return this.keyEncryptionKeyUrl;
    }

    /** @return the algorithm used to encrypt the disk-encryption key */
    public String volumeEncryptionKeyEncryptAlgorithm() {
        return this.encryptionAlgorithm;
    }

    /** @return the pass phrase to encrypt Linux OS and data disks */
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
     * @param keyEncryptionKeyUrl the key (KEK) URL
     * @return VirtualMachineEncryptionConfiguration
     */
    public T withVolumeEncryptionKeyEncrypted(String keyEncryptionKeyUrl) {
        return withVolumeEncryptionKeyEncrypted(keyEncryptionKeyUrl, null);
    }

    /**
     * Specifies the and key vault Id and a vault URL to the key for protecting or wrapping the disk-encryption key.
     *
     * @param keyEncryptionKeyUrl the key (KEK) URL
     * @param keyEncryptionKeyKevVaultId resource ID of the keyVault storing KEK
     * @return VirtualMachineEncryptionConfiguration
     */
    @SuppressWarnings("unchecked")
    public T withVolumeEncryptionKeyEncrypted(String keyEncryptionKeyUrl, String keyEncryptionKeyKevVaultId) {
        this.keyEncryptionKeyUrl = keyEncryptionKeyUrl;
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
