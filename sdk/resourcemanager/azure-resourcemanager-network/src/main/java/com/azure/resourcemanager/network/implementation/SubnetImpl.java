// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.Region;
import com.azure.core.management.SubResource;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.fluent.models.IpAddressAvailabilityResultInner;
import com.azure.resourcemanager.network.fluent.models.IpConfigurationInner;
import com.azure.resourcemanager.network.fluent.models.NetworkSecurityGroupInner;
import com.azure.resourcemanager.network.fluent.models.RouteTableInner;
import com.azure.resourcemanager.network.fluent.models.SubnetInner;
import com.azure.resourcemanager.network.models.Delegation;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.RouteTable;
import com.azure.resourcemanager.network.models.ServiceEndpointPropertiesFormat;
import com.azure.resourcemanager.network.models.ServiceEndpointType;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.models.VirtualNetworkPrivateEndpointNetworkPolicies;
import com.azure.resourcemanager.network.models.VirtualNetworkPrivateLinkServiceNetworkPolicies;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Implementation for Subnet and its create and update interfaces. */
class SubnetImpl extends ChildResourceImpl<SubnetInner, NetworkImpl, Network>
    implements Subnet, Subnet.Definition<Network.DefinitionStages.WithCreateAndSubnet>,
    Subnet.UpdateDefinition<Network.Update>, Subnet.Update {

    private static final ClientLogger LOGGER = new ClientLogger(SubnetImpl.class);

    SubnetImpl(SubnetInner inner, NetworkImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public int networkInterfaceIPConfigurationCount() {
        List<IpConfigurationInner> ipConfigRefs = this.innerModel().ipConfigurations();
        if (ipConfigRefs != null) {
            return ipConfigRefs.size();
        } else {
            return 0;
        }
    }

    @Override
    public String addressPrefix() {
        return this.innerModel().addressPrefix();
    }

    @Override
    public List<String> addressPrefixes() {
        if (CoreUtils.isNullOrEmpty(this.innerModel().addressPrefixes())) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.innerModel().addressPrefixes());
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String networkSecurityGroupId() {
        return (this.innerModel().networkSecurityGroup() != null)
            ? this.innerModel().networkSecurityGroup().id()
            : null;
    }

    @Override
    public String routeTableId() {
        return (this.innerModel().routeTable() != null) ? this.innerModel().routeTable().id() : null;
    }

    @Override
    public Map<ServiceEndpointType, List<Region>> servicesWithAccess() {
        Map<ServiceEndpointType, List<Region>> services = new HashMap<>();
        if (this.innerModel().serviceEndpoints() != null) {
            for (ServiceEndpointPropertiesFormat endpoint : this.innerModel().serviceEndpoints()) {
                ServiceEndpointType serviceEndpointType = ServiceEndpointType.fromString(endpoint.service());
                if (!services.containsKey(serviceEndpointType)) {
                    services.put(serviceEndpointType, new ArrayList<Region>());
                }
                if (endpoint.locations() != null) {
                    List<Region> regions = new ArrayList<>();
                    for (String location : endpoint.locations()) {
                        regions.add(Region.fromName(location));
                    }
                    services.get(serviceEndpointType).addAll(regions);
                }
            }
        }
        return services;
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String natGatewayId() {
        SubResource natGateway = this.innerModel().natGateway();
        if (natGateway == null) {
            return null;
        }
        return natGateway.id();
    }

    // Fluent setters

    @Override
    public SubnetImpl withoutNetworkSecurityGroup() {
        this.innerModel().withNetworkSecurityGroup(null);
        return this;
    }

    @Override
    public SubnetImpl withExistingNetworkSecurityGroup(NetworkSecurityGroup nsg) {
        return withExistingNetworkSecurityGroup(nsg.id());
    }

    @Override
    public SubnetImpl withExistingNetworkSecurityGroup(String resourceId) {
        // Workaround for REST API's expectation of an object rather than string ID - should be fixed in Swagger specs
        // or REST
        NetworkSecurityGroupInner reference = new NetworkSecurityGroupInner().withId(resourceId);
        this.innerModel().withNetworkSecurityGroup(reference);
        return this;
    }

    @Override
    public SubnetImpl withExistingRouteTable(String resourceId) {
        RouteTableInner reference = new RouteTableInner().withId(resourceId);
        this.innerModel().withRouteTable(reference);
        return this;
    }

    @Override
    public SubnetImpl withExistingRouteTable(RouteTable routeTable) {
        return this.withExistingRouteTable(routeTable.id());
    }

    @Override
    public Update withoutRouteTable() {
        this.innerModel().withRouteTable(null);
        return this;
    }

    @Override
    public SubnetImpl withAddressPrefix(String cidr) {
        this.innerModel().withAddressPrefix(cidr);
        this.innerModel().withAddressPrefixes(null);
        return this;
    }

    @Override
    public SubnetImpl withAddressPrefixes(Collection<String> addressPrefixes) {
        Objects.requireNonNull(addressPrefixes);
        this.innerModel().withAddressPrefixes(new ArrayList<>(addressPrefixes));
        this.innerModel().withAddressPrefix(null);
        return this;
    }

    @Override
    public SubnetImpl withAccessFromService(ServiceEndpointType service) {
        if (this.innerModel().serviceEndpoints() == null) {
            this.innerModel().withServiceEndpoints(new ArrayList<ServiceEndpointPropertiesFormat>());
        }
        boolean found = false;
        for (ServiceEndpointPropertiesFormat endpoint : this.innerModel().serviceEndpoints()) {
            if (endpoint.service().equalsIgnoreCase(service.toString())) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.innerModel()
                .serviceEndpoints()
                .add(new ServiceEndpointPropertiesFormat().withService(service.toString())
                    .withLocations(new ArrayList<String>()));
        }
        return this;
    }

    @Override
    public Update withoutAccessFromService(ServiceEndpointType service) {
        if (this.innerModel().serviceEndpoints() != null) {
            int foundIndex = -1;
            int i = 0;
            for (ServiceEndpointPropertiesFormat endpoint : this.innerModel().serviceEndpoints()) {
                if (endpoint.service().equalsIgnoreCase(service.toString())) {
                    foundIndex = i;
                    break;
                }
                i++;
            }
            if (foundIndex != -1) {
                this.innerModel().serviceEndpoints().remove(foundIndex);
            }
        }
        return this;
    }

    // Verbs

    @Override
    public NetworkImpl attach() {
        return this.parent().withSubnet(this);
    }

    @Override
    public RouteTable getRouteTable() {
        return (this.routeTableId() != null)
            ? this.parent().manager().routeTables().getById(this.routeTableId())
            : null;
    }

    @Override
    public NetworkSecurityGroup getNetworkSecurityGroup() {
        String nsgId = this.networkSecurityGroupId();
        return (nsgId != null) ? this.parent().manager().networkSecurityGroups().getById(nsgId) : null;
    }

    @Override
    public Collection<NicIpConfiguration> listNetworkInterfaceIPConfigurations() {
        Collection<NicIpConfiguration> ipConfigs = new ArrayList<>();
        Map<String, NetworkInterface> nics = new TreeMap<>(Comparator.comparing(key -> key.toLowerCase(Locale.ROOT)));
        List<IpConfigurationInner> ipConfigRefs = this.innerModel().ipConfigurations();
        if (ipConfigRefs == null) {
            return ipConfigs;
        }

        for (IpConfigurationInner ipConfigRef : ipConfigRefs) {
            String nicID = ResourceUtils.parentResourceIdFromResourceId(ipConfigRef.id());
            String ipConfigName = ResourceUtils.nameFromResourceId(ipConfigRef.id());
            // Check if NIC already cached
            NetworkInterface nic = nics.get(nicID);
            if (nic == null) {
                //  NIC not previously found, so ask Azure for it
                String resourceType = ResourceUtils.resourceTypeFromResourceId(nicID);
                if ("networkInterfaces".equalsIgnoreCase(resourceType)) {
                    // skip other resource types like "bastionHosts"
                    try {
                        nic = this.parent().manager().networkInterfaces().getById(nicID);
                    } catch (ManagementException e) {
                        if (e.getResponse().getStatusCode() == 404) {
                            // NIC not found, ignore this ipConfigRef
                            LOGGER.warning("Network interface not found '{}'", nicID);
                        } else {
                            throw LOGGER.logExceptionAsError(e);
                        }
                    }
                }
            }

            if (nic == null) {
                // NIC doesn't exist so ignore this bad reference
                continue;
            }

            // Cache the NIC
            nics.put(nic.id(), nic);

            // Get the IP config
            NicIpConfiguration ipConfig = nic.ipConfigurations().get(ipConfigName);
            if (ipConfig == null) {
                // IP config not found, so ignore this bad reference
                continue;
            }

            ipConfigs.add(ipConfig);
        }

        return Collections.unmodifiableCollection(ipConfigs);
    }

    @Override
    public Set<String> listAvailablePrivateIPAddresses() {
        Set<String> result = Collections.emptySet();
        if (!CoreUtils.isNullOrEmpty(this.addressPrefixes())) {
            for (String cidr : this.addressPrefixes()) {
                // According to our test, when doing "checkIpAddressAvailability", backend knows about which subnet the "startIp"
                // belongs to, thus we only need to use one of the address prefixes.
                Set<String> availableIps = listAvailablePrivateIPAddresses(cidr);
                if (!CoreUtils.isNullOrEmpty(availableIps)) {
                    result = availableIps;
                    break;
                }
            }
        } else {
            result = listAvailablePrivateIPAddresses(this.addressPrefix());
        }
        return result;
    }

    @Override
    public SubnetImpl withDelegation(String serviceName) {
        if (innerModel().delegations() == null) {
            innerModel().withDelegations(new ArrayList<>());
        }
        innerModel().delegations().add(new Delegation().withName(serviceName).withServiceName(serviceName));
        return this;
    }

    @Override
    public SubnetImpl withoutDelegation(String serviceName) {
        if (innerModel().delegations() != null) {
            for (int i = 0; i < innerModel().delegations().size();) {
                if (serviceName.equalsIgnoreCase(innerModel().delegations().get(i).serviceName())) {
                    innerModel().delegations().remove(i);
                } else {
                    ++i;
                }
            }
        }
        return this;
    }

    @Override
    public SubnetImpl enableNetworkPoliciesOnPrivateEndpoint() {
        innerModel().withPrivateEndpointNetworkPolicies(VirtualNetworkPrivateEndpointNetworkPolicies.ENABLED);
        return this;
    }

    @Override
    public SubnetImpl disableNetworkPoliciesOnPrivateEndpoint() {
        innerModel().withPrivateEndpointNetworkPolicies(VirtualNetworkPrivateEndpointNetworkPolicies.DISABLED);
        return this;
    }

    @Override
    public SubnetImpl enableNetworkPoliciesOnPrivateLinkService() {
        innerModel().withPrivateLinkServiceNetworkPolicies(VirtualNetworkPrivateLinkServiceNetworkPolicies.ENABLED);
        return this;
    }

    @Override
    public SubnetImpl disableNetworkPoliciesOnPrivateLinkService() {
        innerModel().withPrivateLinkServiceNetworkPolicies(VirtualNetworkPrivateLinkServiceNetworkPolicies.DISABLED);
        return this;
    }

    @Override
    public SubnetImpl withExistingNatGateway(String resourceId) {
        if (resourceId == null) {
            this.innerModel().withNatGateway(null);
        } else {
            this.innerModel().withNatGateway(new SubResource().withId(resourceId));
        }
        return this;
    }

    private Set<String> listAvailablePrivateIPAddresses(String cidr) {
        Set<String> ipAddresses = new TreeSet<>();
        if (cidr == null) {
            return ipAddresses;
        }
        String takenIPAddress = cidr.split("/")[0];

        IpAddressAvailabilityResultInner result = this.parent()
            .manager()
            .serviceClient()
            .getVirtualNetworks()
            .checkIpAddressAvailability(this.parent().resourceGroupName(), this.parent().name(), takenIPAddress);
        if (result == null
            // there's a case when user doesn't have the permission to query, result.availableIpAddresses() will be null
            || result.availableIpAddresses() == null) {
            return ipAddresses;
        }

        ipAddresses.addAll(result.availableIpAddresses());
        return ipAddresses;
    }
}
