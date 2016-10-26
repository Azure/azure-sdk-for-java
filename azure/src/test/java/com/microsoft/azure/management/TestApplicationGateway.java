/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewaySkuName;
import com.microsoft.azure.management.network.ApplicationGateways;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * Test of application gateway management.
 */
public class TestApplicationGateway {
    static final long TEST_ID = System.currentTimeMillis();
    static final Region REGION = Region.US_WEST;
    static final String GROUP_NAME = "rg" + TEST_ID;
    static final String APP_GATEWAY_NAME = "ag" + TEST_ID;
    static final String[] PIP_NAMES = {"pipa" + TEST_ID, "pipb" + TEST_ID};
    static final String[] VM_IDS = {
            "/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/marcinslbtest/providers/Microsoft.Compute/virtualMachines/marcinslbtest1",
            "/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/marcinslbtest/providers/Microsoft.Compute/virtualMachines/marcinslbtest3"
    };

    /**
     * Internet-facing LB test with NAT pool test.
     */
    public static class PublicMinimal extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        private final PublicIpAddresses pips;
        private final VirtualMachines vms;
        private final Networks networks;

        public PublicMinimal(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks) {
            this.pips = pips;
            this.vms = vms;
            this.networks = networks;
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(ApplicationGateways resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, TestApplicationGateway.VM_IDS);
            List<PublicIpAddress> existingPips = ensurePIPs(pips);

            // Create a vnet
            Network vnet = this.networks.define("net" + this.testId)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)
                    .create();

            // Create an application gateway
            ApplicationGateway appGateway = resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)
                    .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)
                    .withContainingSubnet(vnet, "subnet1")
                    .withFrontendSubnet(vnet, "subnet1")
                    .withFrontendPort(80)
                    .withFrontendPort(8080)
                    .defineBackend("backend1")
                        .attach()
                    .defineBackendHttpConfiguration("httpConfig1")
                        .attach()
                    .defineHttpListener("listener1")
                        .attach()
                    .defineRequestRoutingRule("rule1")
                        .attach()
                    .create();

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(ApplicationGateway resource) throws Exception {
            resource =  resource.update()
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            return resource;
        }
    }

    // Create VNet for the app gateway
    private static List<PublicIpAddress> ensurePIPs(PublicIpAddresses pips) throws Exception {
        List<Creatable<PublicIpAddress>> creatablePips = new ArrayList<>();
        for (int i = 0; i < PIP_NAMES.length; i++) {
            creatablePips.add(pips.define(PIP_NAMES[i])
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withLeafDomainLabel(PIP_NAMES[i]));
        }

        return pips.create(creatablePips);
    }

    // Ensure VMs for the app gateway
    private static VirtualMachine[] ensureVMs(Networks networks, VirtualMachines vms, String...vmIds) throws Exception {
        ArrayList<VirtualMachine> createdVMs = new ArrayList<>();
        Network network = null;
        Region region = Region.US_WEST;
        String userName = "testuser" + TEST_ID;
        String availabilitySetName = "as" + TEST_ID;

        for (String vmId : vmIds) {
            String groupName = ResourceUtils.groupFromResourceId(vmId);
            String vmName = ResourceUtils.nameFromResourceId(vmId);
            VirtualMachine vm = null;

            if (groupName == null) {
                // Creating a new VM
                vm = null;
                groupName = "rg" + TEST_ID;
                vmName = "vm" + TEST_ID;

                if (network == null) {
                    // Create a VNet for the VM
                    network = networks.define("net" + TEST_ID)
                        .withRegion(region)
                        .withNewResourceGroup(groupName)
                        .withAddressSpace("10.0.0.0/28")
                        .create();
                }

                vm = vms.define(vmName)
                        .withRegion(Region.US_WEST)
                        .withNewResourceGroup(groupName)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet("subnet1")
                        .withPrimaryPrivateIpAddressDynamic()
                        .withoutPrimaryPublicIpAddress()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                        .withRootUserName(userName)
                        .withPassword("Abcdef.123456")
                        .withNewAvailabilitySet(availabilitySetName)
                        .withSize(VirtualMachineSizeTypes.STANDARD_A1)
                        .create();
            } else {
                // Getting an existing VM
                try {
                    vm = vms.getById(vmId);
                } catch (Exception e) {
                    vm = null;
                }
            }

            if (vm != null) {
                createdVMs.add(vm);
            }
        }

        return createdVMs.toArray(new VirtualMachine[createdVMs.size()]);
    }

    // Print app gateway info
    static void printAppGateway(ApplicationGateway resource) {
        StringBuilder info = new StringBuilder();
        info.append("Application gateway: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("SKU: ").append(resource.sku().toString())
                .append("Operational state: ").append(resource.operationalState().toString())
                .append("SSL policy: ").append(resource.sslPolicy().toString());

        System.out.println(info.toString());
    }
}
