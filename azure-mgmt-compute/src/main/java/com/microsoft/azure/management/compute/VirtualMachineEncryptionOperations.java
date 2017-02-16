/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import rx.Observable;

/**
 * Virtual machine encryption related operations.
 */
public interface VirtualMachineEncryptionOperations {
    /**
     * Enable encryption for virtual machine disks.
     *
     * @param keyVaultId resource id of the key vault to store the disk encryption key
     * @param aadClientId  client id of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     *
     * @return observable that emits current volume encryption status
     */
    Observable<DiskVolumeEncryptionStatus> enableAsync(String keyVaultId, String aadClientId, String aadSecret);

    /**
     * Enable encryption for Windows virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     *
     * @return observable that emits current volume encryption status
     */
    Observable<DiskVolumeEncryptionStatus> enableAsync(WindowsVMDiskEncryptionSettings encryptionSettings);

    /**
     * Enable encryption for Linux virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     *
     * @return observable that emits current volume encryption status
     */
    Observable<DiskVolumeEncryptionStatus> enableAsync(LinuxVMDiskEncryptionSettings encryptionSettings);

    /**
     * Disable encryption for virtual machine disks.
     * @param volumeType volume type to disable encryption
     *
     * @return observable that emits current volume decryption status
     */
    Observable<DiskVolumeEncryptionStatus> disableAsync(final DiskVolumeTypes volumeType);

    /**
     * @return observable that emits current volume decryption status
     */
    Observable<DiskVolumeEncryptionStatus> getStatusAsync();

    /**
     * Enable encryption for virtual machine disks.
     *
     * @param keyVaultId resource id of the key vault to store the disk encryption key
     * @param aadClientId  client id of an AAD application which has permission to the key vault
     * @param aadSecret client secret corresponding to the aadClientId
     *
     * @return current volume decryption status
     */
    DiskVolumeEncryptionStatus enable(String keyVaultId, String aadClientId, String aadSecret);

    /**
     * Enable encryption for Windows virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     *
     * @return current volume encryption status
     */
    DiskVolumeEncryptionStatus enable(WindowsVMDiskEncryptionSettings encryptionSettings);

    /**
     * Enable encryption for Linux virtual machine disks.
     *
     * @param encryptionSettings encryption settings for windows virtual machine
     *
     * @return current volume encryption status
     */
    DiskVolumeEncryptionStatus enable(LinuxVMDiskEncryptionSettings encryptionSettings);

    /**
     * Disable encryption for virtual machine disks.
     * @param volumeType volume type to disable encryption
     *
     * @return current volume encryption status
     */
    DiskVolumeEncryptionStatus disable(final DiskVolumeTypes volumeType);

    /**
     * @return current volume decryption status
     */
    DiskVolumeEncryptionStatus getStatus();
}
