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
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import com.microsoft.azure.management.network.ApplicationGatewayIpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewaySkuName;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import com.microsoft.azure.management.network.ApplicationGateways;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
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

                        // Request routing rules
                        .defineRequestRoutingRule("rule1")
                            .fromPrivateFrontend()
                            .fromFrontendHttpPort(80)
                            .toBackendHttpPort(8080)
                            .toBackendIpAddress("11.1.1.1")
                            .toBackendIpAddress("11.1.1.2")
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
            Assert.assertTrue(appGateway != null);

            // Verify frontend ports
            Assert.assertTrue(appGateway.frontendPorts().size() == 1);
            Assert.assertTrue(appGateway.frontendPortNameFromNumber(80) != null);

            // Verify frontends
            Assert.assertTrue(appGateway.isPrivate());
            Assert.assertTrue(!appGateway.isPublic());
            Assert.assertTrue(appGateway.frontends().size() == 1);

            // Verify listeners
            Assert.assertTrue(appGateway.listeners().size() == 1);
            Assert.assertTrue(appGateway.listenerByPortNumber(80) != null);

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 1);

            // Verify backend HTTP configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 1);

            // Verify rules
            Assert.assertTrue(appGateway.requestRoutingRules().size() == 1);
            ApplicationGatewayRequestRoutingRule rule = appGateway.requestRoutingRules().get("rule1");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(rule.frontendPort() == 80);
            Assert.assertTrue(rule.listener() != null);
            Assert.assertTrue(rule.listener().frontend() != null);
            Assert.assertTrue(!rule.listener().frontend().isPublic());
            Assert.assertTrue(rule.listener().frontend().isPrivate());
            Assert.assertTrue(rule.listener().subnetName() != null);
            Assert.assertTrue(rule.listener().networkId() != null);
            Assert.assertTrue(rule.backendAddresses().size() == 2);
            Assert.assertTrue(ApplicationGatewayProtocol.HTTP.equals(rule.frontendProtocol()));
            Assert.assertTrue(rule.backendPort() == 8080);
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
                    resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                        .withRegion(REGION)
                        .withExistingResourceGroup(GROUP_NAME)
                        .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)

                        // Request routing rules
                        .defineRequestRoutingRule("rule80")
                            .fromPrivateFrontend()
                            .fromFrontendHttpPort(80)
                            .toBackendHttpPort(8080)
                            .toBackendIpAddress("11.1.1.1")
                            .toBackendIpAddress("11.1.1.2")
                            .withCookieBasedAffinity()
                            .attach()
                        .defineRequestRoutingRule("rule443")
                            .fromPrivateFrontend()
                            .fromFrontendHttpsPort(443)
                            .withSslCertificateFromPfxFile(new File("myTest.pfx"))
                            .withSslCertificatePassword("Abc123")
                            .toBackendHttpConfiguration("config1")
                            .toBackend("backend1")
                            .attach()
                        .defineRequestRoutingRule("rule9000")
                            .fromListener("listener1")
                            .toBackendHttpConfiguration("config1")
                            .toBackend("backend1")
                            .attach()

                        // Additional/explicit backend HTTP setting configs
                        .defineBackendHttpConfiguration("config1")
                            .withBackendPort(8081)
                            .withRequestTimeout(45)
                            .attach()

                        // Additional/explicit backends
                        .defineBackend("backend1")
                            .withIpAddress("11.1.1.3")
                            .withIpAddress("11.1.1.4")
                            .attach()

                        // Additional/explicit frontend listeners
                        .defineListener("listener1")
                            // TODO: Where is the frontend?
                            .withFrontendPort(9000)
                            .withHttp()
                            .attach()

                        .withExistingSubnet(vnet, "subnet1")
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
            Assert.assertTrue(!appGateway.isPublic());
            Assert.assertTrue(appGateway.isPrivate());
            Assert.assertTrue(appGateway.ipConfigurations().size() == 1);

            // Verify frontend ports
            Assert.assertTrue(appGateway.frontendPorts().size() == 3);
            Assert.assertTrue(appGateway.frontendPortNameFromNumber(80) != null);
            Assert.assertTrue(appGateway.frontendPortNameFromNumber(443) != null);
            Assert.assertTrue(appGateway.frontendPortNameFromNumber(9000) != null);

            // Verify frontends
            Assert.assertTrue(appGateway.frontends().size() == 1);
            Assert.assertTrue(appGateway.publicFrontends().size() == 0);
            Assert.assertTrue(appGateway.privateFrontends().size() == 1);
            ApplicationGatewayFrontend frontend = appGateway.privateFrontends().values().iterator().next();
            Assert.assertTrue(!frontend.isPublic());
            Assert.assertTrue(frontend.isPrivate());

            // Verify listeners
            Assert.assertTrue(appGateway.listeners().size() == 3);
            ApplicationGatewayListener listener = appGateway.listeners().get("listener1");
            Assert.assertTrue(listener != null);
            Assert.assertTrue(listener.frontendPortNumber() == 9000);
            Assert.assertTrue(ApplicationGatewayProtocol.HTTP.equals(listener.protocol()));
            Assert.assertTrue(appGateway.listenerByPortNumber(80) != null);
            Assert.assertTrue(appGateway.listenerByPortNumber(443) != null);

            // Verify certificates
            Assert.assertTrue(appGateway.sslCertificates().size() == 1);

            // Verify backend HTTP settings configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 2);
            ApplicationGatewayBackendHttpConfiguration config = appGateway.backendHttpConfigurations().get("config1");
            Assert.assertTrue(config != null);
            Assert.assertTrue(config.backendPort() == 8081);
            Assert.assertTrue(config.requestTimeout() == 45);

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 2);
            ApplicationGatewayBackend backend = appGateway.backends().get("backend1");
            Assert.assertTrue(backend != null);
            Assert.assertTrue(backend.addresses().size() == 2);

            // Verify request routing rules
            Assert.assertTrue(appGateway.requestRoutingRules().size() == 3);
            ApplicationGatewayRequestRoutingRule rule;

            rule = appGateway.requestRoutingRules().get("rule80");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(vnet.id().equalsIgnoreCase(rule.listener().frontend().networkId()));
            Assert.assertTrue(rule.frontendPort() == 80);
            Assert.assertTrue(rule.backendPort() == 8080);
            Assert.assertTrue(rule.backendAddresses().size() == 2);
            Assert.assertTrue(rule.cookieBasedAffinity());

            rule = appGateway.requestRoutingRules().get("rule443");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(vnet.id().equalsIgnoreCase(rule.listener().frontend().networkId()));
            Assert.assertTrue(rule.frontendPort() == 443);
            Assert.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(rule.frontendProtocol()));
            Assert.assertTrue(rule.sslCertificate() != null);
            Assert.assertTrue(rule.backendHttpConfiguration() != null);
            Assert.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assert.assertTrue(rule.backend() != null);
            Assert.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

            rule = appGateway.requestRoutingRules().get("rule9000");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(rule.listener() != null);
            Assert.assertTrue(rule.listener().name().equalsIgnoreCase("listener1"));
            Assert.assertTrue(rule.listener().subnetName() != null);
            Assert.assertTrue(rule.listener().networkId() != null);
            Assert.assertTrue(rule.backendHttpConfiguration() != null);
            Assert.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assert.assertTrue(rule.backend() != null);
            Assert.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

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
                    resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                        .withRegion(REGION)
                        .withExistingResourceGroup(GROUP_NAME)
                        .withSku(ApplicationGatewaySkuName.STANDARD_SMALL, 1)

                        // Request routing rules
                        .defineRequestRoutingRule("rule80")
                            .fromPublicFrontend()
                            .fromFrontendHttpPort(80)
                            .toBackendHttpPort(8080)
                            .toBackendFqdn("www.microsoft.com")
                            .toBackendFqdn("www.example.com")
                            .toBackendIpAddress("11.1.1.1")
                            .toBackendIpAddress("11.1.1.2")
                            .withCookieBasedAffinity()
                            .attach()
                        .defineRequestRoutingRule("rule443")
                            .fromPublicFrontend()
                            .fromFrontendHttpsPort(443)
                            .withSslCertificateFromPfxFile(new File("myTest.pfx"))
                            .withSslCertificatePassword("Abc123")
                            .toBackendHttpConfiguration("config1")
                            .toBackend("backend1")
                            .attach()
                        .defineRequestRoutingRule("rule9000")
                            .fromListener("listener1")
                            .toBackendHttpConfiguration("config1")
                            .toBackend("backend1")
                            .attach()

                        // Additional/explicit backend HTTP setting configs
                        .defineBackendHttpConfiguration("config1")
                            .withBackendPort(8081)
                            .withRequestTimeout(45)
                            .attach()

                        // Additional/explicit backends
                        .defineBackend("backend1")
                            .withIpAddress("11.1.1.1")
                            .withIpAddress("11.1.1.2")
                            .attach()

                        // Additional/explicit frontend listeners
                        .defineListener("listener1")
                            .withFrontendPort(9000)
                            .withHttps()
                            .withSslCertificateFromPfxFile(new File("myTest2.pfx"))
                            .withSslCertificatePassword("Abc123")
                            .withServerNameIndication()
                            .withHostName("www.fabricam.com")
                            .attach()

                        .withExistingPublicIpAddress(existingPips.get(0))
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
            Assert.assertTrue(appGateway.isPublic());
            Assert.assertTrue(!appGateway.isPrivate());
            Assert.assertTrue(appGateway.ipConfigurations().size() == 1);

            // Verify frontend ports
            Assert.assertTrue(appGateway.frontendPorts().size() == 3);
            Assert.assertTrue(appGateway.frontendPortNameFromNumber(80) != null);
            Assert.assertTrue(appGateway.frontendPortNameFromNumber(443) != null);
            Assert.assertTrue(appGateway.frontendPortNameFromNumber(9000) != null);

            // Verify frontends
            Assert.assertTrue(appGateway.frontends().size() == 1);
            Assert.assertTrue(appGateway.publicFrontends().size() == 1);
            Assert.assertTrue(appGateway.privateFrontends().size() == 0);
            ApplicationGatewayFrontend frontend = appGateway.publicFrontends().values().iterator().next();
            Assert.assertTrue(frontend.isPublic());
            Assert.assertTrue(!frontend.isPrivate());

            // Verify listeners
            Assert.assertTrue(appGateway.listeners().size() == 3);
            ApplicationGatewayListener listener = appGateway.listeners().get("listener1");
            Assert.assertTrue(listener != null);
            Assert.assertTrue(listener.frontendPortNumber() == 9000);
            Assert.assertTrue("www.fabricam.com".equalsIgnoreCase(listener.hostName()));
            Assert.assertTrue(listener.requiresServerNameIndication());
            Assert.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(listener.protocol()));
            Assert.assertTrue(appGateway.listenerByPortNumber(80) != null);
            Assert.assertTrue(appGateway.listenerByPortNumber(443) != null);

            // Verify certificates
            Assert.assertTrue(appGateway.sslCertificates().size() == 2);

            // Verify backend HTTP settings configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 2);
            ApplicationGatewayBackendHttpConfiguration config = appGateway.backendHttpConfigurations().get("config1");
            Assert.assertTrue(config != null);
            Assert.assertTrue(config.backendPort() == 8081);
            Assert.assertTrue(config.requestTimeout() == 45);

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 2);
            ApplicationGatewayBackend backend = appGateway.backends().get("backend1");
            Assert.assertTrue(backend != null);
            Assert.assertTrue(backend.addresses().size() == 2);

            // Verify request routing rules
            Assert.assertTrue(appGateway.requestRoutingRules().size() == 3);
            ApplicationGatewayRequestRoutingRule rule;

            rule = appGateway.requestRoutingRules().get("rule80");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(existingPips.get(0).id().equalsIgnoreCase(rule.publicIpAddressId()));
            Assert.assertTrue(rule.frontendPort() == 80);
            Assert.assertTrue(rule.backendPort() == 8080);
            Assert.assertTrue(rule.backendAddresses().size() == 4);
            Assert.assertTrue(rule.cookieBasedAffinity());

            rule = appGateway.requestRoutingRules().get("rule443");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(existingPips.get(0).id().equalsIgnoreCase(rule.publicIpAddressId()));
            Assert.assertTrue(rule.frontendPort() == 443);
            Assert.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(rule.frontendProtocol()));
            Assert.assertTrue(rule.sslCertificate() != null);
            Assert.assertTrue(rule.backendHttpConfiguration() != null);
            Assert.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assert.assertTrue(rule.backend() != null);
            Assert.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

            rule = appGateway.requestRoutingRules().get("rule9000");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(rule.listener() != null);
            Assert.assertTrue(rule.listener().name().equalsIgnoreCase("listener1"));
            Assert.assertTrue(rule.backendHttpConfiguration() != null);
            Assert.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assert.assertTrue(rule.backend() != null);
            Assert.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

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

                        // Request routing rules
                        .defineRequestRoutingRule("rule1")
                            .fromPublicFrontend()
                            .fromFrontendHttpsPort(443)
                            .withSslCertificateFromPfxFile(new File("myTest.pfx"))
                            .withSslCertificatePassword("Abc123")
                            .toBackendHttpPort(8080) // TODO: toBackendHttpsPort(...) when supported
                            .toBackendIpAddress("11.1.1.1")
                            .toBackendIpAddress("11.1.1.2")
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
            Assert.assertTrue(appGateway != null);

            // Verify frontend ports
            Assert.assertTrue(appGateway.frontendPorts().size() == 1);
            Assert.assertTrue(appGateway.frontendPortNameFromNumber(443) != null);

            // Verify frontends
            Assert.assertTrue(!appGateway.isPrivate());
            Assert.assertTrue(appGateway.isPublic());
            Assert.assertTrue(appGateway.frontends().size() == 1);

            // Verify listeners
            Assert.assertTrue(appGateway.listeners().size() == 1);
            Assert.assertTrue(appGateway.listenerByPortNumber(443) != null);

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 1);

            // Verify backend HTTP configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 1);

            // Verify rules
            Assert.assertTrue(appGateway.requestRoutingRules().size() == 1);
            ApplicationGatewayRequestRoutingRule rule = appGateway.requestRoutingRules().get("rule1");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(rule.frontendPort() == 443);
            Assert.assertTrue(rule.listener() != null);
            Assert.assertTrue(rule.listener().frontend() != null);
            Assert.assertTrue(rule.listener().frontend().isPublic());
            Assert.assertTrue(!rule.listener().frontend().isPrivate());
            Assert.assertTrue(rule.backendAddresses().size() == 2);
            Assert.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(rule.frontendProtocol()));
            Assert.assertTrue(rule.backendPort() == 8080);
            Assert.assertTrue(rule.backendAddresses().size() == 2);
            Assert.assertTrue(rule.sslCertificate() != null);

            // Verify certificates
            Assert.assertTrue(appGateway.sslCertificates().size() == 1);

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
        Map<String, ApplicationGatewayListener> listeners = resource.listeners();
        info.append("\n\tHTTP listeners: ").append(listeners.size());
        for (ApplicationGatewayListener listener : listeners.values()) {
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
                .append("\n\t\t\tFrontend protocol: ").append(rule.frontendProtocol().toString())
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
            ApplicationGatewayListener listener = rule.listener();
            if (listener == null) {
                info.append("(None)");
            } else {
                info.append(config.name());
            }
        }
        System.out.println(info.toString());
    }
}
