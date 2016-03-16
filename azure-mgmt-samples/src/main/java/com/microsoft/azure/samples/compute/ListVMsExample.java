package com.microsoft.azure.samples.compute;

import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementService;
import com.microsoft.azure.management.compute.models.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.NetworkResourceProviderService;
import com.microsoft.azure.management.network.models.NetworkInterface;
import com.microsoft.azure.management.network.models.NetworkInterfaceIpConfiguration;
import com.microsoft.azure.management.network.models.PublicIpAddress;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.net.URI;
import java.util.ArrayList;

/**
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ListVMsExample {

    /**
     * List all virtual machines within each resource group in the subscription.
     * For each VM list all network interfaces (nics) and associated IP addresses.
     * To use the sample please set following environment variable or simply replace the getenv call
     * here and in ConfigurationHelper.java with actual values:
     *
     management.uri=https://management.core.windows.net/
     arm.url=https://management.azure.com/
     arm.aad.url=https://login.windows.net/
     arm.clientid=[your service principal client id]
     arm.clientkey=[your service principal client key]
     arm.tenant=[your service principal tenant]
     management.subscription.id=[your subscription id (GUID)]
     *
     * @param args arguments supplied at the command line (they are not used)
     * @throws Exception all of the exceptions!!
     */
    public static void main(String args[]) throws Exception {
        Configuration config = createConfiguration();
        ResourceManagementClient resourceManagementClient = ResourceManagementService.create(config);
        ComputeManagementClient computeManagementClient = ComputeManagementService.create(config);
        NetworkResourceProviderClient networkResourceProviderClient = NetworkResourceProviderService.create(config);

        ArrayList<ResourceGroupExtended> resourceGroups = resourceManagementClient.getResourceGroupsOperations().list(null).getResourceGroups();
        for (ResourceGroupExtended resourcesGroup : resourceGroups) {
            String rgName = resourcesGroup.getName();

            ArrayList<VirtualMachine> vms = computeManagementClient.getVirtualMachinesOperations().list(rgName).getVirtualMachines();
            System.out.println("Resource Group: " + rgName);

            for (VirtualMachine vm : vms) {
                System.out.println("    VM: " + vm.getName());
                ArrayList<NetworkInterfaceReference> nics = vm.getNetworkProfile().getNetworkInterfaces();

                for (NetworkInterfaceReference nicReference : nics) {
                    String[] nicURI = nicReference.getReferenceUri().split("/");
                    NetworkInterface nic = networkResourceProviderClient.getNetworkInterfacesOperations().get(rgName, nicURI[nicURI.length - 1]).getNetworkInterface();
                    System.out.println("        NIC: " + nic.getName());
                    System.out.println("        Is primary: " + nic.isPrimary());
                    ArrayList<NetworkInterfaceIpConfiguration> ips = nic.getIpConfigurations();

                    for (NetworkInterfaceIpConfiguration ipConfiguration : ips) {
                        System.out.println("        Private IP address: " + ipConfiguration.getPrivateIpAddress());

                        String[] pipID = ipConfiguration.getPublicIpAddress().getId().split("/");
                        PublicIpAddress pip = networkResourceProviderClient.getPublicIpAddressesOperations().get(rgName, pipID[pipID.length - 1]).getPublicIpAddress();
                        System.out.println("        Public IP address: " + pip.getIpAddress());
                    }
                }
            }
        }
    }

    /**
     * Create configuration builds the management configuration needed for creating the clients.
     * The config contains the baseURI which is the base of the ARM REST service, the subscription id as the context for
     * the ResourceManagementService and the AAD token required for the HTTP Authorization header.
     *
     * @return Configuration the generated configuration
     * @throws Exception all of the exceptions!!
     */
    public static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv("arm.url");

        return ManagementConfiguration.configure(
                null,
                baseUri != null ? new URI(baseUri) : null,
                System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                AuthHelper.getAccessTokenFromServicePrincipalCredentials(
                        System.getenv(ManagementConfiguration.URI), System.getenv("arm.aad.url"),
                        System.getenv("arm.tenant"), System.getenv("arm.clientid"),
                        System.getenv("arm.clientkey"))
                        .getAccessToken());
    }
}
