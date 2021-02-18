// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.management.AzureEnvironment;

/** Type representing encryption settings to be applied to a Linux virtual machine. */
public class LinuxVMDiskEncryptionConfiguration
    extends VirtualMachineEncryptionConfiguration<LinuxVMDiskEncryptionConfiguration> {
    /**
     * Creates LinuxVMDiskEncryptionSettings.
     *
     * Recommend to use the alternative constructor to provide vaultUri.
     *
     * @param keyVaultId the resource ID of the KeyVault to store the disk encryption key
     * @param aadClientId client ID of an AAD application which has permission to the KeyVault
     * @param aadSecret client secret corresponding to the client ID
     */
    public LinuxVMDiskEncryptionConfiguration(String keyVaultId, String aadClientId, String aadSecret) {
        super(keyVaultId, null, aadClientId, aadSecret, null);
    }

    /**
     * Creates LinuxVMDiskEncryptionSettings.
     *
     * @param keyVaultId the resource ID of the KeyVault to store the disk encryption key
     * @param vaultUri URI of the key vault data-plane endpoint
     * @param aadClientId client ID of an AAD application which has permission to the KeyVault
     * @param aadSecret client secret corresponding to the client ID
     */
    public LinuxVMDiskEncryptionConfiguration(String keyVaultId,
                                              String vaultUri,
                                              String aadClientId,
                                              String aadSecret) {
        super(keyVaultId, vaultUri, aadClientId, aadSecret, null);
    }

    /**
     * Creates LinuxVMDiskEncryptionSettings.
     *
     * Recommend to use the alternative constructor to provide vaultUri.
     *
     * @param keyVaultId the resource ID of the KeyVault to store the disk encryption key
     * @param aadClientId client ID of an AAD application which has permission to the KeyVault
     * @param aadSecret client secret corresponding to the client ID
     * @param azureEnvironment Azure environment
     */
    public LinuxVMDiskEncryptionConfiguration(String keyVaultId,
                                              String aadClientId,
                                              String aadSecret,
                                              AzureEnvironment azureEnvironment) {
        super(keyVaultId, null, aadClientId, aadSecret, azureEnvironment);
    }

    /**
     * Creates LinuxVMDiskEncryptionSettings.
     *
     * @param keyVaultId the resource ID of the KeyVault to store the disk encryption key
     */
    public LinuxVMDiskEncryptionConfiguration(String keyVaultId) {
        super(keyVaultId, null, null);
    }

    /**
     * Creates LinuxVMDiskEncryptionSettings.
     *
     * @param keyVaultId the resource ID of the KeyVault to store the disk encryption key
     * @param vaultUri URI of the key vault data-plane endpoint
     */
    public LinuxVMDiskEncryptionConfiguration(String keyVaultId, String vaultUri) {
        super(keyVaultId, vaultUri, null);
    }

    /**
     * Creates LinuxVMDiskEncryptionSettings.
     *
     * Recommend to use the alternative constructor to provide vaultUri.
     *
     * @param keyVaultId the resource ID of the KeyVault to store the disk encryption key
     * @param azureEnvironment Azure environment
     */
    public LinuxVMDiskEncryptionConfiguration(String keyVaultId, AzureEnvironment azureEnvironment) {
        super(keyVaultId, null, azureEnvironment);
    }

    @Override
    public OperatingSystemTypes osType() {
        return OperatingSystemTypes.LINUX;
    }

    /**
     * Specifies the pass phrase for encrypting Linux OS or data disks.
     *
     * @param passPhrase the pass phrase
     * @return LinuxVMDiskEncryptionSettings
     */
    public LinuxVMDiskEncryptionConfiguration withPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
        return this;
    }
}
