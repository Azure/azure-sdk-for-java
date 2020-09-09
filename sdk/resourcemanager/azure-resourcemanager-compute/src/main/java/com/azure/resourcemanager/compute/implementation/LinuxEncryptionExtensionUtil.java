// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.EncryptionStatus;
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionInstanceView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class LinuxEncryptionExtensionUtil {
    /**
     * Gets the encryption progress message.
     *
     * @param instanceView encryption extension instance view
     * @return the encryption progress message
     */
    static String progressMessage(VirtualMachineExtensionInstanceView instanceView) {
        List<InstanceViewStatus> statuses = instanceViewStatuses(instanceView);
        if (statuses.size() == 0) {
            return null;
        }
        return statuses.get(0).message();
    }

    /**
     * Retrieves the operating system disk encryption status from the given instance view.
     *
     * @param instanceView encryption extension instance view
     * @return os disk status
     */
    static EncryptionStatus osDiskStatus(VirtualMachineExtensionInstanceView instanceView) {
        final JsonNode subStatusNode = instanceViewFirstSubStatus(instanceView);
        if (subStatusNode == null) {
            return EncryptionStatus.UNKNOWN;
        }
        JsonNode diskNode = subStatusNode.path("os");
        if (diskNode instanceof MissingNode) {
            return EncryptionStatus.UNKNOWN;
        }
        return EncryptionStatus.fromString(diskNode.asText());
    }

    /**
     * Retrieves the data disk encryption status from the given instance view.
     *
     * @param instanceView encryption extension instance view
     * @return data disk status
     */
    static EncryptionStatus dataDiskStatus(VirtualMachineExtensionInstanceView instanceView) {
        final JsonNode subStatusNode = instanceViewFirstSubStatus(instanceView);
        if (subStatusNode == null) {
            return EncryptionStatus.UNKNOWN;
        }
        JsonNode diskNode = subStatusNode.path("data");
        if (diskNode instanceof MissingNode) {
            return EncryptionStatus.UNKNOWN;
        }
        return EncryptionStatus.fromString(diskNode.asText());
    }

    /**
     * the instance view status collection associated with the provided encryption extension.
     *
     * @param instanceView the extension instance view
     * @return status collection
     */
    static List<InstanceViewStatus> instanceViewStatuses(VirtualMachineExtensionInstanceView instanceView) {
        if (instanceView == null || instanceView.statuses() == null) {
            return new ArrayList<>();
        }
        return instanceView.statuses();
    }

    /**
     * the first sub-status from instance view sub-status collection associated with the provided encryption extension.
     *
     * @param instanceView the extension instance view
     * @return the first sub-status
     */
    static JsonNode instanceViewFirstSubStatus(VirtualMachineExtensionInstanceView instanceView) {
        if (instanceView == null || instanceView.substatuses() == null) {
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
}
