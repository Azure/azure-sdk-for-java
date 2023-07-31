// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.samples.Utils;

import java.util.Arrays;

/**
 * Azure Compute sample for batch create virtual machines -
 *   - Create multiple virtual machines with un-managed OS and data disks
 *   - Use Azure Resource Graph to batch query the status (the number of provisioningState=Succeeded ?).
 */
public final class CreateMultipleVirtualMachines {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @param resourceGraphManager instance of the azure graph client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, ResourceGraphManager resourceGraphManager) {
        final int desiredVMCount = 6;
        final Region region = Region.US_EAST;
        String rgName = Utils.randomResourceName(azureResourceManager, "rg-", 15);
        StringBuffer vmNamesBuffer = new StringBuffer();
        Long total = 0L;
        Boolean rtnValue = false;
        try {
            System.out.println("Creating Resource Group: " + rgName);
            ResourceGroup resourceGroup = azureResourceManager.resourceGroups().define(rgName).withRegion(region).create();
            System.out.println("Batch creating Virtual Machines");
            for (int i = 0; i < desiredVMCount; i++) {
                String vmName = Utils.randomResourceName(azureResourceManager, "javascvm", 15);
                vmNamesBuffer.append("'" + vmName + "',");
                azureResourceManager.virtualMachines()
                        .define(vmName)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .withNewPrimaryNetwork("10.0." + i + ".0/28")
                        .withPrimaryPrivateIPAddressDynamic()
                        .withoutPrimaryPublicIPAddress()
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUsername(Utils.randomResourceName(azureResourceManager, "tirekicker", 15))
                        .withAdminPassword(Utils.password())
                        .withUnmanagedDisks()
                        .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                        .beginCreate();
            }

            System.out.println("Use Azure Resource Graph to batch query the status(the number of provisioningState=Succeeded).");
            StringBuffer queryBuffer = new StringBuffer();
            queryBuffer.append("resources ")
                    .append("| where (resourceGroup =~ ('").append(rgName).append("')) ")
                    .append("| where (type =~ ('microsoft.compute/virtualmachines')) ")
                    .append("| where (name in~ (").append(vmNamesBuffer.substring(0, vmNamesBuffer.length() - 1)).append(")) ")
                    .append("| project name,id,type,kind,location,subscriptionId,resourceGroup,tags,properties['provisioningState'] ");
            while (total < desiredVMCount) {
                total = resourceGraphManager.resourceProviders()
                        .resources(
                                new QueryRequest()
                                        .withSubscriptions(Arrays.asList(azureResourceManager.subscriptionId()))
                                        .withQuery(queryBuffer.toString())
                        )
                        .totalRecords();
                if (total < desiredVMCount) {
                    Thread.sleep(5000);
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
}
