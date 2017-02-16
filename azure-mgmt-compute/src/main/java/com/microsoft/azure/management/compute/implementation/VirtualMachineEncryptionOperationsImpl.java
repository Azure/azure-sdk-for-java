/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.DiskVolumeEncryptionStatus;
import com.microsoft.azure.management.compute.DiskVolumeTypes;
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
    private final VirtualMachine virtualMachine;
    private final VirtualMachineDiskEncrypt virtualMachineDiskEncrypt;

    /**
     * Creates VirtualMachineEncryptionOperationsImpl.
     *
     * @param virtualMachine virtual machine on which encryption related operations to be performed
     */
    VirtualMachineEncryptionOperationsImpl(final VirtualMachine virtualMachine) {
        this.virtualMachine = virtualMachine;
        this.virtualMachineDiskEncrypt = new VirtualMachineDiskEncrypt(virtualMachine);
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> enableAsync(String keyVaultId, String aadClientId, String aadSecret) {
        if (this.virtualMachine.osType() == OperatingSystemTypes.LINUX) {
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
    public Observable<DiskVolumeEncryptionStatus> disableAsync(final DiskVolumeTypes volumeType) {
        return virtualMachineDiskEncrypt.disableEncryptionAsync(volumeType);
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> getStatusAsync() {
        if (this.virtualMachine.osType() == OperatingSystemTypes.LINUX) {
            return new LinuxDiskVolumeEncryptionStatusImpl(virtualMachine.id(), virtualMachine.manager()).refreshAsync();
        } else {
            return new WindowsVolumeEncryptionStatusImpl(virtualMachine.id(), virtualMachine.manager()).refreshAsync();
        }
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
    public DiskVolumeEncryptionStatus disable(final DiskVolumeTypes volumeType) {
        return disableAsync(volumeType).toBlocking().last();
    }

    @Override
    public DiskVolumeEncryptionStatus getStatus() {
        return getStatusAsync().toBlocking().last();
    }
}
