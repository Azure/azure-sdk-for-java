// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.Delegation;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.RouteTable;
import com.azure.resourcemanager.network.models.ServiceEndpointPropertiesFormat;
import com.azure.resourcemanager.network.models.ServiceEndpointType;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.fluent.inner.IpAddressAvailabilityResultInner;
import com.azure.resourcemanager.network.fluent.inner.IpConfigurationInner;
import com.azure.resourcemanager.network.fluent.inner.NetworkSecurityGroupInner;
import com.azure.resourcemanager.network.fluent.inner.RouteTableInner;
import com.azure.resourcemanager.network.fluent.inner.SubnetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Implementation for Subnet and its create and update interfaces. */
class SubnetImpl extends ChildResourceImpl<SubnetInner, NetworkImpl, Network>
    implements Subnet,
        Subnet.Definition<Network.DefinitionStages.WithCreateAndSubnet>,
        Subnet.UpdateDefinition<Network.Update>,
        Subnet.Update {

    SubnetImpl(SubnetInner inner, NetworkImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public int networkInterfaceIPConfigurationCount() {
        List<IpConfigurationInner> ipConfigRefs = this.inner().ipConfigurations();
        if (ipConfigRefs != null) {
            return ipConfigRefs.size();
        } else {
            return 0;
        }
    }

    @Override
    public String addressPrefix() {
        return this.inner().addressPrefix();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String networkSecurityGroupId() {
        return (this.inner().networkSecurityGroup() != null) ? this.inner().networkSecurityGroup().id() : null;
    }

    @Override
    public String routeTableId() {
        return (this.inner().routeTable() != null) ? this.inner().routeTable().id() : null;
    }

    @Override
    public Map<ServiceEndpointType, List<Region>> servicesWithAccess() {
        Map<ServiceEndpointType, List<Region>> services = new HashMap<>();
        if (this.inner().serviceEndpoints() != null) {
            for (ServiceEndpointPropertiesFormat endpoint : this.inner().serviceEndpoints()) {
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

    // Fluent setters

    @Override
    public SubnetImpl withoutNetworkSecurityGroup() {
        this.inner().withNetworkSecurityGroup(null);
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
        this.inner().withNetworkSecurityGroup(reference);
        return this;
    }

    @Override
    public SubnetImpl withExistingRouteTable(String resourceId) {
        RouteTableInner reference = new RouteTableInner().withId(resourceId);
        this.inner().withRouteTable(reference);
        return this;
    }

    @Override
    public SubnetImpl withExistingRouteTable(RouteTable routeTable) {
        return this.withExistingRouteTable(routeTable.id());
    }

    @Override
    public Update withoutRouteTable() {
        this.inner().withRouteTable(null);
        return this;
    }

    @Override
    public SubnetImpl withAddressPrefix(String cidr) {
        this.inner().withAddressPrefix(cidr);
        return this;
    }

    @Override
    public SubnetImpl withAccessFromService(ServiceEndpointType service) {
        if (this.inner().serviceEndpoints() == null) {
            this.inner().withServiceEndpoints(new ArrayList<ServiceEndpointPropertiesFormat>());
        }
        boolean found = false;
        for (ServiceEndpointPropertiesFormat endpoint : this.inner().serviceEndpoints()) {
            if (endpoint.service().equalsIgnoreCase(service.toString())) {
                found = true;
                break;
            }
        }
        if (!found) {
            this
                .inner()
                .serviceEndpoints()
                .add(
                    new ServiceEndpointPropertiesFormat()
                        .withService(service.toString())
                        .withLocations(new ArrayList<String>()));
        }
        return this;
    }

    @Override
    public Update withoutAccessFromService(ServiceEndpointType service) {
        if (this.inner().serviceEndpoints() != null) {
            int foundIndex = -1;
            int i = 0;
            for (ServiceEndpointPropertiesFormat endpoint : this.inner().serviceEndpoints()) {
                if (endpoint.service().equalsIgnoreCase(service.toString())) {
                    foundIndex = i;
                    break;
                }
                i++;
            }
            if (foundIndex != -1) {
                this.inner().serviceEndpoints().remove(foundIndex);
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
    public Set<NicIpConfiguration> getNetworkInterfaceIPConfigurations() {
        return Collections.unmodifiableSet(new TreeSet<NicIpConfiguration>(listNetworkInterfaceIPConfigurations()));
    }

    @Override
    public Collection<NicIpConfiguration> listNetworkInterfaceIPConfigurations() {
        Collection<NicIpConfiguration> ipConfigs = new ArrayList<>();
        Map<String, NetworkInterface> nics = new TreeMap<>();
        List<IpConfigurationInner> ipConfigRefs = this.inner().ipConfigurations();
        if (ipConfigRefs == null) {
            return ipConfigs;
        }

        for (IpConfigurationInner ipConfigRef : ipConfigRefs) {
            String nicID = ResourceUtils.parentResourceIdFromResourceId(ipConfigRef.id());
            String ipConfigName = ResourceUtils.nameFromResourceId(ipConfigRef.id());
            // Check if NIC already cached
            NetworkInterface nic = nics.get(nicID.toLowerCase(Locale.ROOT));
            if (nic == null) {
                //  NIC not previously found, so ask Azure for it
                nic = this.parent().manager().networkInterfaces().getById(nicID);
            }

            if (nic == null) {
                // NIC doesn't exist so ignore this bad reference
                continue;
            }

            // Cache the NIC
            nics.put(nic.id().toLowerCase(Locale.ROOT), nic);

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
        Set<String> ipAddresses = new TreeSet<>();

        String cidr = this.addressPrefix();
        if (cidr == null) {
            return ipAddresses; // Should never happen, but just in case
        }
        String takenIPAddress = cidr.split("/")[0];

        IpAddressAvailabilityResultInner result =
            this
                .parent()
                .manager()
                .networks()
                .inner()
                .checkIpAddressAvailability(this.parent().resourceGroupName(), this.parent().name(), takenIPAddress);
        if (result == null) {
            return ipAddresses;
        }

        ipAddresses.addAll(result.availableIpAddresses());
        return ipAddresses;
    }

    @Override
    public SubnetImpl withDelegation(String serviceName) {
        if (inner().delegations() == null) {
            inner().withDelegations(new ArrayList<>());
        }
        inner().delegations().add(new Delegation().withName(serviceName).withServiceName(serviceName));
        return this;
    }

    @Override
    public SubnetImpl withoutDelegation(String serviceName) {
        if (inner().delegations() != null) {
            for (int i = 0; i < inner().delegations().size();) {
                if (serviceName.equalsIgnoreCase(inner().delegations().get(i).serviceName())) {
                    inner().delegations().remove(i);
                } else {
                    ++i;
                }
            }
        }
        return this;
    }
}
