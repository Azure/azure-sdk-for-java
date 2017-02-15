/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.microsoft.azure.management.compute.DiskEncryptionSettings;
import com.microsoft.azure.management.compute.DiskVolumeEncryptionStatus;
import com.microsoft.azure.management.compute.EncryptionStatuses;
import com.microsoft.azure.management.compute.InstanceViewStatus;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.compute.VirtualMachineExtensionInstanceView;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The implementation for DiskVolumeEncryptionStatus.
 */
class DiskVolumeEncryptionStatusImpl
        extends WrapperImpl<VirtualMachineExtensionInner>
        implements DiskVolumeEncryptionStatus {
    private final OperatingSystemTypes osType;
    private final VirtualMachine virtualMachine;
    private final String extensionName;

    DiskVolumeEncryptionStatusImpl(final OperatingSystemTypes osType,
            final VirtualMachineExtension encryptionExtension) {
        super(encryptionExtension.inner());
        this.osType = osType;
        this.virtualMachine = encryptionExtension.parent();
        this.extensionName = encryptionExtension.name();
    }

    DiskVolumeEncryptionStatusImpl(VirtualMachine virtualMachine, String extensionName) {
        super(null);
        this.osType = virtualMachine.osType();
        this.virtualMachine = virtualMachine;
        this.extensionName = extensionName;
    }

    @Override
    public String progressMessage() {
        if (instanceViewStatuses().size() == 0) {
            return null;
        }
        return this.instanceViewStatuses().get(0).message();
    }

    @Override
    public EncryptionStatuses osDiskStatus() {
        if (this.inner() == null) {
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        if (osType == OperatingSystemTypes.LINUX) {
            // Linux - get OS volume encryption state from the instance view status message
            //
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
        if (osType == OperatingSystemTypes.WINDOWS) {
            // Windows - get OS volume encryption state from the vm model
            //
            if (this.virtualMachine.inner().storageProfile() == null
                    || this.virtualMachine.inner().storageProfile().osDisk() == null
                    || this.virtualMachine.inner().storageProfile().osDisk().encryptionSettings() == null) {
                return EncryptionStatuses.NOT_ENCRYPTED;
            }
            DiskEncryptionSettings encryptionSettings = this.virtualMachine
                    .inner()
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
        return EncryptionStatuses.UNKNOWN;
    }

    @Override
    public EncryptionStatuses dataDiskStatus() {
        if (this.inner() == null) {
            return EncryptionStatuses.NOT_ENCRYPTED;
        }
        if (osType == OperatingSystemTypes.LINUX) {
            // Linux - get Data volume encryption state from the encryption extension instance view status message
            //
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
        if (osType == OperatingSystemTypes.WINDOWS) {
            // Windows - get Data volume encryption state from the encryption extension model
            //
            if (this.inner().provisioningState() == null
                    || !this.inner().provisioningState().equalsIgnoreCase("Succeeded")) {
                return EncryptionStatuses.NOT_ENCRYPTED;
            }
            HashMap<String, Object> publicSettings = new LinkedHashMap<>();
            if (this.inner().settings() == null) {
                publicSettings = (LinkedHashMap<String, Object>) this.inner().settings();
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
        }
        return EncryptionStatuses.UNKNOWN;
    }

    @Override
    public DiskVolumeEncryptionStatus refresh() {
        return refreshAsync().toBlocking().last();
    }

    @Override
    public Observable<DiskVolumeEncryptionStatus> refreshAsync() {
        final DiskVolumeEncryptionStatusImpl self = this;
        return this.virtualMachine
                .manager()
                .inner()
                .virtualMachineExtensions().getAsync(this.virtualMachine.resourceGroupName(),
                this.virtualMachine.name(),
                this.extensionName,
                "instanceView")
                .map(new Func1<VirtualMachineExtensionInner, DiskVolumeEncryptionStatus>() {
                    @Override
                    public DiskVolumeEncryptionStatus call(VirtualMachineExtensionInner virtualMachineExtensionInner) {
                        self.setInner(virtualMachineExtensionInner);
                        return self;
                    }
                });
    }

    private List<InstanceViewStatus> instanceViewStatuses() {
        if (this.inner() == null) {
            return new ArrayList<>();
        }
        VirtualMachineExtensionInstanceView instanceView = this.inner().instanceView();
        if (instanceView == null
                || instanceView.statuses() == null) {
            return new ArrayList<>();
        }
        return instanceView.statuses();
    }

    private List<InstanceViewStatus> instanceViewSubStatuses() {
        if (this.inner() == null) {
            return new ArrayList<>();
        }
        VirtualMachineExtensionInstanceView instanceView = this.inner().instanceView();
        if (instanceView == null
                || instanceView.substatuses() == null) {
            return new ArrayList<>();
        }
        return instanceView.substatuses();
    }

    private JsonNode instanceViewFirstSubStatus() {
        if (instanceViewSubStatuses().size() == 0) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode rootNode;
        try {
            rootNode = mapper.readTree(instanceViewSubStatuses().get(0).message());
        } catch (IOException exception) {
            return null;
        }
        return rootNode;
    }
}