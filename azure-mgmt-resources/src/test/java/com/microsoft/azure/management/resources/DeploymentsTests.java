/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.RestClient;
import org.junit.*;

import java.util.List;

public class DeploymentsTests extends ResourceManagerTestBase {
    private static ResourceGroups resourceGroups;
    private static ResourceGroup resourceGroup;

    private String testId;
    private String rgName;
    private static String templateUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.json";
    private static String parametersUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.parameters.json";
    private static String updateTemplate = "{\"$schema\":\"https://schema.management.azure.com/schemas/2015-01-01/deploymentTemplate.json#\",\"contentVersion\":\"1.0.0.0\",\"parameters\":{\"vnetName\":{\"type\":\"string\",\"defaultValue\":\"VNet2\",\"metadata\":{\"description\":\"VNet name\"}},\"vnetAddressPrefix\":{\"type\":\"string\",\"defaultValue\":\"10.0.0.0/16\",\"metadata\":{\"description\":\"Address prefix\"}},\"subnet1Prefix\":{\"type\":\"string\",\"defaultValue\":\"10.0.0.0/24\",\"metadata\":{\"description\":\"Subnet 1 Prefix\"}},\"subnet1Name\":{\"type\":\"string\",\"defaultValue\":\"Subnet1\",\"metadata\":{\"description\":\"Subnet 1 Name\"}},\"subnet2Prefix\":{\"type\":\"string\",\"defaultValue\":\"10.0.1.0/24\",\"metadata\":{\"description\":\"Subnet 2 Prefix\"}},\"subnet2Name\":{\"type\":\"string\",\"defaultValue\":\"Subnet222\",\"metadata\":{\"description\":\"Subnet 2 Name\"}}},\"variables\":{\"apiVersion\":\"2015-06-15\"},\"resources\":[{\"apiVersion\":\"[variables('apiVersion')]\",\"type\":\"Microsoft.Network/virtualNetworks\",\"name\":\"[parameters('vnetName')]\",\"location\":\"[resourceGroup().location]\",\"properties\":{\"addressSpace\":{\"addressPrefixes\":[\"[parameters('vnetAddressPrefix')]\"]},\"subnets\":[{\"name\":\"[parameters('subnet1Name')]\",\"properties\":{\"addressPrefix\":\"[parameters('subnet1Prefix')]\"}},{\"name\":\"[parameters('subnet2Name')]\",\"properties\":{\"addressPrefix\":\"[parameters('subnet2Prefix')]\"}}]}}]}";
    private static String updateParameters = "{\"vnetAddressPrefix\":{\"value\":\"10.0.0.0/16\"},\"subnet1Name\":{\"value\":\"Subnet1\"},\"subnet1Prefix\":{\"value\":\"10.0.0.0/24\"}}";
    private static String contentVersion = "1.0.0.0";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        super.initializeClients(restClient, defaultSubscription, domain);
        testId = SdkContext.randomResourceName("", 9);
        resourceGroups = resourceClient.resourceGroups();
        rgName = "rg" + testId;
        resourceGroup = resourceGroups.define(rgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .create();
    }

    @Override
    protected void cleanUpResources() {
        resourceGroups.deleteByName(rgName);
    }

    @Test
    public void canDeployVirtualNetwork() throws Exception {
        final String dpName = "dpA" + testId;

        // Create
        resourceClient.deployments()
                .define(dpName)
                .withExistingResourceGroup(rgName)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .create();
        // List
        PagedList<Deployment> deployments = resourceClient.deployments().listByResourceGroup(rgName);
        boolean found = false;
        for (Deployment deployment : deployments) {
            if (deployment.name().equals(dpName)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Check existence
        Assert.assertTrue(resourceClient.deployments().checkExistence(rgName, dpName));

        // Get
        Deployment deployment = resourceClient.deployments().getByResourceGroup(rgName, dpName);
        Assert.assertNotNull(deployment);
        Assert.assertEquals("Succeeded", deployment.provisioningState());
        GenericResource generic = resourceClient.genericResources().get(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet1", "2015-06-15");
        Assert.assertNotNull(generic);
        // Export
        Assert.assertNotNull(deployment.exportTemplate().templateAsJson());
        // Export from resource group
        Assert.assertNotNull(resourceGroup.exportTemplate(ResourceGroupExportTemplateOptions.INCLUDE_BOTH));
        // Deployment operations
        List<DeploymentOperation> operations = deployment.deploymentOperations().list();
        Assert.assertEquals(2, operations.size());
        DeploymentOperation op = deployment.deploymentOperations().getById(operations.get(0).operationId());
        Assert.assertNotNull(op);
        resourceClient.genericResources().delete(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet1", "2015-06-15");
    }

    @Test
    @Ignore("deployment.cancel() doesn't throw but provisining state says Running not Cancelled...")
    public void canCancelVirtualNetworkDeployment() throws Exception {
        final String dp = "dpB" + testId;

        // Begin create
        resourceClient.deployments()
                .define(dp)
                .withExistingResourceGroup(rgName)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .beginCreate();
        Deployment deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assert.assertEquals(dp, deployment.name());
        // Cancel
        deployment.cancel();
        deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assert.assertEquals("Canceled", deployment.provisioningState());
        Assert.assertFalse(resourceClient.genericResources().checkExistence(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet1", "2015-06-15"));
    }

    @Test
    public void canUpdateVirtualNetworkDeployment() throws Exception {
        final String dp = "dpC" + testId;

        // Begin create
        resourceClient.deployments()
                .define(dp)
                .withExistingResourceGroup(rgName)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .beginCreate();
        Deployment deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assert.assertEquals(dp, deployment.name());
        // Cancel
        deployment.cancel();
        deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assert.assertEquals("Canceled", deployment.provisioningState());
        // Update
        deployment.update()
                .withTemplate(updateTemplate)
                .withParameters(updateParameters)
                .withMode(DeploymentMode.INCREMENTAL)
                .apply();
        deployment = resourceClient.deployments().getByResourceGroup(rgName, dp);
        Assert.assertEquals(DeploymentMode.INCREMENTAL, deployment.mode());
        Assert.assertEquals("Succeeded", deployment.provisioningState());
        GenericResource genericVnet = resourceClient.genericResources().get(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet2", "2015-06-15");
        Assert.assertNotNull(genericVnet);
        resourceClient.genericResources().delete(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet2", "2015-06-15");
    }
}
