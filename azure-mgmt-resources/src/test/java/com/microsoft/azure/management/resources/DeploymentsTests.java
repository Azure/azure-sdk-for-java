package com.microsoft.azure.management.resources;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.ARMResourceConnector;
import com.microsoft.azure.management.resources.implementation.api.DeploymentMode;
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
        resourceGroups.delete(rgName);
    }

    @Test
    public void canDeployVirtualNetwork() throws Exception {
        // Create
        ARMResourceConnector connector = resourceGroup.connectToResource(new ARMResourceConnector.Builder());
        connector.deployments()
                .define(dp1)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .create();
        // List
        PagedList<Deployment> deployments = connector.deployments().list();
        boolean found = false;
        for (Deployment deployment :
                deployments) {
            if (deployment.name().equals(dp1)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        Deployment deployment = resourceClient.deployments().get(rgName, dp1);
        Assert.assertNotNull(deployment);
        Assert.assertEquals("Succeeded", deployment.provisioningState());
        GenericResource generic = connector.genericResources().get("VNet1");
        Assert.assertNotNull(generic);
        // Deployment operations
        List<DeploymentOperation> operations = deployment.deploymentOperations().list();
        Assert.assertEquals(2, operations.size());
        DeploymentOperation op = deployment.deploymentOperations().get(operations.get(0).operationId());
        Assert.assertNotNull(op);
    }

    @Test
    public void canCancelVirtualNetworkDeployment() throws Exception {
        // Begin create
        ARMResourceConnector connector = resourceGroup.connectToResource(new ARMResourceConnector.Builder());
        connector.deployments()
                .define(dp2)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .beginCreate();
        Deployment deployment = resourceClient.deployments().get(rgName, dp2);
        Assert.assertEquals(dp2, deployment.name());
        // Cancel
        deployments.cancel(deployment.resourceGroupName(), deployment.name());
        deployment = resourceClient.deployments().get(rgName, dp2);
        Assert.assertEquals("Canceled", deployment.provisioningState());
        GenericResource generic = connector.genericResources().get("VNet1");
        Assert.assertNull(generic);
    }

    @Test
    public void canUpdateVirtualNetworkDeployment() throws Exception {
        // Begin create
        ARMResourceConnector connector = resourceGroup.connectToResource(new ARMResourceConnector.Builder());
        connector.deployments()
                .define(dp3)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .beginCreate();
        Deployment deployment = resourceClient.deployments().get(rgName, dp3);
        Assert.assertEquals(dp3, deployment.name());
        // Cancel
        deployments.cancel(deployment.resourceGroupName(), deployment.name());
        deployment = resourceClient.deployments().get(rgName, dp2);
        Assert.assertEquals("Canceled", deployment.provisioningState());
        // Update
        deployment.update()
                .withMode(DeploymentMode.INCREMENTAL)
                .apply();
        deployment = resourceClient.deployments().get(rgName, dp3);
        Assert.assertEquals(DeploymentMode.INCREMENTAL, deployment.mode());
        Assert.assertEquals("Succeeded", deployment.provisioningState());
        GenericResource generic = connector.genericResources().get("VNet1");
        Assert.assertNotNull(generic);
    }
}
