/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Common utils for Azure management samples.
 */

public final class Utils {

    /**
     * Print virtual machine info.
     * @param resource a virtual machine
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
                storageProfile.append("\n\t\t\t\tEnabled: ").append(resource.storageProfile().osDisk().encryptionSettings().enabled());
                storageProfile.append("\n\t\t\t\tDiskEncryptionKey Uri: ").append(resource
                        .storageProfile()
                        .osDisk()
                        .encryptionSettings()
                        .diskEncryptionKey().secretUrl());
                storageProfile.append("\n\t\t\t\tKeyEncryptionKey Uri: ").append(resource
                        .storageProfile()
                        .osDisk()
                        .encryptionSettings()
                        .keyEncryptionKey().keyUrl());
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
                if (disk.vhd().uri() != null) {
                    storageProfile.append("\n\t\t\tVhd Uri: ").append(disk.vhd().uri());
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
            osProfile.append("\n\t\t\t\tProvisionVMAgent: ")
                    .append(resource.osProfile().windowsConfiguration().provisionVMAgent());
            osProfile.append("\n\t\t\t\tEnableAutomaticUpdates: ")
                    .append(resource.osProfile().windowsConfiguration().enableAutomaticUpdates());
            osProfile.append("\n\t\t\t\tTimeZone: ")
                    .append(resource.osProfile().windowsConfiguration().timeZone());
        }

        if (resource.osProfile().linuxConfiguration() != null) {
            osProfile.append("\n\t\t\tLinuxConfiguration: ");
            osProfile.append("\n\t\t\t\tDisablePasswordAuthentication: ")
                    .append(resource.osProfile().linuxConfiguration().disablePasswordAuthentication());
        }

        StringBuilder networkProfile = new StringBuilder().append("\n\tNetworkProfile: ");
        for (String networkInterfaceId : resource.networkInterfaceIds()) {
            networkProfile.append("\n\t\tId:").append(networkInterfaceId);
        }

        StringBuilder extensions = new StringBuilder().append("\n\tExtensions: ");
        for (Map.Entry<String, VirtualMachineExtension> extensionEntry : resource.extensions().entrySet()) {
            VirtualMachineExtension extension = extensionEntry.getValue();
            extensions.append("\n\t\tExtension: ").append(extension.id())
                    .append("\n\t\t\tName: ").append(extension.name())
                    .append("\n\t\t\tTags: ").append(extension.tags())
                    .append("\n\t\t\tProvisioningState: ").append(extension.provisioningState())
                    .append("\n\t\t\tAuto upgrade minor version enabled: ").append(extension.autoUpgradeMinorVersionEnabled())
                    .append("\n\t\t\tPublisher: ").append(extension.publisherName())
                    .append("\n\t\t\tType: ").append(extension.typeName())
                    .append("\n\t\t\tVersion: ").append(extension.versionName())
                    .append("\n\t\t\tPublic Settings: ").append(extension.publicSettingsAsJsonString());
        }


        System.out.println(new StringBuilder().append("Virtual Machine: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tHardwareProfile: ")
                .append("\n\t\tSize: ").append(resource.size())
                .append(storageProfile)
                .append(osProfile)
                .append(networkProfile)
                .append(extensions)
                .toString());
    }


    /**
     * Print availability set info.
     * @param resource an availability set
     */
    public static void print(AvailabilitySet resource) {

        System.out.println(new StringBuilder().append("Availability Set: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tFault domain count: ").append(resource.faultDomainCount())
                .append("\n\tUpdate domain count: ").append(resource.updateDomainCount())
                .toString());
    }

    /**
     * Print network info.
     * @param resource a network
     * @throws IOException IO errors
     * @throws CloudException Cloud errors
     */
    public static void print(Network resource) throws CloudException, IOException {
        StringBuilder info = new StringBuilder();
        info.append("Network: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tAddress spaces: ").append(resource.addressSpaces())
                .append("\n\tDNS server IPs: ").append(resource.dnsServerIPs());

        // Output subnets
        for (Subnet subnet : resource.subnets().values()) {
            info.append("\n\tSubnet: ").append(subnet.name())
                    .append("\n\t\tAddress prefix: ").append(subnet.addressPrefix());
            NetworkSecurityGroup subnetNsg = subnet.networkSecurityGroup();
            if (subnetNsg != null) {
                info.append("\n\t\tNetwork security group: ").append(subnetNsg.id());
            }
        }

        System.out.println(info.toString());
    }

