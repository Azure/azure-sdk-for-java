/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.management.RestClient;
import com.azure.management.network.LoadBalancer;
import com.azure.management.network.LoadBalancerFrontend;
import com.azure.management.network.LoadBalancerPublicFrontend;
import com.azure.management.network.LoadBalancerSkuType;
import com.azure.management.network.LoadBalancingRule;
import com.azure.management.network.Network;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.PublicIPSkuType;
import com.azure.management.network.Subnet;
import com.azure.management.network.TransportProtocol;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.CreatedResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;

public class VirtualMachineAvailabilityZoneOperationsTests extends ComputeManagementTest {
    private String RG_NAME = "";
    private final Region REGION = Region.US_EAST2;
    private final String VMNAME = "javavm";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCreateZonedVirtualMachineWithImplicitZoneForRelatedResources() throws Exception {
        final String pipDnsLabel = generateRandomResourceName("pip", 10);
        final String proxyGroupName = "plg1Test";
        // Create a zoned virtual machine
        //
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(pipDnsLabel)
                .withNewProximityPlacementGroup(proxyGroupName, ProximityPlacementGroupType.STANDARD)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                // Optionals
                .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                // Create VM
                .create();

        // Checks the zone assigned to the virtual machine
        //
        Assertions.assertNotNull(virtualMachine.availabilityZones());
        Assertions.assertFalse(virtualMachine.availabilityZones().isEmpty());
        Assertions.assertTrue(virtualMachine.availabilityZones().contains(AvailabilityZoneId.ZONE_1));

        //Check the proximity placement group information
        Assertions.assertNotNull(virtualMachine.proximityPlacementGroup());
        Assertions.assertEquals(ProximityPlacementGroupType.STANDARD, virtualMachine.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(virtualMachine.proximityPlacementGroup().virtualMachineIds());
        Assertions.assertTrue(virtualMachine.id().equalsIgnoreCase(virtualMachine.proximityPlacementGroup().virtualMachineIds().get(0)));

        // Checks the zone assigned to the implicitly created public IP address.
        // Implicitly created PIP will be BASIC
        //
        PublicIPAddress publicIPAddress = virtualMachine.getPrimaryPublicIPAddress();
        Assertions.assertNotNull(publicIPAddress.availabilityZones());
        Assertions.assertFalse(publicIPAddress.availabilityZones().isEmpty());
        Assertions.assertTrue(publicIPAddress.availabilityZones().contains(AvailabilityZoneId.ZONE_1));
        // Checks the zone assigned to the implicitly created managed OS disk.
        //
        String osDiskId = virtualMachine.osDiskId();    // Only VM based on managed disk can have zone assigned
        Assertions.assertNotNull(osDiskId);
        Assertions.assertFalse(osDiskId.isEmpty());
        Disk osDisk = computeManager.disks().getById(osDiskId);
        Assertions.assertNotNull(osDisk);
        // Checks the zone assigned to the implicitly created managed OS disk.
        //
        Assertions.assertNotNull(osDisk.availabilityZones());
        Assertions.assertFalse(osDisk.availabilityZones().isEmpty());
        Assertions.assertTrue(osDisk.availabilityZones().contains(AvailabilityZoneId.ZONE_1));
    }

    @Test
    public void canCreateZonedVirtualMachineWithExplicitZoneForRelatedResources() throws Exception {
        // Create zoned public IP for the virtual machine
        //
        final String pipDnsLabel = generateRandomResourceName("pip", 10);
        PublicIPAddress publicIPAddress = networkManager.publicIPAddresses()
                .define(pipDnsLabel)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withStaticIP()
                // Optionals
                .withAvailabilityZone(AvailabilityZoneId.ZONE_1)  // since the SKU is BASIC and VM is zoned, PIP must be zoned
                .withSku(PublicIPSkuType.BASIC)    // Basic sku is never zone resilient, so if you want it zoned, specify explicitly as above.
                // Create PIP
                .create();
        // Create a zoned data disk for the virtual machine
        //
        final String diskName = generateRandomResourceName("dsk", 10);
        Disk dataDisk = computeManager.disks()
                .define(diskName)
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withData()
                .withSizeInGB(100)
                // Optionals
                .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                // Create Disk
                .create();
        // Create a zoned virtual machine
        //
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(publicIPAddress)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                // Optionals
                .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                .withExistingDataDisk(dataDisk)
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                // Create VM
                .create();
        // Checks the zone assigned to the virtual machine
        //
        Assertions.assertNotNull(virtualMachine.availabilityZones());
        Assertions.assertFalse(virtualMachine.availabilityZones().isEmpty());
        Assertions.assertTrue(virtualMachine.availabilityZones().contains(AvailabilityZoneId.ZONE_1));
        // Checks the zone assigned to the explicitly created public IP address.
        //
        publicIPAddress = virtualMachine.getPrimaryPublicIPAddress();
        Assertions.assertNotNull(publicIPAddress.sku());
        Assertions.assertTrue(publicIPAddress.sku().equals(PublicIPSkuType.BASIC));
        Assertions.assertNotNull(publicIPAddress.availabilityZones());
        Assertions.assertFalse(publicIPAddress.availabilityZones().isEmpty());
        Assertions.assertTrue(publicIPAddress.availabilityZones().contains(AvailabilityZoneId.ZONE_1));
        // Check the zone assigned to the explicitly created data disk
        //
        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine.dataDisks();
        Assertions.assertNotNull(dataDisks);
        Assertions.assertFalse(dataDisks.isEmpty());
        VirtualMachineDataDisk dataDisk1 = dataDisks.values().iterator().next();
        Assertions.assertNotNull(dataDisk1.id());
        dataDisk = computeManager.disks().getById(dataDisk1.id());
        Assertions.assertNotNull(dataDisk);
        Assertions.assertNotNull(dataDisk.availabilityZones());
        Assertions.assertFalse(dataDisk.availabilityZones().isEmpty());
        Assertions.assertTrue(dataDisk.availabilityZones().contains(AvailabilityZoneId.ZONE_1));
        // Checks the zone assigned to the implicitly created managed OS disk.
        //
        String osDiskId = virtualMachine.osDiskId();    // Only VM based on managed disk can have zone assigned
        Assertions.assertNotNull(osDiskId);
        Assertions.assertFalse(osDiskId.isEmpty());
        Disk osDisk = computeManager.disks().getById(osDiskId);
        Assertions.assertNotNull(osDisk);
        // Checks the zone assigned to the implicitly created managed OS disk.
        //
        Assertions.assertNotNull(osDisk.availabilityZones());
        Assertions.assertFalse(osDisk.availabilityZones().isEmpty());
        Assertions.assertTrue(osDisk.availabilityZones().contains(AvailabilityZoneId.ZONE_1));
    }

    @Test
    public void canCreateZonedVirtualMachineWithZoneResilientPublicIP() throws Exception {
        // Create zone resilient public IP for the virtual machine
        //
        final String pipDnsLabel = generateRandomResourceName("pip", 10);
        PublicIPAddress publicIPAddress = networkManager.publicIPAddresses()
                .define(pipDnsLabel)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withStaticIP()
                // Optionals
                .withSku(PublicIPSkuType.STANDARD)  // No zone selected, STANDARD SKU is zone resilient [zone resilient: resources deployed in all zones by the service and it will be served by all AZs all the time]
                // Create PIP
                .create();
        // Create a zoned virtual machine
        //
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(publicIPAddress)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                // Optionals
                .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                // Create VM
                .create();
        // Checks the zone assigned to the virtual machine
        //
        Assertions.assertNotNull(virtualMachine.availabilityZones());
        Assertions.assertFalse(virtualMachine.availabilityZones().isEmpty());
        Assertions.assertTrue(virtualMachine.availabilityZones().contains(AvailabilityZoneId.ZONE_1));
        // Check the zone resilient PIP
        //
        publicIPAddress = virtualMachine.getPrimaryPublicIPAddress();
        Assertions.assertNotNull(publicIPAddress.sku());
        Assertions.assertTrue(publicIPAddress.sku().equals(PublicIPSkuType.STANDARD));
        Assertions.assertNotNull(publicIPAddress.availabilityZones());  // Though zone-resilient, this property won't be populated by the service.
        Assertions.assertTrue(publicIPAddress.availabilityZones().isEmpty());
    }

    @Test
    @Disabled("Though valid scenario, ignoring it due to network service bug")
    public void canCreateRegionalNonAvailSetVirtualMachinesAndAssociateThemWithSingleBackendPoolOfZoneResilientLoadBalancer() throws Exception {
        final String networkName = generateRandomResourceName("net", 10);
        Network network = networkManager.networks().define(networkName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/29")
                .withSubnet("subnet2", "10.0.0.8/29")
                .create();

        // create two regional virtual machine, which does not belongs to any availability set
        //
        Iterator<Subnet> subnets = network.subnets().values().iterator();
        // Define first regional virtual machine
        //
        Creatable<VirtualMachine> creatableVM1 = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm1", 10))
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withExistingPrimaryNetwork(network)
                .withSubnet(subnets.next().name())      // Put VM in first subnet
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                // Optionals
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2);

        // Define second regional virtual machine
        //
        Creatable<VirtualMachine> creatableVM2 = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm2", 10))
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withExistingPrimaryNetwork(network)
                .withSubnet(subnets.next().name())  // Put VM in second subnet
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                // Optionals
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2);

        CreatedResources<VirtualMachine> createdVMs = computeManager.virtualMachines()
                .create(creatableVM1, creatableVM2);

        VirtualMachine firstVirtualMachine = createdVMs.get(creatableVM1.key());
        VirtualMachine secondVirtualMachine  = createdVMs.get(creatableVM2.key());

        // Work around bug in the network service
        // Once the fix is deployed remove below code to powerOff and deallocate VMs
        //
        //  Completable completable1 = firstVirtualMachine.powerOffAsync().concatWith(firstVirtualMachine.deallocateAsync());
        //  Completable completable2 = secondVirtualMachine.powerOffAsync().concatWith(secondVirtualMachine.deallocateAsync());
        //  Completable.merge(completable1, completable2).await();

        // Creates a public IP address for the internet-facing load-balancer
        //
        final String pipDnsLabel = generateRandomResourceName("pip", 10);
        PublicIPAddress publicIPAddress = networkManager.publicIPAddresses()
                .define(pipDnsLabel)
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withStaticIP()
                // Optionals
                .withSku(PublicIPSkuType.STANDARD)  //  STANDARD LB requires STANDARD PIP
                // Create PIP
                .create();

        // Creates a Internet-Facing LoadBalancer with one front-end IP configuration and
        // two backend pool associated with this IP Config
        //
        final String lbName = generateRandomResourceName("lb", 10);
        LoadBalancer lb = this.networkManager.loadBalancers()
                .define(lbName)
                    .withRegion(REGION)
                    .withExistingResourceGroup(RG_NAME)
                    .defineLoadBalancingRule("rule-1")
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend("front-end-1")
                        .fromFrontendPort(80)
                        .toExistingVirtualMachines(firstVirtualMachine, secondVirtualMachine)
                        .withProbe("tcpProbe-1")
                        .attach()
                    .definePublicFrontend("front-end-1") // Define the frontend IP configuration used by the LB rule
                        .withExistingPublicIPAddress(publicIPAddress)
                        .attach()
                    .defineTcpProbe("tcpProbe-1") // Define the Probe used by the LB rule
                        .withPort(25)
                        .withIntervalInSeconds(15)
                        .withNumberOfProbes(5)
                        .attach()
                    .withSku(LoadBalancerSkuType.STANDARD)  // "zone-resilient LB" which don't have the constraint that all VMs needs to be in the same availability set
                    .create();

        // Zone resilient LB does not care VMs are zoned or regional, in the above cases VMs are regional.
        //
        // rx.Completable.merge(firstVirtualMachine.startAsync(), secondVirtualMachine.startAsync()).await();

        // Verify frontends
        Assertions.assertEquals(1, lb.frontends().size());
        Assertions.assertEquals(1, lb.publicFrontends().size());
        Assertions.assertEquals(0, lb.privateFrontends().size());
        LoadBalancerFrontend frontend = lb.frontends().values().iterator().next();
        Assertions.assertTrue(frontend.isPublic());
        LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
        Assertions.assertTrue(publicIPAddress.id().equalsIgnoreCase(publicFrontend.publicIPAddressId()));

        // Verify backends
        Assertions.assertEquals(1, lb.backends().size());

        // Verify probes
        Assertions.assertEquals(1, lb.tcpProbes().size());
        Assertions.assertTrue(lb.tcpProbes().containsKey("tcpProbe-1"));

        // Verify rules
        Assertions.assertEquals(1, lb.loadBalancingRules().size());
        Assertions.assertTrue(lb.loadBalancingRules().containsKey("rule-1"));
        LoadBalancingRule rule = lb.loadBalancingRules().get("rule-1");
        Assertions.assertNotNull(rule.backend());
        Assertions.assertTrue(rule.probe().name().equalsIgnoreCase("tcpProbe-1"));

        // Note that above configuration is not possible for BASIC LB, BASIC LB has following limitation
        // It supports VMs only in a single availability Set in a backend pool, though multiple backend pool
        // can be associated with VMs in the single availability set, you cannot create a set of VMs in another
        // availability set and put it in a different backend pool.

        // Start the VM [This can be removed once the fix is deployed]
        // rx.Completable.merge(firstVirtualMachine.startAsync(), secondVirtualMachine.startAsync()).await();
    }

