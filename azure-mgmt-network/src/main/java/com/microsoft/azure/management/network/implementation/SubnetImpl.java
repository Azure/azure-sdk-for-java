/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for Subnet and its create and update interfaces.
 */
@LangDefinition
class SubnetImpl
    extends ChildResourceImpl<SubnetInner, NetworkImpl, Network>
    implements
        Subnet,
        Subnet.Definition<Network.DefinitionStages.WithCreateAndSubnet>,
        Subnet.UpdateDefinition<Network.Update>,
        Subnet.Update {

    SubnetImpl(SubnetInner inner, NetworkImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public int networkInterfaceIPConfigurationCount() {
        List<IPConfigurationInner> ipConfigRefs = this.inner().ipConfigurations();
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

    // Fluent setters

    @Override
    public SubnetImpl withExistingNetworkSecurityGroup(NetworkSecurityGroup nsg) {
        return withExistingNetworkSecurityGroup(nsg.id());
    }

    @Override
    public SubnetImpl withExistingNetworkSecurityGroup(String resourceId) {
        // Workaround for REST API's expectation of an object rather than string ID - should be fixed in Swagger specs or REST
        SubResource reference = new SubResource().withId(resourceId);
        this.inner().withNetworkSecurityGroup(reference);
        return this;
    }

    @Override
    public SubnetImpl withExistingRouteTable(String resourceId) {
        SubResource reference = new SubResource().withId(resourceId);
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
        return (nsgId != null)
                ? this.parent().manager().networkSecurityGroups().getById(nsgId)
                : null;
    }

    @Override
    public Set<NicIPConfiguration> getNetworkInterfaceIPConfigurations() {
        Set<NicIPConfiguration> ipConfigs = new HashSet<>();
        Map<String, NetworkInterface> nics = new HashMap<>();
        List<IPConfigurationInner> ipConfigRefs = this.inner().ipConfigurations();
        if (ipConfigRefs == null) {
            return ipConfigs;
        }

        for (IPConfigurationInner ipConfigRef : ipConfigRefs) {
            String nicID = ResourceUtils.parentResourceIdFromResourceId(ipConfigRef.id());
            String ipConfigName = ResourceUtils.nameFromResourceId(ipConfigRef.id());
            // Check if NIC already cached
            NetworkInterface nic = nics.get(nicID.toLowerCase());
            if (nic == null) {
                //  NIC not previously found, so ask Azure for it
                nic = this.parent().manager().networkInterfaces().getById(nicID);
            }

            if (nic == null) {
                // NIC doesn't exist so ignore this bad reference
                continue;
            }

            // Cache the NIC
            nics.put(nic.id().toLowerCase(), nic);

            // Get the IP config
            NicIPConfiguration ipConfig = nic.ipConfigurations().get(ipConfigName);
            if (ipConfig == null) {
                // IP config not found, so ignore this bad reference
                continue;
            }

            ipConfigs.add(ipConfig);
        }

        return Collections.unmodifiableSet(ipConfigs);
    }
}