    /**
     * Print network interface.
     * @param resource a network interface
     */
    public static void print(NetworkInterface resource) {
        StringBuilder info = new StringBuilder();
        info.append("NetworkInterface: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tInternal DNS name label: ").append(resource.internalDnsNameLabel())
                .append("\n\tInternal FQDN: ").append(resource.internalFqdn())
                .append("\n\tInternal domain name suffix: ").append(resource.internalDomainNameSuffix())
                .append("\n\tNetwork security group: ").append(resource.networkSecurityGroupId())
                .append("\n\tApplied DNS servers: ").append(resource.appliedDnsServers().toString())
                .append("\n\tDNS server IPs: ");

        // Output dns servers
        for (String dnsServerIp : resource.dnsServers()) {
            info.append("\n\t\t").append(dnsServerIp);
        }
        info.append("\n\t IP forwarding enabled: ").append(resource.isIpForwardingEnabled())
                .append("\n\tMAC Address:").append(resource.macAddress())
                .append("\n\tPrivate IP:").append(resource.primaryPrivateIp())
                .append("\n\tPrivate allocation method:").append(resource.primaryPrivateIpAllocationMethod())
                .append("\n\tSubnet Id:").append(resource.primarySubnetId());

        System.out.println(info.toString());
    }

    /**
     * Print network security group.
     * @param resource a network security group
     */
    public static void print(NetworkSecurityGroup resource) {
        StringBuilder info = new StringBuilder();
        info.append("NSG: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags());

        // Output security rules
        for (NetworkSecurityRule rule : resource.securityRules().values()) {
            info.append("\n\tRule: ").append(rule.name())
                    .append("\n\t\tAccess: ").append(rule.access())
                    .append("\n\t\tDirection: ").append(rule.direction())
                    .append("\n\t\tFrom address: ").append(rule.sourceAddressPrefix())
                    .append("\n\t\tFrom port range: ").append(rule.sourcePortRange())
                    .append("\n\t\tTo address: ").append(rule.destinationAddressPrefix())
                    .append("\n\t\tTo port: ").append(rule.destinationPortRange())
                    .append("\n\t\tProtocol: ").append(rule.protocol())
                    .append("\n\t\tPriority: ").append(rule.priority());
        }

        System.out.println(info.toString());
    }

    /**
     * Print public IP address.
     * @param resource a public IP address
     */
    public static void print(PublicIpAddress resource) {
        System.out.println(new StringBuilder().append("Public IP Address: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tIP Address: ").append(resource.ipAddress())
                .append("\n\tLeaf domain label: ").append(resource.leafDomainLabel())
                .append("\n\tFQDN: ").append(resource.fqdn())
                .append("\n\tReverse FQDN: ").append(resource.reverseFqdn())
                .append("\n\tIdle timeout (minutes): ").append(resource.idleTimeoutInMinutes())
                .append("\n\tIP allocation method: ").append(resource.ipAllocationMethod())
                .toString());
    }


    /**
     * Print storage account.
     * @param storageAccount a storage account
     */
    public static void print(StorageAccount storageAccount) {
        System.out.println(storageAccount.name()
                + " created @ " + storageAccount.creationTime());
    }

    /**
     * Print storage account keys.
     * @param storageAccountKeys a list of storage account keys
     */
    public static void print(List<StorageAccountKey> storageAccountKeys) {
        for (int i = 0; i < storageAccountKeys.size(); i++) {
            StorageAccountKey storageAccountKey = storageAccountKeys.get(i);
            System.out.println("Key (" + i + ") " + storageAccountKey.keyName() + "="
                    + storageAccountKey.value());
        }
    }

    /**
     * Creates and returns a randomized name based on the prefix file for use by the sample.
     * @param namePrefix The prefix string to be used in generating the name.
     * @return a random name
     * */
    public static String createRandomName(String namePrefix) {
        String root = UUID.randomUUID().toString().replace("-", "");
        long millis = Calendar.getInstance().getTimeInMillis();
        long datePart = millis % 10000000L;
        return namePrefix + root.toLowerCase().substring(0, 3) + datePart;
    }

    private Utils() {

    }
}
