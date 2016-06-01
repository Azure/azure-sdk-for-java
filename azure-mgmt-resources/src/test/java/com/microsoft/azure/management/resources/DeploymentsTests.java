package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
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
        resourceGroups = resourceClient.resourceGroups();
        resourceGroup = resourceGroups.define(rgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .create();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceGroups.delete(rgName);
    }

    @Test
    public void canDeployVirtualNetwork() throws Exception {
        Deployment deployment = resourceClient.deployments().get(rgName, deploymentName);
        Assert.assertNotNull(deployment);
        Assert.assertEquals("Succeeded", deployment.provisioningState());
    }
}
