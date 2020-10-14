// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import reactor.core.publisher.Mono;

/** Virtual machine encryption related operations. */
@Fluent
public interface VirtualMachineEncryption {
    /**
     * Enable encryption for virtual machine disks. Legacy encryption extension will be used to enable encryption.
     *
     * @param keyVaultId resource ID of the key vault to store the disk encryption key
     * @param aadClientId client ID of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     * @return a representation of the deferred computation of this call, returning the current volume encryption status
     */
    Mono<DiskVolumeEncryptionMonitor> enableAsync(String keyVaultId, String aadClientId, String aadSecret);

    /**
     * Enable encryption for virtual machine disks. NoAAD encryption extension will be used to enable encryption.
     *
     * @param keyVaultId resource ID of the key vault to store the disk encryption key
     * @return a representation of the deferred computation of this call, returning the current volume encryption status
     */
    Mono<DiskVolumeEncryptionMonitor> enableAsync(String keyVaultId);

    /**
     * Enable encryption for Windows virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     * @return a representation of the deferred computation of this call, returning the current volume encryption status
     */
    Mono<DiskVolumeEncryptionMonitor> enableAsync(WindowsVMDiskEncryptionConfiguration encryptionSettings);

    /**
     * Enable encryption for Linux virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     * @return a representation of the deferred computation of this call, returning the current volume encryption status
     */
    Mono<DiskVolumeEncryptionMonitor> enableAsync(LinuxVMDiskEncryptionConfiguration encryptionSettings);

    /**
     * Disable encryption for virtual machine disks.
     *
     * @param volumeType volume type to disable encryption
     * @return a representation of the deferred computation of this call, returning the current volume decryption status
     */
    Mono<DiskVolumeEncryptionMonitor> disableAsync(DiskVolumeType volumeType);

    /** @return observable that emits current volume encryption/decryption status */
    Mono<DiskVolumeEncryptionMonitor> getMonitorAsync();

    /**
     * Enable encryption for virtual machine disks.
     *
     * @param keyVaultId resource ID of the key vault to store the disk encryption key
     * @param aadClientId client ID of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     * @return current volume decryption status
     */
    DiskVolumeEncryptionMonitor enable(String keyVaultId, String aadClientId, String aadSecret);

    /**
     * Enable encryption for Windows virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     * @return current volume encryption status
     */
    DiskVolumeEncryptionMonitor enable(WindowsVMDiskEncryptionConfiguration encryptionSettings);

    /**
     * Enable encryption for Linux virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     * @return current volume encryption status
     */
    DiskVolumeEncryptionMonitor enable(LinuxVMDiskEncryptionConfiguration encryptionSettings);

    /**
     * Disable encryption for virtual machine disks.
     *
     * @param volumeType volume type to disable encryption
     * @return current volume encryption status
     */
    DiskVolumeEncryptionMonitor disable(DiskVolumeType volumeType);

    /** @return current volume decryption status */
    DiskVolumeEncryptionMonitor getMonitor();
}
