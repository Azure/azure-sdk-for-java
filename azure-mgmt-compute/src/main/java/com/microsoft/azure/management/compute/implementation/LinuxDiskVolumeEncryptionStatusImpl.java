/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.microsoft.azure.management.compute.DiskVolumeEncryptionStatus;
import com.microsoft.azure.management.compute.EncryptionStatuses;
import com.microsoft.azure.management.compute.InstanceViewStatus;
import com.microsoft.azure.management.compute.VirtualMachineExtensionInstanceView;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for DiskVolumeEncryptionStatus for Linux virtual machine.
 */
class LinuxDiskVolumeEncryptionStatusImpl implements DiskVolumeEncryptionStatus {
    private final String virtualMachineId;
    private final ComputeManager computeManager;
    private VirtualMachineExtensionInner encryptionExtension;

    /**
     * Creates LinuxDiskVolumeEncryptionStatusImpl.
     *
     * @param virtualMachineId resource id of Linux virtual machine to retrieve encryption status from
     * @param computeManager compute manager
     */
    LinuxDiskVolumeEncryptionStatusImpl(String virtualMachineId, ComputeManager computeManager) {
        this.virtualMachineId = virtualMachineId;
        this.computeManager = computeManager;
    }

    @Override
    public String progressMessage() {
        if (!hasEncryptionExtension()) {
            return null;
        }
        List<InstanceViewStatus> statuses = instanceViewStatuses();
        if (statuses.size() == 0) {
            return null;
        }
        return statuses.get(0).message();
    }

    @Override
    public EncryptionStatuses osDiskStatus() {
        if (!hasEncryptionExtension()) {
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        final JsonNode subStatusNode = instanceViewFirstSubStatus();
        if (subStatusNode == null) {
            return EncryptionStatuses.UNKNOWN;
        }
        JsonNode diskNode = subStatusNode.path("os");
        if (diskNode instanceof MissingNode) {
            return EncryptionStatuses.UNKNOWN;
        }
        return new EncryptionStatuses(diskNode.asText());
    }

    @Override
    public EncryptionStatuses dataDiskStatus() {
        if (!hasEncryptionExtension()) {
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        final JsonNode subStatusNode = instanceViewFirstSubStatus();
        if (subStatusNode == null) {
            return EncryptionStatuses.UNKNOWN;
        }
        JsonNode diskNode = subStatusNode.path("data");
        if (diskNode instanceof MissingNode) {
            return EncryptionStatuses.UNKNOWN;
        }
        return new EncryptionStatuses(diskNode.asText());
    }

    @Override
    public DiskVolumeEncryptionStatus refresh() {
        return refreshAsync().toBlocking().last();
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> refreshAsync() {
        if (encryptionExtension != null) {
            return refreshEncryptionExtensionAsync(encryptionExtension);
        } else {
            final String rgName = ResourceUtils.groupFromResourceId(virtualMachineId);
            final String vmName = ResourceUtils.nameFromResourceId(virtualMachineId);
            final DiskVolumeEncryptionStatus self = this;
            return this.computeManager
                    .inner()
                    .virtualMachines()
                    .getAsync(rgName, vmName)
                    .flatMap(new Func1<VirtualMachineInner, Observable<DiskVolumeEncryptionStatus>>() {
                        @Override
                        public Observable<DiskVolumeEncryptionStatus> call(VirtualMachineInner virtualMachine) {
                            if (virtualMachine == null) {
                                return Observable.error(new Exception(String.format("VM with name '%s' not found (resource group '%s')",
                                        vmName, rgName)));
                            }
                            if (virtualMachine.resources() != null) {
                                for (VirtualMachineExtensionInner extension : virtualMachine.resources()) {
                                    if (extension.publisher().equalsIgnoreCase("Microsoft.Azure.Security")
                                            && extension.typeHandlerVersion().equalsIgnoreCase("AzureDiskEncryptionForLinux")) {
                                        return refreshEncryptionExtensionAsync(extension);
                                    }
                                }
                            }
                            return Observable.empty();
                        }
                    })
                    .switchIfEmpty(Observable.just(self));
        }
    }

    private Observable<DiskVolumeEncryptionStatus> refreshEncryptionExtensionAsync(VirtualMachineExtensionInner extension) {
        final String rgName = ResourceUtils.groupFromResourceId(virtualMachineId);
        final String vmName = ResourceUtils.nameFromResourceId(virtualMachineId);
        final LinuxDiskVolumeEncryptionStatusImpl self = this;
        return retrieveExtensionWithInstanceViewAsync(rgName, vmName, extension.name())
        .map(new Func1<VirtualMachineExtensionInner, DiskVolumeEncryptionStatus>() {
            @Override
            public DiskVolumeEncryptionStatus call(VirtualMachineExtensionInner extension) {
                self.encryptionExtension = extension;
                return self;
            }
        })
        .switchIfEmpty(Observable.just(self));
    }

    private Observable<VirtualMachineExtensionInner> retrieveExtensionWithInstanceViewAsync(String rgName, String vmName, String extensionName) {
        return this.computeManager
                .inner()
                .virtualMachineExtensions()
                .getAsync(rgName, vmName, extensionName, "instanceView")
                .flatMap(new Func1<VirtualMachineExtensionInner, Observable<VirtualMachineExtensionInner>>() {
                    @Override
                    public Observable<VirtualMachineExtensionInner> call(VirtualMachineExtensionInner virtualMachineExtensionInner) {
                        if (virtualMachineExtensionInner == null) {
                            return Observable.empty();
                        }
                        return Observable.just(virtualMachineExtensionInner);
                    }
                });
    }

    private List<InstanceViewStatus> instanceViewStatuses() {
        if (!hasEncryptionExtension()) {
            return new ArrayList<>();
        }
        VirtualMachineExtensionInstanceView instanceView = this.encryptionExtension.instanceView();
        if (instanceView == null
                || instanceView.statuses() == null) {
            return new ArrayList<>();
        }
        return instanceView.statuses();
    }

    private JsonNode instanceViewFirstSubStatus() {
        if (!hasEncryptionExtension()) {
            return null;
        }
        VirtualMachineExtensionInstanceView instanceView = this.encryptionExtension.instanceView();
        if (instanceView == null
                || instanceView.substatuses() == null) {
            return null;
        }
        List<InstanceViewStatus> instanceViewSubStatuses = instanceView.substatuses();
        if (instanceViewSubStatuses.size() == 0) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode rootNode;
        try {
            rootNode = mapper.readTree(instanceViewSubStatuses.get(0).message());
        } catch (IOException exception) {
            return null;
        }
        return rootNode;
    }

    private boolean hasEncryptionExtension() {
        return this.encryptionExtension != null;
    }
}
