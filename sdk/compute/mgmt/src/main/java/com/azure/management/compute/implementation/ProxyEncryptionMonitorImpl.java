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
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.models.VirtualMachineExtensionInner;
import com.azure.management.compute.models.VirtualMachineInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * A proxy implementation of DiskVolumeEncryptionMonitor to resolve actual encryption monitor
 *  based on the version of encryption extension installed (legacy vs noAAD) on a virtual machine.
 */
class ProxyEncryptionMonitorImpl implements DiskVolumeEncryptionMonitor {
    private final String vmId;
    private final OperatingSystemTypes osType;
    private final ComputeManager computeManager;
    private DiskVolumeEncryptionMonitor resolvedEncryptionMonitor;

    /**
     * Creates ProxyEncryptionMonitorImpl.
     *
     * @param virtualMachine the virtual machine for which encryption monitor needs to be resolved.
     */
    ProxyEncryptionMonitorImpl(VirtualMachine virtualMachine) {
        this.vmId = virtualMachine.id();
        this.osType = virtualMachine.osType();
        this.computeManager = virtualMachine.manager();
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.osType;
    }

    @Override
    public String progressMessage() {
        return this.resolvedEncryptionMonitor != null
                ? this.resolvedEncryptionMonitor.progressMessage()
                : null;
    }

    @Override
    public EncryptionStatus osDiskStatus() {
        return this.resolvedEncryptionMonitor != null
                ? this.resolvedEncryptionMonitor.osDiskStatus()
                : EncryptionStatus.NOT_ENCRYPTED;
    }

    @Override
    public EncryptionStatus dataDiskStatus() {
        return this.resolvedEncryptionMonitor != null
                ? this.resolvedEncryptionMonitor.dataDiskStatus()
                : EncryptionStatus.NOT_ENCRYPTED;
    }

    @Override
    public Map<String, InstanceViewStatus> diskInstanceViewEncryptionStatuses() {
        return this.resolvedEncryptionMonitor != null
                ? this.resolvedEncryptionMonitor.diskInstanceViewEncryptionStatuses()
                : new HashMap<String, InstanceViewStatus>();
    }

    @Override
    public DiskVolumeEncryptionMonitor refresh() {
        return refreshAsync().block();
    }

    @Override
    public Mono<DiskVolumeEncryptionMonitor> refreshAsync() {
        if (this.resolvedEncryptionMonitor != null) {
            return this.resolvedEncryptionMonitor.refreshAsync();
        } else {
            final ProxyEncryptionMonitorImpl self = this;
            return retrieveVirtualMachineAsync()
                    .flatMap(virtualMachine -> {
                        VirtualMachineExtensionInner extension = encryptionExtension(virtualMachine);
                        if (EncryptionExtensionIdentifier.isNoAADVersion(osType(), extension.typeHandlerVersion())) {
                            self.resolvedEncryptionMonitor = (osType() == OperatingSystemTypes.LINUX)
                                    ? new LinuxDiskVolumeNoAADEncryptionMonitorImpl(virtualMachine.getId(), computeManager)
                                    : new WindowsVolumeNoAADEncryptionMonitorImpl(virtualMachine.getId(), computeManager);
                        } else {
                            self.resolvedEncryptionMonitor = (osType() == OperatingSystemTypes.LINUX)
                                    ? new LinuxDiskVolumeLegacyEncryptionMonitorImpl(virtualMachine.getId(), computeManager)
                                    : new WindowsVolumeLegacyEncryptionMonitorImpl(virtualMachine.getId(), computeManager);
                        }
                        return self.resolvedEncryptionMonitor.refreshAsync();
                    })
                    .switchIfEmpty(Mono.just(self));
        }
    }

    /**
     * Retrieve the virtual machine.
     * If the virtual machine does not exists then an error observable will be returned.
     *
     * @return the retrieved virtual machine
     */
    private Mono<VirtualMachineInner> retrieveVirtualMachineAsync() {
        return computeManager.inner().virtualMachines()
                .getByResourceGroupAsync(
                        ResourceUtils.groupFromResourceId(vmId),
                        ResourceUtils.nameFromResourceId(vmId)
                )
                .onErrorResume(e -> Mono.error(new Exception(String.format("VM with id '%s' not found.", vmId))));
    }

    /**
     * Given virtual machine find encryption extension if exists.
     *
     * @param vm the virtual machine
     * @return the encryption extension if exists, null otherwise.
     */
    private VirtualMachineExtensionInner encryptionExtension(VirtualMachineInner vm) {
        if (vm.resources() != null) {
            for (VirtualMachineExtensionInner extension : vm.resources()) {
                if (EncryptionExtensionIdentifier.isEncryptionPublisherName(extension.publisher())
                        && EncryptionExtensionIdentifier.isEncryptionTypeName(extension.virtualMachineExtensionType(), osType())) {
                    return extension;
                }
            }
        }
        return null;
    }
}
