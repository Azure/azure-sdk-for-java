// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Azure Compute sample for batch create virtual machines and batch query status
 * - Create multiple virtual machines from PIR image with data disks
 * - Use Azure Resource Graph to batch query the status (the number of provisioningState=Succeeded ?).
 */
public final class CreateMultipleVirtualMachinesAndBatchQueryStatus {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @param resourceGraphManager instance of the azure graph client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, ResourceGraphManager resourceGraphManager) {
        final Long desiredVMCount = 6L;
        final Region region = Region.US_EAST;
        String rgName = Utils.randomResourceName(azureResourceManager, "rg-", 15);
        List<String> vmNameList = new ArrayList<>();
        Long total = 0L;
        Boolean rtnValue = false;
        try {
            System.out.println("Creating Resource Group: " + rgName);
            azureResourceManager.resourceGroups().define(rgName).withRegion(region).create();
            System.out.println("Batch creating Virtual Machines");
            for (int i = 0; i < desiredVMCount; i++) {
                String vmName = Utils.randomResourceName(azureResourceManager, "javascvm", 15);
                vmNameList.add("'".concat(vmName).concat("'"));
                azureResourceManager.virtualMachines()
                    .define(vmName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0." + i + ".0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                    .withRootUsername(Utils.randomResourceName(azureResourceManager, "tirekicker", 15))
                    .withSsh(Utils.sshPublicKey())
                    .withNewDataDisk(50, 1, CachingTypes.READ_WRITE)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .beginCreate();
            }

            System.out.println("Use Azure Resource Graph to batch query the status(the number of provisioningState=Succeeded).");
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("resources ")
                .append("| where (resourceGroup =~ ('").append(rgName).append("')) ")
                .append("| where (type =~ ('microsoft.compute/virtualmachines')) ")
                .append("| where (name in~ (").append(String.join(",", vmNameList)).append(")) ")
                .append("| where (properties['provisioningState'] =~ ('Succeeded')) ")
                .append("| project name,id,type,location,subscriptionId,resourceGroup,tags");
            while (total < desiredVMCount) {
                total = resourceGraphManager.resourceProviders()
                    .resources(
                        new QueryRequest()
                            .withSubscriptions(Collections.singletonList(azureResourceManager.subscriptionId()))
                            .withQuery(queryBuilder.toString())
                    )
                    .totalRecords();
                if (total < desiredVMCount) {
                    ResourceManagerUtils.sleep(Duration.ofSeconds(5L));
                }
            }
            rtnValue = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return rtnValue;
    }

    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate
            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            ResourceGraphManager resourceGraphManager = ResourceGraphManager
                .configure()
                .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
                .authenticate(credential, profile);

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager, resourceGraphManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private CreateMultipleVirtualMachinesAndBatchQueryStatus() {}
}
