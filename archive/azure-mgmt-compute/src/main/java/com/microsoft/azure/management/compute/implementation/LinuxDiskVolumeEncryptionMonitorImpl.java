/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.DiskVolumeEncryptionMonitor;
import com.microsoft.azure.management.compute.EncryptionStatus;
import com.microsoft.azure.management.compute.InstanceViewStatus;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
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
@LangDefinition
class LinuxDiskVolumeEncryptionMonitorImpl implements DiskVolumeEncryptionMonitor {
    private final String rgName;
    private final String vmName;
    private final ComputeManager computeManager;
    private VirtualMachineExtensionInner encryptionExtension;

    /**
     * Creates LinuxDiskVolumeEncryptionMonitorImpl.
     *
     * @param virtualMachineId resource id of Linux virtual machine to retrieve encryption status from
     * @param computeManager compute manager
     */
    LinuxDiskVolumeEncryptionMonitorImpl(String virtualMachineId, ComputeManager computeManager) {
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
        List<InstanceViewStatus> statuses = instanceViewStatuses();
        if (statuses.size() == 0) {
            return null;
        }
        return statuses.get(0).message();
    }

    @Override
    public EncryptionStatus osDiskStatus() {
        if (!hasEncryptionExtension()) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        final JsonNode subStatusNode = instanceViewFirstSubStatus();
        if (subStatusNode == null) {
            return EncryptionStatus.UNKNOWN;
        }
        JsonNode diskNode = subStatusNode.path("os");
        if (diskNode instanceof MissingNode) {
            return EncryptionStatus.UNKNOWN;
        }
        return EncryptionStatus.fromString(diskNode.asText());
    }

    @Override
    public EncryptionStatus dataDiskStatus() {
        if (!hasEncryptionExtension()) {
            return EncryptionStatus.NOT_ENCRYPTED;
        }
        final JsonNode subStatusNode = instanceViewFirstSubStatus();
        if (subStatusNode == null) {
            return EncryptionStatus.UNKNOWN;
        }
        JsonNode diskNode = subStatusNode.path("data");
        if (diskNode instanceof MissingNode) {
            return EncryptionStatus.UNKNOWN;
        }
        return EncryptionStatus.fromString(diskNode.asText());
    }

    @Override
    public DiskVolumeEncryptionMonitor refresh() {
        return refreshAsync().toBlocking().last();
    }

    @Override
    public Observable<DiskVolumeEncryptionMonitor> refreshAsync() {
        // Refreshes the cached encryption extension installed in the Linux virtual machine.
        //
        final DiskVolumeEncryptionMonitor self = this;
        return retrieveEncryptExtensionWithInstanceViewAsync()
                .flatMap(new Func1<VirtualMachineExtensionInner, Observable<DiskVolumeEncryptionMonitor>>() {
                    @Override
                    public Observable<DiskVolumeEncryptionMonitor> call(VirtualMachineExtensionInner virtualMachineExtensionInner) {
                        encryptionExtension = virtualMachineExtensionInner;
                        return Observable.just(self);
                    }
                })
                .switchIfEmpty(Observable.just(self));
    }

    /**
     * Retrieves the latest state of encryption extension in the virtual machine.
     *
     * @return the retrieved extension
     */
    private Observable<VirtualMachineExtensionInner> retrieveEncryptExtensionWithInstanceViewAsync() {
        if (encryptionExtension != null) {
            // If there is already a cached extension simply retrieve it again with instance view.
            //
            return retrieveExtensionWithInstanceViewAsync(encryptionExtension);
        } else {
            // Extension is not cached already so retrieve name from the virtual machine and
            // then get the extension with instance view.
            //
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
    private Observable<VirtualMachineExtensionInner> retrieveExtensionWithInstanceViewAsync(VirtualMachineExtensionInner extension) {
        return this.computeManager
                .inner()
                .virtualMachineExtensions()
                .getAsync(rgName, vmName, extension.name(), "instanceView")
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

    /**
     * Retrieve the encryption extension from the virtual machine and then retrieve it again with instance view expanded.
     * If the virtual machine does not exists then an error observable will be returned, if the extension could not be
     * located then an empty observable will be returned.
     *
     * @return the retrieved extension
     */
    private Observable<VirtualMachineExtensionInner> retrieveEncryptExtensionWithInstanceViewFromVMAsync() {
        return this.computeManager
                .inner()
                .virtualMachines()
                .getByResourceGroupAsync(rgName, vmName)
                .flatMap(new Func1<VirtualMachineInner, Observable<VirtualMachineExtensionInner>>() {
                    @Override
                    public Observable<VirtualMachineExtensionInner> call(VirtualMachineInner virtualMachine) {
                        if (virtualMachine == null) {
                            return Observable.error(new Exception(String.format("VM with name '%s' not found (resource group '%s')",
                                    vmName, rgName)));
                        }
                        if (virtualMachine.resources() != null) {
                            for (VirtualMachineExtensionInner extension : virtualMachine.resources()) {
                                if (extension.publisher().equalsIgnoreCase("Microsoft.Azure.Security")
                                        && extension.virtualMachineExtensionType().equalsIgnoreCase("AzureDiskEncryptionForLinux")) {
                                    return retrieveExtensionWithInstanceViewAsync(extension);
                                }
                            }
                        }
                        return Observable.empty();
                    }
                });
    }

    /**
     * @return the instance view status collection associated with the encryption extension
     */
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

    /**
     * @return the first sub-status from instance view sub-status collection associated with the
     * encryption extension
     */
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
