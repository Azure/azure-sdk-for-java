/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.google.common.base.Joiner;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.ApplicationPackage;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.BatchAccountKeys;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.keyvault.AccessPolicy;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.TcpProbe;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.InboundNatPool;
import com.microsoft.azure.management.network.InboundNatRule;
import com.microsoft.azure.management.network.Frontend;
import com.microsoft.azure.management.network.Backend;
import com.microsoft.azure.management.network.Probe;
import com.microsoft.azure.management.network.HttpProbe;
import com.microsoft.azure.management.network.PublicFrontend;
import com.microsoft.azure.management.network.PrivateFrontend;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
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
                .append("\n\tDNS server IPs: ").append(resource.dnsServerIps());

        // Output subnets
        for (Subnet subnet : resource.subnets().values()) {
            info.append("\n\tSubnet: ").append(subnet.name())
                    .append("\n\t\tAddress prefix: ").append(subnet.addressPrefix());
            NetworkSecurityGroup subnetNsg = subnet.getNetworkSecurityGroup();
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
                .append("\n\tPrimary virtual network ID: ").append(resource.primaryIpConfiguration().networkId())
                .append("\n\tPrimary subnet name:").append(resource.primaryIpConfiguration().subnetName());

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
     * Print a key vault.
     * @param vault the key vault resource
     */
    public static void print(Vault vault) {
        StringBuilder info = new StringBuilder().append("Key Vault: ").append(vault.id())
                .append("Name: ").append(vault.name())
                .append("\n\tResource group: ").append(vault.resourceGroupName())
                .append("\n\tRegion: ").append(vault.region())
                .append("\n\tSku: ").append(vault.sku().name()).append(" - ").append(vault.sku().family())
                .append("\n\tVault URI: ").append(vault.vaultUri())
                .append("\n\tAccess policies: ");
        for (AccessPolicy accessPolicy: vault.accessPolicies()) {
            info.append("\n\t\tIdentity:").append(accessPolicy.objectId())
                    .append("\n\t\tKey permissions: ").append(Joiner.on(", ").join(accessPolicy.permissions().keys()))
                    .append("\n\t\tSecret permissions: ").append(Joiner.on(", ").join(accessPolicy.permissions().secrets()));
        }
        System.out.println(info.toString());
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
     * Print load balancer.
     * @param resource a load balancer
     */
    public static void print(LoadBalancer resource) {
        StringBuilder info = new StringBuilder();
        info.append("Load balancer: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tBackends: ").append(resource.backends().keySet().toString());

        // Show public IP addresses
        info.append("\n\tPublic IP address IDs: ")
                .append(resource.publicIpAddressIds().size());
        for (String pipId : resource.publicIpAddressIds()) {
            info.append("\n\t\tPIP id: ").append(pipId);
        }

        // Show TCP probes
        info.append("\n\tTCP probes: ")
                .append(resource.tcpProbes().size());
        for (TcpProbe probe : resource.tcpProbes().values()) {
            info.append("\n\t\tProbe name: ").append(probe.name())
                    .append("\n\t\t\tPort: ").append(probe.port())
                    .append("\n\t\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                    .append("\n\t\t\tRetries before unhealthy: ").append(probe.numberOfProbes());

            // Show associated load balancing rules
            info.append("\n\t\t\tReferenced from load balancing rules: ")
                    .append(probe.loadBalancingRules().size());
            for (LoadBalancingRule rule : probe.loadBalancingRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }
        }

        // Show HTTP probes
        info.append("\n\tHTTP probes: ")
                .append(resource.httpProbes().size());
        for (HttpProbe probe : resource.httpProbes().values()) {
            info.append("\n\t\tProbe name: ").append(probe.name())
                    .append("\n\t\t\tPort: ").append(probe.port())
                    .append("\n\t\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                    .append("\n\t\t\tRetries before unhealthy: ").append(probe.numberOfProbes())
                    .append("\n\t\t\tHTTP request path: ").append(probe.requestPath());

            // Show associated load balancing rules
            info.append("\n\t\t\tReferenced from load balancing rules: ")
                    .append(probe.loadBalancingRules().size());
            for (LoadBalancingRule rule : probe.loadBalancingRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }
        }

        // Show load balancing rules
        info.append("\n\tLoad balancing rules: ")
                .append(resource.loadBalancingRules().size());
        for (LoadBalancingRule rule : resource.loadBalancingRules().values()) {
            info.append("\n\t\tLB rule name: ").append(rule.name())
                    .append("\n\t\t\tProtocol: ").append(rule.protocol())
                    .append("\n\t\t\tFloating IP enabled? ").append(rule.floatingIpEnabled())
                    .append("\n\t\t\tIdle timeout in minutes: ").append(rule.idleTimeoutInMinutes())
                    .append("\n\t\t\tLoad distribution method: ").append(rule.loadDistribution().toString());

            Frontend frontend = rule.frontend();
            info.append("\n\t\t\tFrontend: ");
            if (frontend != null) {
                info.append(frontend.name());
            } else {
                info.append("(None)");
            }

            info.append("\n\t\t\tFrontend port: ").append(rule.frontendPort());

            Backend backend = rule.backend();
            info.append("\n\t\t\tBackend: ");
            if (backend != null) {
                info.append(backend.name());
            } else {
                info.append("(None)");
            }

            info.append("\n\t\t\tBackend port: ").append(rule.backendPort());

            Probe probe = rule.probe();
            info.append("\n\t\t\tProbe: ");
            if (probe == null) {
                info.append("(None)");
            } else {
                info.append(probe.name()).append(" [").append(probe.protocol().toString()).append("]");
            }
        }

        // Show frontends
        info.append("\n\tFrontends: ")
                .append(resource.frontends().size());
        for (Frontend frontend : resource.frontends().values()) {
            info.append("\n\t\tFrontend name: ").append(frontend.name())
                    .append("\n\t\t\tInternet facing: ").append(frontend.isPublic());
            if (frontend.isPublic()) {
                info.append("\n\t\t\tPublic IP Address ID: ").append(((PublicFrontend) frontend).publicIpAddressId());
            } else {
                info.append("\n\t\t\tVirtual network ID: ").append(((PrivateFrontend) frontend).networkId())
                        .append("\n\t\t\tSubnet name: ").append(((PrivateFrontend) frontend).subnetName())
                        .append("\n\t\t\tPrivate IP address: ").append(((PrivateFrontend) frontend).privateIpAddress())
                        .append("\n\t\t\tPrivate IP allocation method: ").append(((PrivateFrontend) frontend).privateIpAllocationMethod());
            }

            // Inbound NAT pool references
            info.append("\n\t\t\tReferenced inbound NAT pools: ")
                    .append(frontend.inboundNatPools().size());
            for (InboundNatPool pool : frontend.inboundNatPools().values()) {
                info.append("\n\t\t\t\tName: ").append(pool.name());
            }

            // Inbound NAT rule references
            info.append("\n\t\t\tReferenced inbound NAT rules: ")
                    .append(frontend.inboundNatRules().size());
            for (InboundNatRule rule : frontend.inboundNatRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }

            // Load balancing rule references
            info.append("\n\t\t\tReferenced load balancing rules: ")
                    .append(frontend.loadBalancingRules().size());
            for (LoadBalancingRule rule : frontend.loadBalancingRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }
        }

        // Show inbound NAT rules
        info.append("\n\tInbound NAT rules: ")
                .append(resource.inboundNatRules().size());
        for (InboundNatRule natRule : resource.inboundNatRules().values()) {
            info.append("\n\t\tInbound NAT rule name: ").append(natRule.name())
                    .append("\n\t\t\tProtocol: ").append(natRule.protocol().toString())
                    .append("\n\t\t\tFrontend: ").append(natRule.frontend().name())
                    .append("\n\t\t\tFrontend port: ").append(natRule.frontendPort())
                    .append("\n\t\t\tBackend port: ").append(natRule.backendPort())
                    .append("\n\t\t\tBackend NIC ID: ").append(natRule.backendNetworkInterfaceId())
                    .append("\n\t\t\tBackend NIC IP config name: ").append(natRule.backendNicIpConfigurationName())
                    .append("\n\t\t\tFloating IP? ").append(natRule.floatingIpEnabled())
                    .append("\n\t\t\tIdle timeout in minutes: ").append(natRule.idleTimeoutInMinutes());
        }

        // Show inbound NAT pools
        info.append("\n\tInbound NAT pools: ")
                .append(resource.inboundNatPools().size());
        for (InboundNatPool natPool: resource.inboundNatPools().values()) {
            info.append("\n\t\tInbound NAT pool name: ").append(natPool.name())
                    .append("\n\t\t\tProtocol: ").append(natPool.protocol().toString())
                    .append("\n\t\t\tFrontend: ").append(natPool.frontend().name())
                    .append("\n\t\t\tFrontend port range: ")
                    .append(natPool.frontendPortRangeStart())
                    .append("-")
                    .append(natPool.frontendPortRangeEnd())
                    .append("\n\t\t\tBackend port: ").append(natPool.backendPort());
        }

        // Show backends
        info.append("\n\tBackends: ")
                .append(resource.backends().size());
        for (Backend backend : resource.backends().values()) {
            info.append("\n\t\tBackend name: ").append(backend.name());

            // Show assigned backend NICs
            info.append("\n\t\t\tReferenced NICs: ")
                    .append(backend.backendNicIpConfigurationNames().entrySet().size());
            for (Map.Entry<String, String> entry : backend.backendNicIpConfigurationNames().entrySet()) {
                info.append("\n\t\t\t\tNIC ID: ").append(entry.getKey())
                        .append(" - IP Config: ").append(entry.getValue());
            }

            // Show assigned virtual machines
            Set<String> vmIds = backend.getVirtualMachineIds();
            info.append("\n\t\t\tReferenced virtual machine ids: ")
                    .append(vmIds.size());
            for (String vmId : vmIds) {
                info.append("\n\t\t\t\tVM ID: ").append(vmId);
            }

            // Show assigned load balancing rules
            info.append("\n\t\t\tReferenced load balancing rules: ")
                    .append(new ArrayList<String>(backend.loadBalancingRules().keySet()));
        }

        System.out.println(info.toString());
    }

    /**
     * Prints batch account keys.
     * @param batchAccountKeys a list of batch account keys
     */
    public static void print(BatchAccountKeys batchAccountKeys) {
        System.out.println("Primary Key (" +  batchAccountKeys.primary() + ") Secondary key = ("
                + batchAccountKeys.secondary() + ")");
    }

    /**
     * Prints batch account.
     * @param batchAccount a Batch Account
     */
    public static void print(BatchAccount batchAccount) {
        StringBuilder applicationsOutput = new StringBuilder().append("\n\tapplications: ");

        if (batchAccount.applications().size() > 0) {
            for (Map.Entry<String, Application> applicationEntry : batchAccount.applications().entrySet()) {
                Application application = applicationEntry.getValue();
                StringBuilder applicationPackages = new StringBuilder().append("\n\t\t\tapplicationPackages : ");

                for (Map.Entry<String, ApplicationPackage> applicationPackageEntry: application.applicationPackages().entrySet()) {
                    ApplicationPackage applicationPackage = applicationPackageEntry.getValue();
                    StringBuilder singleApplicationPackage = new StringBuilder().append("\n\t\t\t\tapplicationPackage : " + applicationPackage.name());
                    singleApplicationPackage.append("\n\t\t\t\tapplicationPackageState : " + applicationPackage.state());

                    applicationPackages.append(singleApplicationPackage);
                    singleApplicationPackage.append("\n");
                }

                StringBuilder singleApplication = new StringBuilder().append("\n\t\tapplication: " + application.name());
                singleApplication.append("\n\t\tdisplayName: " + application.displayName());
                singleApplication.append("\n\t\tdefaultVersion: " + application.defaultVersion());
                singleApplication.append(applicationPackages);
                applicationsOutput.append(singleApplication);
                applicationsOutput.append("\n");
            }
        }

        System.out.println(new StringBuilder().append("BatchAccount: ").append(batchAccount.id())
                .append("Name: ").append(batchAccount.name())
                .append("\n\tResource group: ").append(batchAccount.resourceGroupName())
                .append("\n\tRegion: ").append(batchAccount.region())
                .append("\n\tTags: ").append(batchAccount.tags())
                .append("\n\tAccountEndpoint: ").append(batchAccount.accountEndpoint())
                .append("\n\tPoolQuota: ").append(batchAccount.poolQuota())
                .append("\n\tActiveJobAndJobScheduleQuota: ").append(batchAccount.activeJobAndJobScheduleQuota())
                .append("\n\tStorageAccount: ").append(batchAccount.autoStorage() == null ? "No storage account attached" : batchAccount.autoStorage().storageAccountId())
                .append(applicationsOutput)
                .toString());
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
