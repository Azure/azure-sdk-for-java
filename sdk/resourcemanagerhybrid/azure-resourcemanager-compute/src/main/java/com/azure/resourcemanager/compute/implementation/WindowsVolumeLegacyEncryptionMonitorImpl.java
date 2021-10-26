// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.DiskEncryptionSettings;
import com.azure.resourcemanager.compute.models.DiskVolumeEncryptionMonitor;
import com.azure.resourcemanager.compute.models.EncryptionStatus;
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineExtensionInner;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * The implementation for DiskVolumeEncryptionStatus for Windows virtual machine. This implementation monitor status of
 * encrypt-decrypt through legacy encryption extension.
 */
class WindowsVolumeLegacyEncryptionMonitorImpl implements DiskVolumeEncryptionMonitor {
    private final String rgName;
    private final String vmName;
    private final ComputeManager computeManager;
    private VirtualMachineExtensionInner encryptionExtension;
    private VirtualMachineInner virtualMachine;

    /**
     * Creates WindowsVolumeLegacyEncryptionMonitorImpl.
     *
     * @param virtualMachineId resource id of Windows virtual machine to retrieve encryption status from
     * @param computeManager compute manager
     */
    WindowsVolumeLegacyEncryptionMonitorImpl(String virtualMachineId, ComputeManager computeManager) {
        this.rgName = ResourceUtils.groupFromResourceId(virtualMachineId);
        this.vmName = ResourceUtils.nameFromResourceId(virtualMachineId);
        this.computeManager = computeManager;
    }

    @Override
    public OperatingSystemTypes osType() {
        return OperatingSystemTypes.WINDOWS;
    }

    @Override
    public String progressMessage() {
        if (!hasEncryptionDetails()) {
            return null;
        }
        return String.format("OSDisk: %s DataDisk: %s", osDiskStatus(), dataDiskStatus());
    }

    @Override
    public EncryptionStatus osDiskStatus() {
        if (!hasEncryptionDetails()) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        if (encryptionExtension.provisioningState() == null) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        if (!encryptionExtension.provisioningState().equalsIgnoreCase("Succeeded")) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        if (this.virtualMachine.storageProfile() == null
            || virtualMachine.storageProfile().osDisk() == null
            || virtualMachine.storageProfile().osDisk().encryptionSettings() == null) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        DiskEncryptionSettings encryptionSettings = virtualMachine.storageProfile().osDisk().encryptionSettings();
        if (encryptionSettings.diskEncryptionKey() != null
            && encryptionSettings.diskEncryptionKey().secretUrl() != null
            && ResourceManagerUtils.toPrimitiveBoolean(encryptionSettings.enabled())) {
            return EncryptionStatus.ENCRYPTED;
        }
        return EncryptionStatus.NOT_ENCRYPTED;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EncryptionStatus dataDiskStatus() {
        if (!hasEncryptionDetails()) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        if (encryptionExtension.provisioningState() == null) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        if (!encryptionExtension.provisioningState().equalsIgnoreCase("Succeeded")) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        HashMap<String, Object> publicSettings = new LinkedHashMap<>();
        if (encryptionExtension.settings() != null) {
            publicSettings = (LinkedHashMap<String, Object>) encryptionExtension.settings();
        }
        if (!publicSettings.containsKey("VolumeType")
            || publicSettings.get("VolumeType") == null
            || ((String) publicSettings.get("VolumeType")).isEmpty()
            || ((String) publicSettings.get("VolumeType")).equalsIgnoreCase("All")
            || ((String) publicSettings.get("VolumeType")).equalsIgnoreCase("Data")) {
            String encryptionOperation = (String) publicSettings.get("EncryptionOperation");
            if (encryptionOperation != null && encryptionOperation.equalsIgnoreCase("EnableEncryption")) {
                return EncryptionStatus.ENCRYPTED;
            }
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        return EncryptionStatus.UNKNOWN;
    }

    @Override
    public Map<String, InstanceViewStatus> diskInstanceViewEncryptionStatuses() {
        // Not available for legacy based encryption
        return new HashMap<String, InstanceViewStatus>();
    }

    @Override
    public DiskVolumeEncryptionMonitor refresh() {
        return refreshAsync().block();
    }

    @Override
    public Mono<DiskVolumeEncryptionMonitor> refreshAsync() {
        final WindowsVolumeLegacyEncryptionMonitorImpl self = this;
        // Refreshes the cached Windows virtual machine and installed encryption extension
        return retrieveVirtualMachineAsync()
            .map(
                virtualMachine -> {
                    self.virtualMachine = virtualMachine;
                    if (virtualMachine.resources() != null) {
                        for (VirtualMachineExtensionInner extension : virtualMachine.resources()) {
                            if (EncryptionExtensionIdentifier.isEncryptionPublisherName(extension.publisher())
                                && EncryptionExtensionIdentifier
                                    .isEncryptionTypeName(
                                        extension.typePropertiesType(), OperatingSystemTypes.WINDOWS)) {
                                self.encryptionExtension = extension;
                                break;
                            }
                        }
                    }
                    return self;
                });
    }

    /**
     * Retrieve the virtual machine. If the virtual machine does not exists then an error observable will be returned.
     *
     * @return the retrieved virtual machine
     */
    private Mono<VirtualMachineInner> retrieveVirtualMachineAsync() {
        return this.computeManager.serviceClient().getVirtualMachines().getByResourceGroupAsync(rgName, vmName);
        // Exception if vm not found
    }

    private boolean hasEncryptionDetails() {
        return virtualMachine != null && this.encryptionExtension != null;
    }
}
