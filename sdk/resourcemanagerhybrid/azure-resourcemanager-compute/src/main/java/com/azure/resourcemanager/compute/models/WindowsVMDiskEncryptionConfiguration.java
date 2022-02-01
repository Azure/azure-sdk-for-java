// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.management.AzureEnvironment;

/** Type representing encryption configuration to be applied to a Windows virtual machine. */
public class WindowsVMDiskEncryptionConfiguration
    extends VirtualMachineEncryptionConfiguration<WindowsVMDiskEncryptionConfiguration> {
    /**
     * Creates WindowsVMDiskEncryptionConfiguration.
     *
     * Recommend to use the alternative constructor to provide vaultUri.
     *
     * @param keyVaultId the resource ID of the key vault to store the disk encryption key
     * @param aadClientId client ID of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     */
    public WindowsVMDiskEncryptionConfiguration(String keyVaultId, String aadClientId, String aadSecret) {
        super(keyVaultId, null, aadClientId, aadSecret, null);
    }

    /**
     * Creates WindowsVMDiskEncryptionConfiguration.
     *
     * @param keyVaultId the resource ID of the key vault to store the disk encryption key
     * @param vaultUri URI of the key vault data-plane endpoint
     * @param aadClientId client ID of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     */
    public WindowsVMDiskEncryptionConfiguration(String keyVaultId,
                                                String vaultUri,
                                                String aadClientId,
                                                String aadSecret) {
        super(keyVaultId, vaultUri, aadClientId, aadSecret, null);
    }

    /**
     * Creates WindowsVMDiskEncryptionConfiguration.
     *
     * Recommend to use the alternative constructor to provide vaultUri.
     *
     * @param keyVaultId the resource ID of the key vault to store the disk encryption key
     * @param aadClientId client ID of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     * @param azureEnvironment Azure environment
     */
    public WindowsVMDiskEncryptionConfiguration(String keyVaultId,
                                                String aadClientId,
                                                String aadSecret,
                                                AzureEnvironment azureEnvironment) {
        super(keyVaultId, null, aadClientId, aadSecret, azureEnvironment);
    }

    /**
     * Creates WindowsVMDiskEncryptionConfiguration.
     *
     * Recommend to use the alternative constructor to provide vaultUri.
     *
     * @param keyVaultId the resource ID of the key vault to store the disk encryption key
     */
    public WindowsVMDiskEncryptionConfiguration(String keyVaultId) {
        super(keyVaultId, null, null);
    }

    /**
     * Creates WindowsVMDiskEncryptionConfiguration.
     *
     * @param keyVaultId the resource ID of the key vault to store the disk encryption key
     * @param vaultUri URI of the key vault data-plane endpoint
     */
    public WindowsVMDiskEncryptionConfiguration(String keyVaultId,
                                                String vaultUri) {
        super(keyVaultId, vaultUri, null);
    }

    /**
     * Creates WindowsVMDiskEncryptionConfiguration.
     *
     * Recommend to use the alternative constructor to provide vaultUri.
     *
     * @param keyVaultId the resource ID of the key vault to store the disk encryption key
     * @param azureEnvironment Azure environment
     */
    public WindowsVMDiskEncryptionConfiguration(String keyVaultId,
                                                AzureEnvironment azureEnvironment) {
        super(keyVaultId, null, azureEnvironment);
    }

    @Override
    public OperatingSystemTypes osType() {
        return OperatingSystemTypes.WINDOWS;
    }
}
