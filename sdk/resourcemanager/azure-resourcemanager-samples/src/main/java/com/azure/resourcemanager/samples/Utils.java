// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.AppSetting;
import com.azure.resourcemanager.appservice.models.ConnectionString;
import com.azure.resourcemanager.appservice.models.Contact;
import com.azure.resourcemanager.appservice.models.HostnameBinding;
import com.azure.resourcemanager.appservice.models.HostnameSslState;
import com.azure.resourcemanager.appservice.models.PublishingProfile;
import com.azure.resourcemanager.appservice.models.SslState;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.appservice.models.WebSiteBase;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryObject;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.Permission;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.RoleDefinition;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.DataDisk;
import com.azure.resourcemanager.compute.models.ImageDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineExtension;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountListKeysResult;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountListReadOnlyKeysResult;
import com.azure.resourcemanager.cosmos.models.Location;
import com.azure.resourcemanager.dns.models.ARecordSet;
import com.azure.resourcemanager.dns.models.AaaaRecordSet;
import com.azure.resourcemanager.dns.models.CnameRecordSet;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.dns.models.MxRecord;
import com.azure.resourcemanager.dns.models.MxRecordSet;
import com.azure.resourcemanager.dns.models.NsRecordSet;
import com.azure.resourcemanager.dns.models.PtrRecordSet;
import com.azure.resourcemanager.dns.models.SoaRecord;
import com.azure.resourcemanager.dns.models.SoaRecordSet;
import com.azure.resourcemanager.dns.models.SrvRecord;
import com.azure.resourcemanager.dns.models.SrvRecordSet;
import com.azure.resourcemanager.dns.models.TxtRecord;
import com.azure.resourcemanager.dns.models.TxtRecordSet;
import com.azure.resourcemanager.keyvault.models.AccessPolicy;
import com.azure.resourcemanager.keyvault.models.CertificatePermissions;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.monitor.models.ActionGroup;
import com.azure.resourcemanager.monitor.models.ActivityLogAlert;
import com.azure.resourcemanager.monitor.models.AutomationRunbookReceiver;
import com.azure.resourcemanager.monitor.models.AzureAppPushReceiver;
import com.azure.resourcemanager.monitor.models.AzureFunctionReceiver;
import com.azure.resourcemanager.monitor.models.DiagnosticSetting;
import com.azure.resourcemanager.monitor.models.EmailReceiver;
import com.azure.resourcemanager.monitor.models.ItsmReceiver;
import com.azure.resourcemanager.monitor.models.LogSettings;
import com.azure.resourcemanager.monitor.models.LogicAppReceiver;
import com.azure.resourcemanager.monitor.models.MetricAlert;
import com.azure.resourcemanager.monitor.models.MetricAlertCondition;
import com.azure.resourcemanager.monitor.models.MetricDimension;
import com.azure.resourcemanager.monitor.models.MetricSettings;
import com.azure.resourcemanager.monitor.models.SmsReceiver;
import com.azure.resourcemanager.monitor.models.VoiceReceiver;
import com.azure.resourcemanager.monitor.models.WebhookReceiver;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.fluent.models.SecurityRuleInner;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendAddress;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayFrontend;
import com.azure.resourcemanager.network.models.ApplicationGatewayIpConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayListener;
import com.azure.resourcemanager.network.models.ApplicationGatewayProbe;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRule;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslCertificate;
import com.azure.resourcemanager.network.models.EffectiveNetworkSecurityRule;
import com.azure.resourcemanager.network.models.FlowLogSettings;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerHttpProbe;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatPool;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatRule;
import com.azure.resourcemanager.network.models.LoadBalancerPrivateFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerProbe;
import com.azure.resourcemanager.network.models.LoadBalancerPublicFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerTcpProbe;
import com.azure.resourcemanager.network.models.LoadBalancingRule;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkPeering;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NetworkSecurityRule;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.NextHop;
import com.azure.resourcemanager.network.models.PacketCapture;
import com.azure.resourcemanager.network.models.PacketCaptureFilter;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.RouteTable;
import com.azure.resourcemanager.network.models.SecurityGroupNetworkInterface;
import com.azure.resourcemanager.network.models.SecurityGroupView;
import com.azure.resourcemanager.network.models.ServiceEndpointType;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.models.Topology;
import com.azure.resourcemanager.network.models.TopologyAssociation;
import com.azure.resourcemanager.network.models.TopologyResource;
import com.azure.resourcemanager.network.models.VerificationIPFlow;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionStatus;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageService;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Common utils for Azure management samples.
 */

public final class Utils {

    private Utils() {
    }

    /** @return a generated password */
    public static String password() {
        String password = new ResourceManagerUtils.InternalRuntimeContext().randomResourceName("Pa5$", 12);
        System.out.printf("Password: %s%n", password);
        return password;
    }

    /**
     * Creates a randomized resource name.
     * Please provider your own implementation, or avoid using the method, if code is to be used in production.
     *
     * @param azure the AzureResourceManager instance.
     * @param prefix the prefix to the name.
     * @param maxLen the max length of the name.
     * @return the randomized resource name.
     */
    public static String randomResourceName(AzureResourceManager azure, String prefix, int maxLen) {
        return azure.resourceGroups().manager().internalContext().randomResourceName(prefix, maxLen);
    }

    /**
     * Generates the specified number of random resource names with the same prefix.
     * Please provider your own implementation, or avoid using the method, if code is to be used in production.
     *
     * @param azure the AzureResourceManager instance.
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @param count the number of names to generate
     * @return the randomized resource names.
     */
    public static String[] randomResourceNames(AzureResourceManager azure, String prefix, int maxLen, int count) {
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = randomResourceName(azure, prefix, maxLen);
        }
        return names;
    }

    /**
     * Creates a random UUID.
     * Please provider your own implementation, or avoid using the method, if code is to be used in production.
     *
     * @param azure the AzureResourceManager instance.
     * @return the random UUID.
     */
    public static String randomUuid(AzureResourceManager azure) {
        return azure.resourceGroups().manager().internalContext().randomUuid();
    }

    /**
     * Creates a randomized resource name.
     * Please provider your own implementation, or avoid using the method, if code is to be used in production.
     *
     * @param authenticated the AzureResourceManager.Authenticated instance.
     * @param prefix the prefix to the name.
     * @param maxLen the max length of the name.
     * @return the randomized resource name.
     */
    public static String randomResourceName(AzureResourceManager.Authenticated authenticated, String prefix, int maxLen) {
        return authenticated.roleAssignments().manager().internalContext().randomResourceName(prefix, maxLen);
    }

    /**
     * Print resource group info.
     *
     * @param resource a resource group
     */
    public static void print(ResourceGroup resource) {
        StringBuilder info = new StringBuilder();
        info.append("Resource Group: ").append(resource.id())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags());
        System.out.println(info.toString());
    }

    /**
     * Print User Assigned MSI info.
     *
     * @param resource a User Assigned MSI
     */
    public static void print(Identity resource) {
        StringBuilder info = new StringBuilder();
        info.append("Resource Group: ").append(resource.id())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tService Principal Id: ").append(resource.principalId())
                .append("\n\tClient Id: ").append(resource.clientId())
                .append("\n\tTenant Id: ").append(resource.tenantId());
        System.out.println(info.toString());
    }

    /**
     * Print virtual machine info.
     *
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
        for (Map.Entry<String, VirtualMachineExtension> extensionEntry : resource.listExtensions().entrySet()) {
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

        StringBuilder msi = new StringBuilder().append("\n\tMSI: ");
        msi.append("\n\t\t\tMSI enabled:").append(resource.isManagedServiceIdentityEnabled());
        msi.append("\n\t\t\tSystem Assigned MSI Active Directory Service Principal Id:").append(resource.systemAssignedManagedServiceIdentityPrincipalId());
        msi.append("\n\t\t\tSystem Assigned MSI Active Directory Tenant Id:").append(resource.systemAssignedManagedServiceIdentityTenantId());

        StringBuilder zones = new StringBuilder().append("\n\tZones: ");
        zones.append(resource.availabilityZones());

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
                .append(msi)
                .append(zones)
                .toString());
    }


    /**
     * Print availability set info.
     *
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
     *
     * @param resource a network
     * @throws ManagementException Cloud errors
     */
    public static void print(Network resource) {
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

            // Output associated NSG
            NetworkSecurityGroup subnetNsg = subnet.getNetworkSecurityGroup();
            if (subnetNsg != null) {
                info.append("\n\t\tNetwork security group ID: ").append(subnetNsg.id());
            }

            // Output associated route table
            RouteTable routeTable = subnet.getRouteTable();
            if (routeTable != null) {
                info.append("\n\tRoute table ID: ").append(routeTable.id());
            }

            // Output services with access
            Map<ServiceEndpointType, List<Region>> services = subnet.servicesWithAccess();
            if (services.size() > 0) {
                info.append("\n\tServices with access");
                for (Map.Entry<ServiceEndpointType, List<Region>> service : services.entrySet()) {
                    info.append("\n\t\tService: ")
                            .append(service.getKey())
                            .append(" Regions: " + service.getValue() + "");
                }
            }
        }

        // Output peerings
        for (NetworkPeering peering : resource.peerings().list()) {
            info.append("\n\tPeering: ").append(peering.name())
                    .append("\n\t\tRemote network ID: ").append(peering.remoteNetworkId())
                    .append("\n\t\tPeering state: ").append(peering.state())
                    .append("\n\t\tIs traffic forwarded from remote network allowed? ").append(peering.isTrafficForwardingFromRemoteNetworkAllowed())
                    .append("\n\t\tGateway use: ").append(peering.gatewayUse());
        }
        System.out.println(info.toString());
    }

    /**
     * Print network interface.
     *
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

        info.append("\n\tIP forwarding enabled? ").append(resource.isIPForwardingEnabled())
                .append("\n\tAccelerated networking enabled? ").append(resource.isAcceleratedNetworkingEnabled())
                .append("\n\tMAC Address:").append(resource.macAddress())
                .append("\n\tPrivate IP:").append(resource.primaryPrivateIP())
                .append("\n\tPrivate allocation method:").append(resource.primaryPrivateIpAllocationMethod())
                .append("\n\tPrimary virtual network ID: ").append(resource.primaryIPConfiguration().networkId())
                .append("\n\tPrimary subnet name:").append(resource.primaryIPConfiguration().subnetName());

        System.out.println(info.toString());
    }

    /**
     * Print network security group.
     *
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
     *
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
                .append("\n\tZones: ").append(resource.availabilityZones())
                .toString());
    }

    /**
     * Print a key vault.
     *
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
            info.append("\n\t\tIdentity:").append(accessPolicy.objectId());
            if (accessPolicy.permissions() != null) {
                if (accessPolicy.permissions().keys() != null) {
                    info.append("\n\t\tKey permissions: ").append(accessPolicy.permissions().keys().stream().map(KeyPermissions::toString).collect(Collectors.joining(", ")));
                }
                if (accessPolicy.permissions().secrets() != null) {
                    info.append("\n\t\tSecret permissions: ").append(accessPolicy.permissions().secrets().stream().map(SecretPermissions::toString).collect(Collectors.joining(", ")));
                }
                if (accessPolicy.permissions().certificates() != null) {
                    info.append("\n\t\tCertificate permissions: ").append(accessPolicy.permissions().certificates().stream().map(CertificatePermissions::toString).collect(Collectors.joining(", ")));
                }
            }
        }
        System.out.println(info.toString());
    }

    /**
     * Print storage account.
     *
     * @param storageAccount a storage account
     */
    public static void print(StorageAccount storageAccount) {
        System.out.println(storageAccount.name()
                + " created @ " + storageAccount.creationTime());

        StringBuilder info = new StringBuilder().append("Storage Account: ").append(storageAccount.id())
                .append("Name: ").append(storageAccount.name())
                .append("\n\tResource group: ").append(storageAccount.resourceGroupName())
                .append("\n\tRegion: ").append(storageAccount.region())
                .append("\n\tSKU: ").append(storageAccount.skuType().name().toString())
                .append("\n\tAccessTier: ").append(storageAccount.accessTier())
                .append("\n\tKind: ").append(storageAccount.kind());

        info.append("\n\tNetwork Rule Configuration: ")
                .append("\n\t\tAllow reading logs from any network: ").append(storageAccount.canReadLogEntriesFromAnyNetwork())
                .append("\n\t\tAllow reading metrics from any network: ").append(storageAccount.canReadMetricsFromAnyNetwork())
                .append("\n\t\tAllow access from all azure services: ").append(storageAccount.canAccessFromAzureServices());

        if (storageAccount.networkSubnetsWithAccess().size() > 0) {
            info.append("\n\t\tNetwork subnets with access: ");
            for (String subnetId : storageAccount.networkSubnetsWithAccess()) {
                info.append("\n\t\t\t").append(subnetId);
            }
        }
        if (storageAccount.ipAddressesWithAccess().size() > 0) {
            info.append("\n\t\tIP addresses with access: ");
            for (String ipAddress : storageAccount.ipAddressesWithAccess()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }
        if (storageAccount.ipAddressRangesWithAccess().size() > 0) {
            info.append("\n\t\tIP address-ranges with access: ");
            for (String ipAddressRange : storageAccount.ipAddressRangesWithAccess()) {
                info.append("\n\t\t\t").append(ipAddressRange);
            }
        }
        info.append("\n\t\tTraffic allowed from only HTTPS: ").append(storageAccount.innerModel().enableHttpsTrafficOnly());

        info.append("\n\tEncryption status: ");
        for (Map.Entry<StorageService, StorageAccountEncryptionStatus> eStatus : storageAccount.encryptionStatuses().entrySet()) {
            info.append("\n\t\t").append(eStatus.getValue().storageService()).append(": ").append(eStatus.getValue().isEnabled() ? "Enabled" : "Disabled");
        }

        System.out.println(info.toString());
    }

    /**
     * Print storage account keys.
     *
     * @param storageAccountKeys a list of storage account keys
     */
    public static void print(List<StorageAccountKey> storageAccountKeys) {
        for (int i = 0; i < storageAccountKeys.size(); i++) {
            StorageAccountKey storageAccountKey = storageAccountKeys.get(i);
            System.out.println("Key (" + i + ") " + storageAccountKey.keyName() + "="
                    + storageAccountKey.value());
        }
    }


//    /**
//     * Print Redis Cache.
//     *
//     * @param redisCache a Redis cache.
//     */
//    public static void print(RedisCache redisCache) {
//        StringBuilder redisInfo = new StringBuilder()
//                .append("Redis Cache Name: ").append(redisCache.name())
//                .append("\n\tResource group: ").append(redisCache.resourceGroupName())
//                .append("\n\tRegion: ").append(redisCache.region())
//                .append("\n\tSKU Name: ").append(redisCache.sku().name())
//                .append("\n\tSKU Family: ").append(redisCache.sku().family())
//                .append("\n\tHostname: ").append(redisCache.hostname())
//                .append("\n\tSSL port: ").append(redisCache.sslPort())
//                .append("\n\tNon-SSL port (6379) enabled: ").append(redisCache.nonSslPort());
//        if (redisCache.redisConfiguration() != null && !redisCache.redisConfiguration().isEmpty()) {
//            redisInfo.append("\n\tRedis Configuration:");
//            for (Map.Entry<String, String> redisConfiguration : redisCache.redisConfiguration().entrySet()) {
//                redisInfo.append("\n\t  '").append(redisConfiguration.getKey())
//                        .append("' : '").append(redisConfiguration.getValue()).append("'");
//            }
//        }
//        if (redisCache.isPremium()) {
//            RedisCachePremium premium = redisCache.asPremium();
//            List<ScheduleEntry> scheduleEntries = premium.listPatchSchedules();
//            if (scheduleEntries != null && !scheduleEntries.isEmpty()) {
//                redisInfo.append("\n\tRedis Patch Schedule:");
//                for (ScheduleEntry schedule : scheduleEntries) {
//                    redisInfo.append("\n\t\tDay: '").append(schedule.dayOfWeek())
//                            .append("', start at: '").append(schedule.startHourUtc())
//                            .append("', maintenance window: '").append(schedule.maintenanceWindow())
//                            .append("'");
//                }
//            }
//        }
//
//        System.out.println(redisInfo.toString());
//    }

//    /**
//     * Print Redis Cache access keys.
//     *
//     * @param redisAccessKeys a keys for Redis Cache
//     */
//    public static void print(RedisAccessKeys redisAccessKeys) {
//        StringBuilder redisKeys = new StringBuilder()
//                .append("Redis Access Keys: ")
//                .append("\n\tPrimary Key: '").append(redisAccessKeys.primaryKey()).append("', ")
//                .append("\n\tSecondary Key: '").append(redisAccessKeys.secondaryKey()).append("', ");
//
//        System.out.println(redisKeys.toString());
//    }

//    /**
//     * Print management lock.
//     *
//     * @param lock a management lock
//     */
//    public static void print(ManagementLock lock) {
//        StringBuilder info = new StringBuilder();
//        info.append("\nLock ID: ").append(lock.id())
//                .append("\nLocked resource ID: ").append(lock.lockedResourceId())
//                .append("\nLevel: ").append(lock.level());
//        System.out.println(info.toString());
//    }

    /**
     * Print load balancer.
     *
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

        // Show HTTPS probes
        info.append("\n\tHTTPS probes: ")
                .append(resource.httpsProbes().size());
        for (LoadBalancerHttpProbe probe : resource.httpsProbes().values()) {
            info.append("\n\t\tProbe name: ").append(probe.name())
                    .append("\n\t\t\tPort: ").append(probe.port())
                    .append("\n\t\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                    .append("\n\t\t\tRetries before unhealthy: ").append(probe.numberOfProbes())
                    .append("\n\t\t\tHTTPS request path: ").append(probe.requestPath());

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
                    .append("\n\t\t\tFloating IP enabled? ").append(rule.floatingIPEnabled())
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
                    .append("\n\t\t\tFloating IP? ").append(natRule.floatingIPEnabled())
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
                    .append(backend.backendNicIPConfigurationNames().entrySet().size());
            for (Map.Entry<String, String> entry : backend.backendNicIPConfigurationNames().entrySet()) {
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
//
//    /**
//     * Prints batch account keys.
//     *
//     * @param batchAccountKeys a list of batch account keys
//     */
//    public static void print(BatchAccountKeys batchAccountKeys) {
//        System.out.println("Primary Key (" + batchAccountKeys.primary() + ") Secondary key = ("
//                + batchAccountKeys.secondary() + ")");
//    }

//    /**
//     * Prints batch account.
//     *
//     * @param batchAccount a Batch Account
//     */
//    public static void print(BatchAccount batchAccount) {
//        StringBuilder applicationsOutput = new StringBuilder().append("\n\tapplications: ");
//
//        if (batchAccount.applications().size() > 0) {
//            for (Map.Entry<String, Application> applicationEntry : batchAccount.applications().entrySet()) {
//                Application application = applicationEntry.getValue();
//                StringBuilder applicationPackages = new StringBuilder().append("\n\t\t\tapplicationPackages : ");
//
//                for (Map.Entry<String, ApplicationPackage> applicationPackageEntry : application.applicationPackages().entrySet()) {
//                    ApplicationPackage applicationPackage = applicationPackageEntry.getValue();
//                    StringBuilder singleApplicationPackage = new StringBuilder().append("\n\t\t\t\tapplicationPackage : " + applicationPackage.name());
//                    singleApplicationPackage.append("\n\t\t\t\tapplicationPackageState : " + applicationPackage.state());
//
//                    applicationPackages.append(singleApplicationPackage);
//                    singleApplicationPackage.append("\n");
//                }
//
//                StringBuilder singleApplication = new StringBuilder().append("\n\t\tapplication: " + application.name());
//                singleApplication.append("\n\t\tdisplayName: " + application.displayName());
//                singleApplication.append("\n\t\tdefaultVersion: " + application.defaultVersion());
//                singleApplication.append(applicationPackages);
//                applicationsOutput.append(singleApplication);
//                applicationsOutput.append("\n");
//            }
//        }
//
//        System.out.println(new StringBuilder().append("BatchAccount: ").append(batchAccount.id())
//                .append("Name: ").append(batchAccount.name())
//                .append("\n\tResource group: ").append(batchAccount.resourceGroupName())
//                .append("\n\tRegion: ").append(batchAccount.region())
//                .append("\n\tTags: ").append(batchAccount.tags())
//                .append("\n\tAccountEndpoint: ").append(batchAccount.accountEndpoint())
//                .append("\n\tPoolQuota: ").append(batchAccount.poolQuota())
//                .append("\n\tActiveJobAndJobScheduleQuota: ").append(batchAccount.activeJobAndJobScheduleQuota())
//                .append("\n\tStorageAccount: ").append(batchAccount.autoStorage() == null ? "No storage account attached" : batchAccount.autoStorage().storageAccountId())
//                .append(applicationsOutput)
//                .toString());
//    }

    /**
     * Print app service domain.
     *
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
     *
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
     *
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
     *
     * @param resource a web app
     */
    public static void print(WebAppBase resource) {
        StringBuilder builder = new StringBuilder().append("Web app: ").append(resource.id())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tState: ").append(resource.state())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tDefault hostname: ").append(resource.defaultHostname())
                .append("\n\tApp service plan: ").append(resource.appServicePlanId())
                .append("\n\tHost name bindings: ");
        for (HostnameBinding binding : resource.getHostnameBindings().values()) {
            builder = builder.append("\n\t\t" + binding.toString());
        }
        builder = builder.append("\n\tSSL bindings: ");
        for (HostnameSslState binding : resource.hostnameSslStates().values()) {
            builder = builder.append("\n\t\t" + binding.name() + ": " + binding.sslState());
            if (binding.sslState() != null && binding.sslState() != SslState.DISABLED) {
                builder = builder.append(" - " + binding.thumbprint());
            }
        }
        builder = builder.append("\n\tApp settings: ");
        for (AppSetting setting : resource.getAppSettings().values()) {
            builder = builder.append("\n\t\t" + setting.key() + ": " + setting.value() + (setting.sticky() ? " - slot setting" : ""));
        }
        builder = builder.append("\n\tConnection strings: ");
        for (ConnectionString conn : resource.getConnectionStrings().values()) {
            builder = builder.append("\n\t\t" + conn.name() + ": " + conn.value() + " - " + conn.type() + (conn.sticky() ? " - slot setting" : ""));
        }
        System.out.println(builder.toString());
    }

    /**
     * Print a web site.
     *
     * @param resource a web site
     */
    public static void print(WebSiteBase resource) {
        StringBuilder builder = new StringBuilder().append("Web app: ").append(resource.id())
            .append("\n\tName: ").append(resource.name())
            .append("\n\tState: ").append(resource.state())
            .append("\n\tResource group: ").append(resource.resourceGroupName())
            .append("\n\tRegion: ").append(resource.region())
            .append("\n\tDefault hostname: ").append(resource.defaultHostname())
            .append("\n\tApp service plan: ").append(resource.appServicePlanId());
        builder = builder.append("\n\tSSL bindings: ");
        for (HostnameSslState binding : resource.hostnameSslStates().values()) {
            builder = builder.append("\n\t\t" + binding.name() + ": " + binding.sslState());
            if (binding.sslState() != null && binding.sslState() != SslState.DISABLED) {
                builder = builder.append(" - " + binding.thumbprint());
            }
        }
        System.out.println(builder.toString());
    }

