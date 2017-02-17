/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import rx.Observable;

/**
 * Type that can be used to monitor encryption enable and disable status of a virtual machine.
 *
 * User get access to implementation of this interface from following methods:
 *  1. VirtualMachineEncryption.[enable|disable]{1}[Async]{0-1}
 *  2. VirtualMachineEncryption.getMonitor[Async]{0-1}
 * It is possible that user first get monitor instance via 2 then starts encrypting the virtual
 * machine, in this case he can still use the same monitor instance to monitor the encryption
 * progress.
 */
public interface DiskVolumeEncryptionMonitor
        extends Refreshable<DiskVolumeEncryptionMonitor> {
    /**
     * @return operating system type of the virtual machine
     */
    OperatingSystemTypes osType();
    /**
     * @return the encryption progress message
     */
    String progressMessage();
    /**
     * @return operating system disk encryption status
     */
    EncryptionStatus osDiskStatus();
    /**
     * @return data disks encryption status
     */
    EncryptionStatus dataDiskStatus();
    /**
     * @return observable that emits encryption status once the refresh is done
     */
    Observable<DiskVolumeEncryptionMonitor> refreshAsync();
}
