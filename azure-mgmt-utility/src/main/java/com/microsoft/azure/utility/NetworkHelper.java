/**
 * Copyright Microsoft Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.utility;

import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.models.*;

import java.util.ArrayList;

public class NetworkHelper {
    public static PublicIpAddress createPublicIpAddress(
            NetworkResourceProviderClient networkResourceProviderClient, ResourceContext context)
            throws Exception {
        PublicIpAddress publicIpParams = new PublicIpAddress(context.getLocation(), IpAllocationMethod.DYNAMIC);
        String publicIpName = context.getPublicIpName();

        AzureAsyncOperationResponse response = networkResourceProviderClient.getPublicIpAddressesOperations()
                .createOrUpdate(context.getResourceGroupName(), publicIpName, publicIpParams);

        PublicIpAddress ip = networkResourceProviderClient.getPublicIpAddressesOperations()
                .get(context.getResourceGroupName(), publicIpName).getPublicIpAddress();
        context.setPublicIpAddress(ip);
        return ip;
    }

    public static VirtualNetwork createVirtualNetwork(
            NetworkResourceProviderClient networkResourceProviderClient, ResourceContext context)
            throws Exception {
        VirtualNetwork vnet = new VirtualNetwork(context.getLocation());
        String subnetName = context.getSubnetName();
        String vnetName = context.getVirtualNetworkName();

        // set AddressSpace
        AddressSpace asp = new AddressSpace();
        ArrayList<String> addrPrefixes = new ArrayList<String>(1);
        addrPrefixes.add("10.0.0.0/16");
        asp.setAddressPrefixes(addrPrefixes);
        vnet.setAddressSpace(asp);

        // set DhcpOptions
        DhcpOptions dop = new DhcpOptions();
        ArrayList<String> dnsServers = new ArrayList<String>(2);
        dnsServers.add("10.1.1.1");
        dop.setDnsServers(dnsServers);
        vnet.setDhcpOptions(dop);

        // set subNet
        Subnet subnet = new Subnet("10.0.0.0/24");
        subnet.setName(subnetName);
        ArrayList<Subnet> subNets = new ArrayList<Subnet>(1);
        subNets.add(subnet);
        vnet.setSubnets(subNets);

        // send request
        AzureAsyncOperationResponse response = networkResourceProviderClient.getVirtualNetworksOperations()
                .createOrUpdate(context.getResourceGroupName(), vnetName, vnet);

        VirtualNetwork createdVnet = networkResourceProviderClient.getVirtualNetworksOperations()
                .get(context.getResourceGroupName(), vnetName)
                .getVirtualNetwork();

        context.setVirtualNetwork(createdVnet);
        return createdVnet;
    }

    public static NetworkInterface createNIC(
            NetworkResourceProviderClient networkResourceProviderClient, ResourceContext context, Subnet subNet)
            throws Exception {
        NetworkInterface nic = new NetworkInterface(context.getLocation());
        String nicName = context.getNetworkInterfaceName();
        String ipConfigName = context.getIpConfigName();
        nic.setName(nicName);

        //set tags
        if (context.getTags() != null) {
            nic.setTags(context.getTags());
        }

        //set ipconfiguration
        NetworkInterfaceIpConfiguration nicConfig = new NetworkInterfaceIpConfiguration();
        nicConfig.setName(ipConfigName);
        nicConfig.setPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC);
        nicConfig.setSubnet(subNet);
        ArrayList<NetworkInterfaceIpConfiguration> ipConfigs = new ArrayList<NetworkInterfaceIpConfiguration>(1);
        ipConfigs.add(nicConfig);
        nic.setIpConfigurations(ipConfigs);

        if (context.getPublicIpAddress() != null) {
            ResourceId publicIpAddressId = new ResourceId();
            publicIpAddressId.setId(context.getPublicIpAddress().getId());
            nic.getIpConfigurations().get(0).setPublicIpAddress(publicIpAddressId);
        }

        // send request
        AzureAsyncOperationResponse response = networkResourceProviderClient.getNetworkInterfacesOperations()
                .createOrUpdate(context.getResourceGroupName(), nicName, nic);

        NetworkInterface createdNic = networkResourceProviderClient.getNetworkInterfacesOperations()
                .get(context.getResourceGroupName(), nicName)
                .getNetworkInterface();
        context.setNetworkInterface(createdNic);
        return createdNic;
    }
}
