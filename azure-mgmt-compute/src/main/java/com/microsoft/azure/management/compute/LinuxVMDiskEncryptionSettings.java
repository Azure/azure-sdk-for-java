/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * Type representing encryption settings to be applied to a Linux virtual machine.
 */
@LangDefinition
public class LinuxVMDiskEncryptionSettings
        extends VirtualMachineEncryptionSettings<LinuxVMDiskEncryptionSettings> {

    /**
     * Creates LinuxVMDiskEncryptionSettings.
     *
     * @param keyVaultId the resource id of the key vault to store the disk encryption key
     * @param aadClientId  client id of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     */
    public LinuxVMDiskEncryptionSettings(String keyVaultId,
                                         String aadClientId,
                                         String aadSecret) {
        super(keyVaultId, aadClientId, aadSecret);
    }

    @Override
    public OperatingSystemTypes osType() {
        return OperatingSystemTypes.LINUX;
    }

    /**
     * Specifies the pass phrase for encrypting Linux OS or data disks
     *
     * @param passPhrase the pass phrase
     * @return LinuxVMDiskEncryptionSettings
     */
    public LinuxVMDiskEncryptionSettings withPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
        return this;
    }
}