//    /**
//     * Print a traffic manager profile.
//     *
//     * @param profile a traffic manager profile
//     */
//    public static void print(TrafficManagerProfile profile) {
//        StringBuilder info = new StringBuilder();
//        info.append("Traffic Manager Profile: ").append(profile.id())
//                .append("\n\tName: ").append(profile.name())
//                .append("\n\tResource group: ").append(profile.resourceGroupName())
//                .append("\n\tRegion: ").append(profile.regionName())
//                .append("\n\tTags: ").append(profile.tags())
//                .append("\n\tDNSLabel: ").append(profile.dnsLabel())
//                .append("\n\tFQDN: ").append(profile.fqdn())
//                .append("\n\tTTL: ").append(profile.timeToLive())
//                .append("\n\tEnabled: ").append(profile.isEnabled())
//                .append("\n\tRoutingMethod: ").append(profile.trafficRoutingMethod())
//                .append("\n\tMonitor status: ").append(profile.monitorStatus())
//                .append("\n\tMonitoring port: ").append(profile.monitoringPort())
//                .append("\n\tMonitoring path: ").append(profile.monitoringPath());
//
//        Map<String, TrafficManagerAzureEndpoint> azureEndpoints = profile.azureEndpoints();
//        if (!azureEndpoints.isEmpty()) {
//            info.append("\n\tAzure endpoints:");
//            int idx = 1;
//            for (TrafficManagerAzureEndpoint endpoint : azureEndpoints.values()) {
//                info.append("\n\t\tAzure endpoint: #").append(idx++)
//                        .append("\n\t\t\tId: ").append(endpoint.id())
//                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
//                        .append("\n\t\t\tTarget resourceId: ").append(endpoint.targetAzureResourceId())
//                        .append("\n\t\t\tTarget resourceType: ").append(endpoint.targetResourceType())
//                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
//                        .append("\n\t\t\tEnabled: ").append(endpoint.isEnabled())
//                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
//                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
//            }
//        }
//
//        Map<String, TrafficManagerExternalEndpoint> externalEndpoints = profile.externalEndpoints();
//        if (!externalEndpoints.isEmpty()) {
//            info.append("\n\tExternal endpoints:");
//            int idx = 1;
//            for (TrafficManagerExternalEndpoint endpoint : externalEndpoints.values()) {
//                info.append("\n\t\tExternal endpoint: #").append(idx++)
//                        .append("\n\t\t\tId: ").append(endpoint.id())
//                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
//                        .append("\n\t\t\tFQDN: ").append(endpoint.fqdn())
//                        .append("\n\t\t\tSource Traffic Location: ").append(endpoint.sourceTrafficLocation())
//                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
//                        .append("\n\t\t\tEnabled: ").append(endpoint.isEnabled())
//                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
//                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
//            }
//        }
//
//        Map<String, TrafficManagerNestedProfileEndpoint> nestedProfileEndpoints = profile.nestedProfileEndpoints();
//        if (!nestedProfileEndpoints.isEmpty()) {
//            info.append("\n\tNested profile endpoints:");
//            int idx = 1;
//            for (TrafficManagerNestedProfileEndpoint endpoint : nestedProfileEndpoints.values()) {
//                info.append("\n\t\tNested profile endpoint: #").append(idx++)
//                        .append("\n\t\t\tId: ").append(endpoint.id())
//                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
//                        .append("\n\t\t\tNested profileId: ").append(endpoint.nestedProfileId())
//                        .append("\n\t\t\tMinimum child threshold: ").append(endpoint.minimumChildEndpointCount())
//                        .append("\n\t\t\tSource Traffic Location: ").append(endpoint.sourceTrafficLocation())
//                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
//                        .append("\n\t\t\tEnabled: ").append(endpoint.isEnabled())
//                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
//                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
//            }
//        }
//        System.out.println(info.toString());
//    }

    /**
     * Print a dns zone.
     *
     * @param dnsZone a dns zone
     */
    public static void print(DnsZone dnsZone) {
        StringBuilder info = new StringBuilder();
        info.append("DNS Zone: ").append(dnsZone.id())
                .append("\n\tName (Top level domain): ").append(dnsZone.name())
                .append("\n\tResource group: ").append(dnsZone.resourceGroupName())
                .append("\n\tRegion: ").append(dnsZone.regionName())
                .append("\n\tTags: ").append(dnsZone.tags())
                .append("\n\tName servers:");
        for (String nameServer : dnsZone.nameServers()) {
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
                .append("\n\t\tTTL (seconds):").append(soaRecordSet.timeToLive());

        PagedIterable<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        info.append("\n\tA Record sets:");
        for (ARecordSet aRecordSet : aRecordSets) {
            info.append("\n\t\tId: ").append(aRecordSet.id())
                    .append("\n\t\tName: ").append(aRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(aRecordSet.timeToLive())
                    .append("\n\t\tIP v4 addresses: ");
            for (String ipAddress : aRecordSet.ipv4Addresses()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }

        PagedIterable<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        info.append("\n\tAAAA Record sets:");
        for (AaaaRecordSet aaaaRecordSet : aaaaRecordSets) {
            info.append("\n\t\tId: ").append(aaaaRecordSet.id())
                    .append("\n\t\tName: ").append(aaaaRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(aaaaRecordSet.timeToLive())
                    .append("\n\t\tIP v6 addresses: ");
            for (String ipAddress : aaaaRecordSet.ipv6Addresses()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }

        PagedIterable<CnameRecordSet> cnameRecordSets = dnsZone.cNameRecordSets().list();
        info.append("\n\tCNAME Record sets:");
        for (CnameRecordSet cnameRecordSet : cnameRecordSets) {
            info.append("\n\t\tId: ").append(cnameRecordSet.id())
                    .append("\n\t\tName: ").append(cnameRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(cnameRecordSet.timeToLive())
                    .append("\n\t\tCanonical name: ").append(cnameRecordSet.canonicalName());
        }

        PagedIterable<MxRecordSet> mxRecordSets = dnsZone.mxRecordSets().list();
        info.append("\n\tMX Record sets:");
        for (MxRecordSet mxRecordSet : mxRecordSets) {
            info.append("\n\t\tId: ").append(mxRecordSet.id())
                    .append("\n\t\tName: ").append(mxRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(mxRecordSet.timeToLive())
                    .append("\n\t\tRecords: ");
            for (MxRecord mxRecord : mxRecordSet.records()) {
                info.append("\n\t\t\tExchange server, Preference: ")
                        .append(mxRecord.exchange())
                        .append(" ")
                        .append(mxRecord.preference());
            }
        }

        PagedIterable<NsRecordSet> nsRecordSets = dnsZone.nsRecordSets().list();
        info.append("\n\tNS Record sets:");
        for (NsRecordSet nsRecordSet : nsRecordSets) {
            info.append("\n\t\tId: ").append(nsRecordSet.id())
                    .append("\n\t\tName: ").append(nsRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(nsRecordSet.timeToLive())
                    .append("\n\t\tName servers: ");
            for (String nameServer : nsRecordSet.nameServers()) {
                info.append("\n\t\t\t").append(nameServer);
            }
        }

        PagedIterable<PtrRecordSet> ptrRecordSets = dnsZone.ptrRecordSets().list();
        info.append("\n\tPTR Record sets:");
        for (PtrRecordSet ptrRecordSet : ptrRecordSets) {
            info.append("\n\t\tId: ").append(ptrRecordSet.id())
                    .append("\n\t\tName: ").append(ptrRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(ptrRecordSet.timeToLive())
                    .append("\n\t\tTarget domain names: ");
            for (String domainNames : ptrRecordSet.targetDomainNames()) {
                info.append("\n\t\t\t").append(domainNames);
            }
        }

        PagedIterable<SrvRecordSet> srvRecordSets = dnsZone.srvRecordSets().list();
        info.append("\n\tSRV Record sets:");
        for (SrvRecordSet srvRecordSet : srvRecordSets) {
            info.append("\n\t\tId: ").append(srvRecordSet.id())
                    .append("\n\t\tName: ").append(srvRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(srvRecordSet.timeToLive())
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

        PagedIterable<TxtRecordSet> txtRecordSets = dnsZone.txtRecordSets().list();
        info.append("\n\tTXT Record sets:");
        for (TxtRecordSet txtRecordSet : txtRecordSets) {
            info.append("\n\t\tId: ").append(txtRecordSet.id())
                    .append("\n\t\tName: ").append(txtRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(txtRecordSet.timeToLive())
                    .append("\n\t\tRecords: ");
            for (TxtRecord txtRecord : txtRecordSet.records()) {
                if (txtRecord.value().size() > 0) {
                    info.append("\n\t\t\tValue: ").append(txtRecord.value().get(0));
                }
            }
        }
        System.out.println(info.toString());
    }

//    /**
//     * Print a private dns zone.
//     *
//     * @param privateDnsZone a private dns zone
//     */
//    public static void print(PrivateDnsZone privateDnsZone) {
//        StringBuilder info = new StringBuilder();
//        info.append("Private DNS Zone: ").append(privateDnsZone.id())
//            .append("\n\tName (Top level domain): ").append(privateDnsZone.name())
//            .append("\n\tResource group: ").append(privateDnsZone.resourceGroupName())
//            .append("\n\tRegion: ").append(privateDnsZone.regionName())
//            .append("\n\tTags: ").append(privateDnsZone.tags())
//            .append("\n\tName servers:");
//        com.azure.resourcemanager.privatedns.models.SoaRecordSet soaRecordSet = privateDnsZone.getSoaRecordSet();
//        com.azure.resourcemanager.privatedns.models.SoaRecord soaRecord = soaRecordSet.record();
//        info.append("\n\tSOA Record:")
//            .append("\n\t\tHost:").append(soaRecord.host())
//            .append("\n\t\tEmail:").append(soaRecord.email())
//            .append("\n\t\tExpire time (seconds):").append(soaRecord.expireTime())
//            .append("\n\t\tRefresh time (seconds):").append(soaRecord.refreshTime())
//            .append("\n\t\tRetry time (seconds):").append(soaRecord.retryTime())
//            .append("\n\t\tNegative response cache ttl (seconds):").append(soaRecord.minimumTtl())
//            .append("\n\t\tTTL (seconds):").append(soaRecordSet.timeToLive());
//
//        PagedIterable<com.azure.resourcemanager.privatedns.models.ARecordSet> aRecordSets = privateDnsZone
//            .aRecordSets().list();
//        info.append("\n\tA Record sets:");
//        for (com.azure.resourcemanager.privatedns.models.ARecordSet aRecordSet : aRecordSets) {
//            info.append("\n\t\tId: ").append(aRecordSet.id())
//                .append("\n\t\tName: ").append(aRecordSet.name())
//                .append("\n\t\tTTL (seconds): ").append(aRecordSet.timeToLive())
//                .append("\n\t\tIP v4 addresses: ");
//            for (String ipAddress : aRecordSet.ipv4Addresses()) {
//                info.append("\n\t\t\t").append(ipAddress);
//            }
//        }
//
//        PagedIterable<com.azure.resourcemanager.privatedns.models.AaaaRecordSet> aaaaRecordSets = privateDnsZone
//            .aaaaRecordSets().list();
//        info.append("\n\tAAAA Record sets:");
//        for (com.azure.resourcemanager.privatedns.models.AaaaRecordSet aaaaRecordSet : aaaaRecordSets) {
//            info.append("\n\t\tId: ").append(aaaaRecordSet.id())
//                .append("\n\t\tName: ").append(aaaaRecordSet.name())
//                .append("\n\t\tTTL (seconds): ").append(aaaaRecordSet.timeToLive())
//                .append("\n\t\tIP v6 addresses: ");
//            for (String ipAddress : aaaaRecordSet.ipv6Addresses()) {
//                info.append("\n\t\t\t").append(ipAddress);
//            }
//        }
//
//        PagedIterable<com.azure.resourcemanager.privatedns.models.CnameRecordSet> cnameRecordSets = privateDnsZone.cnameRecordSets().list();
//        info.append("\n\tCNAME Record sets:");
//        for (com.azure.resourcemanager.privatedns.models.CnameRecordSet cnameRecordSet : cnameRecordSets) {
//            info.append("\n\t\tId: ").append(cnameRecordSet.id())
//                .append("\n\t\tName: ").append(cnameRecordSet.name())
//                .append("\n\t\tTTL (seconds): ").append(cnameRecordSet.timeToLive())
//                .append("\n\t\tCanonical name: ").append(cnameRecordSet.canonicalName());
//        }
//
//        PagedIterable<com.azure.resourcemanager.privatedns.models.MxRecordSet> mxRecordSets = privateDnsZone.mxRecordSets().list();
//        info.append("\n\tMX Record sets:");
//        for (com.azure.resourcemanager.privatedns.models.MxRecordSet mxRecordSet : mxRecordSets) {
//            info.append("\n\t\tId: ").append(mxRecordSet.id())
//                .append("\n\t\tName: ").append(mxRecordSet.name())
//                .append("\n\t\tTTL (seconds): ").append(mxRecordSet.timeToLive())
//                .append("\n\t\tRecords: ");
//            for (com.azure.resourcemanager.privatedns.models.MxRecord mxRecord : mxRecordSet.records()) {
//                info.append("\n\t\t\tExchange server, Preference: ")
//                    .append(mxRecord.exchange())
//                    .append(" ")
//                    .append(mxRecord.preference());
//            }
//        }
//
//        PagedIterable<com.azure.resourcemanager.privatedns.models.PtrRecordSet> ptrRecordSets = privateDnsZone
//            .ptrRecordSets().list();
//        info.append("\n\tPTR Record sets:");
//        for (com.azure.resourcemanager.privatedns.models.PtrRecordSet ptrRecordSet : ptrRecordSets) {
//            info.append("\n\t\tId: ").append(ptrRecordSet.id())
//                .append("\n\t\tName: ").append(ptrRecordSet.name())
//                .append("\n\t\tTTL (seconds): ").append(ptrRecordSet.timeToLive())
//                .append("\n\t\tTarget domain names: ");
//            for (String domainNames : ptrRecordSet.targetDomainNames()) {
//                info.append("\n\t\t\t").append(domainNames);
//            }
//        }
//
//        PagedIterable<com.azure.resourcemanager.privatedns.models.SrvRecordSet> srvRecordSets = privateDnsZone
//            .srvRecordSets().list();
//        info.append("\n\tSRV Record sets:");
//        for (com.azure.resourcemanager.privatedns.models.SrvRecordSet srvRecordSet : srvRecordSets) {
//            info.append("\n\t\tId: ").append(srvRecordSet.id())
//                .append("\n\t\tName: ").append(srvRecordSet.name())
//                .append("\n\t\tTTL (seconds): ").append(srvRecordSet.timeToLive())
//                .append("\n\t\tRecords: ");
//            for (com.azure.resourcemanager.privatedns.models.SrvRecord srvRecord : srvRecordSet.records()) {
//                info.append("\n\t\t\tTarget, Port, Priority, Weight: ")
//                    .append(srvRecord.target())
//                    .append(", ")
//                    .append(srvRecord.port())
//                    .append(", ")
//                    .append(srvRecord.priority())
//                    .append(", ")
//                    .append(srvRecord.weight());
//            }
//        }
//
//        PagedIterable<com.azure.resourcemanager.privatedns.models.TxtRecordSet> txtRecordSets = privateDnsZone
//            .txtRecordSets().list();
//        info.append("\n\tTXT Record sets:");
//        for (com.azure.resourcemanager.privatedns.models.TxtRecordSet txtRecordSet : txtRecordSets) {
//            info.append("\n\t\tId: ").append(txtRecordSet.id())
//                .append("\n\t\tName: ").append(txtRecordSet.name())
//                .append("\n\t\tTTL (seconds): ").append(txtRecordSet.timeToLive())
//                .append("\n\t\tRecords: ");
//            for (com.azure.resourcemanager.privatedns.models.TxtRecord txtRecord : txtRecordSet.records()) {
//                if (txtRecord.value().size() > 0) {
//                    info.append("\n\t\t\tValue: ").append(txtRecord.value().get(0));
//                }
//            }
//        }
//
//        PagedIterable<VirtualNetworkLink> virtualNetworkLinks = privateDnsZone.virtualNetworkLinks().list();
//        info.append("\n\tVirtual Network Links:");
//        for (VirtualNetworkLink virtualNetworkLink : virtualNetworkLinks) {
//            info.append("\n\tId: ").append(virtualNetworkLink.id())
//                .append("\n\tName: ").append(virtualNetworkLink.name())
//                .append("\n\tReference of Virtual Network: ").append(virtualNetworkLink.referencedVirtualNetworkId())
//                .append("\n\tRegistration enabled: ").append(virtualNetworkLink.isAutoRegistrationEnabled());
//        }
//        System.out.println(info.toString());
//    }
//
//    /**
//     * Print an Azure Container Registry.
//     *
//     * @param azureRegistry an Azure Container Registry
//     */
//    public static void print(Registry azureRegistry) {
//        StringBuilder info = new StringBuilder();
//
//        RegistryCredentials acrCredentials = azureRegistry.getCredentials();
//        info.append("Azure Container Registry: ").append(azureRegistry.id())
//                .append("\n\tName: ").append(azureRegistry.name())
//                .append("\n\tServer Url: ").append(azureRegistry.loginServerUrl())
//                .append("\n\tUser: ").append(acrCredentials.username())
//                .append("\n\tFirst Password: ").append(acrCredentials.accessKeys().get(AccessKeyType.PRIMARY))
//                .append("\n\tSecond Password: ").append(acrCredentials.accessKeys().get(AccessKeyType.SECONDARY));
//        System.out.println(info.toString());
//    }

    /**
     * Print an Azure Container Service (AKS).
     *
     * @param kubernetesCluster a managed container service
     */
    public static void print(KubernetesCluster kubernetesCluster) {
        StringBuilder info = new StringBuilder();

        info.append("Azure Container Service: ").append(kubernetesCluster.id())
                .append("\n\tName: ").append(kubernetesCluster.name())
                .append("\n\tFQDN: ").append(kubernetesCluster.fqdn())
                .append("\n\tDNS prefix label: ").append(kubernetesCluster.dnsPrefix())
                .append("\n\t\tWith Agent pool name: ").append(new ArrayList<>(kubernetesCluster.agentPools().keySet()).get(0))
                .append("\n\t\tAgent pool count: ").append(new ArrayList<>(kubernetesCluster.agentPools().values()).get(0).count())
                .append("\n\t\tAgent pool VM size: ").append(new ArrayList<>(kubernetesCluster.agentPools().values()).get(0).vmSize().toString())
                .append("\n\tLinux user name: ").append(kubernetesCluster.linuxRootUsername())
                .append("\n\tSSH key: ").append(kubernetesCluster.sshKey())
                .append("\n\tService principal client ID: ").append(kubernetesCluster.servicePrincipalClientId());

        System.out.println(info.toString());
    }

//    /**
//     * Print an Azure Search Service.
//     *
//     * @param searchService an Azure Search Service
//     */
//    public static void print(SearchService searchService) {
//        StringBuilder info = new StringBuilder();
//        AdminKeys adminKeys = searchService.getAdminKeys();
//        List<QueryKey> queryKeys = searchService.listQueryKeys();
//
//        info.append("Azure Search: ").append(searchService.id())
//                .append("\n\tResource group: ").append(searchService.resourceGroupName())
//                .append("\n\tRegion: ").append(searchService.region())
//                .append("\n\tTags: ").append(searchService.tags())
//                .append("\n\tSku: ").append(searchService.sku().name())
//                .append("\n\tStatus: ").append(searchService.status())
//                .append("\n\tProvisioning State: ").append(searchService.provisioningState())
//                .append("\n\tHosting Mode: ").append(searchService.hostingMode())
//                .append("\n\tReplicas: ").append(searchService.replicaCount())
//                .append("\n\tPartitions: ").append(searchService.partitionCount())
//                .append("\n\tPrimary Admin Key: ").append(adminKeys.primaryKey())
//                .append("\n\tSecondary Admin Key: ").append(adminKeys.secondaryKey())
//                .append("\n\tQuery keys:");
//
//        for (QueryKey queryKey : queryKeys) {
//            info.append("\n\t\tKey name: ").append(queryKey.name());
//            info.append("\n\t\t   Value: ").append(queryKey.key());
//        }
//        System.out.println(info.toString());
//    }

    /**
     * Retrieve the secondary service principal client ID.
     *
     * @param envSecondaryServicePrincipal an Azure Container Registry
     * @return a service principal client ID
     * @throws Exception exception
     */
    public static String getSecondaryServicePrincipalClientID(String envSecondaryServicePrincipal) throws IOException {
        String content = new String(Files.readAllBytes(new File(envSecondaryServicePrincipal).toPath()), StandardCharsets.UTF_8).trim();
        HashMap<String, String> auth = new HashMap<>();

        if (content.startsWith("{")) {
            auth = new JacksonAdapter().deserialize(content, auth.getClass(), SerializerEncoding.JSON);
            return auth.get("clientId");
        } else {
            Properties authSettings = new Properties();
            try (FileInputStream credentialsFileStream = new FileInputStream(new File(envSecondaryServicePrincipal))) {
                authSettings.load(credentialsFileStream);
            }
            return authSettings.getProperty("client");
        }
    }

    /**
     * Retrieve the secondary service principal secret.
     *
     * @param envSecondaryServicePrincipal an Azure Container Registry
     * @return a service principal secret
     * @throws Exception exception
     */
    public static String getSecondaryServicePrincipalSecret(String envSecondaryServicePrincipal) throws IOException {
        String content = new String(Files.readAllBytes(new File(envSecondaryServicePrincipal).toPath()), StandardCharsets.UTF_8).trim();
        HashMap<String, String> auth = new HashMap<>();

        if (content.startsWith("{")) {
            auth = new JacksonAdapter().deserialize(content, auth.getClass(), SerializerEncoding.JSON);
            return auth.get("clientSecret");
        } else {
            Properties authSettings = new Properties();
            try (FileInputStream credentialsFileStream = new FileInputStream(new File(envSecondaryServicePrincipal))) {
                authSettings.load(credentialsFileStream);
            }
            return authSettings.getProperty("key");
        }
    }
//
//    /**
//     * Creates and returns a randomized name based on the prefix file for use by the sample.
//     *
//     * @param namePrefix The prefix string to be used in generating the name.
//     * @return a random name
//     */
//    public static String createRandomName(String namePrefix) {
//        return ResourceManagerUtils.InternalRuntimeContext.randomResourceName(namePrefix, 30);
//    }

    /**
     * This method creates a certificate for given password.
     *
     * @param certPath location of certificate file
     * @param pfxPath location of pfx file
     * @param alias User alias
     * @param password alias password
     * @param cnName domain name
     * @throws Exception exceptions from the creation
     * @throws IOException IO Exception
     */
    public static void createCertificate(String certPath, String pfxPath,
                                         String alias, String password, String cnName) throws IOException {
        if (new File(pfxPath).exists()) {
            return;
        }
        String validityInDays = "3650";
        String keyAlg = "RSA";
        String sigAlg = "SHA1withRSA";
        String keySize = "2048";
        String storeType = "pkcs12";
        String command = "keytool";
        String jdkPath = System.getProperty("java.home");
        if (jdkPath != null && !jdkPath.isEmpty()) {
            jdkPath = jdkPath.concat("\\bin");
            if (new File(jdkPath).isDirectory()) {
                command = String.format("%s%s%s", jdkPath, File.separator, command);
            }
        } else {
            return;
        }

        // Create Pfx file
        String[] commandArgs = {command, "-genkey", "-alias", alias,
            "-keystore", pfxPath, "-storepass", password, "-validity",
            validityInDays, "-keyalg", keyAlg, "-sigalg", sigAlg, "-keysize", keySize,
            "-storetype", storeType, "-dname", "CN=" + cnName, "-ext", "EKU=1.3.6.1.5.5.7.3.1"};
        Utils.cmdInvocation(commandArgs, true);

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
                                + String.join(" ", certCommandArgs));
            }
        } else {
            throw new IOException("Error occurred while creating certificates"
                    + String.join(" ", commandArgs));
        }
    }

    /**
     * This method is used for invoking native commands.
     *
     * @param command :- command to invoke.
     * @param ignoreErrorStream : Boolean which controls whether to throw exception or not
     *                          based on error stream.
     * @return result :- depending on the method invocation.
     * @throws Exception exceptions thrown from the execution
     */
    public static String cmdInvocation(String[] command,
                                       boolean ignoreErrorStream) throws IOException {
        String result = "";
        String error = "";

        Process process = new ProcessBuilder(command).start();
        try (
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            BufferedReader ebr = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
        ) {
            result = br.readLine();
            process.waitFor();
            error = ebr.readLine();
            if (error != null && (!error.equals(""))) {
                // To do - Log error message

                if (!ignoreErrorStream) {
                    throw new IOException(error, null);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while invoking command", e);
        }
        return result;
    }


//    /**
//     * Prints information for passed SQL Server.
//     *
//     * @param sqlServer sqlServer to be printed
//     */
//    public static void print(SqlServer sqlServer) {
//        StringBuilder builder = new StringBuilder().append("Sql Server: ").append(sqlServer.id())
//                .append("Name: ").append(sqlServer.name())
//                .append("\n\tResource group: ").append(sqlServer.resourceGroupName())
//                .append("\n\tRegion: ").append(sqlServer.region())
//                .append("\n\tSqlServer version: ").append(sqlServer.version())
//                .append("\n\tFully qualified name for Sql Server: ").append(sqlServer.fullyQualifiedDomainName());
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information for the passed SQL Database.
//     *
//     * @param database database to be printed
//     */
//    public static void print(SqlDatabase database) {
//        StringBuilder builder = new StringBuilder().append("Sql Database: ").append(database.id())
//                .append("Name: ").append(database.name())
//                .append("\n\tResource group: ").append(database.resourceGroupName())
//                .append("\n\tRegion: ").append(database.region())
//                .append("\n\tSqlServer Name: ").append(database.sqlServerName())
//                .append("\n\tEdition of SQL database: ").append(database.edition())
//                .append("\n\tCollation of SQL database: ").append(database.collation())
//                .append("\n\tCreation date of SQL database: ").append(database.creationDate())
//                .append("\n\tIs data warehouse: ").append(database.isDataWarehouse())
//                .append("\n\tRequested service objective of SQL database: ").append(database.requestedServiceObjectiveName())
//                .append("\n\tName of current service objective of SQL database: ").append(database.currentServiceObjectiveName())
//                .append("\n\tMax size bytes of SQL database: ").append(database.maxSizeBytes())
//                .append("\n\tDefault secondary location of SQL database: ").append(database.defaultSecondaryLocation());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information for the passed firewall rule.
//     *
//     * @param firewallRule firewall rule to be printed.
//     */
//    public static void print(SqlFirewallRule firewallRule) {
//        StringBuilder builder = new StringBuilder().append("Sql firewall rule: ").append(firewallRule.id())
//                .append("Name: ").append(firewallRule.name())
//                .append("\n\tResource group: ").append(firewallRule.resourceGroupName())
//                .append("\n\tRegion: ").append(firewallRule.region())
//                .append("\n\tSqlServer Name: ").append(firewallRule.sqlServerName())
//                .append("\n\tStart IP Address of the firewall rule: ").append(firewallRule.startIpAddress())
//                .append("\n\tEnd IP Address of the firewall rule: ").append(firewallRule.endIpAddress());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information for the passed virtual network rule.
//     *
//     * @param virtualNetworkRule virtual network rule to be printed.
//     */
//    public static void print(SqlVirtualNetworkRule virtualNetworkRule) {
//        StringBuilder builder = new StringBuilder().append("SQL virtual network rule: ").append(virtualNetworkRule.id())
//                .append("Name: ").append(virtualNetworkRule.name())
//                .append("\n\tResource group: ").append(virtualNetworkRule.resourceGroupName())
//                .append("\n\tSqlServer Name: ").append(virtualNetworkRule.sqlServerName())
//                .append("\n\tSubnet ID: ").append(virtualNetworkRule.subnetId())
//                .append("\n\tState: ").append(virtualNetworkRule.state());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information for the passed SQL subscription usage metric.
//     *
//     * @param subscriptionUsageMetric metric to be printed.
//     */
//    public static void print(SqlSubscriptionUsageMetric subscriptionUsageMetric) {
//        StringBuilder builder = new StringBuilder().append("SQL Subscription Usage Metric: ").append(subscriptionUsageMetric.id())
//                .append("Name: ").append(subscriptionUsageMetric.name())
//                .append("\n\tDisplay Name: ").append(subscriptionUsageMetric.displayName())
//                .append("\n\tCurrent Value: ").append(subscriptionUsageMetric.currentValue())
//                .append("\n\tLimit: ").append(subscriptionUsageMetric.limit())
//                .append("\n\tUnit: ").append(subscriptionUsageMetric.unit())
//                .append("\n\tType: ").append(subscriptionUsageMetric.type());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information for the passed SQL database usage metric.
//     *
//     * @param dbUsageMetric metric to be printed.
//     */
//    public static void print(SqlDatabaseUsageMetric dbUsageMetric) {
//        StringBuilder builder = new StringBuilder().append("SQL Database Usage Metric")
//                .append("Name: ").append(dbUsageMetric.name())
//                .append("\n\tResource Name: ").append(dbUsageMetric.resourceName())
//                .append("\n\tDisplay Name: ").append(dbUsageMetric.displayName())
//                .append("\n\tCurrent Value: ").append(dbUsageMetric.currentValue())
//                .append("\n\tLimit: ").append(dbUsageMetric.limit())
//                .append("\n\tUnit: ").append(dbUsageMetric.unit())
//                .append("\n\tNext Reset Time: ").append(dbUsageMetric.nextResetTime());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information for the passed SQL database metric.
//     *
//     * @param dbMetric metric to be printed.
//     */
//    public static void print(SqlDatabaseMetric dbMetric) {
//        StringBuilder builder = new StringBuilder().append("SQL Database Metric")
//                .append("Name: ").append(dbMetric.name())
//                .append("\n\tStart Time: ").append(dbMetric.startTime())
//                .append("\n\tEnd Time: ").append(dbMetric.endTime())
//                .append("\n\tTime Grain: ").append(dbMetric.timeGrain())
//                .append("\n\tUnit: ").append(dbMetric.unit());
//        for (SqlDatabaseMetricValue metricValue : dbMetric.metricValues()) {
//            builder
//                    .append("\n\tMetric Value: ")
//                    .append("\n\t\tCount: ").append(metricValue.count())
//                    .append("\n\t\tAverage: ").append(metricValue.average())
//                    .append("\n\t\tMaximum: ").append(metricValue.maximum())
//                    .append("\n\t\tMinimum: ").append(metricValue.minimum())
//                    .append("\n\t\tTimestamp: ").append(metricValue.timestamp())
//                    .append("\n\t\tTotal: ").append(metricValue.total());
//        }
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information for the passed Failover Group.
//     *
//     * @param failoverGroup the SQL Failover Group to be printed.
//     */
//    public static void print(SqlFailoverGroup failoverGroup) {
//        StringBuilder builder = new StringBuilder().append("SQL Failover Group: ").append(failoverGroup.id())
//                .append("Name: ").append(failoverGroup.name())
//                .append("\n\tResource group: ").append(failoverGroup.resourceGroupName())
//                .append("\n\tSqlServer Name: ").append(failoverGroup.sqlServerName())
//                .append("\n\tRead-write endpoint policy: ").append(failoverGroup.readWriteEndpointPolicy())
//                .append("\n\tData loss grace period: ").append(failoverGroup.readWriteEndpointDataLossGracePeriodMinutes())
//                .append("\n\tRead-only endpoint policy: ").append(failoverGroup.readOnlyEndpointPolicy())
//                .append("\n\tReplication state: ").append(failoverGroup.replicationState())
//                .append("\n\tReplication role: ").append(failoverGroup.replicationRole());
//        builder.append("\n\tPartner Servers: ");
//        for (PartnerInfo item : failoverGroup.partnerServers()) {
//            builder
//                    .append("\n\t\tId: ").append(item.id())
//                    .append("\n\t\tLocation: ").append(item.location())
//                    .append("\n\t\tReplication role: ").append(item.replicationRole());
//        }
//        builder.append("\n\tDatabases: ");
//        for (String databaseId : failoverGroup.databases()) {
//            builder.append("\n\t\tID: ").append(databaseId);
//        }
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information for the passed SQL server key.
//     *
//     * @param serverKey virtual network rule to be printed.
//     */
//    public static void print(SqlServerKey serverKey) {
//        StringBuilder builder = new StringBuilder().append("SQL server key: ").append(serverKey.id())
//                .append("Name: ").append(serverKey.name())
//                .append("\n\tResource group: ").append(serverKey.resourceGroupName())
//                .append("\n\tSqlServer Name: ").append(serverKey.sqlServerName())
//                .append("\n\tRegion: ").append(serverKey.region() != null ? serverKey.region().name() : "")
//                .append("\n\tServer Key Type: ").append(serverKey.serverKeyType())
//                .append("\n\tServer Key URI: ").append(serverKey.uri())
//                .append("\n\tServer Key Thumbprint: ").append(serverKey.thumbprint())
//                .append("\n\tServer Key Creation Date: ").append(serverKey.creationDate() != null ? serverKey.creationDate().toString() : "");
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information of the elastic pool passed in.
//     *
//     * @param elasticPool elastic pool to be printed
//     */
//    public static void print(SqlElasticPool elasticPool) {
//        StringBuilder builder = new StringBuilder().append("Sql elastic pool: ").append(elasticPool.id())
//                .append("Name: ").append(elasticPool.name())
//                .append("\n\tResource group: ").append(elasticPool.resourceGroupName())
//                .append("\n\tRegion: ").append(elasticPool.region())
//                .append("\n\tSqlServer Name: ").append(elasticPool.sqlServerName())
//                .append("\n\tEdition of elastic pool: ").append(elasticPool.edition())
//                .append("\n\tTotal number of DTUs in the elastic pool: ").append(elasticPool.dtu())
//                .append("\n\tMaximum DTUs a database can get in elastic pool: ").append(elasticPool.databaseDtuMax())
//                .append("\n\tMinimum DTUs a database is guaranteed in elastic pool: ").append(elasticPool.databaseDtuMin())
//                .append("\n\tCreation date for the elastic pool: ").append(elasticPool.creationDate())
//                .append("\n\tState of the elastic pool: ").append(elasticPool.state())
//                .append("\n\tStorage capacity in MBs for the elastic pool: ").append(elasticPool.storageCapacity());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Prints information of the elastic pool activity.
//     *
//     * @param elasticPoolActivity elastic pool activity to be printed
//     */
//    public static void print(ElasticPoolActivity elasticPoolActivity) {
//        StringBuilder builder = new StringBuilder().append("Sql elastic pool activity: ").append(elasticPoolActivity.id())
//                .append("Name: ").append(elasticPoolActivity.name())
//                .append("\n\tResource group: ").append(elasticPoolActivity.resourceGroupName())
//                .append("\n\tState: ").append(elasticPoolActivity.state())
//                .append("\n\tElastic pool name: ").append(elasticPoolActivity.elasticPoolName())
//                .append("\n\tStart time of activity: ").append(elasticPoolActivity.startTime())
//                .append("\n\tEnd time of activity: ").append(elasticPoolActivity.endTime())
//                .append("\n\tError code of activity: ").append(elasticPoolActivity.errorCode())
//                .append("\n\tError message of activity: ").append(elasticPoolActivity.errorMessage())
//                .append("\n\tError severity of activity: ").append(elasticPoolActivity.errorSeverity())
//                .append("\n\tOperation: ").append(elasticPoolActivity.operation())
//                .append("\n\tCompleted percentage of activity: ").append(elasticPoolActivity.percentComplete())
//                .append("\n\tRequested DTU max limit in activity: ").append(elasticPoolActivity.requestedDatabaseDtuMax())
//                .append("\n\tRequested DTU min limit in activity: ").append(elasticPoolActivity.requestedDatabaseDtuMin())
//                .append("\n\tRequested DTU limit in activity: ").append(elasticPoolActivity.requestedDtu());
//
//        System.out.println(builder.toString());
//
//    }
//
//    /**
//     * Prints information of the database activity.
//     *
//     * @param databaseActivity database activity to be printed
//     */
//    public static void print(ElasticPoolDatabaseActivity databaseActivity) {
//        StringBuilder builder = new StringBuilder().append("Sql elastic pool database activity: ").append(databaseActivity.id())
//                .append("Name: ").append(databaseActivity.name())
//                .append("\n\tResource group: ").append(databaseActivity.resourceGroupName())
//                .append("\n\tSQL Server Name: ").append(databaseActivity.serverName())
//                .append("\n\tDatabase name name: ").append(databaseActivity.databaseName())
//                .append("\n\tCurrent elastic pool name of the database: ").append(databaseActivity.currentElasticPoolName())
//                .append("\n\tState: ").append(databaseActivity.state())
//                .append("\n\tStart time of activity: ").append(databaseActivity.startTime())
//                .append("\n\tEnd time of activity: ").append(databaseActivity.endTime())
//                .append("\n\tCompleted percentage: ").append(databaseActivity.percentComplete())
//                .append("\n\tError code of activity: ").append(databaseActivity.errorCode())
//                .append("\n\tError message of activity: ").append(databaseActivity.errorMessage())
//                .append("\n\tError severity of activity: ").append(databaseActivity.errorSeverity());
//
//        System.out.println(builder.toString());
//    }

    /**
     * Print an application gateway.
     *
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
                .append("\n\tInternet-facing? ").append(resource.isPublic())
                .append("\n\tInternal? ").append(resource.isPrivate())
                .append("\n\tDefault private IP address: ").append(resource.privateIpAddress())
                .append("\n\tPrivate IP address allocation method: ").append(resource.privateIpAllocationMethod())
                .append("\n\tDisabled SSL protocols: ").append(resource.disabledSslProtocols().toString());

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
                    .append("\n\t\t\tAssociated NIC IP configuration IDs: ").append(backend.backendNicIPConfigurationNames().keySet());

            // Show addresses
            Collection<ApplicationGatewayBackendAddress> addresses = backend.addresses();
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
                    .append("\n\t\t\tProtocol: ").append(httpConfig.protocol())
                    .append("\n\t\tHost header: ").append(httpConfig.hostHeader())
                    .append("\n\t\tHost header comes from backend? ").append(httpConfig.isHostHeaderFromBackend())
                    .append("\n\t\tConnection draining timeout in seconds: ").append(httpConfig.connectionDrainingTimeoutInSeconds())
                    .append("\n\t\tAffinity cookie name: ").append(httpConfig.affinityCookieName())
                    .append("\n\t\tPath: ").append(httpConfig.path());
            ApplicationGatewayProbe probe = httpConfig.probe();
            if (probe != null) {
                info.append("\n\t\tProbe: " + probe.name());
            }
            info.append("\n\t\tIs probe enabled? ").append(httpConfig.isProbeEnabled());
        }

        // Show SSL certificates
        Map<String, ApplicationGatewaySslCertificate> sslCerts = resource.sslCertificates();
        info.append("\n\tSSL certificates: ").append(sslCerts.size());
        for (ApplicationGatewaySslCertificate cert : sslCerts.values()) {
            info.append("\n\t\tName: ").append(cert.name())
                    .append("\n\t\t\tCert data: ").append(cert.publicData());
        }

        // Show redirect configurations
        Map<String, ApplicationGatewayRedirectConfiguration> redirects = resource.redirectConfigurations();
        info.append("\n\tRedirect configurations: ").append(redirects.size());
        for (ApplicationGatewayRedirectConfiguration redirect : redirects.values()) {
            info.append("\n\t\tName: ").append(redirect.name())
                    .append("\n\t\tTarget URL: ").append(redirect.type())
                    .append("\n\t\tTarget URL: ").append(redirect.targetUrl())
                    .append("\n\t\tTarget listener: ").append(redirect.targetListener() != null ? redirect.targetListener().name() : null)
                    .append("\n\t\tIs path included? ").append(redirect.isPathIncluded())
                    .append("\n\t\tIs query string included? ").append(redirect.isQueryStringIncluded())
                    .append("\n\t\tReferencing request routing rules: ").append(redirect.requestRoutingRules().values());
        }

        // Show HTTP listeners
        Map<String, ApplicationGatewayListener> listeners = resource.listeners();
        info.append("\n\tHTTP listeners: ").append(listeners.size());
        for (ApplicationGatewayListener listener : listeners.values()) {
            info.append("\n\t\tName: ").append(listener.name())
                    .append("\n\t\t\tHost name: ").append(listener.hostname())
                    .append("\n\t\t\tServer name indication required? ").append(listener.requiresServerNameIndication())
                    .append("\n\t\t\tAssociated frontend name: ").append(listener.frontend().name())
                    .append("\n\t\t\tFrontend port name: ").append(listener.frontendPortName())
                    .append("\n\t\t\tFrontend port number: ").append(listener.frontendPortNumber())
                    .append("\n\t\t\tProtocol: ").append(listener.protocol().toString());
            if (listener.sslCertificate() != null) {
                info.append("\n\t\t\tAssociated SSL certificate: ").append(listener.sslCertificate().name());
            }
        }

        // Show probes
        Map<String, ApplicationGatewayProbe> probes = resource.probes();
        info.append("\n\tProbes: ").append(probes.size());
        for (ApplicationGatewayProbe probe : probes.values()) {
            info.append("\n\t\tName: ").append(probe.name())
                    .append("\n\t\tProtocol:").append(probe.protocol().toString())
                    .append("\n\t\tInterval in seconds: ").append(probe.timeBetweenProbesInSeconds())
                    .append("\n\t\tRetries: ").append(probe.retriesBeforeUnhealthy())
                    .append("\n\t\tTimeout: ").append(probe.timeoutInSeconds())
                    .append("\n\t\tHost: ").append(probe.host())
                    .append("\n\t\tHealthy HTTP response status code ranges: ").append(probe.healthyHttpResponseStatusCodeRanges())
                    .append("\n\t\tHealthy HTTP response body contents: ").append(probe.healthyHttpResponseBodyContents());
        }

        // Show request routing rules
        Map<String, ApplicationGatewayRequestRoutingRule> rules = resource.requestRoutingRules();
        info.append("\n\tRequest routing rules: ").append(rules.size());
        for (ApplicationGatewayRequestRoutingRule rule : rules.values()) {
            info.append("\n\t\tName: ").append(rule.name())
                    .append("\n\t\tType: ").append(rule.ruleType())
                    .append("\n\t\tPublic IP address ID: ").append(rule.publicIpAddressId())
                    .append("\n\t\tHost name: ").append(rule.hostname())
                    .append("\n\t\tServer name indication required? ").append(rule.requiresServerNameIndication())
                    .append("\n\t\tFrontend port: ").append(rule.frontendPort())
                    .append("\n\t\tFrontend protocol: ").append(rule.frontendProtocol().toString())
                    .append("\n\t\tBackend port: ").append(rule.backendPort())
                    .append("\n\t\tCookie based affinity enabled? ").append(rule.cookieBasedAffinity())
                    .append("\n\t\tRedirect configuration: ").append(rule.redirectConfiguration() != null ? rule.redirectConfiguration().name() : "(none)");

            // Show backend addresses
            Collection<ApplicationGatewayBackendAddress> addresses = rule.backendAddresses();
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

    /**
     * Uploads a file to an Azure app service for Web App.
     *
     * @param profile the publishing profile for the app service.
     * @param fileName the name of the file on server
     * @param file the local file
     */
    public static void uploadFileViaFtp(PublishingProfile profile, String fileName, InputStream file) {
        String path = "./site/wwwroot/webapps";
        uploadFileViaFtp(profile, fileName, file, path);
    }

    /**
     * Uploads a file to an Azure app service for Function App.
     *
     * @param profile the publishing profile for the app service.
     * @param fileName the name of the file on server
     * @param file the local file
     */
    public static void uploadFileForFunctionViaFtp(PublishingProfile profile, String fileName, InputStream file) {
        String path = "./site/wwwroot";
        uploadFileViaFtp(profile, fileName, file, path);
    }

    private static void uploadFileViaFtp(PublishingProfile profile, String fileName, InputStream file, String path) {
        FTPClient ftpClient = new FTPClient();
        String[] ftpUrlSegments = profile.ftpUrl().split("/", 2);
        String server = ftpUrlSegments[0];
        if (fileName.contains("/")) {
            int lastslash = fileName.lastIndexOf('/');
            path = path + "/" + fileName.substring(0, lastslash);
            fileName = fileName.substring(lastslash + 1);
        }
        try {
            ftpClient.connect(server);
            ftpClient.enterLocalPassiveMode();
            ftpClient.login(profile.ftpUsername(), profile.ftpPassword());
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            for (String segment : path.split("/")) {
                if (!ftpClient.changeWorkingDirectory(segment)) {
                    ftpClient.makeDirectory(segment);
                    ftpClient.changeWorkingDirectory(segment);
                }
            }
            ftpClient.storeFile(fileName, file);
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * Print service bus namespace info.
//     *
//     * @param serviceBusNamespace a service bus namespace
//     */
//    public static void print(ServiceBusNamespace serviceBusNamespace) {
//        StringBuilder builder = new StringBuilder()
//                .append("Service bus Namespace: ").append(serviceBusNamespace.id())
//                .append("\n\tName: ").append(serviceBusNamespace.name())
//                .append("\n\tRegion: ").append(serviceBusNamespace.regionName())
//                .append("\n\tResourceGroupName: ").append(serviceBusNamespace.resourceGroupName())
//                .append("\n\tCreatedAt: ").append(serviceBusNamespace.createdAt())
//                .append("\n\tUpdatedAt: ").append(serviceBusNamespace.updatedAt())
//                .append("\n\tDnsLabel: ").append(serviceBusNamespace.dnsLabel())
//                .append("\n\tFQDN: ").append(serviceBusNamespace.fqdn())
//                .append("\n\tSku: ")
//                .append("\n\t\tCapacity: ").append(serviceBusNamespace.sku().capacity())
//                .append("\n\t\tSkuName: ").append(serviceBusNamespace.sku().name())
//                .append("\n\t\tTier: ").append(serviceBusNamespace.sku().tier());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Print service bus queue info.
//     *
//     * @param queue a service bus queue
//     */
//    public static void print(Queue queue) {
//        StringBuilder builder = new StringBuilder()
//                .append("Service bus Queue: ").append(queue.id())
//                .append("\n\tName: ").append(queue.name())
//                .append("\n\tResourceGroupName: ").append(queue.resourceGroupName())
//                .append("\n\tCreatedAt: ").append(queue.createdAt())
//                .append("\n\tUpdatedAt: ").append(queue.updatedAt())
//                .append("\n\tAccessedAt: ").append(queue.accessedAt())
//                .append("\n\tActiveMessageCount: ").append(queue.activeMessageCount())
//                .append("\n\tCurrentSizeInBytes: ").append(queue.currentSizeInBytes())
//                .append("\n\tDeadLetterMessageCount: ").append(queue.deadLetterMessageCount())
//                .append("\n\tDefaultMessageTtlDuration: ").append(queue.defaultMessageTtlDuration())
//                .append("\n\tDuplicateMessageDetectionHistoryDuration: ").append(queue.duplicateMessageDetectionHistoryDuration())
//                .append("\n\tIsBatchedOperationsEnabled: ").append(queue.isBatchedOperationsEnabled())
//                .append("\n\tIsDeadLetteringEnabledForExpiredMessages: ").append(queue.isDeadLetteringEnabledForExpiredMessages())
//                .append("\n\tIsDuplicateDetectionEnabled: ").append(queue.isDuplicateDetectionEnabled())
//                .append("\n\tIsExpressEnabled: ").append(queue.isExpressEnabled())
//                .append("\n\tIsPartitioningEnabled: ").append(queue.isPartitioningEnabled())
//                .append("\n\tIsSessionEnabled: ").append(queue.isSessionEnabled())
//                .append("\n\tDeleteOnIdleDurationInMinutes: ").append(queue.deleteOnIdleDurationInMinutes())
//                .append("\n\tMaxDeliveryCountBeforeDeadLetteringMessage: ").append(queue.maxDeliveryCountBeforeDeadLetteringMessage())
//                .append("\n\tMaxSizeInMB: ").append(queue.maxSizeInMB())
//                .append("\n\tMessageCount: ").append(queue.messageCount())
//                .append("\n\tScheduledMessageCount: ").append(queue.scheduledMessageCount())
//                .append("\n\tStatus: ").append(queue.status())
//                .append("\n\tTransferMessageCount: ").append(queue.transferMessageCount())
//                .append("\n\tLockDurationInSeconds: ").append(queue.lockDurationInSeconds())
//                .append("\n\tTransferDeadLetterMessageCount: ").append(queue.transferDeadLetterMessageCount());
//
//        System.out.println(builder.toString());
//
//    }
//
//    /**
//     * Print service bus queue authorization keys info.
//     *
//     * @param queueAuthorizationRule a service bus queue authorization keys
//     */
//    public static void print(QueueAuthorizationRule queueAuthorizationRule) {
//        StringBuilder builder = new StringBuilder()
//                .append("Service bus queue authorization rule: ").append(queueAuthorizationRule.id())
//                .append("\n\tName: ").append(queueAuthorizationRule.name())
//                .append("\n\tResourceGroupName: ").append(queueAuthorizationRule.resourceGroupName())
//                .append("\n\tNamespace Name: ").append(queueAuthorizationRule.namespaceName())
//                .append("\n\tQueue Name: ").append(queueAuthorizationRule.queueName());
//
//        List<com.azure.resourcemanager.servicebus.models.AccessRights> rights = queueAuthorizationRule.rights();
//        builder.append("\n\tNumber of access rights in queue: ").append(rights.size());
//        for (com.azure.resourcemanager.servicebus.models.AccessRights right : rights) {
//            builder.append("\n\t\tAccessRight: ")
//                    .append("\n\t\t\tName :").append(right.name());
//        }
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Print service bus namespace authorization keys info.
//     *
//     * @param keys a service bus namespace authorization keys
//     */
//    public static void print(AuthorizationKeys keys) {
//        StringBuilder builder = new StringBuilder()
//                .append("Authorization keys: ")
//                .append("\n\tPrimaryKey: ").append(keys.primaryKey())
//                .append("\n\tPrimaryConnectionString: ").append(keys.primaryConnectionString())
//                .append("\n\tSecondaryKey: ").append(keys.secondaryKey())
//                .append("\n\tSecondaryConnectionString: ").append(keys.secondaryConnectionString());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Print service bus namespace authorization rule info.
//     *
//     * @param namespaceAuthorizationRule a service bus namespace authorization rule
//     */
//    public static void print(NamespaceAuthorizationRule namespaceAuthorizationRule) {
//        StringBuilder builder = new StringBuilder()
//                .append("Service bus queue authorization rule: ").append(namespaceAuthorizationRule.id())
//                .append("\n\tName: ").append(namespaceAuthorizationRule.name())
//                .append("\n\tResourceGroupName: ").append(namespaceAuthorizationRule.resourceGroupName())
//                .append("\n\tNamespace Name: ").append(namespaceAuthorizationRule.namespaceName());
//
//        List<com.azure.resourcemanager.servicebus.models.AccessRights> rights = namespaceAuthorizationRule.rights();
//        builder.append("\n\tNumber of access rights in queue: ").append(rights.size());
//        for (com.azure.resourcemanager.servicebus.models.AccessRights right : rights) {
//            builder.append("\n\t\tAccessRight: ")
//                    .append("\n\t\t\tName :").append(right.name());
//        }
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Print service bus topic info.
//     *
//     * @param topic a service bus topic
//     */
//    public static void print(Topic topic) {
//        StringBuilder builder = new StringBuilder()
//                .append("Service bus topic: ").append(topic.id())
//                .append("\n\tName: ").append(topic.name())
//                .append("\n\tResourceGroupName: ").append(topic.resourceGroupName())
//                .append("\n\tCreatedAt: ").append(topic.createdAt())
//                .append("\n\tUpdatedAt: ").append(topic.updatedAt())
//                .append("\n\tAccessedAt: ").append(topic.accessedAt())
//                .append("\n\tActiveMessageCount: ").append(topic.activeMessageCount())
//                .append("\n\tCurrentSizeInBytes: ").append(topic.currentSizeInBytes())
//                .append("\n\tDeadLetterMessageCount: ").append(topic.deadLetterMessageCount())
//                .append("\n\tDefaultMessageTtlDuration: ").append(topic.defaultMessageTtlDuration())
//                .append("\n\tDuplicateMessageDetectionHistoryDuration: ").append(topic.duplicateMessageDetectionHistoryDuration())
//                .append("\n\tIsBatchedOperationsEnabled: ").append(topic.isBatchedOperationsEnabled())
//                .append("\n\tIsDuplicateDetectionEnabled: ").append(topic.isDuplicateDetectionEnabled())
//                .append("\n\tIsExpressEnabled: ").append(topic.isExpressEnabled())
//                .append("\n\tIsPartitioningEnabled: ").append(topic.isPartitioningEnabled())
//                .append("\n\tDeleteOnIdleDurationInMinutes: ").append(topic.deleteOnIdleDurationInMinutes())
//                .append("\n\tMaxSizeInMB: ").append(topic.maxSizeInMB())
//                .append("\n\tScheduledMessageCount: ").append(topic.scheduledMessageCount())
//                .append("\n\tStatus: ").append(topic.status())
//                .append("\n\tTransferMessageCount: ").append(topic.transferMessageCount())
//                .append("\n\tSubscriptionCount: ").append(topic.subscriptionCount())
//                .append("\n\tTransferDeadLetterMessageCount: ").append(topic.transferDeadLetterMessageCount());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Print service bus subscription info.
//     *
//     * @param serviceBusSubscription a service bus subscription
//     */
//    public static void print(ServiceBusSubscription serviceBusSubscription) {
//        StringBuilder builder = new StringBuilder()
//                .append("Service bus subscription: ").append(serviceBusSubscription.id())
//                .append("\n\tName: ").append(serviceBusSubscription.name())
//                .append("\n\tResourceGroupName: ").append(serviceBusSubscription.resourceGroupName())
//                .append("\n\tCreatedAt: ").append(serviceBusSubscription.createdAt())
//                .append("\n\tUpdatedAt: ").append(serviceBusSubscription.updatedAt())
//                .append("\n\tAccessedAt: ").append(serviceBusSubscription.accessedAt())
//                .append("\n\tActiveMessageCount: ").append(serviceBusSubscription.activeMessageCount())
//                .append("\n\tDeadLetterMessageCount: ").append(serviceBusSubscription.deadLetterMessageCount())
//                .append("\n\tDefaultMessageTtlDuration: ").append(serviceBusSubscription.defaultMessageTtlDuration())
//                .append("\n\tIsBatchedOperationsEnabled: ").append(serviceBusSubscription.isBatchedOperationsEnabled())
//                .append("\n\tDeleteOnIdleDurationInMinutes: ").append(serviceBusSubscription.deleteOnIdleDurationInMinutes())
//                .append("\n\tScheduledMessageCount: ").append(serviceBusSubscription.scheduledMessageCount())
//                .append("\n\tStatus: ").append(serviceBusSubscription.status())
//                .append("\n\tTransferMessageCount: ").append(serviceBusSubscription.transferMessageCount())
//                .append("\n\tIsDeadLetteringEnabledForExpiredMessages: ").append(serviceBusSubscription.isDeadLetteringEnabledForExpiredMessages())
//                .append("\n\tIsSessionEnabled: ").append(serviceBusSubscription.isSessionEnabled())
//                .append("\n\tLockDurationInSeconds: ").append(serviceBusSubscription.lockDurationInSeconds())
//                .append("\n\tMaxDeliveryCountBeforeDeadLetteringMessage: ").append(serviceBusSubscription.maxDeliveryCountBeforeDeadLetteringMessage())
//                .append("\n\tIsDeadLetteringEnabledForFilterEvaluationFailedMessages: ").append(serviceBusSubscription.isDeadLetteringEnabledForFilterEvaluationFailedMessages())
//                .append("\n\tTransferMessageCount: ").append(serviceBusSubscription.transferMessageCount())
//                .append("\n\tTransferDeadLetterMessageCount: ").append(serviceBusSubscription.transferDeadLetterMessageCount());
//
//        System.out.println(builder.toString());
//    }
//
//    /**
//     * Print topic Authorization Rule info.
//     *
//     * @param topicAuthorizationRule a topic Authorization Rule
//     */
//    public static void print(TopicAuthorizationRule topicAuthorizationRule) {
//        StringBuilder builder = new StringBuilder()
//                .append("Service bus topic authorization rule: ").append(topicAuthorizationRule.id())
//                .append("\n\tName: ").append(topicAuthorizationRule.name())
//                .append("\n\tResourceGroupName: ").append(topicAuthorizationRule.resourceGroupName())
//                .append("\n\tNamespace Name: ").append(topicAuthorizationRule.namespaceName())
//                .append("\n\tTopic Name: ").append(topicAuthorizationRule.topicName());
//
//        List<com.azure.resourcemanager.servicebus.models.AccessRights> rights = topicAuthorizationRule.rights();
//        builder.append("\n\tNumber of access rights in queue: ").append(rights.size());
//        for (com.azure.resourcemanager.servicebus.models.AccessRights right : rights) {
//            builder.append("\n\t\tAccessRight: ")
//                    .append("\n\t\t\tName :").append(right.name());
//        }
//
//        System.out.println(builder.toString());
//    }

    /**
     * Print CosmosDB info.
     *
     * @param cosmosDBAccount a CosmosDB
     */
    public static void print(CosmosDBAccount cosmosDBAccount) {
        StringBuilder builder = new StringBuilder()
                .append("CosmosDB: ").append(cosmosDBAccount.id())
                .append("\n\tName: ").append(cosmosDBAccount.name())
                .append("\n\tResourceGroupName: ").append(cosmosDBAccount.resourceGroupName())
                .append("\n\tKind: ").append(cosmosDBAccount.kind().toString())
                .append("\n\tDefault consistency level: ").append(cosmosDBAccount.consistencyPolicy().defaultConsistencyLevel())
                .append("\n\tIP range filter: ").append(cosmosDBAccount.ipRangeFilter());

        DatabaseAccountListKeysResult keys = cosmosDBAccount.listKeys();
        DatabaseAccountListReadOnlyKeysResult readOnlyKeys = cosmosDBAccount.listReadOnlyKeys();
        builder
                .append("\n\tPrimary Master Key: ").append(keys.primaryMasterKey())
                .append("\n\tSecondary Master Key: ").append(keys.secondaryMasterKey())
                .append("\n\tPrimary Read-Only Key: ").append(readOnlyKeys.primaryReadonlyMasterKey())
                .append("\n\tSecondary Read-Only Key: ").append(readOnlyKeys.secondaryReadonlyMasterKey());

        for (Location writeReplica : cosmosDBAccount.writableReplications()) {
            builder.append("\n\t\tWrite replication: ")
                    .append("\n\t\t\tName :").append(writeReplica.locationName());
        }

        builder.append("\n\tNumber of read replications: ").append(cosmosDBAccount.readableReplications().size());
        for (Location readReplica : cosmosDBAccount.readableReplications()) {
            builder.append("\n\t\tRead replication: ")
                    .append("\n\t\t\tName :").append(readReplica.locationName());
        }

    }

    /**
     * Print Active Directory User info.
     *
     * @param user active directory user
     */
    public static void print(ActiveDirectoryUser user) {
        StringBuilder builder = new StringBuilder()
                .append("Active Directory User: ").append(user.id())
                .append("\n\tName: ").append(user.name())
                .append("\n\tMail: ").append(user.mail())
                .append("\n\tMail Nickname: ").append(user.mailNickname())
                .append("\n\tSign In Name: ").append(user.signInName())
                .append("\n\tUser Principal Name: ").append(user.userPrincipalName());

        System.out.println(builder.toString());
    }

    /**
     * Print Active Directory User info.
     *
     * @param role role definition
     */
    public static void print(RoleDefinition role) {
        StringBuilder builder = new StringBuilder()
                .append("Role Definition: ").append(role.id())
                .append("\n\tName: ").append(role.name())
                .append("\n\tRole Name: ").append(role.roleName())
                .append("\n\tType: ").append(role.type())
                .append("\n\tDescription: ").append(role.description())
                .append("\n\tType: ").append(role.type());

        Set<Permission> permissions = role.permissions();
        builder.append("\n\tPermissions: ").append(permissions.size());
        for (Permission permission : permissions) {
            builder.append("\n\t\tPermission Actions: " + permission.actions().size());
            for (String action : permission.actions()) {
                builder.append("\n\t\t\tName :").append(action);
            }
            builder.append("\n\t\tPermission Not Actions: " + permission.notActions().size());
            for (String notAction : permission.notActions()) {
                builder.append("\n\t\t\tName :").append(notAction);
            }
        }

        Set<String> assignableScopes = role.assignableScopes();
        builder.append("\n\tAssignable scopes: ").append(assignableScopes.size());
        for (String scope : assignableScopes) {
            builder.append("\n\t\tAssignable Scope: ")
                    .append("\n\t\t\tName :").append(scope);
        }

        System.out.println(builder.toString());
    }

    /**
     * Print Role Assignment info.
     *
     * @param roleAssignment role assignment
     */
    public static void print(RoleAssignment roleAssignment) {
        StringBuilder builder = new StringBuilder()
                .append("Role Assignment: ")
                .append("\n\tScope: ").append(roleAssignment.scope())
                .append("\n\tPrincipal Id: ").append(roleAssignment.principalId())
                .append("\n\tRole Definition Id: ").append(roleAssignment.roleDefinitionId());

        System.out.println(builder.toString());
    }

    /**
     * Print Active Directory Group info.
     *
     * @param group active directory group
     */
    public static void print(ActiveDirectoryGroup group) {
        StringBuilder builder = new StringBuilder()
                .append("Active Directory Group: ").append(group.id())
                .append("\n\tName: ").append(group.name())
                .append("\n\tMail: ").append(group.mail())
                .append("\n\tSecurity Enabled: ").append(group.securityEnabled())
                .append("\n\tGroup members:");

        for (ActiveDirectoryObject object : group.listMembers()) {
            builder.append("\n\t\tType: ").append(object.getClass().getSimpleName())
                    .append("\tName: ").append(object.name());
        }

        System.out.println(builder.toString());
    }

    /**
     * Print Active Directory Application info.
     *
     * @param application active directory application
     */
    public static void print(ActiveDirectoryApplication application) {
        StringBuilder builder = new StringBuilder()
                .append("Active Directory Application: ").append(application.id())
                .append("\n\tName: ").append(application.name())
                .append("\n\tSign on URL: ").append(application.signOnUrl())
                .append("\n\tReply URLs:");
        for (String replyUrl : application.replyUrls()) {
            builder.append("\n\t\t").append(replyUrl);
        }

        System.out.println(builder.toString());
    }

    /**
     * Print Service Principal info.
     *
     * @param servicePrincipal service principal
     */
    public static void print(ServicePrincipal servicePrincipal) {
        StringBuilder builder = new StringBuilder()
                .append("Service Principal: ").append(servicePrincipal.id())
                .append("\n\tName: ").append(servicePrincipal.name())
                .append("\n\tApplication Id: ").append(servicePrincipal.applicationId());

        List<String> names = servicePrincipal.servicePrincipalNames();
        builder.append("\n\tNames: ").append(names.size());
        for (String name : names) {
            builder.append("\n\t\tName: ").append(name);
        }
        System.out.println(builder.toString());
    }

    /**
     * Print Network Watcher info.
     *
     * @param nw network watcher
     */
    public static void print(NetworkWatcher nw) {
        StringBuilder builder = new StringBuilder()
                .append("Network Watcher: ").append(nw.id())
                .append("\n\tName: ").append(nw.name())
                .append("\n\tResource group name: ").append(nw.resourceGroupName())
                .append("\n\tRegion name: ").append(nw.regionName());
        System.out.println(builder.toString());
    }

    /**
     * Print packet capture info.
     *
     * @param resource packet capture
     */
    public static void print(PacketCapture resource) {
        StringBuilder sb = new StringBuilder().append("Packet Capture: ").append(resource.id())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tTarget id: ").append(resource.targetId())
                .append("\n\tTime limit in seconds: ").append(resource.timeLimitInSeconds())
                .append("\n\tBytes to capture per packet: ").append(resource.bytesToCapturePerPacket())
                .append("\n\tProvisioning state: ").append(resource.provisioningState())
                .append("\n\tStorage location:")
                .append("\n\tStorage account id: ").append(resource.storageLocation().storageId())
                .append("\n\tStorage account path: ").append(resource.storageLocation().storagePath())
                .append("\n\tFile path: ").append(resource.storageLocation().filePath())
                .append("\n\t Packet capture filters: ").append(resource.filters().size());
        for (PacketCaptureFilter filter : resource.filters()) {
            sb.append("\n\t\tProtocol: ").append(filter.protocol());
            sb.append("\n\t\tLocal IP address: ").append(filter.localIpAddress());
            sb.append("\n\t\tRemote IP address: ").append(filter.remoteIpAddress());
            sb.append("\n\t\tLocal port: ").append(filter.localPort());
            sb.append("\n\t\tRemote port: ").append(filter.remotePort());
        }
        System.out.println(sb.toString());
    }

    /**
     * Print verification IP flow info.
     *
     * @param resource IP flow verification info
     */
    public static void print(VerificationIPFlow resource) {
        System.out.println(new StringBuilder("IP flow verification: ")
                .append("\n\tAccess: ").append(resource.access())
                .append("\n\tRule name: ").append(resource.ruleName())
                .toString());
    }

    /**
     * Print topology info.
     *
     * @param resource topology
     */
    public static void print(Topology resource) {
        StringBuilder sb = new StringBuilder().append("Topology: ").append(resource.id())
                .append("\n\tTopology parameters: ")
                .append("\n\t\tResource group: ").append(resource.topologyParameters().targetResourceGroupName())
                .append("\n\t\tVirtual network: ").append(resource.topologyParameters().targetVirtualNetwork() == null ? "" : resource.topologyParameters().targetVirtualNetwork().id())
                .append("\n\t\tSubnet id: ").append(resource.topologyParameters().targetSubnet() == null ? "" : resource.topologyParameters().targetSubnet().id())
                .append("\n\tCreated time: ").append(resource.createdTime())
                .append("\n\tLast modified time: ").append(resource.lastModifiedTime());
        for (TopologyResource tr : resource.resources().values()) {
            sb.append("\n\tTopology resource: ").append(tr.id())
                    .append("\n\t\tName: ").append(tr.name())
                    .append("\n\t\tLocation: ").append(tr.location())
                    .append("\n\t\tAssociations:");
            for (TopologyAssociation association : tr.associations()) {
                sb.append("\n\t\t\tName:").append(association.name())
                        .append("\n\t\t\tResource id:").append(association.resourceId())
                        .append("\n\t\t\tAssociation type:").append(association.associationType());
            }
        }
        System.out.println(sb.toString());
    }

    /**
     * Print flow log settings info.
     *
     * @param resource flow log settings
     */
    public static void print(FlowLogSettings resource) {
        System.out.println(new StringBuilder().append("Flow log settings: ")
                .append("Target resource id: ").append(resource.targetResourceId())
                .append("\n\tFlow log enabled: ").append(resource.enabled())
                .append("\n\tStorage account id: ").append(resource.storageId())
                .append("\n\tRetention policy enabled: ").append(resource.isRetentionEnabled())
                .append("\n\tRetention policy days: ").append(resource.retentionDays())
                .toString());
    }

    /**
     * Print availability set info.
     *
     * @param resource an availability set
     */
    public static void print(SecurityGroupView resource) {
        StringBuilder sb = new StringBuilder().append("Security group view: ")
                .append("\n\tVirtual machine id: ").append(resource.vmId());
        for (SecurityGroupNetworkInterface sgni : resource.networkInterfaces().values()) {
            sb.append("\n\tSecurity group network interface:").append(sgni.id())
                    .append("\n\t\tSecurity group network interface:")
                    .append("\n\t\tEffective security rules:");
            for (EffectiveNetworkSecurityRule rule : sgni.securityRuleAssociations().effectiveSecurityRules()) {
                sb.append("\n\t\t\tName: ").append(rule.name())
                        .append("\n\t\t\tDirection: ").append(rule.direction())
                        .append("\n\t\t\tAccess: ").append(rule.access())
                        .append("\n\t\t\tPriority: ").append(rule.priority())
                        .append("\n\t\t\tSource address prefix: ").append(rule.sourceAddressPrefix())
                        .append("\n\t\t\tSource port range: ").append(rule.sourcePortRange())
                        .append("\n\t\t\tDestination address prefix: ").append(rule.destinationAddressPrefix())
                        .append("\n\t\t\tDestination port range: ").append(rule.destinationPortRange())
                        .append("\n\t\t\tProtocol: ").append(rule.protocol());
            }
            sb.append("\n\t\tSubnet:").append(sgni.securityRuleAssociations().subnetAssociation().id());
            printSecurityRule(sb, sgni.securityRuleAssociations().subnetAssociation().securityRules());
            if (sgni.securityRuleAssociations().networkInterfaceAssociation() != null) {
                sb.append("\n\t\tNetwork interface:").append(sgni.securityRuleAssociations().networkInterfaceAssociation().id());
                printSecurityRule(sb, sgni.securityRuleAssociations().networkInterfaceAssociation().securityRules());
            }
            sb.append("\n\t\tDefault security rules:");
            printSecurityRule(sb, sgni.securityRuleAssociations().defaultSecurityRules());
        }
        System.out.println(sb.toString());
    }

    private static void printSecurityRule(StringBuilder sb, List<SecurityRuleInner> rules) {
        for (SecurityRuleInner rule : rules) {
            sb.append("\n\t\t\tName: ").append(rule.name())
                    .append("\n\t\t\tDirection: ").append(rule.direction())
                    .append("\n\t\t\tAccess: ").append(rule.access())
                    .append("\n\t\t\tPriority: ").append(rule.priority())
                    .append("\n\t\t\tSource address prefix: ").append(rule.sourceAddressPrefix())
                    .append("\n\t\t\tSource port range: ").append(rule.sourcePortRange())
                    .append("\n\t\t\tDestination address prefix: ").append(rule.destinationAddressPrefix())
                    .append("\n\t\t\tDestination port range: ").append(rule.destinationPortRange())
                    .append("\n\t\t\tProtocol: ").append(rule.protocol())
                    .append("\n\t\t\tDescription: ").append(rule.description())
                    .append("\n\t\t\tProvisioning state: ").append(rule.provisioningState());
        }
    }

    /**
     * Print next hop info.
     *
     * @param resource an availability set
     */
    public static void print(NextHop resource) {
        System.out.println(new StringBuilder("Next hop: ")
                .append("Next hop type: ").append(resource.nextHopType())
                .append("\n\tNext hop ip address: ").append(resource.nextHopIpAddress())
                .append("\n\tRoute table id: ").append(resource.routeTableId())
                .toString());
    }

//    /**
//     * Print container group info.
//     *
//     * @param resource a container group
//     */
//    public static void print(ContainerGroup resource) {
//        StringBuilder info = new StringBuilder().append("Container Group: ").append(resource.id())
//                .append("Name: ").append(resource.name())
//                .append("\n\tResource group: ").append(resource.resourceGroupName())
//                .append("\n\tRegion: ").append(resource.region())
//                .append("\n\tTags: ").append(resource.tags())
//                .append("\n\tOS type: ").append(resource.osType());
//
//        if (resource.ipAddress() != null) {
//            info.append("\n\tPublic IP address: ").append(resource.ipAddress());
//        }
//        if (resource.externalTcpPorts() != null) {
//            info.append("\n\tExternal TCP ports:");
//            for (int port : resource.externalTcpPorts()) {
//                info.append(" ").append(port);
//            }
//        }
//        if (resource.externalUdpPorts() != null) {
//            info.append("\n\tExternal UDP ports:");
//            for (int port : resource.externalUdpPorts()) {
//                info.append(" ").append(port);
//            }
//        }
//        if (resource.imageRegistryServers() != null) {
//            info.append("\n\tPrivate Docker image registries:");
//            for (String server : resource.imageRegistryServers()) {
//                info.append(" ").append(server);
//            }
//        }
//        if (resource.volumes() != null) {
//            info.append("\n\tVolume mapping: ");
//            for (Map.Entry<String, Volume> entry : resource.volumes().entrySet()) {
//                info.append("\n\t\tName: ").append(entry.getKey()).append(" -> ")
//                        .append(entry.getValue().azureFile() != null ? entry.getValue().azureFile().shareName() : "empty direcory volume");
//            }
//        }
//        if (resource.containers() != null) {
//            info.append("\n\tContainer instances: ");
//            for (Map.Entry<String, Container> entry : resource.containers().entrySet()) {
//                Container container = entry.getValue();
//                info.append("\n\t\tName: ").append(entry.getKey()).append(" -> ").append(container.image());
//                info.append("\n\t\t\tResources: ");
//                info.append(container.resources().requests().cpu()).append("CPUs ");
//                info.append(container.resources().requests().memoryInGB()).append("GB");
//                info.append("\n\t\t\tPorts:");
//                for (ContainerPort port : container.ports()) {
//                    info.append(" ").append(port.port());
//                }
//                if (container.volumeMounts() != null) {
//                    info.append("\n\t\t\tVolume mounts:");
//                    for (VolumeMount volumeMount : container.volumeMounts()) {
//                        info.append(" ").append(volumeMount.name()).append("->").append(volumeMount.mountPath());
//                    }
//                }
//                if (container.command() != null) {
//                    info.append("\n\t\t\tStart commands:");
//                    for (String command : container.command()) {
//                        info.append("\n\t\t\t\t").append(command);
//                    }
//                }
//                if (container.environmentVariables() != null) {
//                    info.append("\n\t\t\tENV vars:");
//                    for (EnvironmentVariable envVar : container.environmentVariables()) {
//                        info.append("\n\t\t\t\t").append(envVar.name()).append("=").append(envVar.value());
//                    }
//                }
//            }
//        }
//
//        System.out.println(info.toString());
//    }
//
//    /**
//     * Print event hub namespace.
//     *
//     * @param resource a virtual machine
//     */
//    public static void print(EventHubNamespace resource) {
//        StringBuilder info = new StringBuilder();
//        info.append("Eventhub Namespace: ").append(resource.id())
//                .append("\n\tName: ").append(resource.name())
//                .append("\n\tRegion: ").append(resource.region())
//                .append("\n\tTags: ").append(resource.tags())
//                .append("\n\tAzureInsightMetricId: ").append(resource.azureInsightMetricId())
//                .append("\n\tIsAutoScale enabled: ").append(resource.isAutoScaleEnabled())
//                .append("\n\tServiceBus endpoint: ").append(resource.serviceBusEndpoint())
//                .append("\n\tThroughPut upper limit: ").append(resource.throughputUnitsUpperLimit())
//                .append("\n\tCurrent ThroughPut: ").append(resource.currentThroughputUnits())
//                .append("\n\tCreated time: ").append(resource.createdAt())
//                .append("\n\tUpdated time: ").append(resource.updatedAt());
//
//        System.out.println(info.toString());
//    }
//
//    /**
//     * Print event hub.
//     *
//     * @param resource event hub
//     */
//    public static void print(EventHub resource) {
//        StringBuilder info = new StringBuilder();
//        info.append("Eventhub: ").append(resource.id())
//                .append("\n\tName: ").append(resource.name())
//                .append("\n\tNamespace resource group: ").append(resource.namespaceResourceGroupName())
//                .append("\n\tNamespace: ").append(resource.namespaceName())
//                .append("\n\tIs data capture enabled: ").append(resource.isDataCaptureEnabled())
//                .append("\n\tPartition ids: ").append(resource.partitionIds());
//        if (resource.isDataCaptureEnabled()) {
//            info.append("\n\t\t\tData capture window size in MB: ").append(resource.dataCaptureWindowSizeInMB());
//            info.append("\n\t\t\tData capture window size in seconds: ").append(resource.dataCaptureWindowSizeInSeconds());
//            if (resource.captureDestination() != null) {
//                info.append("\n\t\t\tData capture storage account: ").append(resource.captureDestination().storageAccountResourceId());
//                info.append("\n\t\t\tData capture storage container: ").append(resource.captureDestination().blobContainer());
//            }
//        }
//        System.out.println(info.toString());
//    }
//
//    /**
//     * Print event hub namespace recovery pairing.
//     *
//     * @param resource event hub namespace disaster recovery pairing
//     */
//    public static void print(EventHubDisasterRecoveryPairing resource) {
//        StringBuilder info = new StringBuilder();
//        info.append("DisasterRecoveryPairing: ").append(resource.id())
//                .append("\n\tName: ").append(resource.name())
//                .append("\n\tPrimary namespace resource group name: ").append(resource.primaryNamespaceResourceGroupName())
//                .append("\n\tPrimary namespace name: ").append(resource.primaryNamespaceName())
//                .append("\n\tSecondary namespace: ").append(resource.secondaryNamespaceId())
//                .append("\n\tNamespace role: ").append(resource.namespaceRole());
//        System.out.println(info.toString());
//    }
//
//    /**
//     * Print event hub namespace recovery pairing auth rules.
//     *
//     * @param resource event hub namespace disaster recovery pairing auth rule
//     */
//    public static void print(DisasterRecoveryPairingAuthorizationRule resource) {
//        StringBuilder info = new StringBuilder();
//        info.append("DisasterRecoveryPairing auth rule: ").append(resource.name());
//        List<String> rightsStr = new ArrayList<>();
//        for (AccessRights rights : resource.rights()) {
//            rightsStr.add(rights.toString());
//        }
//        info.append("\n\tRights: ").append(rightsStr);
//        System.out.println(info.toString());
//    }
//
//    /**
//     * Print event hub namespace recovery pairing auth rule key.
//     *
//     * @param resource event hub namespace disaster recovery pairing auth rule key
//     */
//    public static void print(DisasterRecoveryPairingAuthorizationKey resource) {
//        StringBuilder info = new StringBuilder();
//        info.append("DisasterRecoveryPairing auth key: ")
//                .append("\n\t Alias primary connection string: ").append(resource.aliasPrimaryConnectionString())
//                .append("\n\t Alias secondary connection string: ").append(resource.aliasSecondaryConnectionString())
//                .append("\n\t Primary key: ").append(resource.primaryKey())
//                .append("\n\t Secondary key: ").append(resource.secondaryKey())
//                .append("\n\t Primary connection string: ").append(resource.primaryConnectionString())
//                .append("\n\t Secondary connection string: ").append(resource.secondaryConnectionString());
//        System.out.println(info.toString());
//    }
//
//    /**
//     * Print event hub consumer group.
//     *
//     * @param resource event hub consumer group
//     */
//    public static void print(EventHubConsumerGroup resource) {
//        StringBuilder info = new StringBuilder();
//        info.append("Event hub consumer group: ").append(resource.id())
//                .append("\n\tName: ").append(resource.name())
//                .append("\n\tNamespace resource group: ").append(resource.namespaceResourceGroupName())
//                .append("\n\tNamespace: ").append(resource.namespaceName())
//                .append("\n\tEvent hub name: ").append(resource.eventHubName())
//                .append("\n\tUser metadata: ").append(resource.userMetadata());
//        System.out.println(info.toString());
//    }


    /**
     * Print Diagnostic Setting.
     *
     * @param resource Diagnostic Setting instance
     */
    public static void print(DiagnosticSetting resource) {
        StringBuilder info = new StringBuilder("Diagnostic Setting: ")
                .append("\n\tId: ").append(resource.id())
                .append("\n\tAssociated resource Id: ").append(resource.resourceId())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tStorage Account Id: ").append(resource.storageAccountId())
                .append("\n\tEventHub Namespace Autorization Rule Id: ").append(resource.eventHubAuthorizationRuleId())
                .append("\n\tEventHub name: ").append(resource.eventHubName())
                .append("\n\tLog Analytics workspace Id: ").append(resource.workspaceId());
        if (resource.logs() != null && !resource.logs().isEmpty()) {
            info.append("\n\tLog Settings: ");
            for (LogSettings ls : resource.logs()) {
                info.append("\n\t\tCategory: ").append(ls.category());
                info.append("\n\t\tRetention policy: ");
                if (ls.retentionPolicy() != null) {
                    info.append(ls.retentionPolicy().days() + " days");
                } else {
                    info.append("NONE");
                }
            }
        }
        if (resource.metrics() != null && !resource.metrics().isEmpty()) {
            info.append("\n\tMetric Settings: ");
            for (MetricSettings ls : resource.metrics()) {
                info.append("\n\t\tCategory: ").append(ls.category());
                info.append("\n\t\tTimegrain: ").append(ls.timeGrain());
                info.append("\n\t\tRetention policy: ");
                if (ls.retentionPolicy() != null) {
                    info.append(ls.retentionPolicy().days() + " days");
                } else {
                    info.append("NONE");
                }
            }
        }
        System.out.println(info.toString());
    }

    /**
     * Print Action group settings.
     *
     * @param actionGroup action group instance
     */
    public static void print(ActionGroup actionGroup) {
        StringBuilder info = new StringBuilder("Action Group: ")
                .append("\n\tId: ").append(actionGroup.id())
                .append("\n\tName: ").append(actionGroup.name())
                .append("\n\tShort Name: ").append(actionGroup.shortName());

        if (actionGroup.emailReceivers() != null && !actionGroup.emailReceivers().isEmpty()) {
            info.append("\n\tEmail receivers: ");
            for (EmailReceiver er : actionGroup.emailReceivers()) {
                info.append("\n\t\tName: ").append(er.name());
                info.append("\n\t\tEMail: ").append(er.emailAddress());
                info.append("\n\t\tStatus: ").append(er.status());
                info.append("\n\t\t===");
            }
        }

        if (actionGroup.smsReceivers() != null && !actionGroup.smsReceivers().isEmpty()) {
            info.append("\n\tSMS text message receivers: ");
            for (SmsReceiver er : actionGroup.smsReceivers()) {
                info.append("\n\t\tName: ").append(er.name());
                info.append("\n\t\tPhone: ").append(er.countryCode() + er.phoneNumber());
                info.append("\n\t\tStatus: ").append(er.status());
                info.append("\n\t\t===");
            }
        }

        if (actionGroup.webhookReceivers() != null && !actionGroup.webhookReceivers().isEmpty()) {
            info.append("\n\tWebhook receivers: ");
            for (WebhookReceiver er : actionGroup.webhookReceivers()) {
                info.append("\n\t\tName: ").append(er.name());
                info.append("\n\t\tURI: ").append(er.serviceUri());
                info.append("\n\t\t===");
            }
        }

        if (actionGroup.pushNotificationReceivers() != null && !actionGroup.pushNotificationReceivers().isEmpty()) {
            info.append("\n\tApp Push Notification receivers: ");
            for (AzureAppPushReceiver er : actionGroup.pushNotificationReceivers()) {
                info.append("\n\t\tName: ").append(er.name());
                info.append("\n\t\tEmail: ").append(er.emailAddress());
                info.append("\n\t\t===");
            }
        }

        if (actionGroup.voiceReceivers() != null && !actionGroup.voiceReceivers().isEmpty()) {
            info.append("\n\tVoice Message receivers: ");
            for (VoiceReceiver er : actionGroup.voiceReceivers()) {
                info.append("\n\t\tName: ").append(er.name());
                info.append("\n\t\tPhone: ").append(er.countryCode() + er.phoneNumber());
                info.append("\n\t\t===");
            }
        }

        if (actionGroup.automationRunbookReceivers() != null && !actionGroup.automationRunbookReceivers().isEmpty()) {
            info.append("\n\tAutomation Runbook receivers: ");
            for (AutomationRunbookReceiver er : actionGroup.automationRunbookReceivers()) {
                info.append("\n\t\tName: ").append(er.name());
                info.append("\n\t\tRunbook Name: ").append(er.runbookName());
                info.append("\n\t\tAccount Id: ").append(er.automationAccountId());
                info.append("\n\t\tIs Global: ").append(er.isGlobalRunbook());
                info.append("\n\t\tService URI: ").append(er.serviceUri());
                info.append("\n\t\tWebhook resource Id: ").append(er.webhookResourceId());
                info.append("\n\t\t===");
            }
        }

        if (actionGroup.azureFunctionReceivers() != null && !actionGroup.azureFunctionReceivers().isEmpty()) {
            info.append("\n\tAzure Functions receivers: ");
            for (AzureFunctionReceiver er : actionGroup.azureFunctionReceivers()) {
                info.append("\n\t\tName: ").append(er.name());
                info.append("\n\t\tFunction Name: ").append(er.functionName());
                info.append("\n\t\tFunction App Resource Id: ").append(er.functionAppResourceId());
                info.append("\n\t\tFunction Trigger URI: ").append(er.httpTriggerUrl());
                info.append("\n\t\t===");
            }
        }

        if (actionGroup.logicAppReceivers() != null && !actionGroup.logicAppReceivers().isEmpty()) {
            info.append("\n\tLogic App receivers: ");
            for (LogicAppReceiver er : actionGroup.logicAppReceivers()) {
                info.append("\n\t\tName: ").append(er.name());
                info.append("\n\t\tResource Id: ").append(er.resourceId());
                info.append("\n\t\tCallback URL: ").append(er.callbackUrl());
                info.append("\n\t\t===");
            }
        }

        if (actionGroup.itsmReceivers() != null && !actionGroup.itsmReceivers().isEmpty()) {
            info.append("\n\tITSM receivers: ");
            for (ItsmReceiver er : actionGroup.itsmReceivers()) {
                info.append("\n\t\tName: ").append(er.name());
                info.append("\n\t\tWorkspace Id: ").append(er.workspaceId());
                info.append("\n\t\tConnection Id: ").append(er.connectionId());
                info.append("\n\t\tRegion: ").append(er.region());
                info.append("\n\t\tTicket Configuration: ").append(er.ticketConfiguration());
                info.append("\n\t\t===");
            }
        }
        System.out.println(info.toString());
    }

    /**
     * Print activity log alert settings.
     *
     * @param activityLogAlert activity log instance
     */
    public static void print(ActivityLogAlert activityLogAlert) {

        StringBuilder info = new StringBuilder("Activity Log Alert: ")
                .append("\n\tId: ").append(activityLogAlert.id())
                .append("\n\tName: ").append(activityLogAlert.name())
                .append("\n\tDescription: ").append(activityLogAlert.description())
                .append("\n\tIs Enabled: ").append(activityLogAlert.enabled());

        if (activityLogAlert.scopes() != null && !activityLogAlert.scopes().isEmpty()) {
            info.append("\n\tScopes: ");
            for (String er : activityLogAlert.scopes()) {
                info.append("\n\t\tId: ").append(er);
            }
        }

        if (activityLogAlert.actionGroupIds() != null && !activityLogAlert.actionGroupIds().isEmpty()) {
            info.append("\n\tAction Groups: ");
            for (String er : activityLogAlert.actionGroupIds()) {
                info.append("\n\t\tAction Group Id: ").append(er);
            }
        }

        if (activityLogAlert.equalsConditions() != null && !activityLogAlert.equalsConditions().isEmpty()) {
            info.append("\n\tAlert conditions (when all of is true): ");
            for (Map.Entry<String, String> er : activityLogAlert.equalsConditions().entrySet()) {
                info.append("\n\t\t'").append(er.getKey()).append("' equals '").append(er.getValue()).append("'");
            }
        }
        System.out.println(info.toString());
    }

    /**
     * Print metric alert settings.
     *
     * @param metricAlert metric alert instance
     */
    public static void print(MetricAlert metricAlert) {

        StringBuilder info = new StringBuilder("Metric Alert: ")
                .append("\n\tId: ").append(metricAlert.id())
                .append("\n\tName: ").append(metricAlert.name())
                .append("\n\tDescription: ").append(metricAlert.description())
                .append("\n\tIs Enabled: ").append(metricAlert.enabled())
                .append("\n\tIs Auto Mitigated: ").append(metricAlert.autoMitigate())
                .append("\n\tSeverity: ").append(metricAlert.severity())
                .append("\n\tWindow Size: ").append(metricAlert.windowSize())
                .append("\n\tEvaluation Frequency: ").append(metricAlert.evaluationFrequency());

        if (metricAlert.scopes() != null && !metricAlert.scopes().isEmpty()) {
            info.append("\n\tScopes: ");
            for (String er : metricAlert.scopes()) {
                info.append("\n\t\tId: ").append(er);
            }
        }

        if (metricAlert.actionGroupIds() != null && !metricAlert.actionGroupIds().isEmpty()) {
            info.append("\n\tAction Groups: ");
            for (String er : metricAlert.actionGroupIds()) {
                info.append("\n\t\tAction Group Id: ").append(er);
            }
        }

        if (metricAlert.alertCriterias() != null && !metricAlert.alertCriterias().isEmpty()) {
            info.append("\n\tAlert conditions (when all of is true): ");
            for (Map.Entry<String, MetricAlertCondition> er : metricAlert.alertCriterias().entrySet()) {
                MetricAlertCondition alertCondition = er.getValue();
                info.append("\n\t\tCondition name: ").append(er.getKey())
                        .append("\n\t\tSignal name: ").append(alertCondition.metricName())
                        .append("\n\t\tMetric Namespace: ").append(alertCondition.metricNamespace())
                        .append("\n\t\tOperator: ").append(alertCondition.condition())
                        .append("\n\t\tThreshold: ").append(alertCondition.threshold())
                        .append("\n\t\tTime Aggregation: ").append(alertCondition.timeAggregation());
                if (alertCondition.dimensions() != null && !alertCondition.dimensions().isEmpty()) {
                    for (MetricDimension dimon : alertCondition.dimensions()) {
                        info.append("\n\t\tDimension Filter: ").append("Name [").append(dimon.name()).append("] operator [Include] values[");
                        for (String vals : dimon.values()) {
                            info.append(vals).append(", ");
                        }
                        info.append("]");
                    }
                }
            }
        }
        System.out.println(info.toString());
    }

//    /**
//     * Print spring service settings.
//     *
//     * @param springService spring service instance
//     */
//    public static void print(SpringService springService) {
//        StringBuilder info = new StringBuilder("Spring Service: ")
//            .append("\n\tId: ").append(springService.id())
//            .append("\n\tName: ").append(springService.name())
//            .append("\n\tResource Group: ").append(springService.resourceGroupName())
//            .append("\n\tRegion: ").append(springService.region())
//            .append("\n\tTags: ").append(springService.tags());
//
//        ConfigServerProperties serverProperties = springService.getServerProperties();
//        if (serverProperties != null && serverProperties.provisioningState() != null
//            && serverProperties.provisioningState().equals(ConfigServerState.SUCCEEDED) && serverProperties.configServer() != null) {
//            info.append("\n\tProperties: ");
//            if (serverProperties.configServer().gitProperty() != null) {
//                info.append("\n\t\tGit: ").append(serverProperties.configServer().gitProperty().uri());
//            }
//        }
//
//        if (springService.sku() != null) {
//            info.append("\n\tSku: ")
//                .append("\n\t\tName: ").append(springService.sku().name())
//                .append("\n\t\tTier: ").append(springService.sku().tier())
//                .append("\n\t\tCapacity: ").append(springService.sku().capacity());
//        }
//
//        MonitoringSettingProperties monitoringSettingProperties = springService.getMonitoringSetting();
//        if (monitoringSettingProperties != null && monitoringSettingProperties.provisioningState() != null
//            && monitoringSettingProperties.provisioningState().equals(MonitoringSettingState.SUCCEEDED)) {
//            info.append("\n\tTrace: ")
//                .append("\n\t\tEnabled: ").append(monitoringSettingProperties.traceEnabled())
//                .append("\n\t\tApp Insight Instrumentation Key: ").append(monitoringSettingProperties.appInsightsInstrumentationKey());
//        }
//
//        System.out.println(info.toString());
//    }
//
//    /**
//     * Print spring app settings.
//     *
//     * @param springApp spring app instance
//     */
//    public static void print(SpringApp springApp) {
//        StringBuilder info = new StringBuilder("Spring Service: ")
//            .append("\n\tId: ").append(springApp.id())
//            .append("\n\tName: ").append(springApp.name())
//            .append("\n\tCreated Time: ").append(springApp.createdTime())
//            .append("\n\tPublic Endpoint: ").append(springApp.isPublic())
//            .append("\n\tUrl: ").append(springApp.url())
//            .append("\n\tHttps Only: ").append(springApp.isHttpsOnly())
//            .append("\n\tFully Qualified Domain Name: ").append(springApp.fqdn())
//            .append("\n\tActive Deployment Name: ").append(springApp.activeDeploymentName());
//
//        if (springApp.temporaryDisk() != null) {
//            info.append("\n\tTemporary Disk:")
//                .append("\n\t\tSize In GB: ").append(springApp.temporaryDisk().sizeInGB())
//                .append("\n\t\tMount Path: ").append(springApp.temporaryDisk().mountPath());
//        }
//
//        if (springApp.persistentDisk() != null) {
//            info.append("\n\tPersistent Disk:")
//                .append("\n\t\tSize In GB: ").append(springApp.persistentDisk().sizeInGB())
//                .append("\n\t\tMount Path: ").append(springApp.persistentDisk().mountPath());
//        }
//
//        if (springApp.identity() != null) {
//            info.append("\n\tIdentity:")
//                .append("\n\t\tType: ").append(springApp.identity().type())
//                .append("\n\t\tPrincipal Id: ").append(springApp.identity().principalId())
//                .append("\n\t\tTenant Id: ").append(springApp.identity().tenantId());
//        }
//
//        System.out.println(info.toString());
//    }

    /**
     * Sends a GET request to target URL.
     * <p>
     * Retry logic tuned for AppService.
     * The method does not handle 301 redirect.
     *
     * @param urlString the target URL.
     * @return Content of the HTTP response.
     */
    public static String sendGetRequest(String urlString) {
        ClientLogger logger = new ClientLogger(Utils.class);

        try {
            Mono<Response<Flux<ByteBuffer>>> response =
                HTTP_CLIENT.getString(getHost(urlString), getPathAndQuery(urlString))
                    .retryWhen(Retry
                        .fixedDelay(5, Duration.ofSeconds(30))
                        .filter(t -> {
                            boolean retry = false;
                            if (t instanceof TimeoutException) {
                                retry = true;
                            } else if (t instanceof HttpResponseException
                                && ((HttpResponseException) t).getResponse().getStatusCode() == 503) {
                                retry = true;
                            }

                            if (retry) {
                                logger.info("retry GET request to {}", urlString);
                            }
                            return retry;
                        }));
            Response<String> ret = stringResponse(response).block();
            return ret == null ? null : ret.getValue();
        } catch (MalformedURLException e) {
            logger.logThrowableAsError(e);
            return null;
        }
    }

    /**
     * Sends a POST request to target URL.
     * <p>
     * Retry logic tuned for AppService.
     *
     * @param urlString the target URL.
     * @param body the request body.
     * @return Content of the HTTP response.
     * */
    public static String sendPostRequest(String urlString, String body) {
        ClientLogger logger = new ClientLogger(Utils.class);

        try {
            Mono<Response<String>> response =
                stringResponse(HTTP_CLIENT.postString(getHost(urlString), getPathAndQuery(urlString), body))
                    .retryWhen(Retry
                        .fixedDelay(5, Duration.ofSeconds(30))
                        .filter(t -> {
                            boolean retry = false;
                            if (t instanceof TimeoutException) {
                                retry = true;
                            }

                            if (retry) {
                                logger.info("retry POST request to {}", urlString);
                            }
                            return retry;
                        }));
            Response<String> ret = response.block();
            return ret == null ? null : ret.getValue();
        } catch (Exception e) {
            logger.logThrowableAsError(e);
            return null;
        }
    }

    private static Mono<Response<String>> stringResponse(Mono<Response<Flux<ByteBuffer>>> responseMono) {
        return responseMono.flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getValue())
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .map(str -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), str)));
    }

    private static String getHost(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        String protocol = url.getProtocol();
        String host = url.getAuthority();
        return protocol + "://" + host;
    }

    private static String getPathAndQuery(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        String path = url.getPath();
        String query = url.getQuery();
        if (query != null && !query.isEmpty()) {
            path = path + "?" + query;
        }
        return path;
    }

    private static final WebAppTestClient HTTP_CLIENT = RestProxy.create(
            WebAppTestClient.class,
            new HttpPipelineBuilder()
                    .policies(
                        new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC)),
                        new RetryPolicy("Retry-After", ChronoUnit.SECONDS))
                    .build());

    @Host("{$host}")
    @ServiceInterface(name = "WebAppTestClient")
    private interface WebAppTestClient {
        @Get("{path}")
        @ExpectedResponses({200, 400, 404})
        Mono<Response<Flux<ByteBuffer>>> getString(@HostParam("$host") String host, @PathParam(value = "path", encoded = true) String path);

        @Post("{path}")
        @ExpectedResponses({200, 400, 404})
        Mono<Response<Flux<ByteBuffer>>> postString(@HostParam("$host") String host, @PathParam(value = "path", encoded = true) String path, @BodyParam("text/plain") String body);
    }

    public static <T> int getSize(Iterable<T> iterable) {
        int res = 0;
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        return res;
    }
}