    @Test
    @Disabled("Though valid scenario, ignoring it due to network service bug")
    public void canCreateZonedVirtualMachinesAndAssociateThemWithSingleBackendPoolOfZoneResilientLoadBalancer() throws Exception {
        final String networkName = generateRandomResourceName("net", 10);
        Network network = networkManager.networks().define(networkName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/29")
                .withSubnet("subnet2", "10.0.0.8/29")
                .create();

        // create two regional virtual machine, which does not belongs to any availability set
        //
        Iterator<Subnet> subnets = network.subnets().values().iterator();
        // Define first regional virtual machine
        //
        Creatable<VirtualMachine> creatableVM1 = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm1", 10))
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withExistingPrimaryNetwork(network)
                .withSubnet(subnets.next().name())      // Put VM in first subnet
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                // Optionals
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2);

        // Define second regional virtual machine
        //
        Creatable<VirtualMachine> creatableVM2 = computeManager.virtualMachines()
                .define(generateRandomResourceName("vm2", 10))
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withExistingPrimaryNetwork(network)
                .withSubnet(subnets.next().name())  // Put VM in second subnet
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                // Optionals
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2);

        CreatedResources<VirtualMachine> createdVMs = computeManager.virtualMachines()
                .create(creatableVM1, creatableVM2);

