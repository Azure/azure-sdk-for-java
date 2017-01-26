/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.management.samples;

import com.google.common.base.Joiner;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrder;
import com.microsoft.azure.management.appservice.AppServiceDomain;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.AppSetting;
import com.microsoft.azure.management.appservice.ConnectionString;
import com.microsoft.azure.management.appservice.Contact;
import com.microsoft.azure.management.appservice.HostNameBinding;
import com.microsoft.azure.management.appservice.HostNameSslState;
import com.microsoft.azure.management.appservice.SslState;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.ApplicationPackage;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.BatchAccountKeys;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.ImageDataDisk;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.dns.CnameRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.MxRecord;
import com.microsoft.azure.management.dns.MxRecordSet;
import com.microsoft.azure.management.dns.NsRecordSet;
import com.microsoft.azure.management.dns.PtrRecordSet;
import com.microsoft.azure.management.dns.SoaRecord;
import com.microsoft.azure.management.dns.SoaRecordSet;
import com.microsoft.azure.management.dns.SrvRecord;
import com.microsoft.azure.management.dns.SrvRecordSet;
import com.microsoft.azure.management.dns.TxtRecord;
import com.microsoft.azure.management.dns.TxtRecordSet;
import com.microsoft.azure.management.keyvault.AccessPolicy;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendAddress;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayIpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerFrontend;
import com.microsoft.azure.management.network.LoadBalancerHttpProbe;
import com.microsoft.azure.management.network.LoadBalancerInboundNatPool;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import com.microsoft.azure.management.network.LoadBalancerProbe;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.LoadBalancerTcpProbe;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.redis.RedisAccessKeys;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisCachePremium;
import com.microsoft.azure.management.redis.ScheduleEntry;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.sql.ElasticPoolActivity;
import com.microsoft.azure.management.sql.ElasticPoolDatabaseActivity;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.trafficmanager.TrafficManagerAzureEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerExternalEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerNestedProfileEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        if (resource.osProfile() != null) {
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
        } else {
            // OSProfile will be null for a VM attached to specialized VHD.
            osProfile.append("null");
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
        for (AccessPolicy accessPolicy : vault.accessPolicies()) {
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
     * Print Redis Cache.
     * @param redisCache a Redis cache.
     */
    public static void print(RedisCache redisCache) {
        StringBuilder redisInfo = new StringBuilder()
                .append("Redis Cache Name: ").append(redisCache.name())
                .append("\n\tResource group: ").append(redisCache.resourceGroupName())
                .append("\n\tRegion: ").append(redisCache.region())
                .append("\n\tSKU Name: ").append(redisCache.sku().name())
                .append("\n\tSKU Family: ").append(redisCache.sku().family())
                .append("\n\tHost name: ").append(redisCache.hostName())
                .append("\n\tSSL port: ").append(redisCache.sslPort())
                .append("\n\tNon-SSL port (6379) enabled: ").append(redisCache.nonSslPort());
        if (redisCache.redisConfiguration() != null && !redisCache.redisConfiguration().isEmpty()) {
            redisInfo.append("\n\tRedis Configuration:");
            for (Map.Entry<String, String> redisConfiguration : redisCache.redisConfiguration().entrySet()) {
                redisInfo.append("\n\t  '").append(redisConfiguration.getKey())
                        .append("' : '").append(redisConfiguration.getValue()).append("'");
            }
        }
        if (redisCache.isPremium()) {
            RedisCachePremium premium = redisCache.asPremium();
            List<ScheduleEntry> scheduleEntries = premium.listPatchSchedules();
            if (scheduleEntries != null && !scheduleEntries.isEmpty()) {
                redisInfo.append("\n\tRedis Patch Schedule:");
                for (ScheduleEntry schedule : scheduleEntries) {
                    redisInfo.append("\n\t\tDay: '").append(schedule.dayOfWeek())
                            .append("', start at: '").append(schedule.startHourUtc())
                            .append("', maintenance window: '").append(schedule.maintenanceWindow())
                            .append("'");
                }
            }
        }

        System.out.println(redisInfo.toString());
    }

    /**
     * Print Redis Cache access keys.
     * @param redisAccessKeys a keys for Redis Cache
     */
    public static void print(RedisAccessKeys redisAccessKeys) {
        StringBuilder redisKeys = new StringBuilder()
                .append("Redis Access Keys: ")
                .append("\n\tPrimary Key: '").append(redisAccessKeys.primaryKey()).append("', ")
                .append("\n\tSecondary Key: '").append(redisAccessKeys.secondaryKey()).append("', ");

        System.out.println(redisKeys.toString());
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
        for (LoadBalancerTcpProbe probe : resource.tcpProbes().values()) {
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
        for (LoadBalancerHttpProbe probe : resource.httpProbes().values()) {
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

            LoadBalancerFrontend frontend = rule.frontend();
            info.append("\n\t\t\tFrontend: ");
            if (frontend != null) {
                info.append(frontend.name());
            } else {
                info.append("(None)");
            }

            info.append("\n\t\t\tFrontend port: ").append(rule.frontendPort());

            LoadBalancerBackend backend = rule.backend();
            info.append("\n\t\t\tBackend: ");
            if (backend != null) {
                info.append(backend.name());
            } else {
                info.append("(None)");
            }

            info.append("\n\t\t\tBackend port: ").append(rule.backendPort());

            LoadBalancerProbe probe = rule.probe();
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
        for (LoadBalancerFrontend frontend : resource.frontends().values()) {
            info.append("\n\t\tFrontend name: ").append(frontend.name())
                    .append("\n\t\t\tInternet facing: ").append(frontend.isPublic());
            if (frontend.isPublic()) {
                info.append("\n\t\t\tPublic IP Address ID: ").append(((LoadBalancerPublicFrontend) frontend).publicIpAddressId());
            } else {
                info.append("\n\t\t\tVirtual network ID: ").append(((LoadBalancerPrivateFrontend) frontend).networkId())
                        .append("\n\t\t\tSubnet name: ").append(((LoadBalancerPrivateFrontend) frontend).subnetName())
                        .append("\n\t\t\tPrivate IP address: ").append(((LoadBalancerPrivateFrontend) frontend).privateIpAddress())
                        .append("\n\t\t\tPrivate IP allocation method: ").append(((LoadBalancerPrivateFrontend) frontend).privateIpAllocationMethod());
            }

            // Inbound NAT pool references
            info.append("\n\t\t\tReferenced inbound NAT pools: ")
                    .append(frontend.inboundNatPools().size());
            for (LoadBalancerInboundNatPool pool : frontend.inboundNatPools().values()) {
                info.append("\n\t\t\t\tName: ").append(pool.name());
            }

            // Inbound NAT rule references
            info.append("\n\t\t\tReferenced inbound NAT rules: ")
                    .append(frontend.inboundNatRules().size());
            for (LoadBalancerInboundNatRule rule : frontend.inboundNatRules().values()) {
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
        for (LoadBalancerInboundNatRule natRule : resource.inboundNatRules().values()) {
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
        for (LoadBalancerInboundNatPool natPool : resource.inboundNatPools().values()) {
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
        for (LoadBalancerBackend backend : resource.backends().values()) {
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
        System.out.println("Primary Key (" + batchAccountKeys.primary() + ") Secondary key = ("
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

                for (Map.Entry<String, ApplicationPackage> applicationPackageEntry : application.applicationPackages().entrySet()) {
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
     * Print app service domain.
     * @param resource an app service domain
     */
    public static void print(AppServiceDomain resource) {
        StringBuilder builder = new StringBuilder().append("Domain: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tCreated time: ").append(resource.createdTime())
                .append("\n\tExpiration time: ").append(resource.expirationTime())
                .append("\n\tContact: ");
        Contact contact = resource.registrantContact();
        if (contact == null) {
            builder = builder.append("Private");
        } else {
            builder = builder.append("\n\t\tName: ").append(contact.nameFirst() + " " + contact.nameLast());
        }
        builder = builder.append("\n\tName servers: ");
        for (String nameServer : resource.nameServers()) {
            builder = builder.append("\n\t\t" + nameServer);
        }
        System.out.println(builder.toString());
    }

    /**
     * Print app service certificate order.
     * @param resource an app service certificate order
     */
    public static void print(AppServiceCertificateOrder resource) {
        StringBuilder builder = new StringBuilder().append("App service certificate order: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tDistinguished name: ").append(resource.distinguishedName())
                .append("\n\tProduct type: ").append(resource.productType())
                .append("\n\tValid years: ").append(resource.validityInYears())
                .append("\n\tStatus: ").append(resource.status())
                .append("\n\tIssuance time: ").append(resource.lastCertificateIssuanceTime())
                .append("\n\tSigned certificate: ").append(resource.signedCertificate() == null ? null : resource.signedCertificate().thumbprint());
        System.out.println(builder.toString());
    }

    /**
     * Print app service plan.
     * @param resource an app service plan
     */
    public static void print(AppServicePlan resource) {
        StringBuilder builder = new StringBuilder().append("App service certificate order: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tPricing tier: ").append(resource.pricingTier());
        System.out.println(builder.toString());
    }

    /**
     * Print a web app.
     * @param resource a web app
     */
    public static void print(WebAppBase resource) {
        StringBuilder builder = new StringBuilder().append("Web app: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tState: ").append(resource.state())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tDefault hostname: ").append(resource.defaultHostName())
                .append("\n\tApp service plan: ").append(resource.appServicePlanId())
                .append("\n\tHost name bindings: ");
        for (HostNameBinding binding : resource.getHostNameBindings().values()) {
            builder = builder.append("\n\t\t" + binding.toString());
        }
        builder = builder.append("\n\tSSL bindings: ");
        for (HostNameSslState binding : resource.hostNameSslStates().values()) {
            builder = builder.append("\n\t\t" + binding.name() + ": " + binding.sslState());
            if (binding.sslState() != null && binding.sslState() != SslState.DISABLED) {
                builder = builder.append(" - " + binding.thumbprint());
            }
        }
        builder = builder.append("\n\tApp settings: ");
        for (AppSetting setting : resource.appSettings().values()) {
            builder = builder.append("\n\t\t" + setting.key() + ": " + setting.value() + (setting.sticky() ? " - slot setting" : ""));
        }
        builder = builder.append("\n\tConnection strings: ");
        for (ConnectionString conn : resource.connectionStrings().values()) {
            builder = builder.append("\n\t\t" + conn.name() + ": " + conn.value() + " - " + conn.type() + (conn.sticky() ? " - slot setting" : ""));
        }
        System.out.println(builder.toString());
    }

    /**
     * Print a traffic manager profile.
     * @param profile a traffic manager profile
     */
    public static void print(TrafficManagerProfile profile) {
        StringBuilder info = new StringBuilder();
        info.append("Traffic Manager Profile: ").append(profile.id())
                .append("\n\tName: ").append(profile.name())
                .append("\n\tResource group: ").append(profile.resourceGroupName())
                .append("\n\tRegion: ").append(profile.regionName())
                .append("\n\tTags: ").append(profile.tags())
                .append("\n\tDNSLabel: ").append(profile.dnsLabel())
                .append("\n\tFQDN: ").append(profile.fqdn())
                .append("\n\tTTL: ").append(profile.timeToLive())
                .append("\n\tEnabled: ").append(profile.isEnabled())
                .append("\n\tRoutingMethod: ").append(profile.trafficRoutingMethod())
                .append("\n\tMonitor status: ").append(profile.monitorStatus())
                .append("\n\tMonitoring port: ").append(profile.monitoringPort())
                .append("\n\tMonitoring path: ").append(profile.monitoringPath());

        Map<String, TrafficManagerAzureEndpoint> azureEndpoints = profile.azureEndpoints();
        if (!azureEndpoints.isEmpty()) {
            info.append("\n\tAzure endpoints:");
            int idx = 1;
            for (TrafficManagerAzureEndpoint endpoint : azureEndpoints.values()) {
                info.append("\n\t\tAzure endpoint: #").append(idx++)
                        .append("\n\t\t\tId: ").append(endpoint.id())
                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
                        .append("\n\t\t\tTarget resourceId: ").append(endpoint.targetAzureResourceId())
                        .append("\n\t\t\tTarget resourceType: ").append(endpoint.targetResourceType())
                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
                        .append("\n\t\t\tEnabled: ").append(endpoint.isEnabled())
                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
            }
        }

        Map<String, TrafficManagerExternalEndpoint> externalEndpoints = profile.externalEndpoints();
        if (!externalEndpoints.isEmpty()) {
            info.append("\n\tExternal endpoints:");
            int idx = 1;
            for (TrafficManagerExternalEndpoint endpoint : externalEndpoints.values()) {
                info.append("\n\t\tExternal endpoint: #").append(idx++)
                        .append("\n\t\t\tId: ").append(endpoint.id())
                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
                        .append("\n\t\t\tFQDN: ").append(endpoint.fqdn())
                        .append("\n\t\t\tSource Traffic Location: ").append(endpoint.sourceTrafficLocation())
                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
                        .append("\n\t\t\tEnabled: ").append(endpoint.isEnabled())
                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
            }
        }

        Map<String, TrafficManagerNestedProfileEndpoint> nestedProfileEndpoints = profile.nestedProfileEndpoints();
        if (!nestedProfileEndpoints.isEmpty()) {
            info.append("\n\tNested profile endpoints:");
            int idx = 1;
            for (TrafficManagerNestedProfileEndpoint endpoint : nestedProfileEndpoints.values()) {
                info.append("\n\t\tNested profile endpoint: #").append(idx++)
                        .append("\n\t\t\tId: ").append(endpoint.id())
                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
                        .append("\n\t\t\tNested profileId: ").append(endpoint.nestedProfileId())
                        .append("\n\t\t\tMinimum child threshold: ").append(endpoint.minimumChildEndpointCount())
                        .append("\n\t\t\tSource Traffic Location: ").append(endpoint.sourceTrafficLocation())
                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
                        .append("\n\t\t\tEnabled: ").append(endpoint.isEnabled())
                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
            }
        }
        System.out.println(info.toString());
    }

    /**
     * Print a dns zone.
     * @param dnsZone a dns zone
     */
    public static void print(DnsZone dnsZone) {
        StringBuilder info = new StringBuilder();
        info.append("Dns Zone: ").append(dnsZone.id())
                .append("\n\tName (Top level domain): ").append(dnsZone.name())
                .append("\n\tResource group: ").append(dnsZone.resourceGroupName())
                .append("\n\tRegion: ").append(dnsZone.regionName())
                .append("\n\tTags: ").append(dnsZone.tags())
                .append("\n\tName servers:");
        for (String nameServer: dnsZone.nameServers()) {
            info.append("\n\t\t").append(nameServer);
        }
        SoaRecordSet soaRecordSet = dnsZone.getSoaRecordSet();
        SoaRecord soaRecord = soaRecordSet.record();
        info.append("\n\tSOA Record:")
                .append("\n\t\tHost:").append(soaRecord.host())
                .append("\n\t\tEmail:").append(soaRecord.email())
                .append("\n\t\tExpire time (seconds):").append(soaRecord.expireTime())
                .append("\n\t\tRefresh time (seconds):").append(soaRecord.refreshTime())
                .append("\n\t\tRetry time (seconds):").append(soaRecord.retryTime())
                .append("\n\t\tNegative response cache ttl (seconds):").append(soaRecord.minimumTtl())
                .append("\n\t\tTtl (seconds):").append(soaRecordSet.timeToLive());

        PagedList<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        info.append("\n\tA Record sets:");
        for (ARecordSet aRecordSet : aRecordSets) {
            info.append("\n\t\tId: ").append(aRecordSet.id())
                    .append("\n\t\tName: ").append(aRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(aRecordSet.timeToLive())
                    .append("\n\t\tIp v4 addresses: ");
            for (String ipAddress : aRecordSet.ipv4Addresses()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }

        PagedList<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        info.append("\n\tAAAA Record sets:");
        for (AaaaRecordSet aaaaRecordSet : aaaaRecordSets) {
            info.append("\n\t\tId: ").append(aaaaRecordSet.id())
                    .append("\n\t\tName: ").append(aaaaRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(aaaaRecordSet.timeToLive())
                    .append("\n\t\tIp v6 addresses: ");
            for (String ipAddress : aaaaRecordSet.ipv6Addresses()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }

        PagedList<CnameRecordSet> cnameRecordSets = dnsZone.cnameRecordSets().list();
        info.append("\n\tCNAME Record sets:");
        for (CnameRecordSet cnameRecordSet : cnameRecordSets) {
            info.append("\n\t\tId: ").append(cnameRecordSet.id())
                    .append("\n\t\tName: ").append(cnameRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(cnameRecordSet.timeToLive())
                    .append("\n\t\tCanonical name: ").append(cnameRecordSet.canonicalName());
        }

        PagedList<MxRecordSet> mxRecordSets = dnsZone.mxRecordSets().list();
        info.append("\n\tMX Record sets:");
        for (MxRecordSet mxRecordSet : mxRecordSets) {
            info.append("\n\t\tId: ").append(mxRecordSet.id())
                    .append("\n\t\tName: ").append(mxRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(mxRecordSet.timeToLive())
                    .append("\n\t\tRecords: ");
            for (MxRecord mxRecord : mxRecordSet.records()) {
                info.append("\n\t\t\tExchange server, Preference: ")
                        .append(mxRecord.exchange())
                        .append(" ")
                        .append(mxRecord.preference());
            }
        }

        PagedList<NsRecordSet> nsRecordSets = dnsZone.nsRecordSets().list();
        info.append("\n\tNS Record sets:");
        for (NsRecordSet nsRecordSet : nsRecordSets) {
            info.append("\n\t\tId: ").append(nsRecordSet.id())
                    .append("\n\t\tName: ").append(nsRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(nsRecordSet.timeToLive())
                    .append("\n\t\tName servers: ");
            for (String nameServer : nsRecordSet.nameServers()) {
                info.append("\n\t\t\t").append(nameServer);
            }
        }

        PagedList<PtrRecordSet> ptrRecordSets = dnsZone.ptrRecordSets().list();
        info.append("\n\tPTR Record sets:");
        for (PtrRecordSet ptrRecordSet : ptrRecordSets) {
            info.append("\n\t\tId: ").append(ptrRecordSet.id())
                    .append("\n\t\tName: ").append(ptrRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(ptrRecordSet.timeToLive())
                    .append("\n\t\tTarget domain names: ");
            for (String domainNames : ptrRecordSet.targetDomainNames()) {
                info.append("\n\t\t\t").append(domainNames);
            }
        }

        PagedList<SrvRecordSet> srvRecordSets = dnsZone.srvRecordSets().list();
        info.append("\n\tSRV Record sets:");
        for (SrvRecordSet srvRecordSet : srvRecordSets) {
            info.append("\n\t\tId: ").append(srvRecordSet.id())
                    .append("\n\t\tName: ").append(srvRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(srvRecordSet.timeToLive())
                    .append("\n\t\tRecords: ");
            for (SrvRecord srvRecord : srvRecordSet.records()) {
                info.append("\n\t\t\tTarget, Port, Priority, Weight: ")
                        .append(srvRecord.target())
                        .append(", ")
                        .append(srvRecord.port())
                        .append(", ")
                        .append(srvRecord.priority())
                        .append(", ")
                        .append(srvRecord.weight());
            }
        }

        PagedList<TxtRecordSet> txtRecordSets = dnsZone.txtRecordSets().list();
        info.append("\n\tTXT Record sets:");
        for (TxtRecordSet txtRecordSet : txtRecordSets) {
            info.append("\n\t\tId: ").append(txtRecordSet.id())
                    .append("\n\t\tName: ").append(txtRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(txtRecordSet.timeToLive())
                    .append("\n\t\tRecords: ");
            for (TxtRecord txtRecord : txtRecordSet.records()) {
                if (txtRecord.value().size() > 0) {
                    info.append("\n\t\t\tValue: ").append(txtRecord.value().get(0));
                }
            }
        }
        System.out.println(info.toString());
    }

    /**
     * Creates and returns a randomized name based on the prefix file for use by the sample.
     * @param namePrefix The prefix string to be used in generating the name.
     * @return a random name
     * */
    public static String createRandomName(String namePrefix) {
        return SdkContext.randomResourceName(namePrefix, 30);
    }

    /**
     * This method creates a certificate for given password.
     *
     * @param certPath location of certificate file
     * @param pfxPath location of pfx file
     * @param alias User alias
     * @param password alias password
     * @param cnName domain name
     * @throws Exception exceptions from the creation
     */
    public static void createCertificate(String certPath, String pfxPath,
                                         String alias, String password, String cnName) throws Exception {

        String validityInDays = "3650";
        String keyAlg = "RSA";
        String sigAlg = "SHA1withRSA";
        String keySize = "2048";
        String storeType = "pkcs12";
        String command = "keytool";
        String jdkPath = System.getProperty("java.home");
        if (jdkPath != null && !jdkPath.isEmpty()) {
            jdkPath = jdkPath.concat("\\bin");
        }
        if (new File(jdkPath).isDirectory()) {
            command = String.format("%s%s%s", jdkPath, File.separator, command);
        }

        // Create Pfx file
        String[] commandArgs = {command, "-genkey", "-alias", alias,
                "-keystore", pfxPath, "-storepass", password, "-validity",
                validityInDays, "-keyalg", keyAlg, "-sigalg", sigAlg, "-keysize", keySize,
                "-storetype", storeType, "-dname", "CN=" + cnName, "-ext", "EKU=1.3.6.1.5.5.7.3.1"};
        Utils.cmdInvocation(commandArgs, false);

        // Create cer file i.e. extract public key from pfx
        File pfxFile = new File(pfxPath);
        if (pfxFile.exists()) {
            String[] certCommandArgs = {command, "-export", "-alias", alias,
                    "-storetype", storeType, "-keystore", pfxPath,
                    "-storepass", password, "-rfc", "-file", certPath};
            // output of keytool export command is going to error stream
            // although command is
            // executed successfully, hence ignoring error stream in this case
            Utils.cmdInvocation(certCommandArgs, true);

            // Check if file got created or not
            File cerFile = new File(pfxPath);
            if (!cerFile.exists()) {
                throw new IOException(
                        "Error occurred while creating certificate"
                                + StringUtils.join(" ", certCommandArgs));
            }
        } else {
            throw new IOException("Error occurred while creating certificates"
                    + StringUtils.join(" ", commandArgs));
        }
    }

    /**
     * This method is used for invoking native commands.
     *
     * @param command
     *            :- command to invoke.
     * @param ignoreErrorStream
     *            : Boolean which controls whether to throw exception or not
     *            based on error stream.
     * @return result :- depending on the method invocation.
     * @throws Exception exceptions thrown from the execution
     */
    public static String cmdInvocation(String[] command,
                                       boolean ignoreErrorStream) throws Exception {
        String result = "";
        String error = "";
        InputStream inputStream = null;
        InputStream errorStream = null;
        BufferedReader br = null;
        BufferedReader ebr = null;
        try {
            Process process = new ProcessBuilder(command).start();
            inputStream = process.getInputStream();
            errorStream = process.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream));
            result = br.readLine();
            process.waitFor();
            ebr = new BufferedReader(new InputStreamReader(errorStream));
            error = ebr.readLine();
            if (error != null && (!error.equals(""))) {
                // To do - Log error message

                if (!ignoreErrorStream) {
                    throw new Exception(error, null);
                }
            }
        } catch (Exception e) {
            throw new Exception("Exception occurred while invoking command", e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (errorStream != null) {
                errorStream.close();
            }
            if (br != null) {
                br.close();
            }
            if (ebr != null) {
                ebr.close();
            }
        }
        return result;
    }


    /**
     * Prints information for passed SQL Server.
     * @param sqlServer sqlServer to be printed
     */
    public static void print(SqlServer sqlServer) {
        StringBuilder builder = new StringBuilder().append("Sql Server: ").append(sqlServer.id())
                .append("Name: ").append(sqlServer.name())
                .append("\n\tResource group: ").append(sqlServer.resourceGroupName())
                .append("\n\tRegion: ").append(sqlServer.region())
                .append("\n\tSqlServer version: ").append(sqlServer.version())
                .append("\n\tFully qualified name for Sql Server: ").append(sqlServer.fullyQualifiedDomainName());
        System.out.println(builder.toString());
    }

    /**
     * Prints information for the passed SQL Database.
     * @param database database to be printed
     */
    public static void print(SqlDatabase database) {
        StringBuilder builder = new StringBuilder().append("Sql Database: ").append(database.id())
                .append("Name: ").append(database.name())
                .append("\n\tResource group: ").append(database.resourceGroupName())
                .append("\n\tRegion: ").append(database.region())
                .append("\n\tSqlServer Name: ").append(database.sqlServerName())
                .append("\n\tEdition of SQL database: ").append(database.edition())
                .append("\n\tCollation of SQL database: ").append(database.collation())
                .append("\n\tCreation date of SQL database: ").append(database.creationDate())
                .append("\n\tIs data warehouse: ").append(database.isDataWarehouse())
                .append("\n\tCurrent service objective of SQL database: ").append(database.serviceLevelObjective())
                .append("\n\tId of current service objective of SQL database: ").append(database.currentServiceObjectiveId())
                .append("\n\tMax size bytes of SQL database: ").append(database.maxSizeBytes())
                .append("\n\tDefault secondary location of SQL database: ").append(database.defaultSecondaryLocation());

        System.out.println(builder.toString());
    }

    /**
     * Prints information for the passed firewall rule.
     * @param firewallRule firewall rule to be printed.
     */
    public static void print(SqlFirewallRule firewallRule) {
        StringBuilder builder = new StringBuilder().append("Sql firewall rule: ").append(firewallRule.id())
                .append("Name: ").append(firewallRule.name())
                .append("\n\tResource group: ").append(firewallRule.resourceGroupName())
                .append("\n\tRegion: ").append(firewallRule.region())
                .append("\n\tSqlServer Name: ").append(firewallRule.sqlServerName())
                .append("\n\tStart IP Address of the firewall rule: ").append(firewallRule.startIpAddress())
                .append("\n\tEnd IP Address of the firewall rule: ").append(firewallRule.endIpAddress());

        System.out.println(builder.toString());
    }

    /**
     * Prints information of the elastic pool passed in.
     * @param elasticPool elastic pool to be printed
     */
    public static void print(SqlElasticPool elasticPool) {
        StringBuilder builder = new StringBuilder().append("Sql elastic pool: ").append(elasticPool.id())
                .append("Name: ").append(elasticPool.name())
                .append("\n\tResource group: ").append(elasticPool.resourceGroupName())
                .append("\n\tRegion: ").append(elasticPool.region())
                .append("\n\tSqlServer Name: ").append(elasticPool.sqlServerName())
                .append("\n\tEdition of elastic pool: ").append(elasticPool.edition())
                .append("\n\tTotal number of DTUs in the elastic pool: ").append(elasticPool.dtu())
                .append("\n\tMaximum DTUs a database can get in elastic pool: ").append(elasticPool.databaseDtuMax())
                .append("\n\tMinimum DTUs a database is guaranteed in elastic pool: ").append(elasticPool.databaseDtuMin())
                .append("\n\tCreation date for the elastic pool: ").append(elasticPool.creationDate())
                .append("\n\tState of the elastic pool: ").append(elasticPool.state())
                .append("\n\tStorage capacity in MBs for the elastic pool: ").append(elasticPool.storageMB());

        System.out.println(builder.toString());
    }

    /**
     * Prints information of the elastic pool activity.
     * @param elasticPoolActivity elastic pool activity to be printed
     */
    public static void print(ElasticPoolActivity elasticPoolActivity) {
        StringBuilder builder = new StringBuilder().append("Sql elastic pool activity: ").append(elasticPoolActivity.id())
                .append("Name: ").append(elasticPoolActivity.name())
                .append("\n\tResource group: ").append(elasticPoolActivity.resourceGroupName())
                .append("\n\tState: ").append(elasticPoolActivity.state())
                .append("\n\tElastic pool name: ").append(elasticPoolActivity.elasticPoolName())
                .append("\n\tStart time of activity: ").append(elasticPoolActivity.startTime())
                .append("\n\tEnd time of activity: ").append(elasticPoolActivity.endTime())
                .append("\n\tError code of activity: ").append(elasticPoolActivity.errorCode())
                .append("\n\tError message of activity: ").append(elasticPoolActivity.errorMessage())
                .append("\n\tError severity of activity: ").append(elasticPoolActivity.errorSeverity())
                .append("\n\tOperation: ").append(elasticPoolActivity.operation())
                .append("\n\tCompleted percentage of activity: ").append(elasticPoolActivity.percentComplete())
                .append("\n\tRequested DTU max limit in activity: ").append(elasticPoolActivity.requestedDatabaseDtuMax())
                .append("\n\tRequested DTU min limit in activity: ").append(elasticPoolActivity.requestedDatabaseDtuMin())
                .append("\n\tRequested DTU limit in activity: ").append(elasticPoolActivity.requestedDtu());

        System.out.println(builder.toString());

    }

    /**
     * Prints information of the database activity.
     * @param databaseActivity database activity to be printed
     */
    public static void print(ElasticPoolDatabaseActivity databaseActivity) {
        StringBuilder builder = new StringBuilder().append("Sql elastic pool database activity: ").append(databaseActivity.id())
                .append("Name: ").append(databaseActivity.name())
                .append("\n\tResource group: ").append(databaseActivity.resourceGroupName())
                .append("\n\tSQL Server Name: ").append(databaseActivity.serverName())
                .append("\n\tDatabase name name: ").append(databaseActivity.databaseName())
                .append("\n\tCurrent elastic pool name of the database: ").append(databaseActivity.currentElasticPoolName())
                .append("\n\tState: ").append(databaseActivity.state())
                .append("\n\tStart time of activity: ").append(databaseActivity.startTime())
                .append("\n\tEnd time of activity: ").append(databaseActivity.endTime())
                .append("\n\tCompleted percentage: ").append(databaseActivity.percentComplete())
                .append("\n\tError code of activity: ").append(databaseActivity.errorCode())
                .append("\n\tError message of activity: ").append(databaseActivity.errorMessage())
                .append("\n\tError severity of activity: ").append(databaseActivity.errorSeverity());

        System.out.println(builder.toString());
    }

    /**
     * Print an application gateway.
     * @param resource an application gateway
     */
    public static void print(ApplicationGateway resource) {
        StringBuilder info = new StringBuilder();
        info.append("Application gateway: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tSKU: ").append(resource.sku().toString())
                .append("\n\tOperational state: ").append(resource.operationalState())
                .append("\n\tSSL policy: ").append(resource.sslPolicy())
                .append("\n\tInternet-facing? ").append(resource.isPublic())
                .append("\n\tInternal? ").append(resource.isPrivate())
                .append("\n\tDefault private IP address: ").append(resource.privateIpAddress())
                .append("\n\tPrivate IP address allocation method: ").append(resource.privateIpAllocationMethod());

        // Show IP configs
        Map<String, ApplicationGatewayIpConfiguration> ipConfigs = resource.ipConfigurations();
        info.append("\n\tIP configurations: ").append(ipConfigs.size());
        for (ApplicationGatewayIpConfiguration ipConfig : ipConfigs.values()) {
            info.append("\n\t\tName: ").append(ipConfig.name())
                    .append("\n\t\t\tNetwork id: ").append(ipConfig.networkId())
                    .append("\n\t\t\tSubnet name: ").append(ipConfig.subnetName());
        }

        // Show frontends
        Map<String, ApplicationGatewayFrontend> frontends = resource.frontends();
        info.append("\n\tFrontends: ").append(frontends.size());
        for (ApplicationGatewayFrontend frontend : frontends.values()) {
            info.append("\n\t\tName: ").append(frontend.name())
                    .append("\n\t\t\tPublic? ").append(frontend.isPublic());

            if (frontend.isPublic()) {
                // Show public frontend info
                info.append("\n\t\t\tPublic IP address ID: ").append(frontend.publicIpAddressId());
            }

            if (frontend.isPrivate()) {
                // Show private frontend info
                info.append("\n\t\t\tPrivate IP address: ").append(frontend.privateIpAddress())
                        .append("\n\t\t\tPrivate IP allocation method: ").append(frontend.privateIpAllocationMethod())
                        .append("\n\t\t\tSubnet name: ").append(frontend.subnetName())
                        .append("\n\t\t\tVirtual network ID: ").append(frontend.networkId());
            }
        }

        // Show backends
        Map<String, ApplicationGatewayBackend> backends = resource.backends();
        info.append("\n\tBackends: ").append(backends.size());
        for (ApplicationGatewayBackend backend : backends.values()) {
            info.append("\n\t\tName: ").append(backend.name())
                    .append("\n\t\t\tAssociated NIC IP configuration IDs: ").append(backend.backendNicIpConfigurationNames().keySet());

            // Show addresses
            List<ApplicationGatewayBackendAddress> addresses = backend.addresses();
            info.append("\n\t\t\tAddresses: ").append(addresses.size());
            for (ApplicationGatewayBackendAddress address : addresses) {
                info.append("\n\t\t\t\tFQDN: ").append(address.fqdn())
                        .append("\n\t\t\t\tIP: ").append(address.ipAddress());
            }
        }

        // Show backend HTTP configurations
        Map<String, ApplicationGatewayBackendHttpConfiguration> httpConfigs = resource.backendHttpConfigurations();
        info.append("\n\tHTTP Configurations: ").append(httpConfigs.size());
        for (ApplicationGatewayBackendHttpConfiguration httpConfig : httpConfigs.values()) {
            info.append("\n\t\tName: ").append(httpConfig.name())
                    .append("\n\t\t\tCookie based affinity: ").append(httpConfig.cookieBasedAffinity())
                    .append("\n\t\t\tPort: ").append(httpConfig.port())
                    .append("\n\t\t\tRequest timeout in seconds: ").append(httpConfig.requestTimeout())
                    .append("\n\t\t\tProtocol: ").append(httpConfig.protocol());
        }

        // Show SSL certificates
        Map<String, ApplicationGatewaySslCertificate> sslCerts = resource.sslCertificates();
        info.append("\n\tSSL certificates: ").append(sslCerts.size());
        for (ApplicationGatewaySslCertificate cert : sslCerts.values()) {
            info.append("\n\t\tName: ").append(cert.name())
                    .append("\n\t\t\tCert data: ").append(cert.publicData());
        }

        // Show HTTP listeners
        Map<String, ApplicationGatewayListener> listeners = resource.listeners();
        info.append("\n\tHTTP listeners: ").append(listeners.size());
        for (ApplicationGatewayListener listener : listeners.values()) {
            info.append("\n\t\tName: ").append(listener.name())
                    .append("\n\t\t\tHost name: ").append(listener.hostName())
                    .append("\n\t\t\tServer name indication required? ").append(listener.requiresServerNameIndication())
                    .append("\n\t\t\tAssociated frontend name: ").append(listener.frontend().name())
                    .append("\n\t\t\tFrontend port name: ").append(listener.frontendPortName())
                    .append("\n\t\t\tFrontend port number: ").append(listener.frontendPortNumber())
                    .append("\n\t\t\tProtocol: ").append(listener.protocol().toString());
            if (listener.sslCertificate() != null) {
                info.append("\n\t\t\tAssociated SSL certificate: ").append(listener.sslCertificate().name());
            }
        }

        // Show request routing rules
        Map<String, ApplicationGatewayRequestRoutingRule> rules = resource.requestRoutingRules();
        info.append("\n\tRequest routing rules: ").append(rules.size());
        for (ApplicationGatewayRequestRoutingRule rule : rules.values()) {
            info.append("\n\t\tName: ").append(rule.name())
                    .append("\n\t\t\tType: ").append(rule.ruleType())
                    .append("\n\t\t\tPublic IP address ID: ").append(rule.publicIpAddressId())
                    .append("\n\t\t\tHost name: ").append(rule.hostName())
                    .append("\n\t\t\tServer name indication required? ").append(rule.requiresServerNameIndication())
                    .append("\n\t\t\tFrontend port: ").append(rule.frontendPort())
                    .append("\n\t\t\tFrontend protocol: ").append(rule.frontendProtocol().toString())
                    .append("\n\t\t\tBackend port: ").append(rule.backendPort())
                    .append("\n\t\t\tCookie based affinity enabled? ").append(rule.cookieBasedAffinity());

            // Show backend addresses
            List<ApplicationGatewayBackendAddress> addresses = rule.backendAddresses();
            info.append("\n\t\t\tBackend addresses: ").append(addresses.size());
            for (ApplicationGatewayBackendAddress address : addresses) {
                info.append("\n\t\t\t\t")
                        .append(address.fqdn())
                        .append(" [").append(address.ipAddress()).append("]");
            }

            // Show SSL cert
            info.append("\n\t\t\tSSL certificate name: ");
            ApplicationGatewaySslCertificate cert = rule.sslCertificate();
            if (cert == null) {
                info.append("(None)");
            } else {
                info.append(cert.name());
            }

            // Show backend
            info.append("\n\t\t\tAssociated backend address pool: ");
            ApplicationGatewayBackend backend = rule.backend();
            if (backend == null) {
                info.append("(None)");
            } else {
                info.append(backend.name());
            }

            // Show backend HTTP settings config
            info.append("\n\t\t\tAssociated backend HTTP settings configuration: ");
            ApplicationGatewayBackendHttpConfiguration config = rule.backendHttpConfiguration();
            if (config == null) {
                info.append("(None)");
            } else {
                info.append(config.name());
            }

            // Show frontend listener
            info.append("\n\t\t\tAssociated frontend listener: ");
            ApplicationGatewayListener listener = rule.listener();
            if (listener == null) {
                info.append("(None)");
            } else {
                info.append(config.name());
            }
        }
        System.out.println(info.toString());
    }

    /**
     * Prints information of a virtual machine custom image.
     *
     * @param image the image
     */
    public static void print(VirtualMachineCustomImage image) {
        StringBuilder builder = new StringBuilder().append("Virtual machine custom image: ").append(image.id())
                .append("Name: ").append(image.name())
                .append("\n\tResource group: ").append(image.resourceGroupName())
                .append("\n\tCreated from virtual machine: ").append(image.sourceVirtualMachineId());

        builder.append("\n\tOS disk image: ")
                .append("\n\t\tOperating system: ").append(image.osDiskImage().osType())
                .append("\n\t\tOperating system state: ").append(image.osDiskImage().osState())
                .append("\n\t\tCaching: ").append(image.osDiskImage().caching())
                .append("\n\t\tSize (GB): ").append(image.osDiskImage().diskSizeGB());
        if (image.isCreatedFromVirtualMachine()) {
            builder.append("\n\t\tSource virtual machine: ").append(image.sourceVirtualMachineId());
        }
        if (image.osDiskImage().managedDisk() != null) {
            builder.append("\n\t\tSource managed disk: ").append(image.osDiskImage().managedDisk().id());
        }
        if (image.osDiskImage().snapshot() != null) {
            builder.append("\n\t\tSource snapshot: ").append(image.osDiskImage().snapshot().id());
        }
        if (image.osDiskImage().blobUri() != null) {
            builder.append("\n\t\tSource un-managed vhd: ").append(image.osDiskImage().blobUri());
        }
        if (image.dataDiskImages() != null) {
            for (ImageDataDisk diskImage : image.dataDiskImages().values()) {
                builder.append("\n\tDisk Image (Lun) #: ").append(diskImage.lun())
                        .append("\n\t\tCaching: ").append(diskImage.caching())
                        .append("\n\t\tSize (GB): ").append(diskImage.diskSizeGB());
                if (image.isCreatedFromVirtualMachine()) {
                    builder.append("\n\t\tSource virtual machine: ").append(image.sourceVirtualMachineId());
                }
                if (diskImage.managedDisk() != null) {
                    builder.append("\n\t\tSource managed disk: ").append(diskImage.managedDisk().id());
                }
                if (diskImage.snapshot() != null) {
                    builder.append("\n\t\tSource snapshot: ").append(diskImage.snapshot().id());
                }
                if (diskImage.blobUri() != null) {
                    builder.append("\n\t\tSource un-managed vhd: ").append(diskImage.blobUri());
                }
            }
        }
        System.out.println(builder.toString());
    }

    private Utils() {

    }
}
