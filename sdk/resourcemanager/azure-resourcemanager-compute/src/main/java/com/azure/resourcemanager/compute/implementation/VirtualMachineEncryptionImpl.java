// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.DiskVolumeEncryptionMonitor;
import com.azure.resourcemanager.compute.models.DiskVolumeType;
import com.azure.resourcemanager.compute.models.LinuxVMDiskEncryptionConfiguration;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineEncryption;
import com.azure.resourcemanager.compute.models.WindowsVMDiskEncryptionConfiguration;
import reactor.core.publisher.Mono;

/** Implementation of VirtualMachineEncryption. */
class VirtualMachineEncryptionImpl implements VirtualMachineEncryption {
    private final VirtualMachine virtualMachine;
    private final VirtualMachineEncryptionHelper virtualMachineEncryptionHelper;

    /**
     * Creates VirtualMachineEncryptionImpl.
     *
     * @param virtualMachine virtual machine on which encryption related operations to be performed
     */
    VirtualMachineEncryptionImpl(final VirtualMachine virtualMachine) {
        this.virtualMachine = virtualMachine;
        this.virtualMachineEncryptionHelper = new VirtualMachineEncryptionHelper(virtualMachine);
    }

    @Override
    public Mono<DiskVolumeEncryptionMonitor> enableAsync(String keyVaultId, String aadClientId, String aadSecret) {
        if (this.virtualMachine.osType() == OperatingSystemTypes.LINUX) {
            return enableAsync(new LinuxVMDiskEncryptionConfiguration(keyVaultId, aadClientId, aadSecret));
        } else {
            return enableAsync(new WindowsVMDiskEncryptionConfiguration(keyVaultId, aadClientId, aadSecret));
        }
    }

    @Override
    public Mono<DiskVolumeEncryptionMonitor> enableAsync(String keyVaultId) {
        if (this.virtualMachine.osType() == OperatingSystemTypes.LINUX) {
            return enableAsync(new LinuxVMDiskEncryptionConfiguration(keyVaultId));
        } else {
            return enableAsync(new WindowsVMDiskEncryptionConfiguration(keyVaultId));
        }
    }

    @Override
    public Mono<DiskVolumeEncryptionMonitor> enableAsync(WindowsVMDiskEncryptionConfiguration encryptionSettings) {
        return virtualMachineEncryptionHelper.enableEncryptionAsync(encryptionSettings);
    }

    @Override
    public Mono<DiskVolumeEncryptionMonitor> enableAsync(LinuxVMDiskEncryptionConfiguration encryptionSettings) {
        return virtualMachineEncryptionHelper.enableEncryptionAsync(encryptionSettings);
    }

    @Override
    public Mono<DiskVolumeEncryptionMonitor> disableAsync(final DiskVolumeType volumeType) {
        return virtualMachineEncryptionHelper.disableEncryptionAsync(volumeType);
    }

    @Override
    public Mono<DiskVolumeEncryptionMonitor> getMonitorAsync() {
        return new ProxyEncryptionMonitorImpl(this.virtualMachine).refreshAsync();
    }

    @Override
    public DiskVolumeEncryptionMonitor enable(String keyVaultId, String aadClientId, String aadSecret) {
        return enableAsync(keyVaultId, aadClientId, aadSecret).block();
    }

    @Override
    public DiskVolumeEncryptionMonitor enable(WindowsVMDiskEncryptionConfiguration encryptionSettings) {
        return enableAsync(encryptionSettings).block();
    }

    @Override
    public DiskVolumeEncryptionMonitor enable(LinuxVMDiskEncryptionConfiguration encryptionSettings) {
        return enableAsync(encryptionSettings).block();
    }

    @Override
    public DiskVolumeEncryptionMonitor disable(final DiskVolumeType volumeType) {
        return disableAsync(volumeType).block();
    }

    @Override
    public DiskVolumeEncryptionMonitor getMonitor() {
        return getMonitorAsync().block();
    }
}
