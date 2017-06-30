/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.Location;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class AzureTests extends TestBase {
    private Azure azure;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        Azure.Authenticated azureAuthed = Azure.authenticate(restClient, defaultSubscription, domain);
        azure = azureAuthed.withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {

    }

    /**
     * Tests ARM template deployments.
     * @throws IOException exception
     * @throws CloudException exception
     */
    @Test
    public void testDeployments() throws Exception {
        String testId = SdkContext.randomResourceName("", 8);
        List<Deployment> deployments = azure.deployments().list();
        System.out.println("Deployments: " + deployments.size());
        Deployment deployment = azure.deployments()
            .define("depl" + testId)
            .withNewResourceGroup("rg" + testId, Region.US_WEST)
            .withTemplateLink(
                    "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.json",
                    "1.0.0.0")
            .withParametersLink(
                    "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vnet-two-subnets/azuredeploy.parameters.json",
                    "1.0.0.0")
            .withMode(DeploymentMode.COMPLETE)
            .create();
        System.out.println("Created deployment: " + deployment.correlationId());

        azure.resourceGroups().beginDeleteByName("rg" + testId);
    }

    /**
     * Tests basic generic resources retrieval.
     * @throws Exception exception
     */
    @Test
    public void testGenericResources() throws Exception {
        // Create some resources
        NetworkSecurityGroup nsg = azure.networkSecurityGroups().define(SdkContext.randomResourceName("nsg", 13))
            .withRegion(Region.US_EAST)
            .withNewResourceGroup()
            .create();
        azure.publicIPAddresses().define(SdkContext.randomResourceName("pip", 13))
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(nsg.resourceGroupName())
            .create();

        PagedList<GenericResource> resources = azure.genericResources().listByResourceGroup(nsg.resourceGroupName());
        Assert.assertEquals(2, resources.size());
        GenericResource firstResource = resources.get(0);

        GenericResource resourceById = azure.genericResources().getById(firstResource.id());
        GenericResource resourceByDetails = azure.genericResources().get(
                firstResource.resourceGroupName(),
                firstResource.resourceProviderNamespace(),
                firstResource.resourceType(),
                firstResource.name());
        Assert.assertTrue(resourceById.id().equalsIgnoreCase(resourceByDetails.id()));
        azure.resourceGroups().beginDeleteByName(nsg.resourceGroupName());
    }

    /**
     * Tests the regions enum.
     */
    @Test
    public void testRegions() {
        // Show built-in regions
        System.out.println("Built-in regions list:");
        int regionsCount = Region.values().length;

        for (Region region : Region.values()) {
            System.out.println("Name: " + region.name() + ", Label: " + region.label());
        }

        // Look up built-in region
        Region region = Region.fromName("westus");
        Assert.assertTrue(region == Region.US_WEST);

        // Add a region
        Region region2 = Region.fromName("madeUpRegion");
        Assert.assertNotNull(region2);
        Assert.assertTrue(region2.name().equalsIgnoreCase("madeUpRegion"));
        Region region3 = Region.fromName("madeupregion");
        Assert.assertEquals(region3, region2);
        Assert.assertEquals(Region.values().length, regionsCount + 1);
    }

    /**
     * Tests subscription listing.
     * @throws Exception
     */
    @Test
    public void listSubscriptions() throws Exception {
        Assert.assertTrue(0 < azure.subscriptions().list().size());
        Subscription subscription = azure.getCurrentSubscription();
        Assert.assertNotNull(subscription);
        Assert.assertTrue(azure.subscriptionId().equalsIgnoreCase(subscription.subscriptionId()));
    }

    /**
     * Tests location listing.
     * @throws Exception
     */
    @Test
    public void listLocations() throws Exception {
        Subscription subscription = azure.getCurrentSubscription();
        Assert.assertNotNull(subscription);
        for (Location location : subscription.listLocations()) {
            Region region = Region.findByLabelOrName(location.name());
            Assert.assertNotNull(region);
            Assert.assertEquals(region, location.region());
            Assert.assertEquals(region.name().toLowerCase(), location.name().toLowerCase());
        }

        Location location = subscription.getLocationByRegion(Region.US_WEST);
        Assert.assertNotNull(location);
        Assert.assertTrue(Region.US_WEST.name().equalsIgnoreCase(location.name()));
    }

    /**
     * Tests resource group listing.
     * @throws Exception
     */
    @Test
    public void listResourceGroups() throws Exception {
        int groupCount = azure.resourceGroups().list().size();
        System.out.println(String.format("Group count: %s", groupCount));
        Assert.assertTrue(0 < groupCount);
    }

    /**
     * Tests storage account listing.
     * @throws Exception
     */
    @Test
    public void listStorageAccounts() throws Exception {
        Assert.assertTrue(0 < azure.storageAccounts().list().size());
    }

    @Test
    public void createStorageAccount() throws Exception {
        String storageAccountName = generateRandomResourceName("testsa", 12);
        StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup()
                .withSku(SkuName.PREMIUM_LRS)
                .create();

        Assert.assertEquals(storageAccount.name(), storageAccountName);

        azure.resourceGroups().beginDeleteByName(storageAccount.resourceGroupName());
    }

    @Test
    public void testBatchAccount() throws Exception {
        new TestBatch().runTest(azure.batchAccounts(), azure.resourceGroups());
    }

    @Test
    public void testTrafficManager() throws Exception {
        new TestTrafficManager(azure.publicIPAddresses())
                .runTest(azure.trafficManagerProfiles(), azure.resourceGroups());
    }

    @Test
    public void testRedis() throws Exception {
        new TestRedis()
                .runTest(azure.redisCaches(), azure.resourceGroups());
    }

    @Test
    public void testCdnManager() throws Exception {
        new TestCdn()
                .runTest(azure.cdnProfiles(), azure.resourceGroups());
    }

    @Test
    public void testDnsZones() throws Exception {
        addTextReplacementRule("https://management.azure.com:443/", this.mockUri() + "/");
        new TestDns()
                .runTest(azure.dnsZones(), azure.resourceGroups());
    }


    @Test
    public void testSqlServer() throws Exception {
        new TestSql().runTest(azure.sqlServers(), azure.resourceGroups());
    }

    @Test
    public void testResourceStreaming() throws Exception {
        new TestResourceStreaming(azure.storageAccounts()).runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    @Test
    public void testContainerService() throws Exception {
        new TestContainerService()
                .runTest(azure.containerServices(), azure.resourceGroups());
    }

    @Test
    public void testContainerRegistry() throws Exception {
        new TestContainerRegistry()
                .runTest(azure.containerRegistries(), azure.resourceGroups());
    }

    @Test
    @Ignore("Runs locally find but fails for unknown reason on check in.")
    public void testDocumentDB() throws Exception {
        new TestDocumentDB()
                .runTest(azure.documentDBAccounts(), azure.resourceGroups());
    }
}
