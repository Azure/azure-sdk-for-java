/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management;


import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.CloudException;
import com.azure.management.compute.CachingTypes;
import com.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.azure.management.compute.PowerState;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineImage;
import com.azure.management.compute.VirtualMachineOffer;
import com.azure.management.compute.VirtualMachinePublisher;
import com.azure.management.compute.VirtualMachineSizeTypes;
import com.azure.management.compute.VirtualMachineSku;
import com.azure.management.msi.implementation.MSIManager;
import com.azure.management.network.Access;
import com.azure.management.network.ConnectionMonitor;
import com.azure.management.network.ConnectionMonitorQueryResult;
import com.azure.management.network.ConnectivityCheck;
import com.azure.management.network.Direction;
import com.azure.management.network.FlowLogSettings;
import com.azure.management.network.IpFlowProtocol;
import com.azure.management.network.NetworkSecurityGroup;
import com.azure.management.network.NetworkWatcher;
import com.azure.management.network.NextHop;
import com.azure.management.network.NextHopType;
import com.azure.management.network.PacketCapture;
import com.azure.management.network.PcProtocol;
import com.azure.management.network.PcStatus;
import com.azure.management.network.SecurityGroupView;
import com.azure.management.network.Topology;
import com.azure.management.network.VerificationIPFlow;
import com.azure.management.resources.Deployment;
import com.azure.management.resources.DeploymentMode;
import com.azure.management.resources.GenericResource;
import com.azure.management.resources.Location;
import com.azure.management.resources.Subscription;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.CountryIsoCode;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.SkuName;
import com.azure.management.storage.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class AzureTests extends TestBase {
    private Azure azure;
    private MSIManager msiManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        Azure.Authenticated azureAuthed = Azure.authenticate(restClient, defaultSubscription, domain).withSdkContext(sdkContext);
        azure = azureAuthed.withSubscription(defaultSubscription);
        this.msiManager = MSIManager.authenticate(restClient, defaultSubscription, sdkContext);
    }

    @Override
    protected void cleanUpResources() {

    }

    /**
     * Stress-tests the resilience of ExpandableEnum to multi-threaded access
     *
     * @throws Exception
     */
    @Test
    public void testExpandableEnum() throws Exception {

        // Define some threads that read from enum
        Runnable reader1 = new Runnable() {
            @Override
            public void run() {
                Assertions.assertEquals(CountryIsoCode.AFGHANISTAN, CountryIsoCode.fromString("AF"));
                Assertions.assertEquals(CountryIsoCode.ANTARCTICA, CountryIsoCode.fromString("AQ"));
                Assertions.assertEquals(CountryIsoCode.ANDORRA, CountryIsoCode.fromString("AD"));
                Assertions.assertEquals(CountryIsoCode.ARGENTINA, CountryIsoCode.fromString("AR"));
                Assertions.assertEquals(CountryIsoCode.ALBANIA, CountryIsoCode.fromString("AL"));
                Assertions.assertEquals(CountryIsoCode.ALGERIA, CountryIsoCode.fromString("DZ"));
                Assertions.assertEquals(CountryIsoCode.AMERICAN_SAMOA, CountryIsoCode.fromString("AS"));
                Assertions.assertEquals(CountryIsoCode.ANGOLA, CountryIsoCode.fromString("AO"));
                Assertions.assertEquals(CountryIsoCode.ANGUILLA, CountryIsoCode.fromString("AI"));
                Assertions.assertEquals(CountryIsoCode.ANTIGUA_AND_BARBUDA, CountryIsoCode.fromString("AG"));
                Assertions.assertEquals(CountryIsoCode.ARMENIA, CountryIsoCode.fromString("AM"));
                Assertions.assertEquals(CountryIsoCode.ARUBA, CountryIsoCode.fromString("AW"));
                Assertions.assertEquals(CountryIsoCode.AUSTRALIA, CountryIsoCode.fromString("AU"));
                Assertions.assertEquals(CountryIsoCode.AUSTRIA, CountryIsoCode.fromString("AT"));
                Assertions.assertEquals(CountryIsoCode.AZERBAIJAN, CountryIsoCode.fromString("AZ"));
                Assertions.assertEquals(PowerState.DEALLOCATED, PowerState.fromString("PowerState/deallocated"));
                Assertions.assertEquals(PowerState.DEALLOCATING, PowerState.fromString("PowerState/deallocating"));
                Assertions.assertEquals(PowerState.RUNNING, PowerState.fromString("PowerState/running"));
            }
        };

        Runnable reader2 = new Runnable() {
            @Override
            public void run() {
                Assertions.assertEquals(CountryIsoCode.BAHAMAS, CountryIsoCode.fromString("BS"));
                Assertions.assertEquals(CountryIsoCode.BAHRAIN, CountryIsoCode.fromString("BH"));
                Assertions.assertEquals(CountryIsoCode.BANGLADESH, CountryIsoCode.fromString("BD"));
                Assertions.assertEquals(CountryIsoCode.BARBADOS, CountryIsoCode.fromString("BB"));
                Assertions.assertEquals(CountryIsoCode.BELARUS, CountryIsoCode.fromString("BY"));
                Assertions.assertEquals(CountryIsoCode.BELGIUM, CountryIsoCode.fromString("BE"));
                Assertions.assertEquals(PowerState.STARTING, PowerState.fromString("PowerState/starting"));
                Assertions.assertEquals(PowerState.STOPPED, PowerState.fromString("PowerState/stopped"));
                Assertions.assertEquals(PowerState.STOPPING, PowerState.fromString("PowerState/stopping"));
                Assertions.assertEquals(PowerState.UNKNOWN, PowerState.fromString("PowerState/unknown"));
            }
        };

        // Define some threads that write to enum
        Runnable writer1 = new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 10; i++) {
                    CountryIsoCode.fromString("CountryIsoCode" + i);
                    PowerState.fromString("PowerState" + i);
                }
            }
        };

        Runnable writer2 = new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 20; i++) {
                    CountryIsoCode.fromString("CountryIsoCode" + i);
                    PowerState.fromString("PowerState" + i);
                }
            }
        };

        // Start the threads and repeat a few times
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        for (int repeat = 0; repeat < 10; repeat++) {
            threadPool.submit(reader1);
            threadPool.submit(reader2);
            threadPool.submit(writer1);
            threadPool.submit(writer2);
        }

        // Give the test a fixed amount of time to finish
        threadPool.awaitTermination(10, TimeUnit.SECONDS);

        // Verify country ISO codes
        Collection<CountryIsoCode> countryIsoCodes = CountryIsoCode.values();
        System.out.println("\n## Country ISO codes: " + countryIsoCodes.size());
        for (CountryIsoCode value : countryIsoCodes) {
            System.out.println(value.toString());
        }
        Assertions.assertEquals(257, countryIsoCodes.size());

        // Verify power states
        Collection<PowerState> powerStates = PowerState.values();
        System.out.println("\n## Power states: " + powerStates.size());
        for (PowerState value : powerStates) {
            System.out.println(value.toString());
        }
        Assertions.assertEquals(27, powerStates.size());
    }

    /**
     * Tests ARM template deployments.
     *
     * @throws IOException
     * @throws CloudException
     */
    @Test
    public void testDeployments() throws Exception {
        String testId = azure.deployments().manager().getSdkContext().randomResourceName("", 8);
        PagedIterable<Deployment> deployments = azure.deployments().list();
        System.out.println("Deployments: " + TestUtilities.getSize(deployments));
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
     *
     * @throws Exception
     */
    @Test
    public void testGenericResources() throws Exception {
        // Create some resources
        NetworkSecurityGroup nsg = azure.networkSecurityGroups().define(azure.networkSecurityGroups().manager().getSdkContext().randomResourceName("nsg", 13))
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .create();
        azure.publicIPAddresses().define(azure.networkSecurityGroups().manager().getSdkContext().randomResourceName("pip", 13))
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(nsg.resourceGroupName())
                .create();

        PagedIterable<GenericResource> resources = azure.genericResources().listByResourceGroup(nsg.resourceGroupName());
        Assertions.assertEquals(2, TestUtilities.getSize(resources));
        GenericResource firstResource = resources.iterator().next();

        GenericResource resourceById = azure.genericResources().getById(firstResource.id());
        GenericResource resourceByDetails = azure.genericResources().get(
                firstResource.resourceGroupName(),
                firstResource.resourceProviderNamespace(),
                firstResource.resourceType(),
                firstResource.name());
        Assertions.assertTrue(resourceById.id().equalsIgnoreCase(resourceByDetails.id()));
        azure.resourceGroups().beginDeleteByName(nsg.resourceGroupName());
    }

//    /**
//     * Tests management locks.
//     * NOTE: This requires the service principal to have an Owner role on the subscription
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testManagementLocks() throws Exception {
//        // Prepare a VM
//        final String password = SdkContext.randomResourceName("P@s", 14);
//        final String rgName = SdkContext.randomResourceName("rg", 15);
//        final String vmName = SdkContext.randomResourceName("vm", 15);
//        final String storageName = SdkContext.randomResourceName("st", 15);
//        final String diskName = SdkContext.randomResourceName("dsk", 15);
//        final String netName = SdkContext.randomResourceName("net", 15);
//        final Region region = Region.US_EAST;
//
//        ResourceGroup resourceGroup = null;
//        ManagementLock lockGroup = null,
//                lockVM = null,
//                lockStorage = null,
//                lockDiskRO = null,
//                lockDiskDel = null,
//                lockSubnet = null;
//        try {
//            resourceGroup = azure.resourceGroups().define(rgName)
//                    .withRegion(region)
//                    .create();
//            Assert.assertNotNull(resourceGroup);
//
//            Creatable<Network> netDefinition = azure.networks().define(netName)
//                    .withRegion(region)
//                    .withExistingResourceGroup(resourceGroup)
//                    .withAddressSpace("10.0.0.0/28");
//
//            // Define a VM for testing VM locks
//            Creatable<VirtualMachine> vmDefinition = azure.virtualMachines().define(vmName)
//                    .withRegion(region)
//                    .withExistingResourceGroup(resourceGroup)
//                    .withNewPrimaryNetwork(netDefinition)
//                    .withPrimaryPrivateIPAddressDynamic()
//                    .withoutPrimaryPublicIPAddress()
//                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
//                    .withRootUsername("tester")
//                    .withRootPassword(password)
//                    .withSize(VirtualMachineSizeTypes.BASIC_A1);
//
//            // Define a managed disk for testing locks on that
//            Creatable<Disk> diskDefinition = azure.disks().define(diskName)
//                    .withRegion(region)
//                    .withExistingResourceGroup(resourceGroup)
//                    .withData()
//                    .withSizeInGB(100);
//
//            // Define a storage account for testing locks on that
//            Creatable<StorageAccount> storageDefinition = azure.storageAccounts().define(storageName)
//                    .withRegion(region)
//                    .withExistingResourceGroup(resourceGroup);
//
//            // Create resources in parallel to save time and money
//            Observable.merge(
//                    storageDefinition.createAsync().subscribeOn(Schedulers.io()),
//                    vmDefinition.createAsync().subscribeOn(Schedulers.io()),
//                    diskDefinition.createAsync().subscribeOn(Schedulers.io()))
//                    .toBlocking().subscribe();
//
//            VirtualMachine vm = (VirtualMachine) vmDefinition;
//            StorageAccount storage = (StorageAccount) storageDefinition;
//            Disk disk = (Disk) diskDefinition;
//            Network network = vm.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();
//            Subnet subnet = network.subnets().values().iterator().next();
//
//            // Lock subnet
//            Creatable<ManagementLock> lockSubnetDef = azure.managementLocks().define("subnetLock")
//                    .withLockedResource(subnet.inner().id())
//                    .withLevel(LockLevel.READ_ONLY);
//
//            // Lock VM
//            Creatable<ManagementLock> lockVMDef = azure.managementLocks().define("vmlock")
//                    .withLockedResource(vm)
//                    .withLevel(LockLevel.READ_ONLY)
//                    .withNotes("vm readonly lock");
//
//            // Lock resource group
//            Creatable<ManagementLock> lockGroupDef = azure.managementLocks().define("rglock")
//                    .withLockedResource(resourceGroup.id())
//                    .withLevel(LockLevel.CAN_NOT_DELETE);
//
//            // Lock storage
//            Creatable<ManagementLock> lockStorageDef = azure.managementLocks().define("stLock")
//                    .withLockedResource(storage)
//                    .withLevel(LockLevel.CAN_NOT_DELETE);
//
//            // Create locks in parallel
//            @SuppressWarnings("unchecked")
//            CreatedResources<ManagementLock> created = azure.managementLocks().create(lockVMDef, lockGroupDef, lockStorageDef, lockSubnetDef);
//            lockVM = created.get(lockVMDef.key());
//            lockStorage = created.get(lockStorageDef.key());
//            lockGroup = created.get(lockGroupDef.key());
//            lockSubnet = created.get(lockSubnetDef.key());
//
//            // Lock disk synchronously
//            lockDiskRO = azure.managementLocks().define("diskLockRO")
//                    .withLockedResource(disk)
//                    .withLevel(LockLevel.READ_ONLY)
//                    .create();
//
//            lockDiskDel = azure.managementLocks().define("diskLockDel")
//                    .withLockedResource(disk)
//                    .withLevel(LockLevel.CAN_NOT_DELETE)
//                    .create();
//
//            // Verify VM lock
//            Assert.assertEquals(2, azure.managementLocks().listForResource(vm.id()).size());
//
//            Assert.assertNotNull(lockVM);
//            lockVM = azure.managementLocks().getById(lockVM.id());
//            Assert.assertNotNull(lockVM);
//            TestUtils.print(lockVM);
//            Assert.assertEquals(LockLevel.READ_ONLY, lockVM.level());
//            Assert.assertTrue(vm.id().equalsIgnoreCase(lockVM.lockedResourceId()));
//
//            // Verify resource group lock
//            Assert.assertNotNull(lockGroup);
//            lockGroup = azure.managementLocks().getByResourceGroup(resourceGroup.name(), "rglock");
//            Assert.assertNotNull(lockGroup);
//            TestUtils.print(lockVM);
//            Assert.assertEquals(LockLevel.CAN_NOT_DELETE, lockGroup.level());
//            Assert.assertTrue(resourceGroup.id().equalsIgnoreCase(lockGroup.lockedResourceId()));
//
//            // Verify storage account lock
//            Assert.assertEquals(2, azure.managementLocks().listForResource(storage.id()).size());
//
//            Assert.assertNotNull(lockStorage);
//            lockStorage = azure.managementLocks().getById(lockStorage.id());
//            Assert.assertNotNull(lockStorage);
//            TestUtils.print(lockStorage);
//            Assert.assertEquals(LockLevel.CAN_NOT_DELETE, lockStorage.level());
//            Assert.assertTrue(storage.id().equalsIgnoreCase(lockStorage.lockedResourceId()));
//
//            // Verify disk lock
//            Assert.assertEquals(3, azure.managementLocks().listForResource(disk.id()).size());
//
//            Assert.assertNotNull(lockDiskRO);
//            lockDiskRO = azure.managementLocks().getById(lockDiskRO.id());
//            Assert.assertNotNull(lockDiskRO);
//            TestUtils.print(lockDiskRO);
//            Assert.assertEquals(LockLevel.READ_ONLY, lockDiskRO.level());
//            Assert.assertTrue(disk.id().equalsIgnoreCase(lockDiskRO.lockedResourceId()));
//
//            Assert.assertNotNull(lockDiskDel);
//            lockDiskDel = azure.managementLocks().getById(lockDiskDel.id());
//            Assert.assertNotNull(lockDiskDel);
//            TestUtils.print(lockDiskDel);
//            Assert.assertEquals(LockLevel.CAN_NOT_DELETE, lockDiskDel.level());
//            Assert.assertTrue(disk.id().equalsIgnoreCase(lockDiskDel.lockedResourceId()));
//
//            // Verify subnet lock
//            Assert.assertEquals(2, azure.managementLocks().listForResource(network.id()).size());
//
//            lockSubnet = azure.managementLocks().getById(lockSubnet.id());
//            Assert.assertNotNull(lockSubnet);
//            TestUtils.print(lockSubnet);
//            Assert.assertEquals(LockLevel.READ_ONLY, lockSubnet.level());
//            Assert.assertTrue(subnet.inner().id().equalsIgnoreCase(lockSubnet.lockedResourceId()));
//
//            // Verify lock collection
//            List<ManagementLock> locksSubscription = azure.managementLocks().list();
//            List<ManagementLock> locksGroup = azure.managementLocks().listByResourceGroup(vm.resourceGroupName());
//            Assert.assertNotNull(locksSubscription);
//            Assert.assertNotNull(locksGroup);
//
//            int locksAllCount = locksSubscription.size();
//            System.out.println("All locks: " + locksAllCount);
//            Assert.assertTrue(6 <= locksAllCount);
//
//            int locksGroupCount = locksGroup.size();
//            System.out.println("Group locks: " + locksGroupCount);
//            Assert.assertEquals(6, locksGroup.size());
//        } catch (Exception ex) {
//            ex.printStackTrace(System.out);
//        } finally {
//            if (resourceGroup != null) {
//                if (lockGroup != null) {
//                    azure.managementLocks().deleteById(lockGroup.id());
//                }
//                if (lockVM != null) {
//                    azure.managementLocks().deleteById(lockVM.id());
//                }
//                if (lockDiskRO != null) {
//                    azure.managementLocks().deleteById(lockDiskRO.id());
//                }
//                if (lockDiskDel != null) {
//                    azure.managementLocks().deleteById(lockDiskDel.id());
//                }
//                if (lockStorage != null) {
//                    azure.managementLocks().deleteById(lockStorage.id());
//                }
//                if (lockSubnet != null) {
//                    azure.managementLocks().deleteById(lockSubnet.id());
//                }
//                azure.resourceGroups().beginDeleteByName(resourceGroup.name());
//            }
//        }
//    }
//

    /**
     * Tests VM images.
     *
     * @throws IOException
     * @throws CloudException
     */
    @Test
    public void testVMImages() throws CloudException, IOException {
        PagedIterable<VirtualMachinePublisher> publishers = azure.virtualMachineImages().publishers().listByRegion(Region.US_WEST);
        Assertions.assertTrue(TestUtilities.getSize(publishers) > 0);
        for (VirtualMachinePublisher p : publishers.stream().limit(5).toArray(VirtualMachinePublisher[]::new)) {
            System.out.println(String.format("Publisher name: %s, region: %s", p.name(), p.region()));
            for (VirtualMachineOffer o : p.offers().list().stream().limit(5).toArray(VirtualMachineOffer[]::new)) {
                System.out.println(String.format("\tOffer name: %s", o.name()));
                for (VirtualMachineSku s : o.skus().list().stream().limit(5).toArray(VirtualMachineSku[]::new)) {
                    System.out.println(String.format("\t\tSku name: %s", s.name()));
                }
            }
        }
        // TODO: limit vm images by filter
        PagedIterable<VirtualMachineImage> images = azure.virtualMachineImages().listByRegion(Region.US_WEST);
        Assertions.assertTrue(TestUtilities.getSize(images) > 0);
        // Seems to help avoid connection refused error on subsequent mock test
        SdkContext.sleep(2000);
    }

    /**
     * Tests the network security group implementation.
     *
     * @throws Exception
     */
    @Test
    public void testNetworkSecurityGroups() throws Exception {
        new TestNSG().runTest(azure.networkSecurityGroups(), azure.resourceGroups());
    }

    /**
     * Tests the inbound NAT rule support in load balancers.
     *
     * @throws Exception
     */
    @Test
    public void testLoadBalancersNatRules() throws Exception {
        new TestLoadBalancer().new InternetWithNatRule(azure.virtualMachines().manager())
                .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the inbound NAT pool support in load balancers.
     *
     * @throws Exception
     */
    @Test
    public void testLoadBalancersNatPools() throws Exception {
        new TestLoadBalancer().new InternetWithNatPool(azure.virtualMachines().manager())
                .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the minimum Internet-facing load balancer with a load balancing rule only
     *
     * @throws Exception
     */
    @Test
    public void testLoadBalancersInternetMinimum() throws Exception {
        new TestLoadBalancer().new InternetMinimal(azure.virtualMachines().manager())
                .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the minimum Internet-facing load balancer with a NAT rule only
     *
     * @throws Exception
     */
    @Test
    public void testLoadBalancersNatOnly() throws Exception {
        new TestLoadBalancer().new InternetNatOnly(azure.virtualMachines().manager())
                .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the minimum internal load balancer.
     *
     * @throws Exception
     */
    @Test
    public void testLoadBalancersInternalMinimum() throws Exception {
        new TestLoadBalancer().new InternalMinimal(azure.virtualMachines().manager())
                .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the internal load balancer with availability zone.
     *
     * @throws Exception
     */
    @Test
    @Disabled("Though valid scenario, NRP is failing")
    public void testLoadBalancersInternalWithAvailabilityZone() throws Exception {
        new TestLoadBalancer().new InternalWithZone(azure.virtualMachines().manager())
                .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    @Test
    public void testManagedDiskVMUpdate() throws Exception {
        SdkContext context = azure.disks().manager().getSdkContext();
        final String rgName = context.randomResourceName("rg", 13);
        final String linuxVM2Name = context.randomResourceName("vm" + "-", 10);
        final String linuxVM2Pip = context.randomResourceName("pip" + "-", 18);
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
     * Tests the public IP address implementation.
     *
     * @throws Exception
     */
    @Test
    public void testPublicIPAddresses() throws Exception {
        new TestPublicIPAddress().runTest(azure.publicIPAddresses(), azure.resourceGroups());
    }
//
//    /**
//     * Tests the public IP prefix implementation.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testPublicIPPrefixes() throws Exception {
//        new TestPublicIPPrefix().runTest(azure.publicIPPrefixes(), azure.resourceGroups());
//    }

    /**
     * Tests the availability set implementation.
     *
     * @throws Exception
     */
    @Test
    public void testAvailabilitySets() throws Exception {
        new TestAvailabilitySet().runTest(azure.availabilitySets(), azure.resourceGroups());
    }

    /**
     * Tests the virtual network implementation.
     *
     * @throws Exception
     */
    @Test
    public void testNetworks() throws Exception {
        new TestNetwork().new WithSubnets()
                .runTest(azure.networks(), azure.resourceGroups());
    }

    /**
     * Tests virtual network peering
     *
     * @throws Exception
     */
    @Test
    public void testNetworkWithAccessFromServiceToSubnet() throws Exception {
        new TestNetwork().new WithAccessFromServiceToSubnet()
                .runTest(azure.networks(), azure.resourceGroups());
    }

    /**
     * Tests virtual network peering
     *
     * @throws Exception
     */
    @Test
    public void testNetworkPeerings() throws Exception {
        new TestNetwork().new WithPeering()
                .runTest(azure.networks(), azure.resourceGroups());
    }

    /**
     * Tests virtual network with DDoS protection plan
     *
     * @throws Exception
     */
    @Test
    public void testDdosAndVmProtection() throws Exception {
        new TestNetwork().new WithDDosProtectionPlanAndVmProtection()
                .runTest(azure.networks(), azure.resourceGroups());
    }

    /**
     * Tests updateTags for virtual network.
     *
     * @throws Exception
     */
    @Test
    public void testNetworkUpdateTags() throws Exception {
        new TestNetwork().new WithUpdateTags().runTest(azure.networks(), azure.resourceGroups());
    }

    /**
     * Tests route tables.
     *
     * @throws Exception
     */
    @Test
    public void testRouteTables() throws Exception {
        new TestRouteTables().new Minimal()
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
        Assertions.assertTrue(region == Region.US_WEST);

        // Add a region
        Region region2 = Region.fromName("madeUpRegion");
        Assertions.assertNotNull(region2);
        Assertions.assertTrue(region2.name().equalsIgnoreCase("madeUpRegion"));
        Region region3 = Region.fromName("madeupregion");
        Assertions.assertEquals(region3, region2);
        Assertions.assertEquals(Region.values().length, regionsCount + 1);
    }

    /**
     * Tests the network interface implementation.
     *
     * @throws Exception
     */
    @Test
    public void testNetworkInterfaces() throws Exception {
        new TestNetworkInterface().runTest(azure.networkInterfaces(), azure.resourceGroups());
    }

    /**
     * Tests the network watcher implementation.
     *
     * @throws Exception
     */
    @Test
    public void testNetworkWatchers() throws Exception {
        new TestNetworkWatcher().runTest(azure.networkWatchers(), azure.resourceGroups());
    }

    @Test
    @Disabled("Not stable test cases")
    public void testNetworkWatcherFunctions() throws Exception {
        String nwrg = null;
        String tnwrg = null;
        try {
            TestNetworkWatcher tnw = new TestNetworkWatcher();

            NetworkWatcher nw = tnw.createResource(azure.networkWatchers());

            tnwrg = tnw.groupName();
            nwrg = nw.resourceGroupName();

            // pre-create VMs to show topology on
            VirtualMachine[] virtualMachines = tnw.ensureNetwork(azure.networkWatchers().manager().networks(),
                    azure.virtualMachines(), azure.networkInterfaces());

            ConnectionMonitor connectionMonitor = nw.connectionMonitors()
                    .define("NewConnectionMonitor")
                    .withSourceId(virtualMachines[0].id())
                    .withDestinationId(virtualMachines[1].id())
                    .withDestinationPort(80)
                    .withTag("tag1", "value1")
                    .withoutAutoStart()
                    .withMonitoringInterval(35)
                    .create();
            Assertions.assertEquals("value1", connectionMonitor.tags().get("tag1"));
            Assertions.assertEquals(35, connectionMonitor.monitoringIntervalInSeconds());
            Assertions.assertEquals("NotStarted", connectionMonitor.monitoringStatus());
            Assertions.assertEquals("NewConnectionMonitor", connectionMonitor.name());

            connectionMonitor.start();
            Assertions.assertEquals("Running", connectionMonitor.monitoringStatus());
            Topology topology = nw.topology().withTargetResourceGroup(virtualMachines[0].resourceGroupName()).execute();
            Assertions.assertEquals(11, topology.resources().size());
            Assertions.assertTrue(topology.resources().containsKey(virtualMachines[0].getPrimaryNetworkInterface().networkSecurityGroupId()));
            Assertions.assertEquals(4, topology.resources().get(virtualMachines[0].primaryNetworkInterfaceId()).associations().size());

            SecurityGroupView sgViewResult = nw.getSecurityGroupView(virtualMachines[0].id());
            Assertions.assertEquals(1, sgViewResult.networkInterfaces().size());
            Assertions.assertEquals(virtualMachines[0].primaryNetworkInterfaceId(), sgViewResult.networkInterfaces().keySet().iterator().next());

            FlowLogSettings flowLogSettings = nw.getFlowLogSettings(virtualMachines[0].getPrimaryNetworkInterface().networkSecurityGroupId());
            StorageAccount storageAccount = tnw.ensureStorageAccount(azure.storageAccounts());
            flowLogSettings.update()
                    .withLogging()
                    .withStorageAccount(storageAccount.id())
                    .withRetentionPolicyDays(5)
                    .withRetentionPolicyEnabled()
                    .apply();
            Assertions.assertEquals(true, flowLogSettings.enabled());
            Assertions.assertEquals(5, flowLogSettings.retentionDays());
            Assertions.assertEquals(storageAccount.id(), flowLogSettings.storageId());

            NextHop nextHop = nw.nextHop().withTargetResourceId(virtualMachines[0].id())
                    .withSourceIPAddress("10.0.0.4")
                    .withDestinationIPAddress("8.8.8.8")
                    .execute();
            Assertions.assertEquals("System Route", nextHop.routeTableId());
            Assertions.assertEquals(NextHopType.INTERNET, nextHop.nextHopType());
            Assertions.assertNull(nextHop.nextHopIpAddress());

            VerificationIPFlow verificationIPFlow = nw.verifyIPFlow()
                    .withTargetResourceId(virtualMachines[0].id())
                    .withDirection(Direction.OUTBOUND)
                    .withProtocol(IpFlowProtocol.TCP)
                    .withLocalIPAddress("10.0.0.4")
                    .withRemoteIPAddress("8.8.8.8")
                    .withLocalPort("443")
                    .withRemotePort("443")
                    .execute();
            Assertions.assertEquals(Access.ALLOW, verificationIPFlow.access());
            Assertions.assertTrue("defaultSecurityRules/AllowInternetOutBound".equalsIgnoreCase(verificationIPFlow.ruleName()));

            // test packet capture
            PagedIterable<PacketCapture> packetCaptures = nw.packetCaptures().list();
            Assertions.assertEquals(0, TestUtilities.getSize(packetCaptures));
            PacketCapture packetCapture = nw.packetCaptures()
                    .define("NewPacketCapture")
                    .withTarget(virtualMachines[0].id())
                    .withStorageAccountId(storageAccount.id())
                    .withTimeLimitInSeconds(1500)
                    .definePacketCaptureFilter()
                    .withProtocol(PcProtocol.TCP)
                    .withLocalIPAddresses(Arrays.asList("127.0.0.1", "127.0.0.5"))
                    .attach()
                    .create();
            packetCaptures = nw.packetCaptures().list();
            Assertions.assertEquals(1, TestUtilities.getSize(packetCaptures));
            Assertions.assertEquals("NewPacketCapture", packetCapture.name());
            Assertions.assertEquals(1500, packetCapture.timeLimitInSeconds());
            Assertions.assertEquals(PcProtocol.TCP, packetCapture.filters().get(0).protocol());
            Assertions.assertEquals("127.0.0.1;127.0.0.5", packetCapture.filters().get(0).localIPAddress());
//            Assertions.assertEquals("Running", packetCapture.getStatus().packetCaptureStatus().toString());
            packetCapture.stop();
            Assertions.assertEquals(PcStatus.STOPPED, packetCapture.getStatus().packetCaptureStatus());
            nw.packetCaptures().deleteByName(packetCapture.name());

            ConnectivityCheck connectivityCheck = nw.checkConnectivity()
                    .toDestinationResourceId(virtualMachines[1].id())
                    .toDestinationPort(80)
                    .fromSourceVirtualMachine(virtualMachines[0].id())
                    .execute();
//            Assertions.assertEquals("Reachable", connectivityCheck.connectionStatus().toString());    // not sure why it is Unknown now

            ConnectionMonitorQueryResult queryResult = connectionMonitor.query();

            azure.virtualMachines().deleteById(virtualMachines[1].id());
            topology.execute();
//            Assert.assertEquals(10, topology.resources().size());     // not sure why it is 18 now
        } finally {
            if (nwrg != null)
                azure.resourceGroups().beginDeleteByName(nwrg);
            if (tnwrg != null)
                azure.resourceGroups().beginDeleteByName(tnwrg);
        }
    }

    /**
     * Tests the local network gateway implementation.
     *
     * @throws Exception
     */
    @Test
    public void testLocalNetworkGateways() throws Exception {
        new TestLocalNetworkGateway().runTest(azure.localNetworkGateways(), azure.resourceGroups());
    }

    /**
     * Tests the express route circuit implementation.
     *
     * @throws Exception
     */
    @Test
    public void testExpressRouteCircuits() throws Exception {
        new TestExpressRouteCircuit().new Basic().runTest(azure.expressRouteCircuits(), azure.resourceGroups());
    }

    /**
     * Tests the express route circuit peerings implementation.
     *
     * @throws Exception
     */
    @Test
    public void testExpressRouteCircuitPeering() throws Exception {
        new TestExpressRouteCircuit().new ExpressRouteCircuitPeering().runTest(azure.expressRouteCircuits(), azure.resourceGroups());
    }

    /**
     * Tests virtual machines.
     *
     * @throws Exception
     */
    @Test
    @Disabled("osDiskSize is returned as 127 instead of 128 - known service bug")
    public void testVirtualMachines() throws Exception {
        // Future: This method needs to have a better specific name since we are going to include unit test for
        // different vm scenarios.
        new TestVirtualMachine().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests the virtual machine data disk implementation.
     *
     * @throws Exception
     */
    @Test
    public void testVirtualMachineDataDisk() throws Exception {
        new TestVirtualMachineDataDisk().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests the virtual machine network interface implementation.
     *
     * @throws Exception
     */
    @Test
    public void testVirtualMachineNics() throws Exception {
        new TestVirtualMachineNics(azure.networks().manager())
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests virtual machine support for SSH.
     *
     * @throws Exception
     */
    @Test
    public void testVirtualMachineSSh() throws Exception {
        new TestVirtualMachineSsh(azure.publicIPAddresses())
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests virtual machine sizes.
     *
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
     *
     * @throws Exception
     */
    @Test
    public void listSubscriptions() throws Exception {
        Assertions.assertTrue(0 < TestUtilities.getSize(azure.subscriptions().list()));
        Subscription subscription = azure.getCurrentSubscription();
        Assertions.assertNotNull(subscription);
        Assertions.assertTrue(azure.subscriptionId().equalsIgnoreCase(subscription.subscriptionId()));
    }

    /**
     * Tests location listing.
     *
     * @throws Exception
     */
    @Test
    public void listLocations() throws Exception {
        Subscription subscription = azure.getCurrentSubscription();
        Assertions.assertNotNull(subscription);
        for (Location location : subscription.listLocations()) {
            Region region = Region.fromName(location.name());
            Assertions.assertNotNull(region, "Could not find region " + location.name());
            Assertions.assertEquals(region, location.region());
            Assertions.assertEquals(region.name().toLowerCase(), location.name().toLowerCase());
        }

        Location location = subscription.getLocationByRegion(Region.US_WEST);
        Assertions.assertNotNull(location);
        Assertions.assertTrue(Region.US_WEST.name().equalsIgnoreCase(location.name()));
    }

    /**
     * Tests resource group listing.
     *
     * @throws Exception
     */
    @Test
    public void listResourceGroups() throws Exception {
        int groupCount = TestUtilities.getSize(azure.resourceGroups().list());
        System.out.println(String.format("Group count: %s", groupCount));
        Assertions.assertTrue(0 < groupCount);
    }

    /**
     * Tests storage account listing.
     *
     * @throws Exception
     */
    @Test
    public void listStorageAccounts() throws Exception {
        Assertions.assertTrue(0 < TestUtilities.getSize(azure.storageAccounts().list()));
    }

    @Test
    public void createStorageAccount() throws Exception {
        String storageAccountName = generateRandomResourceName("testsa", 12);
        StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup()
                .withSku(SkuName.PREMIUM_LRS)
                .create();

        Assertions.assertEquals(storageAccount.name(), storageAccountName);

        azure.resourceGroups().beginDeleteByName(storageAccount.resourceGroupName());
    }

//    @Test
//    public void testBatchAccount() throws Exception {
//        new TestBatch().runTest(azure.batchAccounts(), azure.resourceGroups());
//    }

    //    @Test
//    public void testTrafficManager() throws Exception {
//        new TestTrafficManager(azure.publicIPAddresses())
//                .runTest(azure.trafficManagerProfiles(), azure.resourceGroups());
//    }
//
//    @Test
//    public void testRedis() throws Exception {
//        new TestRedis()
//                .runTest(azure.redisCaches(), azure.resourceGroups());
//    }
//
//    @Test
//    public void testCdnManager() throws Exception {
//        new TestCdn()
//                .runTest(azure.cdnProfiles(), azure.resourceGroups());
//    }

//    @Test
//    public void testDnsZones() throws Exception {
//        addTextReplacementRule("https://management.azure.com:443/", playbackUri + "/");
//        new TestDns()
//                .runTest(azure.dnsZones(), azure.resourceGroups());
//    }

    @Test
    public void testSqlServer() throws Exception {
        new TestSql().runTest(azure.sqlServers(), azure.resourceGroups());
    }

    @Test
    public void testResourceStreaming() throws Exception {
        new TestResourceStreaming(azure.storageAccounts()).runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    @Test
    @Disabled("Container service will be deprecated")
    public void testContainerService() throws Exception {
        new TestContainerService()
                .runTest(azure.containerServices(), azure.resourceGroups());
    }

    @Test
    public void testKubernetesCluster() throws Exception {
        new TestKubernetesCluster()
                .runTest(azure.kubernetesClusters(), azure.resourceGroups());
    }

//    @Test
//    public void testContainerInstanceWithPublicIpAddressWithSystemAssignedMsi() throws Exception {
//        new TestContainerInstanceWithPublicIpAddressWithSystemAssignedMSI().runTest(azure.containerGroups(), azure.resourceGroups(), azure.subscriptionId());
//    }
//
//    @Test
//    public void testContainerInstanceWithPublicIpAddressWithUserAssignedMsi() throws Exception {
//        final String cgName = SdkContext.randomResourceName("aci", 10);
//        final String rgName = SdkContext.randomResourceName("rgaci", 10);
//        String identityName1 = generateRandomResourceName("msi-id", 15);
//        String identityName2 = generateRandomResourceName("msi-id", 15);
//
//        final Identity createdIdentity = msiManager.identities()
//                .define(identityName1)
//                .withRegion(Region.US_WEST)
//                .withNewResourceGroup(rgName)
//                .withAccessToCurrentResourceGroup(BuiltInRole.READER)
//                .create();
//
//        Creatable<Identity> creatableIdentity = msiManager.identities()
//                .define(identityName2)
//                .withRegion(Region.US_WEST)
//                .withExistingResourceGroup(rgName)
//                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR);
//
//
//        List<String> dnsServers = new ArrayList<String>();
//        dnsServers.add("dnsServer1");
//        ContainerGroup containerGroup = azure.containerGroups().define(cgName)
//                .withRegion(Region.US_EAST2)
//                .withExistingResourceGroup(rgName)
//                .withLinux()
//                .withPublicImageRegistryOnly()
//                .withEmptyDirectoryVolume("emptydir1")
//                .defineContainerInstance("tomcat")
//                .withImage("tomcat")
//                .withExternalTcpPort(8080)
//                .withCpuCoreCount(1)
//                .withEnvironmentVariable("ENV1", "value1")
//                .attach()
//                .defineContainerInstance("nginx")
//                .withImage("nginx")
//                .withExternalTcpPort(80)
//                .withEnvironmentVariableWithSecuredValue("ENV2", "securedValue1")
//                .attach()
//                .withExistingUserAssignedManagedServiceIdentity(createdIdentity)
//                .withNewUserAssignedManagedServiceIdentity(creatableIdentity)
//                .withRestartPolicy(ContainerGroupRestartPolicy.NEVER)
//                .withDnsPrefix(cgName)
//                .withTag("tag1", "value1")
//                .create();
//
//        Assert.assertEquals(cgName, containerGroup.name());
//        Assert.assertEquals("Linux", containerGroup.osType().toString());
//        Assert.assertEquals(0, containerGroup.imageRegistryServers().size());
//        Assert.assertEquals(1, containerGroup.volumes().size());
//        Assert.assertNotNull(containerGroup.volumes().get("emptydir1"));
//        Assert.assertNotNull(containerGroup.ipAddress());
//        Assert.assertTrue(containerGroup.isIPAddressPublic());
//        Assert.assertEquals(2, containerGroup.externalTcpPorts().length);
//        Assert.assertEquals(2, containerGroup.externalPorts().size());
//        Assert.assertEquals(2, containerGroup.externalTcpPorts().length);
//        Assert.assertEquals(8080, containerGroup.externalTcpPorts()[0]);
//        Assert.assertEquals(80, containerGroup.externalTcpPorts()[1]);
//        Assert.assertEquals(2, containerGroup.containers().size());
//        Container tomcatContainer = containerGroup.containers().get("tomcat");
//        Assert.assertNotNull(tomcatContainer);
//        Container nginxContainer = containerGroup.containers().get("nginx");
//        Assert.assertNotNull(nginxContainer);
//        Assert.assertEquals("tomcat", tomcatContainer.name());
//        Assert.assertEquals("tomcat", tomcatContainer.image());
//        Assert.assertEquals(1.0, tomcatContainer.resources().requests().cpu(), .1);
//        Assert.assertEquals(1.5, tomcatContainer.resources().requests().memoryInGB(), .1);
//        Assert.assertEquals(1, tomcatContainer.ports().size());
//        Assert.assertEquals(8080, tomcatContainer.ports().get(0).port());
//        Assert.assertNull(tomcatContainer.volumeMounts());
//        Assert.assertNull(tomcatContainer.command());
//        Assert.assertNotNull(tomcatContainer.environmentVariables());
//        Assert.assertEquals(1, tomcatContainer.environmentVariables().size());
//        Assert.assertEquals("nginx", nginxContainer.name());
//        Assert.assertEquals("nginx", nginxContainer.image());
//        Assert.assertEquals(1.0, nginxContainer.resources().requests().cpu(), .1);
//        Assert.assertEquals(1.5, nginxContainer.resources().requests().memoryInGB(), .1);
//        Assert.assertEquals(1, nginxContainer.ports().size());
//        Assert.assertEquals(80, nginxContainer.ports().get(0).port());
//        Assert.assertNull(nginxContainer.volumeMounts());
//        Assert.assertNull(nginxContainer.command());
//        Assert.assertNotNull(nginxContainer.environmentVariables());
//        Assert.assertEquals(1, nginxContainer.environmentVariables().size());
//        Assert.assertTrue(containerGroup.tags().containsKey("tag1"));
//        Assert.assertEquals(ContainerGroupRestartPolicy.NEVER, containerGroup.restartPolicy());
//        Assert.assertTrue(containerGroup.isManagedServiceIdentityEnabled());
//        Assert.assertEquals(ResourceIdentityType.USER_ASSIGNED, containerGroup.managedServiceIdentityType());
//        Assert.assertNull(containerGroup.systemAssignedManagedServiceIdentityPrincipalId()); // No Local MSI enabled
//
//        // Ensure the "User Assigned (External) MSI" id can be retrieved from the virtual machine
//        //
//        Set<String> emsiIds = containerGroup.userAssignedManagedServiceIdentityIds();
//        Assert.assertNotNull(emsiIds);
//        Assert.assertEquals(2, emsiIds.size());
//        Assert.assertEquals(cgName, containerGroup.dnsPrefix());
//
//        //TODO: add network and dns testing when questions have been answered
//
//        ContainerGroup containerGroup2 = azure.containerGroups().getByResourceGroup(rgName, cgName);
//
//        List<ContainerGroup> containerGroupList = azure.containerGroups().listByResourceGroup(rgName);
//        Assert.assertTrue(containerGroupList.size() > 0);
//        Assert.assertNotNull(containerGroupList.get(0).state());
//
//        containerGroup.refresh();
//
//        Set<Operation> containerGroupOperations = azure.containerGroups().listOperations();
//        // Number of supported operation can change hence don't assert with a predefined number.
//        Assert.assertTrue(containerGroupOperations.size() > 0);
//    }

//    @Test
//    public void testContainerInstanceWithPrivateIpAddress() throws Exception {
//        //LIVE ONLY TEST BECAUSE IT REQUIRES SUBSCRIPTION ID
//        if (!isPlaybackMode()) {
//            new TestContainerInstanceWithPrivateIpAddress()
//                    .runTest(azure.containerGroups(), azure.resourceGroups(), azure.subscriptionId());
//        }
//    }
//
//    @Test
//    public void testContainerRegistry() throws Exception {
//        new TestContainerRegistry()
//                .runTest(azure.containerRegistries(), azure.resourceGroups());
//    }

    @Test
    public void testCosmosDB() throws Exception {
        new TestCosmosDB()
                .runTest(azure.cosmosDBAccounts(), azure.resourceGroups());
    }

//    @Test
//    public void testSearchServiceFreeSku() throws Exception {
//        new TestSearchService.SearchServiceFreeSku()
//                .runTest(azure.searchServices(), azure.resourceGroups());
//    }

//    @Test
//    public void testSearchServiceBasicSku() throws Exception {
//        new TestSearchService.SearchServiceBasicSku()
//                .runTest(azure.searchServices(), azure.resourceGroups());
//    }
//
//    @Test
//    public void testSearchServiceStandardSku() throws Exception {
//        new TestSearchService.SearchServiceStandardSku()
//                .runTest(azure.searchServices(), azure.resourceGroups());
//    }
//
//    @Test
//    public void testSearchServiceAnySku() throws Exception {
//        new TestSearchService.SearchServiceAnySku()
//                .runTest(azure.searchServices(), azure.resourceGroups());
//    }

    @Test
    @Disabled("Util to generate missing regions")
    public void generateMissingRegion() {
        // Please double check generated code and make adjustment e.g. GERMANY_WEST_CENTRAL -> GERMANY_WESTCENTRAL

        StringBuilder sb = new StringBuilder();

        PagedIterable<Location> locations = azure.getCurrentSubscription().listLocations();  // note the region is not complete since it depends on current subscription
        for (Location location : locations) {
            Region region = Region.findByLabelOrName(location.name());
            if (region == null) {
                sb.append("\n").append(
                        MessageFormat.format("public static final Region {0} = new Region(\"{1}\", \"{2}\");",
                                location.displayName().toUpperCase().replace(" ", "_"),
                                location.name(),
                                location.displayName())
                );
            }
        }

        Assertions.assertTrue(sb.length() == 0, sb.toString());
    }
}
