/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayOperationalState;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.Location;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
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
     * @throws IOException
     * @throws CloudException
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

        azure.resourceGroups().deleteByName("rg" + testId);
    }

    /**
     * Tests basic generic resources retrieval.
     * @throws Exception
     */
    @Test
    public void testGenericResources() throws Exception {
        PagedList<GenericResource> resources = azure.genericResources().listByResourceGroup("sdkpriv");
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
    @Test
    public void testVMImages() throws CloudException, IOException {
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
                azure.virtualMachines(),
                azure.availabilitySets())
            .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the inbound NAT pool support in load balancers.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersNatPools() throws Exception {
        new TestLoadBalancer.InternetWithNatPool(
                azure.virtualMachines(),
                azure.availabilitySets())
        .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the minimum Internet-facing load balancer.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersInternetMinimum() throws Exception {
        new TestLoadBalancer.InternetMinimal(
                azure.virtualMachines(),
                azure.availabilitySets())
            .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the minimum internal load balancer.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersInternalMinimum() throws Exception {
        new TestLoadBalancer.InternalMinimal(
                azure.virtualMachines(),
                azure.availabilitySets())
        .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests a complex internal application gateway
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternalComplex() throws Exception {
        new TestApplicationGateway.PrivateComplex()
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    @Test
    public void testManagedDiskVMUpdate() throws Exception {
        final String rgName = SdkContext.randomResourceName("rg", 13);
        final String linuxVM2Name = SdkContext.randomResourceName("vm" + "-", 10);
        final String linuxVM2Pip = SdkContext.randomResourceName("pip" + "-", 18);
        VirtualMachine linuxVM2 = azure.virtualMachines().define(linuxVM2Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(linuxVM2Pip)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("tester")
                .withRootPassword("Abcdef.123456!")
                // Begin: Managed data disks
                .withNewDataDisk(100)
                .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)
                // End: Managed data disks
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .create();

        linuxVM2.deallocate();
        linuxVM2.update()
                .withoutDataDisk(2)
                .withNewDataDisk(200)
                .apply();
        azure.resourceGroups().beginDeleteByName(rgName);
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

    @Test
    public void testApplicationGatewaysInParallel() throws Exception {
        String rgName = SdkContext.randomResourceName("rg", 13);
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> resourceGroup = azure.resourceGroups().define(rgName)
                .withRegion(region);
        List<Creatable<ApplicationGateway>> agCreatables = new ArrayList<>();

        agCreatables.add(azure.applicationGateways().define(SdkContext.randomResourceName("ag", 13))
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroup)
                .defineRequestRoutingRule("rule1")
                    .fromPrivateFrontend()
                    .fromFrontendHttpPort(80)
                    .toBackendHttpPort(8080)
                    .toBackendIPAddress("10.0.0.1")
                    .toBackendIPAddress("10.0.0.2")
                    .attach());

        agCreatables.add(azure.applicationGateways().define(SdkContext.randomResourceName("ag", 13))
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroup)
                .defineRequestRoutingRule("rule1")
                    .fromPrivateFrontend()
                    .fromFrontendHttpPort(80)
                    .toBackendHttpPort(8080)
                    .toBackendIPAddress("10.0.0.3")
                    .toBackendIPAddress("10.0.0.4")
                    .attach());

        CreatedResources<ApplicationGateway> created = azure.applicationGateways().create(agCreatables);
        List<ApplicationGateway> ags = new ArrayList<>();
        List<String> agIds = new ArrayList<>();
        for (Creatable<ApplicationGateway> creatable : agCreatables) {
            ApplicationGateway ag = created.get(creatable.key());
            Assert.assertNotNull(ag);
            ags.add(ag);
            agIds.add(ag.id());
        }

        azure.applicationGateways().stop(agIds);

        for (ApplicationGateway ag : ags) {
            Assert.assertEquals(ApplicationGatewayOperationalState.STOPPED, ag.refresh().operationalState());
        }

        azure.applicationGateways().start(agIds);

        for (ApplicationGateway ag : ags) {
            Assert.assertEquals(ApplicationGatewayOperationalState.RUNNING, ag.refresh().operationalState());
        }

        azure.applicationGateways().deleteByIds(agIds);
        for (String id : agIds) {
            Assert.assertNull(azure.applicationGateways().getById(id));
        }

        azure.resourceGroups().beginDeleteByName(rgName);
    }

    /**
     * Tests a minimal Internet-facing application gateway.
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternetFacingMinimal() throws Exception {
        new TestApplicationGateway.PublicMinimal()
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    /**
     * Tests a complex Internet-facing application gateway.
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternetFacingComplex() throws Exception {
        new TestApplicationGateway.PublicComplex()
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    /**
     * Tests the public IP address implementation.
     * @throws Exception
     */
    @Test
    public void testPublicIPAddresses() throws Exception {
        new TestPublicIPAddress().runTest(azure.publicIPAddresses(), azure.resourceGroups());
    }

    /**
     * Tests the availability set implementation.
     * @throws Exception
     */
    @Test
    public void testAvailabilitySets() throws Exception {
        new TestAvailabilitySet().runTest(azure.availabilitySets(), azure.resourceGroups());
    }

    /**
     * Tests the virtual network implementation.
     * @throws Exception
     */
    @Test
    public void testNetworks() throws Exception {
        new TestNetwork.WithSubnets()
            .runTest(azure.networks(), azure.resourceGroups());
    }

    /**
     * Tests route tables.
     * @throws Exception
     */
    @Test
    public void testRouteTables() throws Exception {
        new TestRouteTables.Minimal()
            .runTest(azure.routeTables(), azure.resourceGroups());
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
     * Tests the network interface implementation.
     * @throws Exception
     */
    @Test
    public void testNetworkInterfaces() throws Exception {
        new TestNetworkInterface().runTest(azure.networkInterfaces(), azure.resourceGroups());
    }

    /**
     * Tests virtual machines.
     * @throws Exception
     */
    @Test
    public void testVirtualMachines() throws Exception {
        // Future: This method needs to have a better specific name since we are going to include unit test for
        // different vm scenarios.
        new TestVirtualMachine().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests the virtual machine data disk implementation.
     * @throws Exception
     */
    @Test
    public void testVirtualMachineDataDisk() throws Exception {
        new TestVirtualMachineDataDisk().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests the virtual machine network interface implementation.
     * @throws Exception
     */
    @Test
    public void testVirtualMachineNics() throws Exception {
        new TestVirtualMachineNics(
                azure.networks(),
                azure.networkInterfaces())
            .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests virtual machine support for SSH.
     * @throws Exception
     */
    @Test
    public void testVirtualMachineSSh() throws Exception {
        new TestVirtualMachineSsh(azure.publicIPAddresses())
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests virtual machine sizes.
     * @throws Exception
     */
    @Test
    public void testVirtualMachineSizes() throws Exception {
        new TestVirtualMachineSizes()
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    @Test
    public void testVirtualMachineCustomData() throws Exception {
        new TestVirtualMachineCustomData(azure.publicIPAddresses())
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    @Test
    public void testVirtualMachineInAvailabilitySet() throws Exception {
        new TestVirtualMachineInAvailabilitySet().runTest(azure.virtualMachines(), azure.resourceGroups());
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

        azure.resourceGroups().deleteByName(storageAccount.resourceGroupName());
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
        addTextReplacementRule("https://management.azure.com:443", MOCK_URI);
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
}
