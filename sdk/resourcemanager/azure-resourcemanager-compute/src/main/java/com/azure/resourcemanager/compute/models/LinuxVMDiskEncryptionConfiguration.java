// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

/** Type representing encryption settings to be applied to a Linux virtual machine. */
public class LinuxVMDiskEncryptionConfiguration
    extends VirtualMachineEncryptionConfiguration<LinuxVMDiskEncryptionConfiguration> {
    /**
     * Creates LinuxVMDiskEncryptionSettings.
     *
     * @param keyVaultId the resource ID of the KeyVault to store the disk encryption key
     * @param aadClientId client ID of an AAD application which has permission to the KeyVault
     * @param aadSecret client secret corresponding to the client ID
     */
    public LinuxVMDiskEncryptionConfiguration(String keyVaultId, String aadClientId, String aadSecret) {
        super(keyVaultId, aadClientId, aadSecret);
    }

    /**
     * Creates LinuxVMDiskEncryptionSettings.
     *
     * @param keyVaultId the resource ID of the KeyVault to store the disk encryption key
     */
    public LinuxVMDiskEncryptionConfiguration(String keyVaultId) {
        super(keyVaultId);
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
