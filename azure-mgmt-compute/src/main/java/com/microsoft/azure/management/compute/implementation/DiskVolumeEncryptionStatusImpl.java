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
    private VirtualMachineExtensionInner encryptionExtensionInner;
    private final VirtualMachine virtualMachine;
    private VirtualMachineExtensionInstanceView instanceView;
    private final String extensionName;

    DiskVolumeEncryptionStatusImpl(final OperatingSystemTypes osType,
            final VirtualMachineExtension encryptionExtension,
            final VirtualMachineExtensionInstanceView instanceView) {
        super(encryptionExtension.inner());
        this.osType = osType;
        this.encryptionExtensionInner = encryptionExtension.inner();
        this.virtualMachine = encryptionExtension.parent();
        this.instanceView = instanceView;
        this.extensionName = encryptionExtension.name();
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
        if (osType == OperatingSystemTypes.LINUX) {
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
            if (this.virtualMachine.inner().storageProfile() == null
                    || this.virtualMachine.inner().storageProfile().osDisk() == null
                    || this.virtualMachine.inner().storageProfile().osDisk().encryptionSettings() == null) {
                return EncryptionStatuses.UNKNOWN;
            }
            DiskEncryptionSettings encryptionSettings = this.virtualMachine
                    .inner()
                    .storageProfile()
                    .osDisk()
                    .encryptionSettings();
            if (encryptionSettings.diskEncryptionKey() == null || encryptionSettings.diskEncryptionKey().secretUrl() == null) {
                return EncryptionStatuses.UNKNOWN;
            }
            if (Utils.toPrimitiveBoolean(encryptionSettings.enabled())) {
                return EncryptionStatuses.ENCRYPTED;
            }
        }
        return EncryptionStatuses.UNKNOWN;
    }

    @Override
    public EncryptionStatuses dataDiskStatus() {
        if (osType == OperatingSystemTypes.LINUX) {
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
            if (this.encryptionExtensionInner.provisioningState() == null
                    || !this.encryptionExtensionInner.provisioningState().equalsIgnoreCase("Succeeded")) {
                return EncryptionStatuses.UNKNOWN;
            }
            HashMap<String, Object> publicSettings = publicSettings();
            if (!publicSettings.containsKey("VolumeType")) {
                return EncryptionStatuses.UNKNOWN;
            }
            String volumeType = (String) publicSettings.get("VolumeType");
            if (volumeType == null) {
                return EncryptionStatuses.UNKNOWN;
            }
            if (!volumeType.equalsIgnoreCase("All") && !volumeType.equalsIgnoreCase("Data")) {
                return EncryptionStatuses.UNKNOWN;
            }
            if (!publicSettings.containsKey("EncryptionOperation")) {
                return EncryptionStatuses.UNKNOWN;
            }
            String encryptionOperation = (String) publicSettings.get("EncryptionOperation");
            if (encryptionOperation == null) {
                return EncryptionStatuses.UNKNOWN;
            }
            if (encryptionOperation.equalsIgnoreCase("EnableEncryption")) {
                return EncryptionStatuses.ENCRYPTED;
            }
        }
        return EncryptionStatuses.UNKNOWN;
    }

    @Override
    public DiskVolumeEncryptionStatus refresh() {
        this.encryptionExtensionInner = this.virtualMachine
                .manager()
                .inner()
                .virtualMachineExtensions().get(this.virtualMachine.resourceGroupName(),
                        this.virtualMachine.name(),
                        this.extensionName,
                        "instanceView");
        this.instanceView = this.encryptionExtensionInner.instanceView();
        return this;
    }

    private List<InstanceViewStatus> instanceViewStatuses() {
        if (this.instanceView == null
                || this.instanceView.statuses() == null) {
            return new ArrayList<>();
        }
        return this.instanceView.statuses();
    }

    private List<InstanceViewStatus> instanceViewSubStatuses() {
        if (this.inner() == null
                || this.instanceView.substatuses() == null) {
            return new ArrayList<>();
        }
        return this.instanceView.substatuses();
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

    private HashMap<String, Object> publicSettings() {
        if (this.inner().settings() == null) {
            return new LinkedHashMap<>();
        } else {
            return (LinkedHashMap<String, Object>) this.inner().settings();
        }
    }
}