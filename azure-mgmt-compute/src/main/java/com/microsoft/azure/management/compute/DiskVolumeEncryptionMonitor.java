/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import rx.Observable;

/**
 * Type that can be used to monitor encryption enable and disable status of a virtual machine.
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
    @Beta
    Observable<DiskVolumeEncryptionMonitor> refreshAsync();
}
