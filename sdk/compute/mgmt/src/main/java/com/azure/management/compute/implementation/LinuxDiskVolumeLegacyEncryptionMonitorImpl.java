/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.management.compute.DiskVolumeEncryptionMonitor;
import com.azure.management.compute.EncryptionStatus;
import com.azure.management.compute.InstanceViewStatus;
import com.azure.management.compute.OperatingSystemTypes;
import com.azure.management.compute.models.VirtualMachineExtensionInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * The implementation for DiskVolumeEncryptionStatus for Linux virtual machine.
 * This implementation monitor status of encrypt-decrypt through legacy encryption extension.
 */
class LinuxDiskVolumeLegacyEncryptionMonitorImpl implements DiskVolumeEncryptionMonitor {
    private final String rgName;
    private final String vmName;
    private final ComputeManager computeManager;
    private VirtualMachineExtensionInner encryptionExtension;

    /**
     * Creates LinuxDiskVolumeLegacyEncryptionMonitorImpl.
     *
     * @param virtualMachineId resource id of Linux virtual machine to retrieve encryption status from
     * @param computeManager compute manager
     */
    LinuxDiskVolumeLegacyEncryptionMonitorImpl(String virtualMachineId, ComputeManager computeManager) {
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
        if (!hasEncryptionExtension()) {
            return null;
        }
        return LinuxEncryptionExtensionUtil.progressMessage(this.encryptionExtension.instanceView());
    }

    @Override
    public EncryptionStatus osDiskStatus() {
        if (!hasEncryptionExtension()) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        return LinuxEncryptionExtensionUtil.osDiskStatus(this.encryptionExtension.instanceView());
    }

    @Override
    public EncryptionStatus dataDiskStatus() {
        if (!hasEncryptionExtension()) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        return LinuxEncryptionExtensionUtil.dataDiskStatus(this.encryptionExtension.instanceView());
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
        // Refreshes the cached encryption extension installed in the Linux virtual machine.
        final DiskVolumeEncryptionMonitor self = this;
        return retrieveEncryptExtensionWithInstanceViewAsync()
                .flatMap(virtualMachineExtensionInner -> {
                    encryptionExtension = virtualMachineExtensionInner;
                    return Mono.just(self);
                })
                .switchIfEmpty(Mono.just(self));
    }

    /**
     * Retrieves the latest state of encryption extension in the virtual machine.
     *
     * @return the retrieved extension
     */
    private Mono<VirtualMachineExtensionInner> retrieveEncryptExtensionWithInstanceViewAsync() {
        if (encryptionExtension != null) {
            // If there is already a cached extension simply retrieve it again with instance view.
            return retrieveExtensionWithInstanceViewAsync(encryptionExtension);
        } else {
            // Extension is not cached already so retrieve name from the virtual machine and
            // then get the extension with instance view.
            return retrieveEncryptExtensionWithInstanceViewFromVMAsync();
        }
    }

    /**
     * Retrieve the extension with latest state. If the extension could not be found then
     * an empty observable will be returned.
     *
     * @param extension the extension name
     * @return an observable that emits the retrieved extension
     */
    private Mono<VirtualMachineExtensionInner> retrieveExtensionWithInstanceViewAsync(VirtualMachineExtensionInner extension) {
        return computeManager.inner().virtualMachineExtensions()
                .getAsync(rgName, vmName, extension.getName(), "instanceView")
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Retrieve the encryption extension from the virtual machine and then retrieve it again with instance view expanded.
     * If the virtual machine does not exists then an error observable will be returned, if the extension could not be
     * located then an empty observable will be returned.
     *
     * @return the retrieved extension
     */
    private Mono<VirtualMachineExtensionInner> retrieveEncryptExtensionWithInstanceViewFromVMAsync() {
        return computeManager.inner().virtualMachines()
                .getByResourceGroupAsync(rgName, vmName)
                .onErrorResume(e -> Mono.error(new Exception(String.format("VM with name '%s' not found (resource group '%s')", vmName, rgName))))
                .flatMap(virtualMachine -> {
                    if (virtualMachine.resources() != null) {
                        for (VirtualMachineExtensionInner extension : virtualMachine.resources()) {
                            if (EncryptionExtensionIdentifier.isEncryptionPublisherName(extension.publisher())
                                    && EncryptionExtensionIdentifier.isEncryptionTypeName(extension.virtualMachineExtensionType(), OperatingSystemTypes.LINUX)) {
                                return retrieveExtensionWithInstanceViewAsync(extension);
                            }
                        }
                    }
                    return Mono.empty();
                });
    }

    private boolean hasEncryptionExtension() {
        return this.encryptionExtension != null;
    }
}
