/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;

import rx.Observable;

/**
 * Virtual machine encryption related operations.
 */
@Fluent
public interface VirtualMachineEncryption {
    /**
     * Enable encryption for virtual machine disks.
     *
     * @param keyVaultId resource ID of the key vault to store the disk encryption key
     * @param aadClientId  client ID of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     *
     * @return observable that emits current volume encryption status
     */
    @Beta
    Observable<DiskVolumeEncryptionMonitor> enableAsync(String keyVaultId, String aadClientId, String aadSecret);

    /**
     * Enable encryption for Windows virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine

     * @return observable that emits current volume encryption status
     */
    @Beta
    Observable<DiskVolumeEncryptionMonitor> enableAsync(WindowsVMDiskEncryptionConfiguration encryptionSettings);

    /**
     * Enable encryption for Linux virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     *
     * @return observable that emits current volume encryption status
     */
    @Beta
    Observable<DiskVolumeEncryptionMonitor> enableAsync(LinuxVMDiskEncryptionConfiguration encryptionSettings);

    /**
     * Disable encryption for virtual machine disks.
     * @param volumeType volume type to disable encryption
     * @return observable that emits current volume decryption status
     */
    @Beta
    Observable<DiskVolumeEncryptionMonitor> disableAsync(final DiskVolumeType volumeType);

    /**
     * @return observable that emits current volume decryption status
     */
    @Beta
    Observable<DiskVolumeEncryptionMonitor> getMonitorAsync();

    /**
     * Enable encryption for virtual machine disks.
     *
     * @param keyVaultId resource ID of the key vault to store the disk encryption key
     * @param aadClientId  client ID of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     *
     * @return current volume decryption status
     */
    DiskVolumeEncryptionMonitor enable(String keyVaultId, String aadClientId, String aadSecret);

    /**
     * Enable encryption for Windows virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     *
     * @return current volume encryption status
     */
    DiskVolumeEncryptionMonitor enable(WindowsVMDiskEncryptionConfiguration encryptionSettings);

    /**
     * Enable encryption for Linux virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     *
     * @return current volume encryption status
     */
    DiskVolumeEncryptionMonitor enable(LinuxVMDiskEncryptionConfiguration encryptionSettings);

    /**
     * Disable encryption for virtual machine disks.
     * @param volumeType volume type to disable encryption
     *
     * @return current volume encryption status
     */
    DiskVolumeEncryptionMonitor disable(final DiskVolumeType volumeType);

    /**
     * @return current volume decryption status
     */
    DiskVolumeEncryptionMonitor getMonitor();
}
