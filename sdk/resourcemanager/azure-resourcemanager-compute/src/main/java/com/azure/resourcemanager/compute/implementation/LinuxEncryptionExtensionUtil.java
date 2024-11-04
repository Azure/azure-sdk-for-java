// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.resourcemanager.compute.models.EncryptionStatus;
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionInstanceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        final Map<String, Object> subStatus = instanceViewFirstSubStatus(instanceView);
        if (subStatus == null) {
            return EncryptionStatus.UNKNOWN;
        }
        Object disk = subStatus.get("os");
        if (disk == null) {
            return EncryptionStatus.UNKNOWN;
        }
        return EncryptionStatus.fromString(disk.toString());
    }

    /**
     * Retrieves the data disk encryption status from the given instance view.
     *
     * @param instanceView encryption extension instance view
     * @return data disk status
     */
    static EncryptionStatus dataDiskStatus(VirtualMachineExtensionInstanceView instanceView) {
        final Map<String, Object> subStatus = instanceViewFirstSubStatus(instanceView);
        if (subStatus == null) {
            return EncryptionStatus.UNKNOWN;
        }
        Object disk = subStatus.get("data");
        if (disk == null) {
            return EncryptionStatus.UNKNOWN;
        }
        return EncryptionStatus.fromString(disk.toString());
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
    static Map<String, Object> instanceViewFirstSubStatus(VirtualMachineExtensionInstanceView instanceView) {
        if (instanceView == null || instanceView.substatuses() == null) {
            return null;
        }
        List<InstanceViewStatus> instanceViewSubStatuses = instanceView.substatuses();
        if (instanceViewSubStatuses.size() == 0) {
            return null;
        }

        try (JsonReader jsonReader = JsonProviders.createReader(instanceViewSubStatuses.get(0).message())) {
            Map<String, Object> result = jsonReader.readMap(JsonReader::readUntyped);
            return result;
        } catch (IOException e) {
            return null;
        }
    }
}
