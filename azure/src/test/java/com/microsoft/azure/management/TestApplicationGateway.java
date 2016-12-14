/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.io.File;
import java.io.IOException;
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
import com.microsoft.azure.management.network.ApplicationGatewayTier;
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
            Assert.assertTrue(ApplicationGatewayTier.STANDARD.equals(appGateway.tier()));
            Assert.assertTrue(ApplicationGatewaySkuName.STANDARD_SMALL.equals(appGateway.size()));
            Assert.assertTrue(appGateway.instanceCount() == 1);

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
            Assert.assertTrue(ApplicationGatewayProtocol.HTTP.equals(rule.frontendProtocol()));
            Assert.assertTrue(rule.listener() != null);
            Assert.assertTrue(rule.listener().frontend() != null);
            Assert.assertTrue(!rule.listener().frontend().isPublic());
            Assert.assertTrue(rule.listener().frontend().isPrivate());
            Assert.assertTrue(rule.listener().subnetName() != null);
            Assert.assertTrue(rule.listener().networkId() != null);
            Assert.assertTrue(rule.backendAddresses().size() == 2);
            Assert.assertTrue(rule.backend() != null);
            Assert.assertTrue(rule.backend().containsIpAddress("11.1.1.1"));
            Assert.assertTrue(rule.backend().containsIpAddress("11.1.1.2"));
            Assert.assertTrue(rule.backendPort() == 8080);

            creationThread.join();
            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            resource.update()
                .withInstanceCount(2)
                .withSize(ApplicationGatewaySkuName.STANDARD_MEDIUM)
                .withFrontendPort(81, "port81")         // Add a new port
                .withoutBackendIpAddress("11.1.1.1")    // Remove from all existing backends
                .defineListener("listener2")
                    .withPrivateFrontend()
                    .withFrontendPort(81)
                    .withHttps()
                    .withSslCertificateFromPfxFile(new File("myTest.pfx"))
                    .withSslCertificatePassword("Abc123")
                    .attach()
                .defineBackend("backend2")
                    .withIpAddress("11.1.1.3")
                    .attach()
                .defineBackendHttpConfiguration("config2")
                    .withCookieBasedAffinity()
                    .withPort(8081)
                    .withRequestTimeout(33)
                    .attach()
                .defineRequestRoutingRule("rule2")
                    .fromListener("listener2")
                    .toBackendHttpConfiguration("config2")
                    .toBackend("backend2")
                    .attach()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();

            resource.refresh();

            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.tags().containsKey("tag2"));
            Assert.assertTrue(ApplicationGatewaySkuName.STANDARD_MEDIUM.equals(resource.size()));
            Assert.assertTrue(resource.instanceCount() == 2);

            // Verify frontend ports
            Assert.assertTrue(resource.frontendPorts().size() == 2);
            Assert.assertTrue(resource.frontendPorts().containsKey("port81"));
            Assert.assertTrue("port81".equalsIgnoreCase(resource.frontendPortNameFromNumber(81)));

            // Verify listeners
            Assert.assertTrue(resource.listeners().size() == 2);
            ApplicationGatewayListener listener = resource.listeners().get("listener2");
            Assert.assertTrue(listener != null);
            Assert.assertTrue(listener.frontend().isPrivate());
            Assert.assertTrue(!listener.frontend().isPublic());
            Assert.assertTrue("port81".equalsIgnoreCase(listener.frontendPortName()));
            Assert.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(listener.protocol()));
            Assert.assertTrue(listener.sslCertificate() != null);

            // Verify backends
            Assert.assertTrue(resource.backends().size() == 2);
            ApplicationGatewayBackend backend = resource.backends().get("backend2");
            Assert.assertTrue(backend != null);
            Assert.assertTrue(backend.addresses().size() == 1);
            Assert.assertTrue(backend.containsIpAddress("11.1.1.3"));

            // Verify HTTP configs
            Assert.assertTrue(resource.backendHttpConfigurations().size() == 2);
            ApplicationGatewayBackendHttpConfiguration config = resource.backendHttpConfigurations().get("config2");
            Assert.assertTrue(config != null);
            Assert.assertTrue(config.cookieBasedAffinity());
            Assert.assertTrue(config.port() == 8081);
            Assert.assertTrue(config.requestTimeout() == 33);

            // Verify request routing rules
            Assert.assertTrue(resource.requestRoutingRules().size() == 2);
            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule2");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(rule.listener() != null);
            Assert.assertTrue("listener2".equals(rule.listener().name()));
            Assert.assertTrue(rule.backendHttpConfiguration() != null);
            Assert.assertTrue("config2".equalsIgnoreCase(rule.backendHttpConfiguration().name()));
            Assert.assertTrue(rule.backend() != null);
            Assert.assertTrue("backend2".equalsIgnoreCase(rule.backend().name()));

            return resource;
        }
    }

    /**
     * Complex internal (private) app gateway test.
     */
    public static class PrivateComplex extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        //private final VirtualMachines vms;
        private final Networks networks;
        private final List<PublicIpAddress> testPips;

        /**
         * Tests minimal internal app gateways.
         * @param networks networks
         * @param pips public IP addresses
         * @throws Exception when something goes wrong
         */
        public PrivateComplex(Networks networks, PublicIpAddresses pips) throws Exception {
            this.networks = networks;
            this.testPips = ensurePIPs(pips);
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
                    try {
                        resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                            .withRegion(REGION)
                            .withExistingResourceGroup(GROUP_NAME)

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
                                .withPort(8081)
                                .withRequestTimeout(45)
                                .attach()

                            .defineBackendHttpConfiguration("config2")
                                .attach()

                            // Additional/explicit backends
                            .defineBackend("backend1")
                                .withIpAddress("11.1.1.3")
                                .withIpAddress("11.1.1.4")
                                .attach()

                            .defineBackend("backend2")
                                .attach()

                            // Additional/explicit frontend listeners
                            .defineListener("listener1")
                                .withPrivateFrontend()
                                .withFrontendPort(9000)
                                .withHttp()
                                .attach()

                            // Additional/explicit certificates
                            .defineSslCertificate("cert1")
                                .withPfxFromFile(new File("myTest2.pfx"))
                                .withPfxPassword("Abc123")
                                .attach()

                            .withExistingSubnet(vnet, "subnet1")
                            .withSize(ApplicationGatewaySkuName.STANDARD_MEDIUM)
                            .withInstanceCount(2)
                            .create();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
            Assert.assertTrue(ApplicationGatewayTier.STANDARD.equals(appGateway.tier()));
            Assert.assertTrue(ApplicationGatewaySkuName.STANDARD_MEDIUM.equals(appGateway.size()));
            Assert.assertTrue(appGateway.instanceCount() == 2);
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
            Assert.assertTrue(listener.frontend() != null);
            Assert.assertTrue(listener.frontend().isPrivate());
            Assert.assertTrue(!listener.frontend().isPublic());
            Assert.assertTrue(appGateway.listenerByPortNumber(80) != null);
            Assert.assertTrue(appGateway.listenerByPortNumber(443) != null);

            // Verify certificates
            Assert.assertTrue(appGateway.sslCertificates().size() == 2);
            Assert.assertTrue(appGateway.sslCertificates().containsKey("cert1"));

            // Verify backend HTTP settings configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 3);
            ApplicationGatewayBackendHttpConfiguration config = appGateway.backendHttpConfigurations().get("config1");
            Assert.assertTrue(config != null);
            Assert.assertTrue(config.port() == 8081);
            Assert.assertTrue(config.requestTimeout() == 45);
            Assert.assertTrue(appGateway.backendHttpConfigurations().containsKey("config2"));

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 3);
            ApplicationGatewayBackend backend = appGateway.backends().get("backend1");
            Assert.assertTrue(backend != null);
            Assert.assertTrue(backend.addresses().size() == 2);
            Assert.assertTrue(backend.containsIpAddress("11.1.1.3"));
            Assert.assertTrue(backend.containsIpAddress("11.1.1.4"));
            Assert.assertTrue(appGateway.backends().containsKey("backend2"));

            // Verify request routing rules
            Assert.assertTrue(appGateway.requestRoutingRules().size() == 3);
            ApplicationGatewayRequestRoutingRule rule;

            rule = appGateway.requestRoutingRules().get("rule80");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(vnet.id().equalsIgnoreCase(rule.listener().frontend().networkId()));
            Assert.assertTrue(rule.frontendPort() == 80);
            Assert.assertTrue(rule.backendPort() == 8080);
            Assert.assertTrue(rule.cookieBasedAffinity());
            Assert.assertTrue(rule.backendAddresses().size() == 2);
            Assert.assertTrue(rule.backend().containsIpAddress("11.1.1.1"));
            Assert.assertTrue(rule.backend().containsIpAddress("11.1.1.2"));

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

            creationThread.join();

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            final int portCount = resource.frontendPorts().size();
            final int frontendCount = resource.frontends().size();
            final int listenerCount = resource.listeners().size();
            final int ruleCount = resource.requestRoutingRules().size();
            final int backendCount = resource.backends().size();
            final int configCount = resource.backendHttpConfigurations().size();
            final int certCount = resource.sslCertificates().size();

            resource.update()
                .withSize(ApplicationGatewaySkuName.STANDARD_SMALL)
                .withInstanceCount(1)
                .withoutFrontendPort(9000)
                .withoutListener("listener1")
                .withoutBackendIpAddress("11.1.1.4")
                .withoutBackendHttpConfiguration("config2")
                .withoutBackend("backend2")
                .withoutRequestRoutingRule("rule9000")
                .withoutCertificate("cert1")
                .updateListener(resource.requestRoutingRules().get("rule443").listener().name())
                    .withHostName("foobar")
                    .parent()
                .updateBackendHttpConfiguration("config1")
                    .withPort(8082)
                    .withCookieBasedAffinity()
                    .withRequestTimeout(20)
                    .parent()
                .updateBackend("backend1")
                    .withoutIpAddress("11.1.1.3")
                    .withIpAddress("11.1.1.5")
                    .parent()
                .updateRequestRoutingRule("rule80")
                    .toBackend("backend1")
                    .toBackendHttpConfiguration("config1")
                    .parent()
                .withExistingPublicIpAddress(testPips.get(0)) // Associate with a public IP as well
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();

            resource.refresh();

            // Get the resource created so far
            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.tags().containsKey("tag2"));
            Assert.assertTrue(ApplicationGatewaySkuName.STANDARD_SMALL.equals(resource.size()));
            Assert.assertTrue(resource.instanceCount() == 1);

            // Verify frontend ports
            Assert.assertTrue(resource.frontendPorts().size() == portCount - 1);
            Assert.assertTrue(resource.frontendPortNameFromNumber(9000) == null);

            // Verify frontends
            Assert.assertTrue(resource.frontends().size() == frontendCount + 1);
            Assert.assertTrue(resource.publicFrontends().size() == 1);
            Assert.assertTrue(resource.privateFrontends().size() == 1);
            ApplicationGatewayFrontend frontend = resource.privateFrontends().values().iterator().next();
            Assert.assertTrue(!frontend.isPublic());
            Assert.assertTrue(frontend.isPrivate());

            // Verify listeners
            Assert.assertTrue(resource.listeners().size() == listenerCount - 1);
            Assert.assertTrue(!resource.listeners().containsKey("listener1"));

            // Verify backends
            Assert.assertTrue(resource.backends().size() == backendCount - 1);
            Assert.assertTrue(!resource.backends().containsKey("backend2"));
            ApplicationGatewayBackend backend = resource.backends().get("backend1");
            Assert.assertTrue(backend != null);
            Assert.assertTrue(backend.addresses().size() == 1);
            Assert.assertTrue(backend.containsIpAddress("11.1.1.5"));
            Assert.assertTrue(!backend.containsIpAddress("11.1.1.3"));
            Assert.assertTrue(!backend.containsIpAddress("11.1.1.4"));

            // Verify HTTP configs
            Assert.assertTrue(resource.backendHttpConfigurations().size() == configCount - 1);
            Assert.assertTrue(!resource.backendHttpConfigurations().containsKey("config2"));
            ApplicationGatewayBackendHttpConfiguration config = resource.backendHttpConfigurations().get("config1");
            Assert.assertTrue(config.port() == 8082);
            Assert.assertTrue(config.requestTimeout() == 20);
            Assert.assertTrue(config.cookieBasedAffinity());

            // Verify rules
            Assert.assertTrue(resource.requestRoutingRules().size() == ruleCount - 1);
            Assert.assertTrue(!resource.requestRoutingRules().containsKey("rule9000"));

            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule80");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(rule.backend() != null);
            Assert.assertTrue("backend1".equalsIgnoreCase(rule.backend().name()));
            Assert.assertTrue(rule.backendHttpConfiguration() != null);
            Assert.assertTrue("config1".equalsIgnoreCase(rule.backendHttpConfiguration().name()));

            rule = resource.requestRoutingRules().get("rule443");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(rule.listener() != null);
            Assert.assertTrue("foobar".equalsIgnoreCase(rule.listener().hostName()));

            // Verify certificates
            Assert.assertTrue(resource.sslCertificates().size() == certCount - 1);
            Assert.assertTrue(!resource.sslCertificates().containsKey("cert1"));

            return resource;
        }
    }

    /**
     * Complex Internet-facing (public) app gateway test.
     */
    public static class PublicComplex extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        private final List<PublicIpAddress> testPips;

        /**
         * Tests minimal internal app gateways.
         * @param pips public IPs
         * @throws Exception when something goes wrong with test PIP creation
         */
        public PublicComplex(PublicIpAddresses pips) throws Exception {
            this.testPips = ensurePIPs(pips);
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
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
                    try {
                        resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                            .withRegion(REGION)
                            .withExistingResourceGroup(GROUP_NAME)

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
                                .withPort(8081)
                                .withRequestTimeout(45)
                                .attach()

                            // Additional/explicit backends
                            .defineBackend("backend1")
                                .withIpAddress("11.1.1.1")
                                .withIpAddress("11.1.1.2")
                                .attach()

                            // Additional/explicit frontend listeners
                            .defineListener("listener1")
                                .withPublicFrontend()
                                .withFrontendPort(9000)
                                .withHttps()
                                .withSslCertificateFromPfxFile(new File("myTest2.pfx"))
                                .withSslCertificatePassword("Abc123")
                                .withServerNameIndication()
                                .withHostName("www.fabricam.com")
                                .attach()

                            .withExistingPublicIpAddress(testPips.get(0))
                            .withSize(ApplicationGatewaySkuName.STANDARD_MEDIUM)
                            .withInstanceCount(2)
                            .create();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
            Assert.assertTrue(ApplicationGatewayTier.STANDARD.equals(appGateway.tier()));
            Assert.assertTrue(ApplicationGatewaySkuName.STANDARD_MEDIUM.equals(appGateway.size()));
            Assert.assertTrue(appGateway.instanceCount() == 2);
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
            Assert.assertTrue(listener.frontend() != null);
            Assert.assertTrue(!listener.frontend().isPrivate());
            Assert.assertTrue(listener.frontend().isPublic());
            Assert.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(listener.protocol()));
            Assert.assertTrue(appGateway.listenerByPortNumber(80) != null);
            Assert.assertTrue(appGateway.listenerByPortNumber(443) != null);

            // Verify certificates
            Assert.assertTrue(appGateway.sslCertificates().size() == 2);

            // Verify backend HTTP settings configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 2);
            ApplicationGatewayBackendHttpConfiguration config = appGateway.backendHttpConfigurations().get("config1");
            Assert.assertTrue(config != null);
            Assert.assertTrue(config.port() == 8081);
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
            Assert.assertTrue(testPips.get(0).id().equalsIgnoreCase(rule.publicIpAddressId()));
            Assert.assertTrue(rule.frontendPort() == 80);
            Assert.assertTrue(rule.backendPort() == 8080);
            Assert.assertTrue(rule.cookieBasedAffinity());
            Assert.assertTrue(rule.backendAddresses().size() == 4);
            Assert.assertTrue(rule.backend().containsIpAddress("11.1.1.2"));
            Assert.assertTrue(rule.backend().containsIpAddress("11.1.1.1"));
            Assert.assertTrue(rule.backend().containsFqdn("www.microsoft.com"));
            Assert.assertTrue(rule.backend().containsFqdn("www.example.com"));

            rule = appGateway.requestRoutingRules().get("rule443");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(testPips.get(0).id().equalsIgnoreCase(rule.publicIpAddressId()));
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

            creationThread.join();

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            final int rulesCount = resource.requestRoutingRules().size();

            resource.update()
                .withSize(ApplicationGatewaySkuName.STANDARD_SMALL)
                .withInstanceCount(1)
                .updateListener("listener1")
                    .withHostName("www.contoso.com")
                    .parent()
                .updateRequestRoutingRule("rule443")
                    .fromListener("listener1")
                    .parent()
                .withoutRequestRoutingRule("rule9000")
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();

            resource.refresh();

            // Get the resource created so far
            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.size().equals(ApplicationGatewaySkuName.STANDARD_SMALL));
            Assert.assertTrue(resource.instanceCount() == 1);

            // Verify listeners
            ApplicationGatewayListener listener = resource.listeners().get("listener1");
            Assert.assertTrue("www.contoso.com".equalsIgnoreCase(listener.hostName()));

            // Verify request routing rules
            Assert.assertTrue(resource.requestRoutingRules().size() == rulesCount - 1);
            Assert.assertTrue(!resource.requestRoutingRules().containsKey("rule9000"));
            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule443");
            Assert.assertTrue(rule != null);
            Assert.assertTrue("listener1".equalsIgnoreCase(rule.listener().name()));

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
                    try {
                        resources.define(TestApplicationGateway.APP_GATEWAY_NAME)
                            .withRegion(REGION)
                            .withNewResourceGroup(GROUP_NAME)

                            // Request routing rules
                            .defineRequestRoutingRule("rule1")
                                .fromPublicFrontend()
                                .fromFrontendHttpsPort(443)
                                .withSslCertificateFromPfxFile(new File("myTest.pfx"))
                                .withSslCertificatePassword("Abc123")
                                .toBackendHttpPort(8080)
                                .toBackendIpAddress("11.1.1.1")
                                .toBackendIpAddress("11.1.1.2")
                                .attach()

                            .create();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
            Assert.assertTrue(ApplicationGatewayTier.STANDARD.equals(appGateway.tier()));
            Assert.assertTrue(ApplicationGatewaySkuName.STANDARD_SMALL.equals(appGateway.size()));
            Assert.assertTrue(appGateway.instanceCount() == 1);

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
            Assert.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(rule.frontendProtocol()));
            Assert.assertTrue(rule.listener() != null);
            Assert.assertTrue(rule.listener().frontend() != null);
            Assert.assertTrue(rule.listener().frontend().isPublic());
            Assert.assertTrue(!rule.listener().frontend().isPrivate());
            Assert.assertTrue(rule.backendPort() == 8080);
            Assert.assertTrue(rule.sslCertificate() != null);
            Assert.assertTrue(rule.backendAddresses().size() == 2);
            Assert.assertTrue(rule.backend().containsIpAddress("11.1.1.1"));
            Assert.assertTrue(rule.backend().containsIpAddress("11.1.1.2"));

            // Verify certificates
            Assert.assertTrue(appGateway.sslCertificates().size() == 1);

            creationThread.join();
            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            resource.update()
                .withInstanceCount(2)
                .withSize(ApplicationGatewaySkuName.STANDARD_MEDIUM)
                .withoutBackendIpAddress("11.1.1.1")
                .defineListener("listener2")
                    .withPublicFrontend()
                    .withFrontendPort(80)
                    .attach()
                .defineBackend("backend2")
                    .withIpAddress("11.1.1.3")
                    .attach()
                .defineBackendHttpConfiguration("config2")
                    .withCookieBasedAffinity()
                    .withPort(8081)
                    .withRequestTimeout(33)
                    .attach()
                .defineRequestRoutingRule("rule2")
                    .fromListener("listener2")
                    .toBackendHttpConfiguration("config2")
                    .toBackend("backend2")
                    .attach()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();

            resource.refresh();

            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.tags().containsKey("tag2"));
            Assert.assertTrue(ApplicationGatewaySkuName.STANDARD_MEDIUM.equals(resource.size()));
            Assert.assertTrue(resource.instanceCount() == 2);

            // Verify frontend ports
            Assert.assertTrue(resource.frontendPorts().size() == 2);
            Assert.assertTrue(resource.frontendPortNameFromNumber(80) != null);

            // Verify listeners
            Assert.assertTrue(resource.listeners().size() == 2);
            ApplicationGatewayListener listener = resource.listeners().get("listener2");
            Assert.assertTrue(listener != null);
            Assert.assertTrue(!listener.frontend().isPrivate());
            Assert.assertTrue(listener.frontend().isPublic());
            Assert.assertTrue(listener.frontendPortNumber() == 80);
            Assert.assertTrue(ApplicationGatewayProtocol.HTTP.equals(listener.protocol()));
            Assert.assertTrue(listener.sslCertificate() == null);

            // Verify backends
            Assert.assertTrue(resource.backends().size() == 2);
            ApplicationGatewayBackend backend = resource.backends().get("backend2");
            Assert.assertTrue(backend != null);
            Assert.assertTrue(backend.addresses().size() == 1);
            Assert.assertTrue(backend.containsIpAddress("11.1.1.3"));

            // Verify HTTP configs
            Assert.assertTrue(resource.backendHttpConfigurations().size() == 2);
            ApplicationGatewayBackendHttpConfiguration config = resource.backendHttpConfigurations().get("config2");
            Assert.assertTrue(config != null);
            Assert.assertTrue(config.cookieBasedAffinity());
            Assert.assertTrue(config.port() == 8081);
            Assert.assertTrue(config.requestTimeout() == 33);

            // Verify request routing rules
            Assert.assertTrue(resource.requestRoutingRules().size() == 2);
            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule2");
            Assert.assertTrue(rule != null);
            Assert.assertTrue(rule.listener() != null);
            Assert.assertTrue("listener2".equals(rule.listener().name()));
            Assert.assertTrue(rule.backendHttpConfiguration() != null);
            Assert.assertTrue("config2".equalsIgnoreCase(rule.backendHttpConfiguration().name()));
            Assert.assertTrue(rule.backend() != null);
            Assert.assertTrue("backend2".equalsIgnoreCase(rule.backend().name()));

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
                .append("\n\t\t\tPort: ").append(httpConfig.port())
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
