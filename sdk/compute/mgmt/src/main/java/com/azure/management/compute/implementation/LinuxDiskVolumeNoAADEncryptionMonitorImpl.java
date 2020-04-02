/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.management.compute.DiskInstanceView;
import com.azure.management.compute.DiskVolumeEncryptionMonitor;
import com.azure.management.compute.EncryptionStatus;
import com.azure.management.compute.InstanceViewStatus;
import com.azure.management.compute.OperatingSystemTypes;
import com.azure.management.compute.VirtualMachineExtensionInstanceView;
import com.azure.management.compute.models.VirtualMachineInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * The implementation for DiskVolumeEncryptionStatus for Linux virtual machine.
 * This implementation monitor status of encrypt-decrypt through new NoAAD encryption extension.
 */
class LinuxDiskVolumeNoAADEncryptionMonitorImpl implements DiskVolumeEncryptionMonitor {
    private final String rgName;
    private final String vmName;
    private final ComputeManager computeManager;
    private VirtualMachineInner virtualMachine;
    private VirtualMachineExtensionInstanceView extensionInstanceView;

    /**
     * Creates LinuxDiskVolumeNoAADEncryptionMonitorImpl.
     *
     * @param virtualMachineId resource id of Linux virtual machine to retrieve encryption status from
     * @param computeManager compute manager
     */
    LinuxDiskVolumeNoAADEncryptionMonitorImpl(String virtualMachineId, ComputeManager computeManager) {
        this.rgName = ResourceUtils.groupFromResourceId(virtualMachineId);
        this.vmName = ResourceUtils.nameFromResourceId(virtualMachineId);
        this.computeManager = computeManager;
    }

    @Override
    public OperatingSystemTypes osType() {
        return OperatingSystemTypes.LINUX;
    }

    @Override
    public String progressMessage() {
        if (!hasEncryptionExtensionInstanceView()) {
            return null;
        }
        return LinuxEncryptionExtensionUtil.progressMessage(this.extensionInstanceView);
    }

    @Override
    public EncryptionStatus osDiskStatus() {
        if (!hasEncryptionExtensionInstanceView()) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        return LinuxEncryptionExtensionUtil.osDiskStatus(this.extensionInstanceView);
    }

    @Override
    public EncryptionStatus dataDiskStatus() {
        if (!hasEncryptionExtensionInstanceView()) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        return LinuxEncryptionExtensionUtil.dataDiskStatus(this.extensionInstanceView);
    }

    @Override
    public Map<String, InstanceViewStatus> diskInstanceViewEncryptionStatuses() {
        if (virtualMachine.instanceView() == null || virtualMachine.instanceView().disks() == null) {
            return new HashMap<String, InstanceViewStatus>();
        }
        //
        HashMap<String, InstanceViewStatus> div = new HashMap<String, InstanceViewStatus>();
        for (DiskInstanceView diskInstanceView : virtualMachine.instanceView().disks()) {
            for (InstanceViewStatus status : diskInstanceView.statuses()) {
                if (encryptionStatusFromCode(status.code()) != null) {
                    div.put(diskInstanceView.name(), status);
                    break;
                }
            }
        }
        return div;
    }

    @Override
    public DiskVolumeEncryptionMonitor refresh() {
        return refreshAsync().block();
    }

    @Override
    public Mono<DiskVolumeEncryptionMonitor> refreshAsync() {
        final LinuxDiskVolumeNoAADEncryptionMonitorImpl self = this;
        // Refreshes the cached virtual machine and installed encryption extension
        return retrieveVirtualMachineAsync()
                .flatMap(virtualMachine -> {
                    self.virtualMachine = virtualMachine;
                    if (virtualMachine.instanceView() != null && virtualMachine.instanceView().extensions() != null) {
                        for (VirtualMachineExtensionInstanceView eiv : virtualMachine.instanceView().extensions()) {
                            if (eiv.type() != null
                                    && eiv.type().toLowerCase().startsWith(EncryptionExtensionIdentifier.publisherName().toLowerCase())
                                    && eiv.name() != null
                                    && EncryptionExtensionIdentifier.isEncryptionTypeName(eiv.name(), osType())) {
                                self.extensionInstanceView = eiv;
                                break;
                            }
                        }
                    }
                    return Mono.just(self);
                });
    }

    /**
     * Retrieve the virtual machine.
     * If the virtual machine does not exists then an error observable will be returned.
     *
     * @return the retrieved virtual machine
     */
    private Mono<VirtualMachineInner> retrieveVirtualMachineAsync() {
        return computeManager.inner().virtualMachines()
                .getByResourceGroupAsync(rgName, vmName)
                .onErrorResume(e -> Mono.error(new Exception(String.format("VM with name '%s' not found (resource group '%s')", vmName, rgName))));
    }

    private boolean hasEncryptionExtensionInstanceView() {
        return this.extensionInstanceView != null;
    }

    /**
     * Given disk instance view status code, check whether it is encryption status code if yes map it to EncryptionStatus.
     *
     * @param code the encryption status code
     * @return mapped EncryptionStatus if given code is encryption status code, null otherwise.
     */
    private static EncryptionStatus encryptionStatusFromCode(String code) {
        if (code != null && code.toLowerCase().startsWith("encryptionstate")) {
            // e.g. "code": "EncryptionState/encrypted"
            //      "code": "EncryptionState/notEncrypted"
            String[] parts = code.split("/", 2);
            if (parts.length != 2) {
                return EncryptionStatus.UNKNOWN;
            } else {
                return EncryptionStatus.fromString(parts[1]);
            }
        }
        return null;
    }
}
