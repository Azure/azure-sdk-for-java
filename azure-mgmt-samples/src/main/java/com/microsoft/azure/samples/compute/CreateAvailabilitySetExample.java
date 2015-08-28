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
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.samples.ConfigurationHelper;
import com.microsoft.azure.utility.ComputeHelper;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;


public class CreateAvailabilitySetExample {
    /**
     * Create Availability Set
     * within a subscription using a service principal for authentication.
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
    public static void main(String[] args) throws Exception {
        Configuration config = ConfigurationHelper.createConfiguration();

        ComputeManagementClient computeManagementClient = ComputeManagementService.create(config);
        ResourceManagementClient resourceManagementClient = ResourceManagementService.create(config);

        String resourceGroupName = "javasampleresourcegroup";
        String region = "WestUS";

        ResourceContext context = new ResourceContext(
                region, resourceGroupName, System.getenv(ManagementConfiguration.SUBSCRIPTION_ID), false);
        context.setAvailabilitySetName("javasampleavailabilityset");

        ComputeHelper.createOrUpdateResourceGroup(resourceManagementClient, context);

        ComputeHelper.createAvailabilitySet(computeManagementClient, context);

        // Remove the resource group will remove all assets (VM/VirtualNetwork/Storage Account/Availability Set etc.)
        // Comment the following line to keep availability set
        resourceManagementClient.getResourceGroupsOperations().beginDeleting(context.getResourceGroupName());
    }
}
