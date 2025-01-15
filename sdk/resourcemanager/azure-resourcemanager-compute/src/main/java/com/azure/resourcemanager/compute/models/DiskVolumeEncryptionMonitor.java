// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import java.util.Map;
import reactor.core.publisher.Mono;

/** Type that can be used to monitor encryption enable and disable status of a virtual machine. */
public interface DiskVolumeEncryptionMonitor extends Refreshable<DiskVolumeEncryptionMonitor> {
    /**
     * Gets operating system type of the virtual machine.
     *
     * @return operating system type of the virtual machine
     */
    OperatingSystemTypes osType();

    /**
     * Gets the encryption progress message.
     *
     * @return the encryption progress message
     */
    String progressMessage();

    /**
     * Gets operating system disk encryption status.
     *
     * @return operating system disk encryption status
     */
    EncryptionStatus osDiskStatus();

    /**
     * Gets data disks encryption status.
     *
     * @return data disks encryption status
     */
    EncryptionStatus dataDiskStatus();

    /**
     * Gets disks encryption status from instance view level.
     *
     * @return disks encryption status from instance view level.
     */
    Map<String, InstanceViewStatus> diskInstanceViewEncryptionStatuses();

    /**
     * Gets a representation of the deferred computation of this call returning the encryption status once the
     *     refresh is done.
     *
     * @return a representation of the deferred computation of this call returning the encryption status once the
     *     refresh is done
     */
    Mono<DiskVolumeEncryptionMonitor> refreshAsync();
}
