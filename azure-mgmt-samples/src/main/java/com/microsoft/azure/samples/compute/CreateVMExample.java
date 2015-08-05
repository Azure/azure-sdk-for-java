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

package com.microsoft.azure.samples.compute;

import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementService;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.NetworkResourceProviderService;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementService;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.azure.utility.ComputeHelper;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.net.URI;

public class CreateVMExample {
    /**
     * Create a VM with VirtualNetwork and storage account
     * within a subscription using a service principal for authentication.
     * To use the sample please set following environment variable or simply replace the getenv call
     * with actual value:
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
    public static void main(String[] args) throws Exception {
        Configuration config = createConfiguration();
        ResourceManagementClient resourceManagementClient = ResourceManagementService.create(config);
        StorageManagementClient storageManagementClient = StorageManagementService.create(config);
        ComputeManagementClient computeManagementClient = ComputeManagementService.create(config);
        NetworkResourceProviderClient networkResourceProviderClient = NetworkResourceProviderService.create(config);

        String resourceGroupName = "javasampleresourcegroup";
        String region = "EastAsia";

        ResourceContext context = new ResourceContext(
                region, resourceGroupName, System.getenv(ManagementConfiguration.SUBSCRIPTION_ID), false);

        // If you like to set the name of any auto-created resource:
        // context.setNetworkInterfaceName("VnetName1");
        // context.setStorageAccountName("storage1");

        // If you like to use any existing resource:
        // context.setNetworkInterface(NetworkInterface1);

        // To use a differnt VM image
        // context.setImageReference(
        //        ComputeHelper.getWindowsServerDefaultImage(computeManagementClient, context.getLocation()));

        System.out.println("Start create vm...");

//        Password requirements:
//        1) Contains an uppercase character
//        2) Contains a lowercase character
//        3) Contains a numeric digit
//        4) Contains a special character.
        try {
            VirtualMachine vm = ComputeHelper.createVM(
                    resourceManagementClient, computeManagementClient, networkResourceProviderClient, storageManagementClient,
                    context, "javaSampleVM", "Foo12", "BaR@123rgababaab")
                    .getVirtualMachine();

            System.out.println(vm.getName() + " is created");
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        // Remove the resource group will remove all assets (VM/VirtualNetwork/Storage Account etc.)
        // Comment the following line to keep the VM.
        resourceManagementClient.getResourceGroupsOperations().beginDeleting(context.getResourceGroupName());
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
