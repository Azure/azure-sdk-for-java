package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.ARMResourceConnector;
import com.microsoft.azure.management.resources.models.Deployment;
import com.microsoft.azure.management.resources.models.GenericResource;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.implementation.api.DeploymentMode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DeploymentsTests extends ResourceManagerTestBase {
    private static ResourceGroups resourceGroups;
    private static ResourceGroup resourceGroup;

    private static String rgName = "javacsmrg2";
    private static String deploymentName = "javacsmdep2";
    private static String templateUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.json";
    private static String parametersUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.parameters.json";
    private static String contentVersion = "1.0.0.0";

    @BeforeClass
    public static void setup() throws Exception {
        createClient();
        resourceGroups = subscription.resourceGroups();
        resourceGroup = resourceGroups.define(rgName)
                .withLocation(Region.US_SOUTH_CENTRAL)
                .provision();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceGroups.delete(rgName);
    }

    @Test
    public void canDeployVirtualNetwork() throws Exception {
        ARMResourceConnector connector = resourceGroup.resourcesInGroup(new ARMResourceConnector.Builder());
        connector.deployments()
                .define(deploymentName)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .provision();
        Deployment deployment = subscription.deployments(rgName).get(deploymentName);
        Assert.assertNotNull(deployment);
        Assert.assertEquals("Succeeded", deployment.provisioningState());
        GenericResource generic = connector.genericResources().get("VNet1");
        Assert.assertNotNull(generic);
    }
}
