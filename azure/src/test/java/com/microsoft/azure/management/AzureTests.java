/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.azure.management.storage.StorageAccount;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AzureTests {
    private static final ApplicationTokenCredentials CREDENTIALS = new ApplicationTokenCredentials(
            System.getenv("client-id"),
            System.getenv("domain"),
            System.getenv("secret"),
            AzureEnvironment.AZURE);
    private static final String SUBSCRIPTION_ID = System.getenv("arm.subscriptionid");
    private Subscriptions subscriptions;
    private Azure azure;

    public static void main(String[] args) throws IOException, CloudException {
        final File credFile = new File("my.azureauth");
        Azure azure = Azure.authenticate(credFile).withDefaultSubscription();

        try {
            System.out.println(String.valueOf(azure.resourceGroups().list().size()));
        } catch (com.microsoft.rest.RestException e) {
            e.printStackTrace();
        }

        Azure.configure().withLogLevel(Level.BASIC).authenticate(credFile);
        System.out.println("Selected subscription: " + azure.subscriptionId());
        try {
            System.out.println(String.valueOf(azure.resourceGroups().list().size()));
        } catch (com.microsoft.rest.RestException e) {
            e.printStackTrace();
        }

        final File authFileNoSubscription = new File("nosub.azureauth");
        azure = Azure.authenticate(authFileNoSubscription).withDefaultSubscription();
        System.out.println("Selected subscription: " + azure.subscriptionId());
        try {
            System.out.println(String.valueOf(azure.resourceGroups().list().size()));
        } catch (com.microsoft.rest.RestException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() throws Exception {
        // Authenticate based on credentials instance
        Azure.Authenticated azureAuthed = Azure.configure()
                .withLogLevel(Level.BODY)
                .withUserAgent("AzureTests")
                .authenticate(CREDENTIALS);

        subscriptions = azureAuthed.subscriptions();
        // Try to authenticate based on file if present
        File authFile = new File("my.azureauth");
        if (authFile.exists()) {
            this.azure = Azure.configure()
                    .withLogLevel(Level.BODY)
                    .withUserAgent("AzureTests")
                    .withReadTimeout(60, TimeUnit.SECONDS)
                    .authenticate(new File("my.azureauth"))
                    .withDefaultSubscription();
        } else {
            azure = azureAuthed.withSubscription(SUBSCRIPTION_ID);
        }
    }

    /**
     * Tests ARM template deployments.
     * @throws IOException
     * @throws CloudException
     */
    @Test public void testDeployments() throws Exception {
        String testId = String.valueOf(System.currentTimeMillis());
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

        azure.resourceGroups().deleteByName("rg" + testId);
    }

    /**
     * Tests basic generic resources retrieval.
     * @throws Exception
     */
    @Test public void testGenericResources() throws Exception {
        PagedList<GenericResource> resources = azure.genericResources().listByGroup("sdkpriv");
        GenericResource firstResource = resources.get(0);
        GenericResource resourceById = azure.genericResources().getById(firstResource.id());
        GenericResource resourceByDetails = azure.genericResources().get(
                firstResource.resourceGroupName(),
                firstResource.resourceProviderNamespace(),
                firstResource.resourceType(),
                firstResource.name());
        Assert.assertTrue(resourceById.id().equalsIgnoreCase(resourceByDetails.id()));
    }

    /**
     * Tests VM images.
     * @throws IOException
     * @throws CloudException
     */
    @Test public void testVMImages() throws CloudException, IOException {
        List<VirtualMachinePublisher> publishers = azure.virtualMachineImages().publishers().listByRegion(Region.US_WEST);
        Assert.assertTrue(publishers.size() > 0);
        for (VirtualMachinePublisher p : publishers) {
            System.out.println(String.format("Publisher name: %s, region: %s", p.name(), p.region()));
            try {
                for (VirtualMachineOffer o : p.offers().list()) {
                    System.out.println(String.format("\tOffer name: %s", o.name()));
                    try {
                        for (VirtualMachineSku s : o.skus().list()) {
                            System.out.println(String.format("\t\tSku name: %s", s.name()));
                        }
                    } catch (com.microsoft.rest.RestException e) {
                        e.printStackTrace();
                    }
                }
            } catch (com.microsoft.rest.RestException e) {
                e.printStackTrace();
            }
        }
        List<VirtualMachineImage> images = azure.virtualMachineImages().listByRegion(Region.US_WEST);
        Assert.assertTrue(images.size() > 0);
    }

    /**
     * Tests the network security group implementation.
     * @throws Exception
     */
    @Test
    public void testNetworkSecurityGroups() throws Exception {
        new TestNSG().runTest(azure.networkSecurityGroups(), azure.resourceGroups());
    }

    /**
     * Tests the inbound NAT rule support in load balancers.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersNatRules() throws Exception {
        new TestLoadBalancer.InternetWithNatRule(
                azure.publicIpAddresses(),
                azure.virtualMachines(),
                azure.networks())
            .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the inbound NAT pool support in load balancers.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersNatPools() throws Exception {
        new TestLoadBalancer.InternetWithNatPool(
                azure.publicIpAddresses(),
                azure.virtualMachines(),
                azure.networks())
        .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the minimum internet-facing load balancer.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersInternetMinimum() throws Exception {
        new TestLoadBalancer.InternetMinimal(
                azure.publicIpAddresses(),
                azure.virtualMachines(),
                azure.networks())
            .runTest(azure.loadBalancers(),  azure.resourceGroups());
    }

    /**
     * Tests the minimum internal load balancer.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersInternalMinimum() throws Exception {
        new TestLoadBalancer.InternalMinimal(
                azure.virtualMachines(),
                azure.networks())
        .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests a complex internal application gateway
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternalComplex() throws Exception {
        new TestApplicationGateway.PrivateComplex(azure.networks(), azure.publicIpAddresses())
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    /**
     * Tests a minimal internal application gateway
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternalMinimal() throws Exception {
        new TestApplicationGateway.PrivateMinimal()
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    /**
     * Tests a minimal Internet-facing application gateway
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternetFacingMinimal() throws Exception {
        new TestApplicationGateway.PublicMinimal()
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    /**
     * Tests a complex Internet-facing application gateway
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternetFacingComplex() throws Exception {
        new TestApplicationGateway.PublicComplex(
                azure.publicIpAddresses())
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    @Test
    public void testAppGatewaysExisting() {
        String appGatewayId = "/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/rg1478645787244/providers/Microsoft.Network/applicationGateways/ag1478645787244";
        ApplicationGateway ag  = azure.applicationGateways().getById(appGatewayId);
        TestApplicationGateway.printAppGateway(ag);
    }

    /**
     * Tests the public IP address implementation.
     * @throws Exception
     */
    @Test public void testPublicIpAddresses() throws Exception {
        new TestPublicIpAddress().runTest(azure.publicIpAddresses(), azure.resourceGroups());
    }

    /**
     * Tests the availability set implementation.
     * @throws Exception
     */
    @Test public void testAvailabilitySets() throws Exception {
        new TestAvailabilitySet().runTest(azure.availabilitySets(), azure.resourceGroups());
    }

    /**
     * Tests the virtual network implementation.
     * @throws Exception
     */
    @Test public void testNetworks() throws Exception {
        new TestNetwork.WithSubnets(azure.networkSecurityGroups())
            .runTest(azure.networks(), azure.resourceGroups());
    }

    /**
     * Tests route tables.
     * @throws Exception
     */
    @Test public void testRouteTables() throws Exception {
        new TestRouteTables.Minimal(azure.networks())
            .runTest(azure.routeTables(), azure.resourceGroups());
    }

    /**
     * Tests the regions enum
     */
    @Test public void testRegions() {
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
        Assert.assertTrue(region2 != null);
        Assert.assertTrue(region2.name().equals("madeUpRegion"));
        Region region3 = Region.fromName("madeupregion");
        Assert.assertTrue(region3 == region2);
        Assert.assertTrue(Region.values().length == regionsCount + 1);
    }

    /**
     * Tests the network interface implementation.
     * @throws Exception
     */
    @Test public void testNetworkInterfaces() throws Exception {
        new TestNetworkInterface().runTest(azure.networkInterfaces(), azure.resourceGroups());
    }

    /**
     * Tests virtual machines.
     * @throws Exception
     */
    @Test public void testVirtualMachines() throws Exception {
        // Future: This method needs to have a better specific name since we are going to include unit test for
        // different vm scenarios.
        new TestVirtualMachine().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests the virtual machine data disk implementation.
     * @throws Exception
     */
    @Test public void testVirtualMachineDataDisk() throws Exception {
        new TestVirtualMachineDataDisk().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests the virtual machine network interface implementation.
     * @throws Exception
     */
    @Test public void testVirtualMachineNics() throws Exception {
        new TestVirtualMachineNics(azure.resourceGroups(),
                    azure.networks(),
                    azure.networkInterfaces())
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests virtual machine support for SSH.
     * @throws Exception
     */
    @Test public void testVirtualMachineSSh() throws Exception {
        new TestVirtualMachineSsh(azure.publicIpAddresses())
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests virtual machine sizes.
     * @throws Exception
     */
    @Test public void testVirtualMachineSizes() throws Exception {
        new TestVirtualMachineSizes()
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    @Test public void testVirtualMachineCustomData() throws Exception {
        new TestVirtualMachineCustomData(azure.publicIpAddresses())
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    @Test public void testVirtualMachineInAvailabilitySet() throws Exception {
        new TestVirtualMachineInAvailabilitySet().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests subscription listing.
     * @throws Exception
     */
    @Test
    public void listSubscriptions() throws Exception {
        Assert.assertTrue(0 < subscriptions.list().size());
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
        String storageAccountName = "testsa" + String.valueOf(System.currentTimeMillis() % 100000L);
        StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup()
                .withSku(SkuName.PREMIUM_LRS)
                .create();

        Assert.assertEquals(storageAccount.name(), storageAccountName);
    }

    @Test
    public void testBatchAccount() throws Exception {
        new TestBatch().runTest(azure.batchAccounts(), azure.resourceGroups());
    }

    @Test
    public void testTrafficManager() throws Exception {
        new TestTrafficManager(azure.resourceGroups(), azure.publicIpAddresses())
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
        new TestDns()
                .runTest(azure.dnsZones(), azure.resourceGroups());
    }


    @Test
    public void testSqlServer() throws Exception {
        new TestSql().runTest(azure.sqlServers(), azure.resourceGroups());
    }

    @Test public void testResourceStreaming() throws Exception {
        new TestResourceStreaming(azure.storageAccounts(), azure.resourceGroups()).runTest(azure.virtualMachines(), azure.resourceGroups());
    }
}
