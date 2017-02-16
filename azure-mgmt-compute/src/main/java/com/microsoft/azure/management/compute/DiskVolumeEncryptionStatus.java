/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import rx.Observable;

/**
 * Type represents encryption status of a virtual machine.
 */
public interface DiskVolumeEncryptionStatus
        extends Refreshable<DiskVolumeEncryptionStatus> {
    /**
     * @return the encryption progress message
     */
    String progressMessage();
    /**
     * @return operating system disk encryption status
     */
    EncryptionStatuses osDiskStatus();
    /**
     * @return data disks encryption status
     */
    EncryptionStatuses dataDiskStatus();

    /**
     * @return observable that emits encryption status once the refresh is done
     */
    Observable<DiskVolumeEncryptionStatus> refreshAsync();
}
