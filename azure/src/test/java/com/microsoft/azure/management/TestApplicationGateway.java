/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;

import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayAuthenticationCertificate;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendAddress;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import com.microsoft.azure.management.network.ApplicationGatewayOperationalState;
import com.microsoft.azure.management.network.ApplicationGatewayProbe;
import com.microsoft.azure.management.network.ApplicationGatewayIPConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewaySkuName;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import com.microsoft.azure.management.network.ApplicationGatewaySslProtocol;
import com.microsoft.azure.management.network.ApplicationGatewayTier;
import com.microsoft.azure.management.network.ApplicationGateways;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * Test of application gateway management.
 */
public class TestApplicationGateway {
    static String TEST_ID = "";
    static final Region REGION = Region.US_WEST;
    static String GROUP_NAME = "";
    static String APP_GATEWAY_NAME = "";
    static String[] PIP_NAMES = null;
    static final String ID_TEMPLATE = "/subscriptions/${subId}/resourceGroups/${rgName}/providers/Microsoft.Network/applicationGateways/${resourceName}";

    static String createResourceId(String subscriptionId) {
        return ID_TEMPLATE
                .replace("${subId}", subscriptionId)
                .replace("${rgName}", GROUP_NAME)
                .replace("${resourceName}", APP_GATEWAY_NAME);
    }

