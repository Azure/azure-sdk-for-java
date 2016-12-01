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

import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendAddress;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontendListener;
import com.microsoft.azure.management.network.ApplicationGatewayIpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
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
        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            // Prepare a separate thread for resource creation
            Thread creationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Create an application gateway
                    resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                        .withRegion(REGION)
                        .withNewResourceGroup(GROUP_NAME)
                        .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)
                        .withoutPublicFrontend()            // No public frontend
                        .withPrivateFrontend()              // Private frontend

                        // Request routing rules
                        .defineRequestRoutingRule("rule1")
                            .fromFrontendHttpPort(80)
                            .toBackendHttpPort(8080)
                            .withBackendIpAddress("11.1.1.1")
                            .withBackendIpAddress("11.1.1.2")
                            .attach()
                        .create();
                }
            });

            // Start the creation...
            creationThread.start();

            //...But bail out after 30 sec, as it is enough to test the results
            Thread.sleep(30 * 1000);

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.manager().applicationGateways().getById(resourceId);

            // Verify frontends
            Assert.assertTrue(appGateway.isPrivate());
            Assert.assertTrue(!appGateway.isPublic());
            Assert.assertTrue(appGateway.frontends().size() == 1);
            ApplicationGatewayFrontend frontend = appGateway.defaultFrontend();
            Assert.assertTrue(frontend != null);
            Assert.assertTrue(frontend.isPrivate());
            Assert.assertTrue(!frontend.isPublic());

            // Verify frontend ports
            // TODO

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 1);

            // Verify backend HTTP configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 1);

            // Verify listeners
            // TODO

            // Verify rules
            Assert.assertTrue(appGateway.requestRoutingRules().size() == 1);
            ApplicationGatewayRequestRoutingRule rule = appGateway.requestRoutingRules().get("rule1");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(rule.backendAddresses().size() == 2);

            creationThread.join(5 * 1000);
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
            Thread.sleep(1000 * 30);

            resource.refresh();

            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.sku().name().equals(ApplicationGatewaySkuName.STANDARD_MEDIUM));
            Assert.assertTrue(resource.sku().capacity() == 2);

            // Verify backends
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
            updateThread.join(5 * 1000);
            return resource;
        }
    }

    /**
     * Complex internal (private) app gateway test.
     */
    public static class PrivateComplex extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        //private final VirtualMachines vms;
        private final Networks networks;

        /**
         * Tests minimal internal app gateways.
         * @param networks networks
         */
        public PrivateComplex(Networks networks) {
            this.networks = networks;
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            final Network vnet = this.networks.define("net" + TEST_ID)
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

                            // Public frontend
                            .withoutPublicFrontend()

                            // Private frontend
                            .withPrivateFrontend())
                    .withContainingSubnet(vnet, "subnet1")
                    .withPrivateIpAddressStatic("10.0.0.4")
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

            // Verify frontends
            Assert.assertTrue(appGateway.isPrivate());
            Assert.assertTrue(!appGateway.isPublic());
            Assert.assertTrue(appGateway.frontends().size() == 1);
            ApplicationGatewayFrontend frontend;

            frontend = appGateway.defaultFrontend();
            Assert.assertTrue(frontend != null);
            Assert.assertTrue(!frontend.isPublic());
            Assert.assertTrue(frontend.isPrivate());
            Assert.assertTrue(frontend.subnetName().equalsIgnoreCase("subnet1"));
            Assert.assertTrue(IPAllocationMethod.STATIC.equals(frontend.privateIpAllocationMethod()));
            Assert.assertTrue("10.0.0.4".equalsIgnoreCase(frontend.privateIpAddress()));

            assertRestOfComplexDefinition(appGateway);

            creationThread.join(5 * 1000);

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

        /**
         * Tests minimal internal app gateways.
         * @param pips public IPs
         */
        public PublicComplex(PublicIpAddresses pips) {
            this.pips = pips;
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            //VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, TestApplicationGateway.VM_IDS);
            final List<PublicIpAddress> existingPips = ensurePIPs(pips);
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

                            // Public frontend
                            .withExistingPublicIpAddress(existingPips.get(0)) // TODO Make optional

                            // Private frontend
                            .withoutPrivateFrontend()) // TODO Make optional (enable by default)
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

            // Verify frontends
            Assert.assertTrue(appGateway.isPublic());
            Assert.assertTrue(!appGateway.isPrivate());
            Assert.assertTrue(appGateway.frontends().size() == 1);
            ApplicationGatewayFrontend frontend = appGateway.defaultFrontend();
            Assert.assertTrue(frontend != null);
            Assert.assertTrue(frontend.isPublic());
            Assert.assertTrue(!frontend.isPrivate());
            ApplicationGatewayFrontend publicFrontend = frontend;
            Assert.assertTrue(publicFrontend.publicIpAddressId().equalsIgnoreCase(existingPips.get(0).id()));

            assertRestOfComplexDefinition(appGateway);

            creationThread.join(5 * 1000);

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

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            // Prepare a separate thread for resource creation
            Thread creationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Create an application gateway
                    resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                        .withRegion(REGION)
                        .withNewResourceGroup(GROUP_NAME)
                        .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)
                        .withNewPublicIpAddress()                           // Public default frontend
                        .withoutPrivateFrontend()                           // No private frontend

                        // Request routing rules
                        .defineRequestRoutingRule("rule1")
                            .fromFrontendHttpsPort(443)
                            .withSslCertificateFromPfxFile(new File("myTest.pfx"))
                            .withSslCertificatePassword("Abc123")
                            .toBackendHttpPort(8080) // TODO: toBackendHttpsPort(...)
                            .withBackendIpAddress("11.1.1.1")
                            .withBackendIpAddress("11.1.1.2")
                            .attach()
                        .create();
                }
            });

            // Start the creation...
            creationThread.start();

            //...But bail out after 30 sec, as it is enough to test the results
            Thread.sleep(30 * 1000);

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.manager().applicationGateways().getById(resourceId);

            // Verify frontends
            Assert.assertTrue(appGateway.isPublic());
            Assert.assertTrue(!appGateway.isPrivate());
            Assert.assertTrue(appGateway.frontends().size() == 1);
            ApplicationGatewayFrontend frontend = appGateway.defaultFrontend();
            Assert.assertTrue(frontend != null);
            Assert.assertTrue(frontend.isPublic());
            Assert.assertTrue(!frontend.isPrivate());

            // Verify frontend ports
            // TODO

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 1);

            // Verify backend HTTP configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 1);

            // Verify listeners
            // TODO

            // Verify rules
            Assert.assertTrue(appGateway.requestRoutingRules().size() == 1);
            ApplicationGatewayRequestRoutingRule rule = appGateway.requestRoutingRules().get("rule1");
            Assert.assertTrue(rule.publicIpAddressId() != null);
            Assert.assertTrue(rule.frontendListener() != null);
            Assert.assertTrue(rule.frontendPort() == 443);
            Assert.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(rule.protocol()));
            Assert.assertTrue(rule.sslCertificate() != null);
            Assert.assertTrue(rule.backendAddresses().size() == 2);

            creationThread.join(5 * 1000);
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

    // Defines the common rest unrelated to the Internet-facing vs internal nature of application gateway for the complex tests
    private static ApplicationGateway.DefinitionStages.WithCreate restOfComplexDefinition(
            ApplicationGateway.DefinitionStages.WithRequestRoutingRule agDefinition) {
        return agDefinition
            // Request routing rules
            .defineRequestRoutingRule("rule80")
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(8080)
                .withBackendIpAddress("11.1.1.1")
                .withBackendIpAddress("11.1.1.2")
                .withHostName("www.fabricam.com")
                .withoutCookieBasedAffinity()
                // TODO withRuleType
                .attach()

            .defineRequestRoutingRule("rule443")
                .fromFrontendHttpsPort(443)
                .withSslCertificateFromPfxFile(new File("myTest2.pfx"))
                .withSslCertificatePassword("Abc123")
                .withServerNameIndication()
                .toBackendHttpPort(8080)
                .withBackend("backend2")
                .withHostName("www.contoso.com")
                .withCookieBasedAffinity()
                .attach()

            // OPTIONALS

            // HTTP listeners
            .defineFrontendListener("listener444")
                .withFrontendPort(444)
                .withHttps()
                .withSslCertificateFromPfxFile(new File("myTest.pfx"))
                .withSslCertificatePassword("Abc123")
                .withHostName("www.example.com")
                .attach()

            // Backend HTTP configs (explicit)
            .defineBackendHttpConfiguration("httpConfig1")
                .withBackendPort(81) // Optional, 80 default
                .withCookieBasedAffinity()
                .withProtocol(ApplicationGatewayProtocol.HTTP)
                .withRequestTimeout(10)
                .attach()

            // Additional frontend ports
            .withFrontendPort(82, "port1")

            // Backends
            .defineBackend("backend2")
                .withFqdn("www.microsoft.com")
                .attach();
    }

    // Verifies the settings of the common rest of a complex application gateway
    private static void assertRestOfComplexDefinition(ApplicationGateway appGateway) {
        // Verify IP configs
        Assert.assertTrue(appGateway.ipConfigurations().size() == 1);
        ApplicationGatewayIpConfiguration ipConfig = appGateway.ipConfigurations().values().iterator().next();
        Assert.assertTrue(ipConfig != null);
        Assert.assertTrue("default".equalsIgnoreCase(ipConfig.name()));
        Subnet subnet = ipConfig.getSubnet();
        Assert.assertTrue(subnet != null);

        // Verify frontend ports
        Assert.assertTrue(appGateway.frontendPorts().size() == 4);

        // Verify backends
        Assert.assertTrue(appGateway.backends().size() == 2);
        ApplicationGatewayBackend backend = appGateway.backends().get("backend2");
        Assert.assertTrue(backend != null);
        Assert.assertTrue(backend.addresses().size() == 1);

        // Verify backend HTTP configs
        Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 3);
        Assert.assertTrue(appGateway.backendHttpConfigurations().containsKey("httpConfig1"));
        ApplicationGatewayBackendHttpConfiguration httpConfig;

        httpConfig = appGateway.backendHttpConfigurations().get("httpConfig1");
        Assert.assertTrue(httpConfig.backendPort() == 81);
        Assert.assertTrue(httpConfig.cookieBasedAffinity());
        Assert.assertTrue(httpConfig.protocol().equals(ApplicationGatewayProtocol.HTTP));
        Assert.assertTrue(httpConfig.requestTimeout() == 10);

        // Verify listeners
        Assert.assertTrue(appGateway.frontendListeners().size() == 3);
        ApplicationGatewayFrontendListener listener;

        listener = appGateway.frontendListeners().get("listener444");
        Assert.assertTrue(listener != null);
        Assert.assertTrue(listener.sslCertificate() != null);
        Assert.assertTrue(listener.protocol().equals(ApplicationGatewayProtocol.HTTPS));
        Assert.assertTrue(listener.frontendPortNumber() == 444);
        Assert.assertTrue("www.example.com".equalsIgnoreCase(listener.hostName()));
        Assert.assertTrue(!listener.requiresServerNameIndication());

        listener = appGateway.getFrontendListenerByPortNumber(80);
        Assert.assertTrue(listener != null);
        Assert.assertTrue(listener.protocol().equals(ApplicationGatewayProtocol.HTTP));
        Assert.assertTrue(listener.frontendPortNumber() == 80);

        listener = appGateway.getFrontendListenerByPortNumber(443);
        Assert.assertTrue(listener != null);
        Assert.assertTrue(listener.sslCertificate() != null);
        Assert.assertTrue(listener.protocol().equals(ApplicationGatewayProtocol.HTTPS));
        Assert.assertTrue(listener.frontendPortNumber() == 443);
        Assert.assertTrue("www.contoso.com".equalsIgnoreCase(listener.hostName()));
        Assert.assertTrue(listener.requiresServerNameIndication());

        // Verify SSL certs
        Assert.assertTrue(appGateway.sslCertificates().size() == 2);

        // Verify request routing rules
        Assert.assertTrue(appGateway.requestRoutingRules().size() == 2);
        ApplicationGatewayRequestRoutingRule rule;

        rule = appGateway.requestRoutingRules().get("rule80");
        Assert.assertTrue(rule != null);
        Assert.assertTrue(rule.frontendPort() == 80);
        Assert.assertTrue(ApplicationGatewayProtocol.HTTP.equals(rule.protocol()));
        Assert.assertTrue("www.fabricam.com".equalsIgnoreCase(rule.hostName()));
        backend = rule.backend();
        Assert.assertTrue(backend != null);
        Assert.assertTrue(backend.addresses().size() == 2);
        httpConfig = rule.backendHttpConfiguration();
        Assert.assertTrue(httpConfig != null);
        Assert.assertTrue(httpConfig.backendPort() == 8080);
        Assert.assertTrue(!rule.cookieBasedAffinity());

        rule = appGateway.requestRoutingRules().get("rule443");
        Assert.assertTrue(rule != null);
        Assert.assertTrue(rule.frontendPort() == 443);
        Assert.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(rule.protocol()));
        Assert.assertTrue("www.contoso.com".equalsIgnoreCase(rule.hostName()));
        Assert.assertTrue(rule.requiresServerNameIndication());
        backend = rule.backend();
        Assert.assertTrue(backend != null);
        Assert.assertTrue("backend2".equalsIgnoreCase(backend.name()));
        httpConfig = rule.backendHttpConfiguration();
        Assert.assertTrue(httpConfig != null);
        Assert.assertTrue(httpConfig.backendPort() == 8080);
        Assert.assertTrue(rule.cookieBasedAffinity());
    }

    // Create VNet for the app gateway
    private static List<PublicIpAddress> ensurePIPs(PublicIpAddresses pips) throws Exception {
        List<Creatable<PublicIpAddress>> creatablePips = new ArrayList<>();
        for (int i = 0; i < PIP_NAMES.length; i++) {
            creatablePips.add(
                    pips.define(PIP_NAMES[i])
                        .withRegion(REGION)
                        .withNewResourceGroup(GROUP_NAME));
        }

        return pips.create(creatablePips);
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
                .append("\n\tSSL policy: ").append(resource.sslPolicy())
                .append("\n\tInternet-facing? ").append(resource.isPublic())
                .append("\n\tInternal? ").append(resource.isPrivate())
                .append("\n\tDefault private IP address: ").append(resource.privateIpAddress())
                .append("\n\tPrivate IP address allocation method: ").append(resource.privateIpAllocationMethod());

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
                // Show public frontend info
                info.append("\n\t\t\tPublic IP address ID: ").append(frontend.publicIpAddressId());
            }

            if (frontend.isPrivate()) {
                // Show private frontend info
                info.append("\n\t\t\tPrivate IP address: ").append(frontend.privateIpAddress())
                    .append("\n\t\t\tPrivate IP allocation method: ").append(frontend.privateIpAllocationMethod())
                    .append("\n\t\t\tSubnet name: ").append(frontend.subnetName())
                    .append("\n\t\t\tVirtual network ID: ").append(frontend.networkId());
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
        Map<String, ApplicationGatewayFrontendListener> listeners = resource.frontendListeners();
        info.append("\n\tHTTP listeners: ").append(listeners.size());
        for (ApplicationGatewayFrontendListener listener : listeners.values()) {
            info.append("\n\t\tName: ").append(listener.name())
                .append("\n\t\t\tHost name: ").append(listener.hostName())
                .append("\n\t\t\tServer name indication required? ").append(listener.requiresServerNameIndication())
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
                .append("\n\t\t\tType: ").append(rule.ruleType())
                .append("\n\t\t\tPublic IP address ID: ").append(rule.publicIpAddressId())
                .append("\n\t\t\tHost name: ").append(rule.hostName())
                .append("\n\t\t\tServer name indication required? ").append(rule.requiresServerNameIndication())
                .append("\n\t\t\tFrontend port: ").append(rule.frontendPort())
                .append("\n\t\t\tProtocol: ").append(rule.protocol().toString())
                .append("\n\t\t\tBackend port: ").append(rule.backendPort())
                .append("\n\t\t\tCookie based affinity enabled? ").append(rule.cookieBasedAffinity());

            // Show backend addresses
            List<ApplicationGatewayBackendAddress> addresses = rule.backendAddresses();
            info.append("\n\t\t\tBackend addresses: ").append(addresses.size());
            for (ApplicationGatewayBackendAddress address : addresses) {
                info.append("\n\t\t\t\t")
                    .append(address.fqdn())
                    .append(" [").append(address.ipAddress()).append("]");
            }

            // Show SSL cert
            info.append("\n\t\t\tSSL certificate name: ");
            ApplicationGatewaySslCertificate cert = rule.sslCertificate();
            if (cert == null) {
                info.append("(None)");
            } else {
                info.append(cert.name());
            }

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
            ApplicationGatewayFrontendListener listener = rule.frontendListener();
            if (listener == null) {
                info.append("(None)");
            } else {
                info.append(config.name());
            }
        }
        System.out.println(info.toString());
    }
}
