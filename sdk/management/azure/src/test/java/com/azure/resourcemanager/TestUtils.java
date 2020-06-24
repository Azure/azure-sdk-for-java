// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.DataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachine;

/** Test utilities. */
public final class TestUtils {
    private TestUtils() {
    }

    //    public static void print(ManagementLock lock) {
    //        StringBuffer info = new StringBuffer();
    //        info.append("\nLock ID: ").append(lock.id())
    //            .append("\nLocked resource ID: ").append(lock.lockedResourceId())
    //            .append("\nLevel: ").append(lock.level());
    //        System.out.println(info.toString());
    //    }

    /**
     * Shows the virtual machine.
     *
     * @param resource virtual machine to show
     */
    public static void print(VirtualMachine resource) {
        StringBuilder storageProfile = new StringBuilder().append("\n\tStorageProfile: ");
        if (resource.storageProfile().imageReference() != null) {
            storageProfile.append("\n\t\tImageReference:");
            storageProfile.append("\n\t\t\tPublisher: ").append(resource.storageProfile().imageReference().publisher());
            storageProfile.append("\n\t\t\tOffer: ").append(resource.storageProfile().imageReference().offer());
            storageProfile.append("\n\t\t\tSKU: ").append(resource.storageProfile().imageReference().sku());
            storageProfile.append("\n\t\t\tVersion: ").append(resource.storageProfile().imageReference().version());
        }

        if (resource.storageProfile().osDisk() != null) {
            storageProfile.append("\n\t\tOSDisk:");
            storageProfile.append("\n\t\t\tOSType: ").append(resource.storageProfile().osDisk().osType());
            storageProfile.append("\n\t\t\tName: ").append(resource.storageProfile().osDisk().name());
            storageProfile.append("\n\t\t\tCaching: ").append(resource.storageProfile().osDisk().caching());
            storageProfile.append("\n\t\t\tCreateOption: ").append(resource.storageProfile().osDisk().createOption());
            storageProfile.append("\n\t\t\tDiskSizeGB: ").append(resource.storageProfile().osDisk().diskSizeGB());
            if (resource.storageProfile().osDisk().image() != null) {
                storageProfile.append("\n\t\t\tImage Uri: ").append(resource.storageProfile().osDisk().image().uri());
            }
            if (resource.storageProfile().osDisk().vhd() != null) {
                storageProfile.append("\n\t\t\tVhd Uri: ").append(resource.storageProfile().osDisk().vhd().uri());
            }
            if (resource.storageProfile().osDisk().encryptionSettings() != null) {
                storageProfile.append("\n\t\t\tEncryptionSettings: ");
                storageProfile
                    .append("\n\t\t\t\tEnabled: ")
                    .append(resource.storageProfile().osDisk().encryptionSettings().enabled());
                storageProfile
                    .append("\n\t\t\t\tDiskEncryptionKey Uri: ")
                    .append(resource.storageProfile().osDisk().encryptionSettings().diskEncryptionKey().secretUrl());
                storageProfile
                    .append("\n\t\t\t\tKeyEncryptionKey Uri: ")
                    .append(resource.storageProfile().osDisk().encryptionSettings().keyEncryptionKey().keyUrl());
            }
        }

        if (resource.storageProfile().dataDisks() != null) {
            int i = 0;
            for (DataDisk disk : resource.storageProfile().dataDisks()) {
                storageProfile.append("\n\t\tDataDisk: #").append(i++);
                storageProfile.append("\n\t\t\tName: ").append(disk.name());
                storageProfile.append("\n\t\t\tCaching: ").append(disk.caching());
                storageProfile.append("\n\t\t\tCreateOption: ").append(disk.createOption());
                storageProfile.append("\n\t\t\tDiskSizeGB: ").append(disk.diskSizeGB());
                storageProfile.append("\n\t\t\tLun: ").append(disk.lun());
                if (resource.isManagedDiskEnabled()) {
                    if (disk.managedDisk() != null) {
                        storageProfile.append("\n\t\t\tManaged Disk Id: ").append(disk.managedDisk().id());
                    }
                } else {
                    if (disk.vhd().uri() != null) {
                        storageProfile.append("\n\t\t\tVhd Uri: ").append(disk.vhd().uri());
                    }
                }
                if (disk.image() != null) {
                    storageProfile.append("\n\t\t\tImage Uri: ").append(disk.image().uri());
                }
            }
        }

        StringBuilder osProfile = new StringBuilder().append("\n\tOSProfile: ");
        osProfile.append("\n\t\tComputerName:").append(resource.osProfile().computerName());
        if (resource.osProfile().windowsConfiguration() != null) {
            osProfile.append("\n\t\t\tWindowsConfiguration: ");
            osProfile
                .append("\n\t\t\t\tProvisionVMAgent: ")
                .append(resource.osProfile().windowsConfiguration().provisionVMAgent());
            osProfile
                .append("\n\t\t\t\tEnableAutomaticUpdates: ")
                .append(resource.osProfile().windowsConfiguration().enableAutomaticUpdates());
            osProfile.append("\n\t\t\t\tTimeZone: ").append(resource.osProfile().windowsConfiguration().timeZone());
        }

        if (resource.osProfile().linuxConfiguration() != null) {
            osProfile.append("\n\t\t\tLinuxConfiguration: ");
            osProfile
                .append("\n\t\t\t\tDisablePasswordAuthentication: ")
                .append(resource.osProfile().linuxConfiguration().disablePasswordAuthentication());
        }

        StringBuilder networkProfile = new StringBuilder().append("\n\tNetworkProfile: ");
        for (String networkInterfaceId : resource.networkInterfaceIds()) {
            networkProfile.append("\n\t\tId:").append(networkInterfaceId);
        }

        System
            .out
            .println(
                new StringBuilder()
                    .append("Virtual Machine: ")
                    .append(resource.id())
                    .append("Name: ")
                    .append(resource.name())
                    .append("\n\tResource group: ")
                    .append(resource.resourceGroupName())
                    .append("\n\tRegion: ")
                    .append(resource.region())
                    .append("\n\tTags: ")
                    .append(resource.tags())
                    .append("\n\tHardwareProfile: ")
                    .append("\n\t\tSize: ")
                    .append(resource.size())
                    .append(storageProfile)
                    .append(osProfile)
                    .append(networkProfile)
                    .toString());
    }
}