    static void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        GROUP_NAME = "rg" + TEST_ID;
        APP_GATEWAY_NAME = "ag" + TEST_ID;
        PIP_NAMES = new String[]{"pipa" + TEST_ID, "pipb" + TEST_ID};
    }
    /**
     * Minimalistic internal (private) app gateway test.
     */
    public static class PrivateMinimal extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        PrivateMinimal() {
            initializeResourceNames();
        }

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
                            .toBackendIPAddress("11.1.1.1")
                            .toBackendIPAddress("11.1.1.2")
                            .attach()
                        .create();
                }
            });

            // Start the creation...
            creationThread.start();

            //...But bail out after 30 sec, as it is enough to test the results
            SdkContext.sleep(30 * 1000);

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
            Assert.assertTrue(rule.backend().containsIPAddress("11.1.1.1"));
            Assert.assertTrue(rule.backend().containsIPAddress("11.1.1.2"));
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
                .withoutBackendIPAddress("11.1.1.1")    // Remove from all existing backends
                .defineListener("listener2")
                    .withPrivateFrontend()
                    .withFrontendPort(81)
                    .withHttps()
                    .withSslCertificateFromPfxFile(new File(getClass().getClassLoader().getResource("myTest.pfx").getFile()))
                    .withSslCertificatePassword("Abc123")
                    .attach()
                .defineBackend("backend2")
                    .withIPAddress("11.1.1.3")
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
            Assert.assertTrue(backend.containsIPAddress("11.1.1.3"));

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

        /**
         * Tests minimal internal app gateways.
         * @throws Exception when something goes wrong
         */
        public PrivateComplex() throws Exception {
            initializeResourceNames();
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            ensurePIPs(resources.manager().publicIPAddresses());

            final Network vnet = resources.manager().networks().define("net" + TEST_ID)
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
                                .toBackendIPAddress("11.1.1.1")
                                .toBackendIPAddress("11.1.1.2")
                                .withCookieBasedAffinity()
                                .attach()
                            .defineRequestRoutingRule("rule443")
                                .fromPrivateFrontend()
                                .fromFrontendHttpsPort(443)
                                .withSslCertificateFromPfxFile(new File(getClass().getClassLoader().getResource("myTest.pfx").getFile()))
                                .withSslCertificatePassword("Abc123")
                                .toBackendHttpConfiguration("config1")
                                .toBackend("backend1")
                                .attach()
                            .defineRequestRoutingRule("rule9000")
                                .fromListener("listener1")
                                .toBackendHttpConfiguration("config1")
                                .toBackend("backend1")
                                .attach()

                            // Additional/explicit backends
                            .defineBackend("backend1")
                                .withIPAddress("11.1.1.3")
                                .withIPAddress("11.1.1.4")
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
                                .withPfxFromFile(new File(getClass().getClassLoader().getResource("myTest2.pfx").getFile()))
                                .withPfxPassword("Abc123")
                                .attach()

                            // Authentication certificates
                            .defineAuthenticationCertificate("auth2")
                                .fromFile(new File(getClass().getClassLoader().getResource("myTest2.cer").getFile()))
                                .attach()

                            // Additional/explicit backend HTTP setting configs
                            .defineBackendHttpConfiguration("config1")
                                .withPort(8081)
                                .withRequestTimeout(45)
                                .withHttps()
                                .withAuthenticationCertificateFromFile(new File(getClass().getClassLoader().getResource("myTest.cer").getFile()))
                                .attach()

                            .defineBackendHttpConfiguration("config2")
                                .withPort(8082)
                                .withHttps()
                                .withAuthenticationCertificate("auth2")
                                // Add the same cert, so only one should be added
                                .withAuthenticationCertificateFromFile(new File(getClass().getClassLoader().getResource("myTest2.cer").getFile()))
                                .attach()

                            .withExistingSubnet(vnet, "subnet1")
                            .withSize(ApplicationGatewaySkuName.STANDARD_MEDIUM)
                            .withInstanceCount(2)
                            .create();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    }
                });

            // Start creating in a separate thread...
            creationThread.setUncaughtExceptionHandler(threadException);
            creationThread.start();

            // ...But don't wait till the end - not needed for the test, 30 sec should be enough
            SdkContext.sleep(30 * 1000);

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

            // Verify SSL certificates
            Assert.assertEquals(2, appGateway.sslCertificates().size());
            Assert.assertTrue(appGateway.sslCertificates().containsKey("cert1"));

            // Verify backend HTTP settings configs
            Assert.assertTrue(appGateway.backendHttpConfigurations().size() == 3);
            ApplicationGatewayBackendHttpConfiguration config = appGateway.backendHttpConfigurations().get("config1");
            Assert.assertTrue(config != null);
            Assert.assertTrue(config.port() == 8081);
            Assert.assertTrue(config.requestTimeout() == 45);
            Assert.assertEquals(1, config.authenticationCertificates().size());

            ApplicationGatewayBackendHttpConfiguration config2 = appGateway.backendHttpConfigurations().get("config2");
            Assert.assertNotNull(config2);

            // Verify authentication certificates
            Assert.assertEquals(2, appGateway.authenticationCertificates().size());
            ApplicationGatewayAuthenticationCertificate authCert2 = appGateway.authenticationCertificates().get("auth2");
            Assert.assertNotNull(authCert2);
            Assert.assertNotNull(authCert2.data());

            ApplicationGatewayAuthenticationCertificate authCert = config.authenticationCertificates().values().iterator().next();
            Assert.assertNotNull(authCert);

            Assert.assertEquals(1, config2.authenticationCertificates().size());
            Assert.assertEquals(authCert2.name(), config2.authenticationCertificates().values().iterator().next().name());

            // Verify backends
            Assert.assertTrue(appGateway.backends().size() == 3);
            ApplicationGatewayBackend backend = appGateway.backends().get("backend1");
            Assert.assertTrue(backend != null);
            Assert.assertTrue(backend.addresses().size() == 2);
            Assert.assertTrue(backend.containsIPAddress("11.1.1.3"));
            Assert.assertTrue(backend.containsIPAddress("11.1.1.4"));
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
            Assert.assertTrue(rule.backend().containsIPAddress("11.1.1.1"));
            Assert.assertTrue(rule.backend().containsIPAddress("11.1.1.2"));

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
            final int sslCertCount = resource.sslCertificates().size();
            final int authCertCount = resource.authenticationCertificates().size();
            final ApplicationGatewayAuthenticationCertificate authCert1 = resource.backendHttpConfigurations().get("config1").authenticationCertificates().values().iterator().next();
            Assert.assertNotNull(authCert1);

            PublicIPAddress pip = resource.manager().publicIPAddresses().getByResourceGroup(GROUP_NAME, PIP_NAMES[0]);

            resource.update()
                .withSize(ApplicationGatewaySkuName.STANDARD_SMALL)
                .withInstanceCount(1)
                .withoutFrontendPort(9000)
                .withoutListener("listener1")
                .withoutBackendIPAddress("11.1.1.4")
                .withoutBackendHttpConfiguration("config2")
                .withoutBackend("backend2")
                .withoutRequestRoutingRule("rule9000")
                .withoutSslCertificate("cert1")
                .withoutAuthenticationCertificate(authCert1.name())
                .updateListener(resource.requestRoutingRules().get("rule443").listener().name())
                    .withHostName("foobar")
                    .parent()
                .updateBackendHttpConfiguration("config1")
                    .withPort(8082)
                    .withCookieBasedAffinity()
                    .withRequestTimeout(20)
                    .withAuthenticationCertificate("auth2")
                    .parent()
                .updateBackend("backend1")
                    .withoutIPAddress("11.1.1.3")
                    .withIPAddress("11.1.1.5")
                    .parent()
                .updateRequestRoutingRule("rule80")
                    .toBackend("backend1")
                    .toBackendHttpConfiguration("config1")
                    .parent()
                .withExistingPublicIPAddress(pip) // Associate with a public IP as well
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();

            resource.refresh();

            // Get the resource created so far
            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertTrue(resource.tags().containsKey("tag2"));
            Assert.assertEquals(ApplicationGatewaySkuName.STANDARD_SMALL, resource.size());
            Assert.assertEquals(1, resource.instanceCount());

            // Verify frontend ports
            Assert.assertEquals(portCount - 1, resource.frontendPorts().size());
            Assert.assertNull(resource.frontendPortNameFromNumber(9000));

            // Verify frontends
            Assert.assertEquals(frontendCount + 1, resource.frontends().size());
            Assert.assertEquals(1, resource.publicFrontends().size());
            Assert.assertTrue(resource.publicFrontends().values().iterator().next().publicIPAddressId().equalsIgnoreCase(pip.id()));
            Assert.assertEquals(1, resource.privateFrontends().size());
            ApplicationGatewayFrontend frontend = resource.privateFrontends().values().iterator().next();
            Assert.assertFalse(frontend.isPublic());
            Assert.assertTrue(frontend.isPrivate());

            // Verify listeners
            Assert.assertEquals(listenerCount - 1, resource.listeners().size());
            Assert.assertFalse(resource.listeners().containsKey("listener1"));

            // Verify backends
            Assert.assertEquals(backendCount - 1, resource.backends().size());
            Assert.assertFalse(resource.backends().containsKey("backend2"));
            ApplicationGatewayBackend backend = resource.backends().get("backend1");
            Assert.assertNotNull(backend);
            Assert.assertEquals(1, backend.addresses().size());
            Assert.assertTrue(backend.containsIPAddress("11.1.1.5"));
            Assert.assertFalse(backend.containsIPAddress("11.1.1.3"));
            Assert.assertFalse(backend.containsIPAddress("11.1.1.4"));

            // Verify HTTP configs
            Assert.assertEquals(configCount - 1, resource.backendHttpConfigurations().size());
            Assert.assertFalse(resource.backendHttpConfigurations().containsKey("config2"));
            ApplicationGatewayBackendHttpConfiguration config = resource.backendHttpConfigurations().get("config1");
            Assert.assertEquals(8082, config.port());
            Assert.assertEquals(20, config.requestTimeout());
            Assert.assertTrue(config.cookieBasedAffinity());
            Assert.assertEquals(1, config.authenticationCertificates().size());
            Assert.assertFalse(config.authenticationCertificates().containsKey(authCert1.name()));
            Assert.assertTrue(config.authenticationCertificates().containsKey("auth2"));

            // Verify rules
            Assert.assertEquals(ruleCount - 1, resource.requestRoutingRules().size());
            Assert.assertFalse(resource.requestRoutingRules().containsKey("rule9000"));

            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule80");
            Assert.assertNotNull(rule);
            Assert.assertNotNull(rule.backend());
            Assert.assertTrue("backend1".equalsIgnoreCase(rule.backend().name()));
            Assert.assertNotNull(rule.backendHttpConfiguration());
            Assert.assertTrue("config1".equalsIgnoreCase(rule.backendHttpConfiguration().name()));

            rule = resource.requestRoutingRules().get("rule443");
            Assert.assertNotNull(rule);
            Assert.assertNotNull(rule.listener());
            Assert.assertTrue("foobar".equalsIgnoreCase(rule.listener().hostName()));

            // Verify SSL certificates
            Assert.assertEquals(sslCertCount - 1, resource.sslCertificates().size());
            Assert.assertFalse(resource.sslCertificates().containsKey("cert1"));

            // Verify authentication certificates
            Assert.assertEquals(authCertCount - 1, resource.authenticationCertificates().size());
            Assert.assertFalse(resource.authenticationCertificates().containsKey("auth1"));

            // Test stop/start
            resource.stop();
            Assert.assertEquals(ApplicationGatewayOperationalState.STOPPED, resource.operationalState());
            resource.start();
            Assert.assertEquals(ApplicationGatewayOperationalState.RUNNING, resource.operationalState());

            return resource;
        }
    }

    /**
     * Complex Internet-facing (public) app gateway test.
     */
    public static class PublicComplex extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        /**
         * Tests minimal internal app gateways.
         * @throws Exception when something goes wrong with test PIP creation
         */
        public PublicComplex() throws Exception {
            initializeResourceNames();
        }

        @Override
        public void print(ApplicationGateway resource) {
            TestApplicationGateway.printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            ensurePIPs(resources.manager().publicIPAddresses());
            Thread.UncaughtExceptionHandler threadException = new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread th, Throwable ex) {
                    System.out.println("Uncaught exception: " + ex);
                }
            };

            final PublicIPAddress pip = resources.manager().publicIPAddresses().getByResourceGroup(GROUP_NAME, PIP_NAMES[0]);

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
                                .toBackendIPAddress("11.1.1.1")
                                .toBackendIPAddress("11.1.1.2")
                                .withCookieBasedAffinity()
                                .attach()
                            .defineRequestRoutingRule("rule443")
                                .fromPublicFrontend()
                                .fromFrontendHttpsPort(443)
                                .withSslCertificateFromPfxFile(new File(getClass().getClassLoader().getResource("myTest.pfx").getFile()))
                                .withSslCertificatePassword("Abc123")
                                .toBackendHttpConfiguration("config1")
                                .toBackend("backend1")
                                .attach()
                            .defineRequestRoutingRule("rule9000")
                                .fromListener("listener1")
                                .toBackendHttpConfiguration("config1")
                                .toBackend("backend1")
                                .attach()

                            // Additional/explicit frontend listeners
                            .defineListener("listener1")
                                .withPublicFrontend()
                                .withFrontendPort(9000)
                                .withHttps()
                                .withSslCertificateFromPfxFile(new File(getClass().getClassLoader().getResource("myTest2.pfx").getFile()))
                                .withSslCertificatePassword("Abc123")
                                .withServerNameIndication()
                                .withHostName("www.fabricam.com")
                                .attach()

                            // Additional/explicit backends
                            .defineBackend("backend1")
                                .withIPAddress("11.1.1.1")
                                .withIPAddress("11.1.1.2")
                                .attach()

                            .withExistingPublicIPAddress(pip)
                            .withSize(ApplicationGatewaySkuName.STANDARD_MEDIUM)
                            .withInstanceCount(2)

                            // Probes
                            .defineProbe("probe1")
                                .withHost("microsoft.com")
                                .withPath("/")
                                .withHttp()
                                .withTimeoutInSeconds(10)
                                .withTimeBetweenProbesInSeconds(9)
                                .withRetriesBeforeUnhealthy(5)
                                .withHealthyHttpResponseStatusCodeRange(200, 249)
                                .attach()
                            .defineProbe("probe2")
                                .withHost("microsoft.com")
                                .withPath("/")
                                .withHttps()
                                .withTimeoutInSeconds(11)
                                .withHealthyHttpResponseStatusCodeRange(600, 610)
                                .withHealthyHttpResponseStatusCodeRange(650, 660)
                                .withHealthyHttpResponseBodyContents("I am too healthy for this test.")
                                .attach()

                            // Additional/explicit backend HTTP setting configs
                            .defineBackendHttpConfiguration("config1")
                                .withPort(8081)
                                .withRequestTimeout(45)
                                .withProbe("probe1")
                                .withHostHeader("foo")
                                .withConnectionDrainingTimeoutInSeconds(100)
                                .withPath("path")
                                .withAffinityCookieName("cookie")
                                .attach()

                            .withDisabledSslProtocols(ApplicationGatewaySslProtocol.TLSV1_0, ApplicationGatewaySslProtocol.TLSV1_1)
                            .create();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Start creating in a separate thread...
            creationThread.setUncaughtExceptionHandler(threadException);
            creationThread.start();

            // ...But don't wait till the end - not needed for the test, 30 sec should be enough
            SdkContext.sleep(30 * 1000);

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.getById(resourceId);
            Assert.assertNotNull(appGateway);
            Assert.assertTrue(appGateway.isPublic());
            Assert.assertTrue(!appGateway.isPrivate());
            Assert.assertEquals(ApplicationGatewayTier.STANDARD, appGateway.tier());
            Assert.assertEquals(ApplicationGatewaySkuName.STANDARD_MEDIUM, appGateway.size());
            Assert.assertEquals(2, appGateway.instanceCount());
            Assert.assertEquals(1, appGateway.ipConfigurations().size());

            // Verify frontend ports
            Assert.assertEquals(3, appGateway.frontendPorts().size());
            Assert.assertNotNull(appGateway.frontendPortNameFromNumber(80));
            Assert.assertNotNull(appGateway.frontendPortNameFromNumber(443));
            Assert.assertNotNull(appGateway.frontendPortNameFromNumber(9000));

            // Verify frontends
            Assert.assertEquals(1, appGateway.frontends().size());
            Assert.assertEquals(1, appGateway.publicFrontends().size());
            Assert.assertEquals(0, appGateway.privateFrontends().size());
            ApplicationGatewayFrontend frontend = appGateway.publicFrontends().values().iterator().next();
            Assert.assertTrue(frontend.isPublic());
            Assert.assertTrue(!frontend.isPrivate());

            // Verify listeners
            Assert.assertEquals(3, appGateway.listeners().size());
            ApplicationGatewayListener listener = appGateway.listeners().get("listener1");
            Assert.assertNotNull(listener);
            Assert.assertEquals(9000, listener.frontendPortNumber());
            Assert.assertTrue("www.fabricam.com".equalsIgnoreCase(listener.hostName()));
            Assert.assertTrue(listener.requiresServerNameIndication());
            Assert.assertNotNull(listener.frontend());
            Assert.assertTrue(!listener.frontend().isPrivate());
            Assert.assertTrue(listener.frontend().isPublic());
            Assert.assertEquals(ApplicationGatewayProtocol.HTTPS, listener.protocol());
            Assert.assertNotNull(appGateway.listenerByPortNumber(80));
            Assert.assertNotNull(appGateway.listenerByPortNumber(443));

            // Verify certificates
            Assert.assertEquals(2, appGateway.sslCertificates().size());

            // Verify backends
            Assert.assertEquals(2, appGateway.backends().size());
            ApplicationGatewayBackend backend = appGateway.backends().get("backend1");
            Assert.assertNotNull(backend);
            Assert.assertEquals(2, backend.addresses().size());

            // Verify request routing rules
            Assert.assertEquals(3, appGateway.requestRoutingRules().size());
            ApplicationGatewayRequestRoutingRule rule, rule80;

            rule80 = appGateway.requestRoutingRules().get("rule80");
            Assert.assertNotNull(rule80);
            Assert.assertTrue(pip.id().equalsIgnoreCase(rule80.publicIPAddressId()));
            Assert.assertEquals(80, rule80.frontendPort());
            Assert.assertEquals(8080, rule80.backendPort());
            Assert.assertTrue(rule80.cookieBasedAffinity());
            Assert.assertEquals(4, rule80.backendAddresses().size());
            Assert.assertTrue(rule80.backend().containsIPAddress("11.1.1.2"));
            Assert.assertTrue(rule80.backend().containsIPAddress("11.1.1.1"));
            Assert.assertTrue(rule80.backend().containsFqdn("www.microsoft.com"));
            Assert.assertTrue(rule80.backend().containsFqdn("www.example.com"));

            rule = appGateway.requestRoutingRules().get("rule443");
            Assert.assertNotNull(rule);
            Assert.assertTrue(pip.id().equalsIgnoreCase(rule.publicIPAddressId()));
            Assert.assertEquals(443, rule.frontendPort());
            Assert.assertEquals(ApplicationGatewayProtocol.HTTPS, rule.frontendProtocol());
            Assert.assertNotNull(rule.sslCertificate());
            Assert.assertNotNull(rule.backendHttpConfiguration());
            Assert.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assert.assertNotNull(rule.backend());
            Assert.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

            rule = appGateway.requestRoutingRules().get("rule9000");
            Assert.assertNotNull(rule);
            Assert.assertNotNull(rule.listener());
            Assert.assertTrue(rule.listener().name().equalsIgnoreCase("listener1"));
            Assert.assertNotNull(rule.backendHttpConfiguration());
            Assert.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assert.assertNotNull(rule.backend());
            Assert.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

            // Verify backend HTTP settings configs
            Assert.assertEquals(2, appGateway.backendHttpConfigurations().size());
            ApplicationGatewayBackendHttpConfiguration config = appGateway.backendHttpConfigurations().get("config1");
            Assert.assertNotNull(config);
            Assert.assertEquals(8081, config.port());
            Assert.assertEquals(45, config.requestTimeout());
            Assert.assertNotNull(config.probe());
            Assert.assertEquals("probe1", config.probe().name());
            Assert.assertFalse(config.isHostHeaderFromBackend());
            Assert.assertEquals("foo", config.hostHeader());
            Assert.assertEquals(100, config.connectionDrainingTimeoutInSeconds());
            Assert.assertEquals("/path/", config.path());
            Assert.assertEquals("cookie", config.affinityCookieName());

            // Verify probes
            Assert.assertEquals(2, appGateway.probes().size());
            ApplicationGatewayProbe probe;
            probe = appGateway.probes().get("probe1");
            Assert.assertNotNull(probe);
            Assert.assertEquals("microsoft.com", probe.host().toLowerCase());
            Assert.assertEquals(ApplicationGatewayProtocol.HTTP, probe.protocol());
            Assert.assertEquals("/", probe.path());
            Assert.assertEquals(5,  probe.retriesBeforeUnhealthy());
            Assert.assertEquals(9, probe.timeBetweenProbesInSeconds());
            Assert.assertEquals(10, probe.timeoutInSeconds());
            Assert.assertNotNull(probe.healthyHttpResponseStatusCodeRanges());
            Assert.assertEquals(1, probe.healthyHttpResponseStatusCodeRanges().size());
            Assert.assertTrue(probe.healthyHttpResponseStatusCodeRanges().contains("200-249"));

            probe = appGateway.probes().get("probe2");
            Assert.assertNotNull(probe);
            Assert.assertEquals(ApplicationGatewayProtocol.HTTPS, probe.protocol());
            Assert.assertEquals(2, probe.healthyHttpResponseStatusCodeRanges().size());
            Assert.assertTrue(probe.healthyHttpResponseStatusCodeRanges().contains("600-610"));
            Assert.assertTrue(probe.healthyHttpResponseStatusCodeRanges().contains("650-660"));
            Assert.assertEquals("I am too healthy for this test.", probe.healthyHttpResponseBodyContents());

            creationThread.join();

            // Verify SSL policy - disabled protocols
            Assert.assertEquals(2, appGateway.disabledSslProtocols().size());
            Assert.assertTrue(appGateway.disabledSslProtocols().contains(ApplicationGatewaySslProtocol.TLSV1_0));
            Assert.assertTrue(appGateway.disabledSslProtocols().contains(ApplicationGatewaySslProtocol.TLSV1_1));
            Assert.assertTrue(!appGateway.disabledSslProtocols().contains(ApplicationGatewaySslProtocol.TLSV1_2));

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            final int rulesCount = resource.requestRoutingRules().size();
            ApplicationGatewayRequestRoutingRule rule80 = resource.requestRoutingRules().get("rule80");
            Assert.assertNotNull(rule80);
            ApplicationGatewayBackendHttpConfiguration backendConfig80 = rule80.backendHttpConfiguration();
            Assert.assertNotNull(backendConfig80);

            resource.update()
                .withSize(ApplicationGatewaySkuName.STANDARD_SMALL)
                .withInstanceCount(1)
                .updateListener("listener1")
                    .withHostName("www.contoso.com")
                    .parent()
                .updateRequestRoutingRule("rule443")
                    .fromListener("listener1")
                    .parent()
                .updateBackendHttpConfiguration("config1")
                    .withoutHostHeader()
                    .withoutConnectionDraining()
                    .withAffinityCookieName(null)
                    .withPath(null)
                    .parent()
                .updateBackendHttpConfiguration(backendConfig80.name())
                    .withHostHeaderFromBackend()
                    .parent()
                .withoutRequestRoutingRule("rule9000")
                .withoutProbe("probe1")
                .updateProbe("probe2")
                    .withoutHealthyHttpResponseStatusCodeRanges()
                    .withHealthyHttpResponseBodyContents(null)
                    .parent()
                .withoutDisabledSslProtocols(ApplicationGatewaySslProtocol.TLSV1_0, ApplicationGatewaySslProtocol.TLSV1_1)
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

            // Verify probes
            Assert.assertEquals(1, resource.probes().size());
            ApplicationGatewayProbe probe = resource.probes().get("probe2");
            Assert.assertNotNull(probe);
            Assert.assertTrue(probe.healthyHttpResponseStatusCodeRanges().isEmpty());
            Assert.assertNull(probe.healthyHttpResponseBodyContents());

            // Verify backend configs
            ApplicationGatewayBackendHttpConfiguration backendConfig = resource.backendHttpConfigurations().get("config1");
            Assert.assertNotNull(backendConfig);
            Assert.assertNull(backendConfig.probe());
            Assert.assertFalse(backendConfig.isHostHeaderFromBackend());
            Assert.assertNull(backendConfig.hostHeader());
            Assert.assertEquals(0, backendConfig.connectionDrainingTimeoutInSeconds());
            Assert.assertNull(backendConfig.affinityCookieName());
            Assert.assertNull(backendConfig.path());

            rule80 = resource.requestRoutingRules().get("rule80");
            Assert.assertNotNull(rule80);
            backendConfig80 = rule80.backendHttpConfiguration();
            Assert.assertNotNull(backendConfig80);
            Assert.assertTrue(backendConfig80.isHostHeaderFromBackend());
            Assert.assertNull(backendConfig80.hostHeader());

            // Verify SSL policy - disabled protocols
            Assert.assertEquals(0, resource.disabledSslProtocols().size());
            return resource;
        }
    }

    /**
     * Internet-facing LB test with NAT pool test.
     */
    public static class PublicMinimal extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        PublicMinimal() {
            initializeResourceNames();
        }

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
                                .withSslCertificateFromPfxFile(new File(getClass().getClassLoader().getResource("myTest.pfx").getFile()))
                                .withSslCertificatePassword("Abc123")
                                .toBackendHttpPort(8080)
                                .toBackendIPAddress("11.1.1.1")
                                .toBackendIPAddress("11.1.1.2")
                                .attach()

                            .create();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Start the creation...
            creationThread.start();

            //...But bail out after 30 sec, as it is enough to test the results
            SdkContext.sleep(30 * 1000);

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
            Assert.assertTrue(rule.backend().containsIPAddress("11.1.1.1"));
            Assert.assertTrue(rule.backend().containsIPAddress("11.1.1.2"));

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
                .withoutBackendIPAddress("11.1.1.1")
                .defineListener("listener2")
                    .withPublicFrontend()
                    .withFrontendPort(80)
                    .attach()
                .defineBackend("backend2")
                    .withIPAddress("11.1.1.3")
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
            Assert.assertTrue(backend.containsIPAddress("11.1.1.3"));

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
    private static Map<String, PublicIPAddress> ensurePIPs(PublicIPAddresses pips) throws Exception {
        List<Creatable<PublicIPAddress>> creatablePips = new ArrayList<>();
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
                .append("\n\tInternet-facing? ").append(resource.isPublic())
                .append("\n\tInternal? ").append(resource.isPrivate())
                .append("\n\tDefault private IP address: ").append(resource.privateIPAddress())
                .append("\n\tPrivate IP address allocation method: ").append(resource.privateIPAllocationMethod())
                .append("\n\tDisabled SSL protocols: ").append(resource.disabledSslProtocols().toString());

        // Show IP configs
        Map<String, ApplicationGatewayIPConfiguration> ipConfigs = resource.ipConfigurations();
        info.append("\n\tIP configurations: ").append(ipConfigs.size());
        for (ApplicationGatewayIPConfiguration ipConfig : ipConfigs.values()) {
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
                info.append("\n\t\t\tPublic IP address ID: ").append(frontend.publicIPAddressId());
            }

            if (frontend.isPrivate()) {
                // Show private frontend info
                info.append("\n\t\t\tPrivate IP address: ").append(frontend.privateIPAddress())
                    .append("\n\t\t\tPrivate IP allocation method: ").append(frontend.privateIPAllocationMethod())
                    .append("\n\t\t\tSubnet name: ").append(frontend.subnetName())
                    .append("\n\t\t\tVirtual network ID: ").append(frontend.networkId());
            }
        }

        // Show backends
        Map<String, ApplicationGatewayBackend> backends = resource.backends();
        info.append("\n\tBackends: ").append(backends.size());
        for (ApplicationGatewayBackend backend : backends.values()) {
            info.append("\n\t\tName: ").append(backend.name())
                .append("\n\t\t\tAssociated NIC IP configuration IDs: ").append(backend.backendNicIPConfigurationNames().keySet());

            // Show addresses
            Collection<ApplicationGatewayBackendAddress> addresses = backend.addresses();
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
                .append("\n\t\t\tProtocol: ").append(httpConfig.protocol())
                .append("\n\t\tHost header: ").append(httpConfig.hostHeader())
                .append("\n\t\tHost header comes from backend? ").append(httpConfig.isHostHeaderFromBackend())
                .append("\n\t\tConnection draining timeout in seconds: ").append(httpConfig.connectionDrainingTimeoutInSeconds())
                .append("\n\t\tAffinity cookie name: ").append(httpConfig.affinityCookieName())
                .append("\n\t\tPath: ").append(httpConfig.path());

            if (httpConfig.probe() != null) {
                info.append("\n\t\t\tProbe: " + httpConfig.probe().name());
            }
            info.append("\n\t\tIs probe enabled? ").append(httpConfig.isProbeEnabled());
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

        // Show probes
        Map<String, ApplicationGatewayProbe> probes = resource.probes();
        info.append("\n\tProbes: ").append(probes.size());
        for (ApplicationGatewayProbe probe : probes.values()) {
            info.append("\n\t\tName: ").append(probe.name())
                .append("\n\t\tProtocol:").append(probe.protocol().toString())
                .append("\n\t\tInterval in seconds: ").append(probe.timeBetweenProbesInSeconds())
                .append("\n\t\tRetries: ").append(probe.retriesBeforeUnhealthy())
                .append("\n\t\tTimeout: ").append(probe.timeoutInSeconds())
                .append("\n\t\tHost: ").append(probe.host())
                .append("\n\t\tHealthy HTTP response status code ranges: ").append(probe.healthyHttpResponseStatusCodeRanges())
                .append("\n\t\tHealthy HTTP response body contents: ").append(probe.healthyHttpResponseBodyContents());
        }

        // Show authentication certificates
        Map<String, ApplicationGatewayAuthenticationCertificate> certs = resource.authenticationCertificates();
        info.append("\n\tAuthentication certificates: ").append(certs.size());
        for (ApplicationGatewayAuthenticationCertificate cert : certs.values()) {
            info.append("\n\t\tName: ").append(cert.name())
                .append("\n\t\tBase-64 encoded data: ").append(cert.data());
        }

        // Show request routing rules
        Map<String, ApplicationGatewayRequestRoutingRule> rules = resource.requestRoutingRules();
        info.append("\n\tRequest routing rules: ").append(rules.size());
        for (ApplicationGatewayRequestRoutingRule rule : rules.values()) {
            info.append("\n\t\tName: ").append(rule.name())
                .append("\n\t\t\tType: ").append(rule.ruleType())
                .append("\n\t\t\tPublic IP address ID: ").append(rule.publicIPAddressId())
                .append("\n\t\t\tHost name: ").append(rule.hostName())
                .append("\n\t\t\tServer name indication required? ").append(rule.requiresServerNameIndication())
                .append("\n\t\t\tFrontend port: ").append(rule.frontendPort())
                .append("\n\t\t\tFrontend protocol: ").append(rule.frontendProtocol().toString())
                .append("\n\t\t\tBackend port: ").append(rule.backendPort())
                .append("\n\t\t\tCookie based affinity enabled? ").append(rule.cookieBasedAffinity());

            // Show backend addresses
            Collection<ApplicationGatewayBackendAddress> addresses = rule.backendAddresses();
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
