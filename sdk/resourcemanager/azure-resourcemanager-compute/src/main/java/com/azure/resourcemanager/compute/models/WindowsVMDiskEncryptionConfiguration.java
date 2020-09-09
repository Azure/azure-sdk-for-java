// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

/** Type representing encryption configuration to be applied to a Windows virtual machine. */
public class WindowsVMDiskEncryptionConfiguration
    extends VirtualMachineEncryptionConfiguration<WindowsVMDiskEncryptionConfiguration> {
    /**
     * Creates WindowsVMDiskEncryptionConfiguration.
     *
     * @param keyVaultId the resource ID of the key vault to store the disk encryption key
     * @param aadClientId client ID of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     */
    public WindowsVMDiskEncryptionConfiguration(String keyVaultId, String aadClientId, String aadSecret) {
        super(keyVaultId, aadClientId, aadSecret);
    }

    /**
     * Creates WindowsVMDiskEncryptionConfiguration.
     *
     * @param keyVaultId the resource ID of the key vault to store the disk encryption key
     */
    public WindowsVMDiskEncryptionConfiguration(String keyVaultId) {
        super(keyVaultId);
    }

    @Override
    public OperatingSystemTypes osType() {
        return OperatingSystemTypes.WINDOWS;
    }
}
