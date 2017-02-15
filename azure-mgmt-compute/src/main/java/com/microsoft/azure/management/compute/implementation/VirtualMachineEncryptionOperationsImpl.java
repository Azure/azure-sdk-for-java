/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.DiskVolumeEncryptionStatus;
import com.microsoft.azure.management.compute.LinuxVMDiskEncryptionSettings;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineEncryptionOperations;
import com.microsoft.azure.management.compute.WindowsVMDiskEncryptionSettings;
import rx.Observable;

/**
 * Implementation of VirtualMachineEncryptionOperations.
 */
class VirtualMachineEncryptionOperationsImpl implements VirtualMachineEncryptionOperations {
    private final VirtualMachineDiskEncrypt virtualMachineDiskEncrypt;
    private final OperatingSystemTypes osType;

    VirtualMachineEncryptionOperationsImpl(final VirtualMachine virtualMachine) {
        this.virtualMachineDiskEncrypt = new VirtualMachineDiskEncrypt(virtualMachine);
        this.osType = virtualMachine.osType();
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> enableAsync(String keyVaultId, String aadClientId, String aadSecret) {
        if (this.osType == OperatingSystemTypes.LINUX) {
            return enableAsync(new LinuxVMDiskEncryptionSettings(keyVaultId, aadClientId, aadSecret));
        } else {
            return enableAsync(new WindowsVMDiskEncryptionSettings(keyVaultId, aadClientId, aadSecret));
        }
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> enableAsync(WindowsVMDiskEncryptionSettings encryptionSettings) {
        return virtualMachineDiskEncrypt.enableEncryptionAsync(encryptionSettings);
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> enableAsync(LinuxVMDiskEncryptionSettings encryptionSettings) {
        return virtualMachineDiskEncrypt.enableEncryptionAsync(encryptionSettings);
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> disableAsync() {
        return virtualMachineDiskEncrypt.disableEncryptionAsync();
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> getStatusAsync() {
        return virtualMachineDiskEncrypt.getEncryptionStatusAsync();
    }

    @Override
    public DiskVolumeEncryptionStatus enable(String keyVaultId, String aadClientId, String aadSecret) {
        return enableAsync(keyVaultId, aadClientId, aadSecret).toBlocking().last();
    }

    @Override
    public DiskVolumeEncryptionStatus enable(WindowsVMDiskEncryptionSettings encryptionSettings) {
        return enableAsync(encryptionSettings).toBlocking().last();
    }

    @Override
    public DiskVolumeEncryptionStatus enable(LinuxVMDiskEncryptionSettings encryptionSettings) {
        return enableAsync(encryptionSettings).toBlocking().last();
    }

    @Override
    public DiskVolumeEncryptionStatus disable() {
        return disableAsync().toBlocking().last();
    }

    @Override
    public DiskVolumeEncryptionStatus getStatus() {
        return getStatusAsync().toBlocking().last();
    }
}
