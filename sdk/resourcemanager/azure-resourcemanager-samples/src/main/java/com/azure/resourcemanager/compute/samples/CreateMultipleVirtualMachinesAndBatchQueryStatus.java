// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.AzureCloud;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resourcegraph.models.QueryRequestOptions;
import com.azure.resourcemanager.resourcegraph.models.QueryResponse;
import com.azure.resourcemanager.resourcegraph.models.ResultFormat;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Azure Compute sample for batch create virtual machines and batch query status
 * - Create multiple virtual machines from PIR image with data disks
 * - Use Azure Resource Graph to query the count number for each [provisioningState].
 */
public final class CreateMultipleVirtualMachinesAndBatchQueryStatus {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @param resourceGraphManager instance of the azure graph client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager,
        ResourceGraphManager resourceGraphManager) {
        final Integer desiredVMCount = 6;
        final Region region = Region.US_EAST;
        final String rgName = Utils.randomResourceName(azureResourceManager, "rg-", 15);
        final String networkName = Utils.randomResourceName(azureResourceManager, "vnet-", 15);
        final String subNetName = Utils.randomResourceName(azureResourceManager, "snet-", 15);
        Integer succeededTotal = 0;
        try {
            System.out.println("Creating Resource Group: " + rgName);
            azureResourceManager.resourceGroups().define(rgName).withRegion(region).create();

            System.out.println("Creating Network: " + networkName);
            Network primaryNetwork = azureResourceManager.networks()
                .define(networkName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withAddressSpace("10.0.0.0/16")
                .withSubnet(subNetName, "10.0.1.0/24")
                .create();

            System.out.println("Batch creating Virtual Machines");
            for (int i = 0; i < desiredVMCount; i++) {
                azureResourceManager.virtualMachines()
                    .define(Utils.randomResourceName(azureResourceManager, "javascvm", 15))
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(primaryNetwork)
                    .withSubnet(subNetName)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                    .withRootUsername(Utils.randomResourceName(azureResourceManager, "tirekicker", 15))
                    .withSsh(Utils.sshPublicKey())
                    .withNewDataDisk(50, 1, CachingTypes.READ_WRITE)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .beginCreate();
            }

            System.out.println("Use Azure Resource Graph to query the count number for each [provisioningState].");
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("resources ")
                .append("| where (resourceGroup =~ ('")
                .append(rgName)
                .append("')) ")
                .append("| where (type =~ ('microsoft.compute/virtualmachines')) ")
                .append("| where (name contains 'javascvm') ")
                .append("| extend provisioningState = case(")
                .append("properties['provisioningState'] =~ 'NotSpecified','NotSpecified',")
                .append("properties['provisioningState'] =~ 'Accepted','Accepted',")
                .append("properties['provisioningState'] =~ 'Running','Running',")
                .append("properties['provisioningState'] =~ 'Ready','Ready',")
                .append("properties['provisioningState'] =~ 'Creating','Creating',")
                .append("properties['provisioningState'] =~ 'Created','Created',")
                .append("properties['provisioningState'] =~ 'Deleting','Deleting',")
                .append("properties['provisioningState'] =~ 'Deleted','Deleted',")
                .append("properties['provisioningState'] =~ 'Canceled','Canceled',")
                .append("properties['provisioningState'] =~ 'Failed','Failed',")
                .append("properties['provisioningState'] =~ 'Succeeded','Succeeded',")
                .append("properties['provisioningState'] =~ 'Updating','Updating',")
                .append("properties['provisioningState']) ")
                .append("| summarize count=count() by provisioningState");

            while (succeededTotal < desiredVMCount) {
                Integer creatingTotal = 0;
                Integer updatingTotal = 0;
                Integer failedTotal = 0;
                Integer otherTotal = 0;
                succeededTotal = 0;

                QueryResponse queryResponse = resourceGraphManager.resourceProviders()
                    .resources(new QueryRequest()
                        .withSubscriptions(Collections.singletonList(azureResourceManager.subscriptionId()))
                        .withQuery(queryBuilder.toString())
                        .withOptions(new QueryRequestOptions().withResultFormat(ResultFormat.OBJECT_ARRAY)));

                List<TotalResult> totalResultList
                    = new ObjectMapper().convertValue(queryResponse.data(), new TypeReference<List<TotalResult>>() {
                    });
                for (TotalResult totalResult : totalResultList) {
                    switch (totalResult.getProvisioningState()) {
                        case "Creating":
                            creatingTotal = totalResult.getCount();
                            break;

                        case "Succeeded":
                            succeededTotal = totalResult.getCount();
                            break;

                        case "Updating":
                            updatingTotal = totalResult.getCount();
                            break;

                        case "Failed":
                            failedTotal = totalResult.getCount();
                            break;

                        default:
                            otherTotal += totalResult.getCount();
                            break;
                    }
                }

                System.out.println(new StringBuilder().append("\n\tThe total number of Creating : ")
                    .append(creatingTotal)
                    .append("\n\tThe total number of Updating : ")
                    .append(updatingTotal)
                    .append("\n\tThe total number of Failed : ")
                    .append(failedTotal)
                    .append("\n\tThe total number of Succeeded : ")
                    .append(succeededTotal)
                    .append("\n\tThe total number of Other Status : ")
                    .append(otherTotal));

                if (failedTotal > 0) {
                    break;
                } else if (succeededTotal < desiredVMCount) {
                    ResourceManagerUtils.sleep(Duration.ofSeconds(5L));
                }
            }
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
        return succeededTotal.equals(desiredVMCount);
    }

    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            ResourceGraphManager resourceGraphManager = ResourceGraphManager.configure()
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

    private CreateMultipleVirtualMachinesAndBatchQueryStatus() {
    }

    private static class TotalResult {
        private String provisioningState;
        private Integer count;

        public String getProvisioningState() {
            return provisioningState;
        }

        public void setProvisioningState(String provisioningState) {
            this.provisioningState = provisioningState;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }
}
