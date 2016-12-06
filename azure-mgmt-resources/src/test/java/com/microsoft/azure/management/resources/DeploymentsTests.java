package com.microsoft.azure.management.resources;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class DeploymentsTests extends ResourceManagerTestBase {
    private static ResourceGroups resourceGroups;
    private static ResourceGroup resourceGroup;
    private static Deployments deployments;

    private static String rgName = "javacsmrg2";
    private static String dp1 = "javacsmdep1";
    private static String dp2 = "javacsmdep2";
    private static String dp3 = "javacsmdep2";
    private static String templateUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.json";
    private static String parametersUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.parameters.json";
    private static String updateTemplate = "{\"$schema\":\"https://schema.management.azure.com/schemas/2015-01-01/deploymentTemplate.json#\",\"contentVersion\":\"1.0.0.0\",\"parameters\":{\"vnetName\":{\"type\":\"string\",\"defaultValue\":\"VNet2\",\"metadata\":{\"description\":\"VNet name\"}},\"vnetAddressPrefix\":{\"type\":\"string\",\"defaultValue\":\"10.0.0.0/16\",\"metadata\":{\"description\":\"Address prefix\"}},\"subnet1Prefix\":{\"type\":\"string\",\"defaultValue\":\"10.0.0.0/24\",\"metadata\":{\"description\":\"Subnet 1 Prefix\"}},\"subnet1Name\":{\"type\":\"string\",\"defaultValue\":\"Subnet1\",\"metadata\":{\"description\":\"Subnet 1 Name\"}},\"subnet2Prefix\":{\"type\":\"string\",\"defaultValue\":\"10.0.1.0/24\",\"metadata\":{\"description\":\"Subnet 2 Prefix\"}},\"subnet2Name\":{\"type\":\"string\",\"defaultValue\":\"Subnet222\",\"metadata\":{\"description\":\"Subnet 2 Name\"}}},\"variables\":{\"apiVersion\":\"2015-06-15\"},\"resources\":[{\"apiVersion\":\"[variables('apiVersion')]\",\"type\":\"Microsoft.Network/virtualNetworks\",\"name\":\"[parameters('vnetName')]\",\"location\":\"[resourceGroup().location]\",\"properties\":{\"addressSpace\":{\"addressPrefixes\":[\"[parameters('vnetAddressPrefix')]\"]},\"subnets\":[{\"name\":\"[parameters('subnet1Name')]\",\"properties\":{\"addressPrefix\":\"[parameters('subnet1Prefix')]\"}},{\"name\":\"[parameters('subnet2Name')]\",\"properties\":{\"addressPrefix\":\"[parameters('subnet2Prefix')]\"}}]}}]}";
    private static String updateParameters = "{\"vnetAddressPrefix\":{\"value\":\"10.0.0.0/16\"},\"subnet1Name\":{\"value\":\"Subnet1\"},\"subnet1Prefix\":{\"value\":\"10.0.0.0/24\"}}";
    private static String contentVersion = "1.0.0.0";

    @BeforeClass
    public static void setup() throws Exception {
        createClient();
        resourceGroups = resourceClient.resourceGroups();
        resourceGroup = resourceGroups.define(rgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .create();
        deployments = resourceClient.deployments();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceGroups.deleteByName(rgName);
    }

    @Test
    public void canDeployVirtualNetwork() throws Exception {
        // Create
        resourceClient.deployments()
                .define(dp1)
                .withExistingResourceGroup(rgName)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .create();
        // List
        PagedList<Deployment> deployments = resourceClient.deployments().listByGroup(rgName);
        boolean found = false;
        for (Deployment deployment : deployments) {
            if (deployment.name().equals(dp1)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        Deployment deployment = resourceClient.deployments().getByGroup(rgName, dp1);
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
    public void canCancelVirtualNetworkDeployment() throws Exception {
        // Begin create
        resourceClient.deployments()
                .define(dp2)
                .withExistingResourceGroup(rgName)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .beginCreate();
        Deployment deployment = resourceClient.deployments().getByGroup(rgName, dp2);
        Assert.assertEquals(dp2, deployment.name());
        // Cancel
        deployment.cancel();
        deployment = resourceClient.deployments().getByGroup(rgName, dp2);
        Assert.assertEquals("Canceled", deployment.provisioningState());
        Assert.assertFalse(resourceClient.genericResources().checkExistence(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet1", "2015-06-15"));
    }

    @Test
    public void canUpdateVirtualNetworkDeployment() throws Exception {
        // Begin create
        resourceClient.deployments()
                .define(dp3)
                .withExistingResourceGroup(rgName)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .beginCreate();
        Deployment deployment = resourceClient.deployments().getByGroup(rgName, dp3);
        Assert.assertEquals(dp3, deployment.name());
        // Cancel
        deployment.cancel();
        deployment = resourceClient.deployments().getByGroup(rgName, dp3);
        Assert.assertEquals("Canceled", deployment.provisioningState());
        // Update
        deployment.update()
                .withTemplate(updateTemplate)
                .withParameters(updateParameters)
                .withMode(DeploymentMode.INCREMENTAL)
                .apply();
        deployment = resourceClient.deployments().getByGroup(rgName, dp3);
        Assert.assertEquals(DeploymentMode.INCREMENTAL, deployment.mode());
        Assert.assertEquals("Succeeded", deployment.provisioningState());
        GenericResource genericVnet = resourceClient.genericResources().get(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet2", "2015-06-15");
        Assert.assertNotNull(genericVnet);
        resourceClient.genericResources().delete(rgName, "Microsoft.Network", "", "virtualnetworks", "VNet2", "2015-06-15");
    }
}
