/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendAddress;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayPrivateFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayPublicFrontend;
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
    public static class PrivateComplex extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        private final PublicIpAddresses pips;
        private final VirtualMachines vms;
        private final Networks networks;

        /**
         * Tests minimal internal app gateways.
         * @param pips public IPs
         * @param vms virtual machines
         * @param networks networks
         */
        public PrivateComplex(
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
            Network vnet = this.networks.define("net" + this.testId)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/29")
                    .withSubnet("subnet2", "10.0.0.8/29")
                    .create();

            // Create an application gateway
            ApplicationGateway appGateway = resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)
                    .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)
                    .withContainingSubnet(vnet, "subnet1")

                    // Public frontend
                    .withoutPublicFrontend()

                    // Private frontend
                    .withPrivateFrontend()

                    // Frontend ports
                    .withFrontendPort(80, "port1")
                    .withFrontendPort(8080, "port2")

                    // Backends
                    .withBackendIpAddress("11.1.1.1")
                    .withBackendIpAddress("11.1.1.2")
                    .withBackendFqdn("www.microsoft.com", "backend2")

                    // HTTP configs
                    .defineHttpConfiguration("httpConfig1")
                        // .withBackendPort(80) // Optional, 80 default
                        .attach()

                    // HTTP listeners
                    .defineHttpListener("listener1")
                        .withFrontend("default")
                        .withPort("port1")
                        .attach()

                    // Request routing rules
                    .defineRequestRoutingRule("rule1")
                        .withListener("listener1")
                        .withBackend("default")
                        .withBackendHttpConfiguration("httpConfig1")
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
            creatablePips.add(
                    pips.define(PIP_NAMES[i])
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
                        .withRootUsername(userName)
                        .withRootPassword("Abcdef.123456")
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
                .append("\n\tSKU: ").append(resource.sku().toString())
                .append("\n\tOperational state: ").append(resource.operationalState())
                .append("\n\tSSL policy: ").append(resource.sslPolicy());

        // Show frontends
        Map<String, ApplicationGatewayFrontend> frontends = resource.frontends();
        info.append("\n\tFrontends: ").append(frontends.size());
        for (ApplicationGatewayFrontend frontend : frontends.values()) {
            info.append("\n\t\tName: ").append(frontend.name())
                .append("\n\t\t\tPublic? ").append(frontend.isPublic());

            if (frontend.isPublic()) {
                // Show public frontend
                ApplicationGatewayPublicFrontend publicFrontend = (ApplicationGatewayPublicFrontend) frontend;
                info.append("\n\t\t\tPublic IP address ID: ").append(publicFrontend.publicIpAddressId());
            } else {
                // Show private frontend
                ApplicationGatewayPrivateFrontend privateFrontend = (ApplicationGatewayPrivateFrontend) frontend;
                info.append("\n\t\t\tPrivate IP address: ").append(privateFrontend.privateIpAddress())
                    .append("\n\t\t\tPrivate IP allocation method: ").append(privateFrontend.privateIpAllocationMethod())
                    .append("\n\t\t\tSubnet name: ").append(privateFrontend.subnetName())
                    .append("\n\t\t\tVirtual network ID: ").append(privateFrontend.networkId());
            }
        }

        // Show backends
        Map<String, ApplicationGatewayBackend> backends = resource.backends();
        info.append("\n\tBackends: ").append(backends.size());
        for (ApplicationGatewayBackend backend : backends.values()) {
            info.append("\n\t\tName: ").append(backend.name())
                .append("\n\t\t\tAssociated NIC IP configuration IDs: ").append(backend.backendNicIpConfigurationNames().keySet());

            // Show addresses
            List<ApplicationGatewayBackendAddress> addresses = backend.addresses();
            info.append("\n\t\t\tAddresses: ").append(addresses.size());
            for (ApplicationGatewayBackendAddress address : addresses) {
                info.append("\n\t\t\t\tFQDN: ").append(address.fqdn())
                    .append("\n\t\t\t\tIP: ").append(address.ipAddress());
            }
        }

        System.out.println(info.toString());
    }
}
