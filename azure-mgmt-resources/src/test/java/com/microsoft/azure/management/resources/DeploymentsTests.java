package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.ResourceResourceAdapter;
import com.microsoft.azure.management.resources.models.Deployment;
import com.microsoft.azure.management.resources.models.GenericResource;
import com.microsoft.azure.management.resources.models.implementation.api.DeploymentMode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DeploymentsTests extends ResourceManagerTestBase {
    private static ResourceGroups resourceGroups;
    private static Deployments deployments;
    private static GenericResources genericResources;

    private static String rgName = "javacsmrg3";
    private static String deploymentName = "javacsmdep2";
    private static String templateUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.json";
    private static String parametersUri = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.parameters.json";
    private static String contentVersion = "1.0.0.0";

    @BeforeClass
    public static void setup() throws Exception {
        createClient();
        resourceGroups = subscription.resourceGroups();
        resourceGroups.define(rgName)
                .withLocation(Region.US_SOUTH_CENTRAL)
                .provision();
        ResourceResourceAdapter adapter = resourceGroups.get(rgName)
                .resourceAdapter(new ResourceResourceAdapter.Builder());
        deployments = adapter.deployments();
        genericResources = adapter.genericResources();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceGroups.delete(rgName);
    }

    @Test
    public void canDeployVirtualNetwork() throws Exception {
        deployments.define(deploymentName)
                .withExistingResourceGroup(rgName)
                .withTemplateLink(templateUri, contentVersion)
                .withParametersLink(parametersUri, contentVersion)
                .withMode(DeploymentMode.COMPLETE)
                .provision();
        Deployment deployment = deployments.get(deploymentName);
        Assert.assertNotNull(deployment);
        Assert.assertEquals("Succeeded", deployment.provisioningState());
        GenericResource generic = genericResources.get("VNet1");
        Assert.assertNotNull(generic);
    }
}
