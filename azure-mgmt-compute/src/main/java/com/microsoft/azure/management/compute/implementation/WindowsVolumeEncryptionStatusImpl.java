/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.DiskEncryptionSettings;
import com.microsoft.azure.management.compute.DiskVolumeEncryptionStatus;
import com.microsoft.azure.management.compute.EncryptionStatuses;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Func1;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * The implementation for DiskVolumeEncryptionStatus for Windows virtual machine.
 */
class WindowsVolumeEncryptionStatusImpl implements DiskVolumeEncryptionStatus {
    private final String virtualMachineId;
    private final ComputeManager computeManager;
    private VirtualMachineExtensionInner encryptionExtension;
    private VirtualMachineInner virtualMachine;

    /**
     * Creates WindowsVolumeEncryptionStatusImpl.
     *
     * @param virtualMachineId resource id of Windows virtual machine to retrieve encryption status from
     * @param computeManager compute manager
     */
    WindowsVolumeEncryptionStatusImpl(String virtualMachineId, ComputeManager computeManager) {
        this.virtualMachineId = virtualMachineId;
        this.computeManager = computeManager;
    }

    @Override
    public String progressMessage() {
        if (!hasEncryptionDetails()) {
            return null;
        }
        return String.format("OSDisk: %s DataDisk: %s", osDiskStatus(), dataDiskStatus());
    }

    @Override
    public EncryptionStatuses osDiskStatus() {
        if (!hasEncryptionDetails()) {
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        if (encryptionExtension.provisioningState() == null
                || !encryptionExtension.provisioningState().equalsIgnoreCase("Succeeded")) {
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        if (this.virtualMachine.storageProfile() == null
                || virtualMachine.storageProfile().osDisk() == null
                || virtualMachine.storageProfile().osDisk().encryptionSettings() == null) {
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        DiskEncryptionSettings encryptionSettings = virtualMachine
                .storageProfile()
                .osDisk()
                .encryptionSettings();
        if (encryptionSettings.diskEncryptionKey() != null
                && encryptionSettings.diskEncryptionKey().secretUrl() != null
                && Utils.toPrimitiveBoolean(encryptionSettings.enabled())) {
            return EncryptionStatuses.ENCRYPTED;
        }
        return EncryptionStatuses.NOT_ENCRYPTED;
    }

    @Override
    public EncryptionStatuses dataDiskStatus() {
        if (!hasEncryptionDetails()) {
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        if (encryptionExtension.provisioningState() == null
                || !encryptionExtension.provisioningState().equalsIgnoreCase("Succeeded")) {
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        HashMap<String, Object> publicSettings = new LinkedHashMap<>();
        if (encryptionExtension.settings() == null) {
            publicSettings = (LinkedHashMap<String, Object>) encryptionExtension.settings();
        }
        if (!publicSettings.containsKey("VolumeType")
                || publicSettings.get("VolumeType") == null
                || ((String) publicSettings.get("VolumeType")).isEmpty()
                || ((String) publicSettings.get("VolumeType")).equalsIgnoreCase("All")
                || ((String) publicSettings.get("VolumeType")).equalsIgnoreCase("Data")) {
            String encryptionOperation = (String) publicSettings.get("EncryptionOperation");
            if (encryptionOperation != null && encryptionOperation.equalsIgnoreCase("EnableEncryption")) {
                return EncryptionStatuses.ENCRYPTED;
            }
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        return EncryptionStatuses.UNKNOWN;
    }

    @Override
    public DiskVolumeEncryptionStatus refresh() {
        return refreshAsync().toBlocking().last();
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> refreshAsync() {
        final WindowsVolumeEncryptionStatusImpl self = this;
        return refreshVirtualMachineAsync()
                .flatMap(new Func1<VirtualMachineInner, Observable<DiskVolumeEncryptionStatus>>() {
                    @Override
                    public Observable<DiskVolumeEncryptionStatus> call(VirtualMachineInner virtualMachine) {
                        if (virtualMachine.resources() != null) {
                            for (VirtualMachineExtensionInner extension : virtualMachine.resources()) {
                                if (extension.publisher().equalsIgnoreCase("Microsoft.Azure.Security")
                                        && extension.typeHandlerVersion().equalsIgnoreCase("AzureDiskEncryption")) {
                                    self.encryptionExtension = extension;
                                    break;
                                }
                            }
                        }
                        return Observable.<DiskVolumeEncryptionStatus>just(self);
                    }
                });
    }

    private Observable<VirtualMachineInner> refreshVirtualMachineAsync() {
        final WindowsVolumeEncryptionStatusImpl self = this;
        final String rgName = ResourceUtils.groupFromResourceId(virtualMachineId);
        final String vmName = ResourceUtils.nameFromResourceId(virtualMachineId);
        return this.computeManager
                .inner()
                .virtualMachines()
                .getAsync(rgName, vmName)
                .flatMap(new Func1<VirtualMachineInner, Observable<VirtualMachineInner>>() {
                    @Override
                    public Observable<VirtualMachineInner> call(VirtualMachineInner virtualMachine) {
                        if (virtualMachine == null) {
                            return Observable.error(new Exception(String.format("VM with name '%s' not found (resource group '%s')",
                                    vmName, rgName)));
                        }
                        self.virtualMachine = virtualMachine;
                        return Observable.just(virtualMachine);
                    }
                });
    }

    private boolean hasEncryptionDetails() {
        return virtualMachine != null && this.encryptionExtension != null;
    }
}
