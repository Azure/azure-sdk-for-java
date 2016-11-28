/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.io.File;
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
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontendHttpListener;
import com.microsoft.azure.management.network.ApplicationGatewayIpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayPrivateFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.network.ApplicationGatewayPublicFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewaySkuName;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import com.microsoft.azure.management.network.ApplicationGateways;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.Subnet;
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
    static final String ID_TEMPLATE = "/subscriptions/${subId}/resourceGroups/${rgName}/providers/Microsoft.Network/applicationGateways/${resourceName}";
    static final String[] VM_IDS = {
            "/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/marcinslbtest/providers/Microsoft.Compute/virtualMachines/marcinslbtest1",
            "/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/marcinslbtest/providers/Microsoft.Compute/virtualMachines/marcinslbtest3"
    };

    static String createResourceId(String subscriptionId) {
        return ID_TEMPLATE
                .replace("${subId}", subscriptionId)
                .replace("${rgName}", GROUP_NAME)
                .replace("${resourceName}", APP_GATEWAY_NAME);
    }

    /**
     * Minimalistic internal (private) app gateway test.
     */
    public static class PrivateMinimal extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        private final Networks networks;

        /**
         * Tests minimal internal app gateways.
         * @param pips public IPs
         * @param vms virtual machines
         * @param networks networks
         */
        public PrivateMinimal(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks) {
            this.networks = networks;
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            final Network vnet = this.networks.define("net" + this.testId)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/29")
                    .withSubnet("subnet2", "10.0.0.8/29")
                    .create();

            // Prepare a separate thread for resource creation
            Thread creationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Create an application gateway
                    ApplicationGateway ag = resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                            .withRegion(REGION)
                            .withExistingResourceGroup(GROUP_NAME)
                            .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)
                            .withContainingSubnet(vnet, "subnet1")
                            .withoutPublicFrontend()            // No public frontend
                            .withPrivateFrontend()              // Private frontend
                            .withFrontendHttpListenerOnPort(80) // Frontend HTTP listener and port

                            // Backend HTTP configs
                            .withBackendHttpConfigurationOnPort(8080)

                            // Backends
                            .withBackendIpAddress("11.1.1.1")
                            .withBackendIpAddress("11.1.1.2")

                            // Request routing rules
                            .defineRequestRoutingRule("rule1")
                                .fromFrontendPort(80)
                                .toBackendPort(8080)
                                .withBackend("default")
                                .attach()
                            .create();
                }
            });

            // Start the creation...
            creationThread.start();

            //...But bail out after 30 sec, as it is enough to test the results
            creationThread.join(30 * 1000);

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.manager().applicationGateways().getById(resourceId);

            // Verify frontends
            Assert.assertTrue(appGateway.frontends().size() == 1);
            Assert.assertTrue(appGateway.frontends().containsKey("default"));
            ApplicationGatewayFrontend frontend = appGateway.frontends().get("default");
            Assert.assertTrue(frontend.isPublic());

            // Verify frontend ports
            // TODO

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 1);
            Assert.assertTrue(appGateway.backends().containsKey("default"));
            ApplicationGatewayBackend backend = appGateway.backends().get("default");
            Assert.assertTrue(backend.addresses().size() == 2);

            // Verify backend HTTP configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 1);
            ApplicationGatewayBackendHttpConfiguration httpConfig = appGateway.getBackendHttpConfigurationByPortNumber(8080);
            Assert.assertTrue(httpConfig.backendPort() == 8080);

            // Verify listeners
            // TODO

            // Verify rules
            // TODO

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            Thread updateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    resource.update()
                            //.withSku(ApplicationGatewaySkuName.STANDARD_MEDIUM, 2)
                            .withoutBackendFqdn("www.microsoft.com")
                            .withoutBackendIpAddress("11.1.1.1")
                            .withBackendIpAddress("11.1.1.3", "backend2")
                            .withoutBackendHttpConfiguration("httpConfig2")
                            .updateBackendHttpConfiguration("httpConfig1")
                                .withBackendPort(83)
                                .withoutCookieBasedAffinity()
                                .withRequestTimeout(20)
                                .parent()
                            .withoutBackend("backend3")
                            .withTag("tag1", "value1")
                            .withTag("tag2", "value2")
                            .apply();
                }
            });

            // Start the update thread...
            updateThread.start();

            // ...But bail out after 30 sec as it should be enough to test the results
            updateThread.join(1000 * 30);

            resource.refresh();

            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.sku().name().equals(ApplicationGatewaySkuName.STANDARD_MEDIUM));
            Assert.assertTrue(resource.sku().capacity() == 2);

            // Verify backends
            ApplicationGatewayBackend defaultBackend = resource.backends().get("default");
            Assert.assertTrue(defaultBackend.addresses().size() == 1);
            Assert.assertTrue(defaultBackend.addresses().get(0).ipAddress().equalsIgnoreCase("11.1.1.2"));

            ApplicationGatewayBackend backend2 = resource.backends().get("backend2");
            Assert.assertTrue(backend2.addresses().size() == 1);
            Assert.assertTrue(backend2.addresses().get(0).ipAddress().equals("11.1.1.3"));
            Assert.assertTrue(!resource.backends().containsKey("backend3"));

            // Verify HTTP configs
            Assert.assertTrue(resource.backendHttpConfigurations().size() == 1);
            Assert.assertTrue(resource.backendHttpConfigurations().containsKey("httpConfig1"));
            ApplicationGatewayBackendHttpConfiguration httpConfig1 = resource.backendHttpConfigurations().get("httpConfig1");
            Assert.assertTrue(httpConfig1.backendPort() == 83);
            Assert.assertTrue(!httpConfig1.cookieBasedAffinity());
            Assert.assertTrue(httpConfig1.requestTimeout() == 20);

            Assert.assertTrue(!resource.backendHttpConfigurations().containsKey("httpConfig2"));
            return resource;
        }
    }

    /**
     * Complex internal (private) app gateway test.
     */
    public static class PrivateComplex extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        //private final PublicIpAddresses pips;
        //private final VirtualMachines vms;
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
            //this.pips = pips;
            //this.vms = vms;
            this.networks = networks;
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            //VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, TestApplicationGateway.VM_IDS);
            //List<PublicIpAddress> existingPips = ensurePIPs(pips);
            final Network vnet = this.networks.define("net" + this.testId)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/29")
                    .withSubnet("subnet2", "10.0.0.8/29")
                    .create();

            Thread.UncaughtExceptionHandler threadException = new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread th, Throwable ex) {
                    System.out.println("Uncaught exception: " + ex);
                }
            };

            // Prepare for execution in a separate thread to shorten the test
            Thread creationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Create an application gateway
                    restOfComplexDefinition(
                            resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                            .withRegion(REGION)
                            .withExistingResourceGroup(GROUP_NAME)
                            .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)

                            // IP configuration for the app gateway (which subnet is it contained in)
                            .withContainingSubnet(vnet, "subnet1")

                            // Public frontend
                            .withoutPublicFrontend()

                            // Private frontend
                            .withPrivateFrontend())
                    .create();
                    }
                });

            // Start creating in a separate thread...
            creationThread.setUncaughtExceptionHandler(threadException);
            creationThread.start();

            // ...But don't wait till the end - not needed for the test, 30 sec should be enough
            Thread.sleep(30 * 1000);

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.getById(resourceId);
            Assert.assertTrue(appGateway != null);

            // Verify IP configs
            Assert.assertTrue(appGateway.ipConfigurations().size() == 1);
            ApplicationGatewayIpConfiguration ipConfig = appGateway.ipConfigurations().values().iterator().next();
            Assert.assertTrue(ipConfig != null);
            Subnet subnet = ipConfig.getSubnet();
            Assert.assertTrue(subnet != null);
            Assert.assertTrue(subnet.name().equalsIgnoreCase("subnet1"));

            // Verify frontends
            Assert.assertTrue(appGateway.frontends().size() == 1);
            ApplicationGatewayFrontend frontend = appGateway.frontends().values().iterator().next();
            Assert.assertTrue(frontend != null);
            Assert.assertTrue(!frontend.isPublic());
            Assert.assertTrue(frontend.isPrivate());
            ApplicationGatewayPrivateFrontend privateFrontend = (ApplicationGatewayPrivateFrontend) frontend;
            Assert.assertTrue(privateFrontend.networkId().equalsIgnoreCase(vnet.id()));
            Assert.assertTrue(privateFrontend.subnetName().equalsIgnoreCase("subnet1"));
            Assert.assertTrue(privateFrontend.privateIpAllocationMethod().equals(IPAllocationMethod.DYNAMIC));

            assertRestOfComplexDefinition(appGateway);

            creationThread.join(30 * 1000);

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            // TODO: Fix this - this test doesn't work yet

            // Prepare a separate thread for running the update to make the test go faster
            Thread updateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    resource.update()
                        .withSku(ApplicationGatewaySkuName.STANDARD_MEDIUM, 2)
                        .withoutFrontendHttpListener("listener1")
                        .withoutBackendFqdn("www.microsoft.com")
                        .withoutBackendIpAddress("11.1.1.1")
                        .withBackendIpAddress("11.1.1.3", "backend2")
                        .withoutBackendHttpConfiguration("httpConfig2")
                        .updateBackendHttpConfiguration("httpConfig1")
                            .withBackendPort(83)
                            .withProtocol(ApplicationGatewayProtocol.HTTPS)
                            .withoutCookieBasedAffinity()
                            .withRequestTimeout(20)
                            .parent()
                        .withoutBackend("backend3")
                        .withTag("tag1", "value1")
                        .withTag("tag2", "value2")
                        .apply();
                }
            });

            // Start the update thread...
            updateThread.start();

            // ...But kill it after 30 sec as that should be enough for the test
            updateThread.join(30 * 1000);

            resource.refresh();

            // Get the resource created so far
            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.sku().name().equals(ApplicationGatewaySkuName.STANDARD_MEDIUM));
            Assert.assertTrue(resource.sku().capacity() == 2);

            // Verify backends
            ApplicationGatewayBackend defaultBackend = resource.backends().get("default");
            Assert.assertTrue(defaultBackend.addresses().size() == 1);
            Assert.assertTrue(defaultBackend.addresses().get(0).ipAddress().equalsIgnoreCase("11.1.1.2"));

            ApplicationGatewayBackend backend2 = resource.backends().get("backend2");
            Assert.assertTrue(backend2.addresses().size() == 1);
            Assert.assertTrue(backend2.addresses().get(0).ipAddress().equals("11.1.1.3"));
            Assert.assertTrue(!resource.backends().containsKey("backend3"));

            // Verify HTTP configs
            Assert.assertTrue(resource.backendHttpConfigurations().size() == 1);
            Assert.assertTrue(resource.backendHttpConfigurations().containsKey("httpConfig1"));
            ApplicationGatewayBackendHttpConfiguration httpConfig1 = resource.backendHttpConfigurations().get("httpConfig1");
            Assert.assertTrue(httpConfig1.backendPort() == 83);
            Assert.assertTrue(!httpConfig1.cookieBasedAffinity());
            Assert.assertTrue(httpConfig1.requestTimeout() == 20);

            Assert.assertTrue(!resource.backendHttpConfigurations().containsKey("httpConfig2"));
            return resource;
        }
    }

    /**
     * Complex Internet-facing (public) app gateway test.
     */
    public static class PublicComplex extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        private final PublicIpAddresses pips;
        //private final VirtualMachines vms;
        private final Networks networks;

        /**
         * Tests minimal internal app gateways.
         * @param pips public IPs
         * @param vms virtual machines
         * @param networks networks
         */
        public PublicComplex(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks) {
            this.pips = pips;
            //this.vms = vms;
            this.networks = networks;
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            //VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, TestApplicationGateway.VM_IDS);
            final List<PublicIpAddress> existingPips = ensurePIPs(pips);
            final Network vnet = this.networks.define("net" + this.testId)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/29")
                    .withSubnet("subnet2", "10.0.0.8/29")
                    .create();

            Thread.UncaughtExceptionHandler threadException = new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread th, Throwable ex) {
                    System.out.println("Uncaught exception: " + ex);
                }
            };

            // Prepare for execution in a separate thread to shorten the test
            Thread creationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Create an application gateway
                    restOfComplexDefinition(
                            resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                            .withRegion(REGION)
                            .withExistingResourceGroup(GROUP_NAME)
                            .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)

                            // IP configuration for the app gateway (which subnet is it contained in)
                            .withContainingSubnet(vnet, "subnet1")

                            // Public frontend
                            .definePublicFrontend("default")
                                .withExistingPublicIpAddress(existingPips.get(0))
                                .attach()

                            // Private frontend
                            .withoutPrivateFrontend())
                    .create();
                    }
                });

            // Start creating in a separate thread...
            creationThread.setUncaughtExceptionHandler(threadException);
            creationThread.start();

            // ...But don't wait till the end - not needed for the test, 30 sec should be enough
            Thread.sleep(30 * 1000);

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.getById(resourceId);
            Assert.assertTrue(appGateway != null);

            // Verify IP configs
            Assert.assertTrue(appGateway.ipConfigurations().size() == 1);
            ApplicationGatewayIpConfiguration ipConfig = appGateway.ipConfigurations().values().iterator().next();
            Assert.assertTrue(ipConfig != null);
            Subnet subnet = ipConfig.getSubnet();
            Assert.assertTrue(subnet != null);
            Assert.assertTrue(subnet.name().equalsIgnoreCase("subnet1"));

            // Verify frontends
            Assert.assertTrue(appGateway.frontends().size() == 1);
            ApplicationGatewayFrontend frontend = appGateway.frontends().values().iterator().next();
            Assert.assertTrue(frontend != null);
            Assert.assertTrue(frontend.isPublic());
            Assert.assertTrue(!frontend.isPrivate());
            ApplicationGatewayPublicFrontend publicFrontend = (ApplicationGatewayPublicFrontend) frontend;
            Assert.assertTrue(publicFrontend.publicIpAddressId().equalsIgnoreCase(existingPips.get(0).id()));

            assertRestOfComplexDefinition(appGateway);

            creationThread.join(30 * 1000);

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            // TODO: Fix this - this test doesn't work yet

            // Prepare a separate thread for running the update to make the test go faster
            Thread updateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    resource.update()
                        .withSku(ApplicationGatewaySkuName.STANDARD_MEDIUM, 2)
                        .withoutFrontendHttpListener("listener1")
                        .withoutBackendFqdn("www.microsoft.com")
                        .withoutBackendIpAddress("11.1.1.1")
                        .withBackendIpAddress("11.1.1.3", "backend2")
                        .withoutBackendHttpConfiguration("httpConfig2")
                        .updateBackendHttpConfiguration("httpConfig1")
                            .withBackendPort(83)
                            .withProtocol(ApplicationGatewayProtocol.HTTPS)
                            .withoutCookieBasedAffinity()
                            .withRequestTimeout(20)
                            .parent()
                        .withoutBackend("backend3")
                        .withTag("tag1", "value1")
                        .withTag("tag2", "value2")
                        .apply();
                }
            });

            // Start the update thread...
            updateThread.start();

            // ...But kill it after 30 sec as that should be enough for the test
            updateThread.join(30 * 1000);

            resource.refresh();

            // Get the resource created so far
            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.sku().name().equals(ApplicationGatewaySkuName.STANDARD_MEDIUM));
            Assert.assertTrue(resource.sku().capacity() == 2);

            // Verify backends
            ApplicationGatewayBackend defaultBackend = resource.backends().get("default");
            Assert.assertTrue(defaultBackend.addresses().size() == 1);
            Assert.assertTrue(defaultBackend.addresses().get(0).ipAddress().equalsIgnoreCase("11.1.1.2"));

            ApplicationGatewayBackend backend2 = resource.backends().get("backend2");
            Assert.assertTrue(backend2.addresses().size() == 1);
            Assert.assertTrue(backend2.addresses().get(0).ipAddress().equals("11.1.1.3"));
            Assert.assertTrue(!resource.backends().containsKey("backend3"));

            // Verify HTTP configs
            Assert.assertTrue(resource.backendHttpConfigurations().size() == 1);
            Assert.assertTrue(resource.backendHttpConfigurations().containsKey("httpConfig1"));
            ApplicationGatewayBackendHttpConfiguration httpConfig1 = resource.backendHttpConfigurations().get("httpConfig1");
            Assert.assertTrue(httpConfig1.backendPort() == 83);
            Assert.assertTrue(!httpConfig1.cookieBasedAffinity());
            Assert.assertTrue(httpConfig1.requestTimeout() == 20);

            Assert.assertTrue(!resource.backendHttpConfigurations().containsKey("httpConfig2"));
            return resource;
        }
    }

    /**
     * Internet-facing LB test with NAT pool test.
     */
    public static class PublicMinimal extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        private final Networks networks;

        /**
         * Tests minimal Internet-facing app gateways.
         * @param pips public IPs
         * @param vms virtual machines
         * @param networks networks
         */
        public PublicMinimal(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks) {
            this.networks = networks;
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            final Network vnet = this.networks.define("net" + this.testId)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/29")
                    .withSubnet("subnet2", "10.0.0.8/29")
                    .create();

            // Prepare a separate thread for resource creation
            Thread creationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Create an application gateway
                    ApplicationGateway ag = resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                            .withRegion(REGION)
                            .withExistingResourceGroup(GROUP_NAME)
                            .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)
                            .withContainingSubnet(vnet, "subnet1")
                            .withNewPublicIpAddress()           // Public frontend
                            .withoutPrivateFrontend()                           // No private frontend
                            .withFrontendHttpListenerOnPort(80)                 // Frontend HTTP listener

                            // Backend HTTP configs
                            .withBackendHttpConfigurationOnPort(8080)

                            // Backends
                            .withBackendIpAddress("11.1.1.1")
                            .withBackendIpAddress("11.1.1.2")

                            // Request routing rules
                            .defineRequestRoutingRule("rule1")
                                .fromFrontendPort(80)
                                .toBackendPort(8080)
                                .withBackend("default")
                                .attach()
                            .create();
                }
            });

            // Start the creation...
            creationThread.start();

            //...But bail out after 30 sec, as it is enough to test the results
            creationThread.join(30 * 1000);

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.manager().applicationGateways().getById(resourceId);

            // Verify frontends
            Assert.assertTrue(appGateway.frontends().size() == 1);
            Assert.assertTrue(appGateway.frontends().containsKey("default"));

            // Verify frontend ports
            // TODO

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 1);
            Assert.assertTrue(appGateway.backends().containsKey("default"));
            ApplicationGatewayBackend backend = appGateway.backends().get("default");
            Assert.assertTrue(backend.addresses().size() == 2);

            // Verify backend HTTP configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 1);
            ApplicationGatewayBackendHttpConfiguration httpConfig = appGateway.getBackendHttpConfigurationByPortNumber(8080);
            Assert.assertTrue(httpConfig.backendPort() == 8080);

            // Verify listeners
            // TODO

            // Verify rules
            // TODO

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            Thread updateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    resource.update()
                            //.withSku(ApplicationGatewaySkuName.STANDARD_MEDIUM, 2)
                            .withoutBackendFqdn("www.microsoft.com")
                            .withoutBackendIpAddress("11.1.1.1")
                            .withBackendIpAddress("11.1.1.3", "backend2")
                            .withoutBackendHttpConfiguration("httpConfig2")
                            .updateBackendHttpConfiguration("httpConfig1")
                                .withBackendPort(83)
                                .withoutCookieBasedAffinity()
                                .withRequestTimeout(20)
                                .parent()
                            .withoutBackend("backend3")
                            .withTag("tag1", "value1")
                            .withTag("tag2", "value2")
                            .apply();
                }
            });

            // Start the update thread...
            updateThread.start();

            // ...But bail out after 30 sec as it should be enough to test the results
            updateThread.join(1000 * 30);

            resource.refresh();

            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.sku().name().equals(ApplicationGatewaySkuName.STANDARD_MEDIUM));
            Assert.assertTrue(resource.sku().capacity() == 2);

            // Verify backends
            ApplicationGatewayBackend defaultBackend = resource.backends().get("default");
            Assert.assertTrue(defaultBackend.addresses().size() == 1);
            Assert.assertTrue(defaultBackend.addresses().get(0).ipAddress().equalsIgnoreCase("11.1.1.2"));

            ApplicationGatewayBackend backend2 = resource.backends().get("backend2");
            Assert.assertTrue(backend2.addresses().size() == 1);
            Assert.assertTrue(backend2.addresses().get(0).ipAddress().equals("11.1.1.3"));
            Assert.assertTrue(!resource.backends().containsKey("backend3"));

            // Verify HTTP configs
            Assert.assertTrue(resource.backendHttpConfigurations().size() == 1);
            Assert.assertTrue(resource.backendHttpConfigurations().containsKey("httpConfig1"));
            ApplicationGatewayBackendHttpConfiguration httpConfig1 = resource.backendHttpConfigurations().get("httpConfig1");
            Assert.assertTrue(httpConfig1.backendPort() == 83);
            Assert.assertTrue(!httpConfig1.cookieBasedAffinity());
            Assert.assertTrue(httpConfig1.requestTimeout() == 20);

            Assert.assertTrue(!resource.backendHttpConfigurations().containsKey("httpConfig2"));
            return resource;
        }
    }

    // Verifies the settings of the common rest of a complex application gateway
    private static void assertRestOfComplexDefinition(ApplicationGateway appGateway) {
        // Verify frontend ports
        Assert.assertTrue(appGateway.frontendPorts().size() == 3);

        // Verify backends
        Assert.assertTrue(appGateway.backends().size() == 3);
        Assert.assertTrue(appGateway.backends().containsKey("default"));
        Assert.assertTrue(appGateway.backends().containsKey("backend2"));
        Assert.assertTrue(appGateway.backends().containsKey("backend3"));

        // Verify backend HTTP configs
        Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 3);
        Assert.assertTrue(appGateway.backendHttpConfigurations().containsKey("httpConfig1"));
        ApplicationGatewayBackendHttpConfiguration httpConfig1 = appGateway.backendHttpConfigurations().get("httpConfig1");
        Assert.assertTrue(httpConfig1.backendPort() == 81);
        Assert.assertTrue(httpConfig1.cookieBasedAffinity());
        Assert.assertTrue(httpConfig1.protocol().equals(ApplicationGatewayProtocol.HTTP));
        Assert.assertTrue(httpConfig1.requestTimeout() == 10);

        Assert.assertTrue(appGateway.backendHttpConfigurations().containsKey("httpConfig2"));
        ApplicationGatewayBackendHttpConfiguration httpConfig2 = appGateway.backendHttpConfigurations().get("httpConfig2");
        Assert.assertTrue(httpConfig2.backendPort() == 82);
        Assert.assertTrue(!httpConfig2.cookieBasedAffinity());
        Assert.assertTrue(httpConfig2.protocol().equals(ApplicationGatewayProtocol.HTTPS));
        Assert.assertTrue(httpConfig2.requestTimeout() == 15);

        ApplicationGatewayBackendHttpConfiguration httpConfig = appGateway.getBackendHttpConfigurationByPortNumber(8080);
        Assert.assertTrue(httpConfig != null);
        Assert.assertTrue(httpConfig.backendPort() == 8080);

        // Verify listeners
        Assert.assertTrue(appGateway.frontendHttpListeners().size() == 2);
        ApplicationGatewayFrontendHttpListener listener = appGateway.frontendHttpListeners().get("listener1");
        Assert.assertTrue(listener != null);
        Assert.assertTrue(listener.sslCertificate() != null);
        Assert.assertTrue(listener.protocol().equals(ApplicationGatewayProtocol.HTTPS));
        Assert.assertTrue(listener.frontendPortNumber() == 443);

        listener = appGateway.getFrontendListenerByPortNumber(80);
        Assert.assertTrue(listener != null);
        Assert.assertTrue(listener.protocol().equals(ApplicationGatewayProtocol.HTTP));
        Assert.assertTrue(listener.frontendPortNumber() == 80);

        // Verify SSL certs
        Assert.assertTrue(appGateway.sslCertificates().size() == 2);
        Assert.assertTrue(appGateway.sslCertificates().containsKey("cert1"));

        // Verify request routing rules
        Assert.assertTrue(appGateway.requestRoutingRules().size() == 1);
        Assert.assertTrue(appGateway.requestRoutingRules().containsKey("rule1"));
        ApplicationGatewayRequestRoutingRule rule = appGateway.requestRoutingRules().get("rule1");

        ApplicationGatewayBackend backend = rule.backend();
        Assert.assertTrue(backend != null);
        Assert.assertTrue(backend.name().equalsIgnoreCase("default"));

        httpConfig = rule.backendHttpConfiguration();
        Assert.assertTrue(httpConfig != null);
        Assert.assertTrue(httpConfig.backendPort() == 8080);

        listener = rule.frontendHttpListener();
        Assert.assertTrue(listener != null);
        Assert.assertTrue(listener.frontendPortNumber() == 80);
    }

    // Defines the common rest unrelated to the Internet-facing vs internal nature of application gateway for the complex tests
    private static Creatable<ApplicationGateway> restOfComplexDefinition(ApplicationGateway.DefinitionStages.WithHttpListener agDefinition) {
        return agDefinition
            // HTTP listeners
            .withFrontendHttpListenerOnPort(80)
            .defineFrontendHttpListener("listener1")
                .withFrontendPort(443)
                .withHttps()
                .withSslCertificateFromPfxFile(new File("myTest.pfx"))
                .withSslCertificatePassword("Abc123")
                .attach()

            // HTTP configs
            .withBackendHttpConfigurationOnPort(8080)
            .defineBackendHttpConfiguration("httpConfig1")
                .withBackendPort(81) // Optional, 80 default
                .withCookieBasedAffinity()
                .withProtocol(ApplicationGatewayProtocol.HTTP)
                .withRequestTimeout(10)
                .attach()
            .defineBackendHttpConfiguration("httpConfig2")
                .withBackendPort(82)
                .withProtocol(ApplicationGatewayProtocol.HTTPS)
                .withRequestTimeout(15)
                .attach()

            // Backends
            .withBackendIpAddress("11.1.1.1")
            .withBackendIpAddress("11.1.1.2")
            .withBackendFqdn("www.microsoft.com", "backend2")
            .defineBackend("backend3")
                .attach()

            // Request routing rules
            .defineRequestRoutingRule("rule1")
                .fromFrontendPort(80)
                .toBackendPort(8080)
                .withBackend("default")
                // TODO withRuleType
                .attach()

            // SSL certificates
            .defineSslCertificate("cert1")
                .withPfxFromFile(new File("myTest2.pfx"))
                .withPfxPassword("Abc123")
                .attach()

            // Additional frontend ports
            .withFrontendPort(81, "port1");
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

        // Show IP configs
        Map<String, ApplicationGatewayIpConfiguration> ipConfigs = resource.ipConfigurations();
        info.append("\n\tIP configurations: ").append(ipConfigs.size());
        for (ApplicationGatewayIpConfiguration ipConfig : ipConfigs.values()) {
            info.append("\n\t\tName: ").append(ipConfig.name())
                .append("\n\t\t\tNetwork id: ").append(ipConfig.networkId())
                .append("\n\t\t\tSubnet name: ").append(ipConfig.subnetName());
        }

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

        // Show backend HTTP configurations
        Map<String, ApplicationGatewayBackendHttpConfiguration> httpConfigs = resource.backendHttpConfigurations();
        info.append("\n\tHTTP Configurations: ").append(httpConfigs.size());
        for (ApplicationGatewayBackendHttpConfiguration httpConfig : httpConfigs.values()) {
            info.append("\n\t\tName: ").append(httpConfig.name())
                .append("\n\t\t\tCookie based affinity: ").append(httpConfig.cookieBasedAffinity())
                .append("\n\t\t\tPort: ").append(httpConfig.backendPort())
                .append("\n\t\t\tRequest timeout in seconds: ").append(httpConfig.requestTimeout())
                .append("\n\t\t\tProtocol: ").append(httpConfig.protocol());
        }

        // Show SSL certificates
        Map<String, ApplicationGatewaySslCertificate> sslCerts = resource.sslCertificates();
        info.append("\n\tSSL certificates: ").append(sslCerts.size());
        for (ApplicationGatewaySslCertificate cert : sslCerts.values()) {
            info.append("\n\t\tName: ").append(cert.name())
                .append("\n\t\t\tCert data: ").append(cert.publicData());
        }

        // Show HTTP listeners
        Map<String, ApplicationGatewayFrontendHttpListener> listeners = resource.frontendHttpListeners();
        info.append("\n\tHTTP listeners: ").append(listeners.size());
        for (ApplicationGatewayFrontendHttpListener listener : listeners.values()) {
            info.append("\n\t\tName: ").append(listener.name())
                .append("\n\t\t\tAssociated frontend name: ").append(listener.frontend().name())
                .append("\n\t\t\tFrontend port name: ").append(listener.frontendPortName())
                .append("\n\t\t\tFrontend port number: ").append(listener.frontendPortNumber())
                .append("\n\t\t\tProtocol: ").append(listener.protocol().toString());
                if (listener.sslCertificate() != null) {
                    info.append("\n\t\t\tAssociated SSL certificate: ").append(listener.sslCertificate().name());
                }
        }

        // Show request routing rules
        Map<String, ApplicationGatewayRequestRoutingRule> rules = resource.requestRoutingRules();
        info.append("\n\tRequest routing rules: ").append(rules.size());
        for (ApplicationGatewayRequestRoutingRule rule : rules.values()) {
            info.append("\n\t\tName: ").append(rule.name())
                .append("\n\t\t\tType: ").append(rule.ruleType());

            // Show backend
            info.append("\n\t\t\tAssociated backend address pool: ");
            ApplicationGatewayBackend backend = rule.backend();
            if (backend == null) {
                info.append("(None)");
            } else {
                info.append(backend.name());
            }

            // Show backend HTTP settings config
            info.append("\n\t\t\tAssociated backend HTTP settings configuration: ");
            ApplicationGatewayBackendHttpConfiguration config = rule.backendHttpConfiguration();
            if (config == null) {
                info.append("(None)");
            } else {
                info.append(config.name());
            }

            // Show frontend listener
            info.append("\n\t\t\tAssociated frontend listener: ");
            ApplicationGatewayFrontendHttpListener listener = rule.frontendHttpListener();
            if (listener == null) {
                info.append("(None)");
            } else {
                info.append(config.name());
            }
        }
        System.out.println(info.toString());
    }
}