        // Creates a public IP address for the internet-facing load-balancer
        //
        final String pipDnsLabel = generateRandomResourceName("pip", 10);
        PublicIPAddress publicIPAddress = networkManager.publicIPAddresses()
                .define(pipDnsLabel)
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withStaticIP()
                // Optionals
                .withSku(PublicIPSkuType.STANDARD)  //  STANDARD LB requires STANDARD PIP
                // Create PIP
                .create();

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().getByName(network.resourceGroupName());

        // Creates a Internet-Facing LoadBalancer with one front-end IP configuration and
        // two backend pool associated with this IP Config
        //
        final String loadBalancerName = generateRandomResourceName("extlb" + "1" + "-", 18);
        final String publicIPName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";

        // Sku of PublicIP and LoadBalancer must match
        //

        PublicIPAddress lbPip = this.networkManager.publicIPAddresses().define(publicIPName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIPName)
                // Optionals
                .withStaticIP()
                .withSku(PublicIPSkuType.STANDARD)
                // Create
                .create();

        LoadBalancer loadBalancer = this.networkManager.loadBalancers().define(loadBalancerName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)

                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPort(80)
                    .toExistingVirtualMachines(createdVMs.get(creatableVM1.key()))
                    .withProbe("httpProbe")
                    .attach()
                .defineLoadBalancingRule("httpsRule")
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPort(443)
                    .toExistingVirtualMachines(createdVMs.get(creatableVM2.key()))
                    .withProbe("httpsProbe")
                    .attach()
                // Explicitly define the frontend
                .definePublicFrontend(frontendName)
                .withExistingPublicIPAddress(publicIPAddress)   // Frontend with PIP means internet-facing load-balancer
                .attach()

                // Add two probes one per rule
                .defineHttpProbe("httpProbe")
                .withRequestPath("/")
                .attach()
                .defineHttpProbe("httpsProbe")
                .withRequestPath("/")
                .attach()
                .withSku(LoadBalancerSkuType.STANDARD)
                .create();
        // Zone resilient LB does not care VMs are zoned or regional, in the above cases VMs are zoned.
        //

        // Note that above configuration is not possible for BASIC LB, BASIC LB has following limitation
        // It supports VMs only in a single availability Set in a backend pool, though multiple backend pool
        // can be associated with VMs in the single availability set, you cannot create a set of VMs in another
        // availability set and put it in a different backend pool.
    }
}


