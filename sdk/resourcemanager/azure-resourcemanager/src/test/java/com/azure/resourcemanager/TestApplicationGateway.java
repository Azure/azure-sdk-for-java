// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayAuthenticationCertificate;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendAddress;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayFrontend;
import com.azure.resourcemanager.network.models.ApplicationGatewayIpConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayListener;
import com.azure.resourcemanager.network.models.ApplicationGatewayProbe;
import com.azure.resourcemanager.network.models.ApplicationGatewayProtocol;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectType;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRule;
import com.azure.resourcemanager.network.models.ApplicationGatewaySkuName;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslCertificate;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslProtocol;
import com.azure.resourcemanager.network.models.ApplicationGatewayTier;
import com.azure.resourcemanager.network.models.ApplicationGatewayUrlPathMap;
import com.azure.resourcemanager.network.models.ApplicationGateways;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIpAddresses;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Test of application gateway management. */
public class TestApplicationGateway {
    String testId = "";
    static final Region REGION = Region.US_WEST;
    String groupName = "";
    String appGatewayName = "";
    String[] pipNames = null;
    static final String ID_TEMPLATE =
        "/subscriptions/${subId}/resourceGroups/${rgName}/providers/Microsoft.Network/applicationGateways/${resourceName}";

    String createResourceId(String subscriptionId) {
        return ID_TEMPLATE
            .replace("${subId}", subscriptionId)
            .replace("${rgName}", groupName)
            .replace("${resourceName}", appGatewayName);
    }

    void initializeResourceNames(ResourceManagerUtils.InternalRuntimeContext internalContext) {
        testId = internalContext.randomResourceName("", 8);
        groupName = "rg" + testId;
        appGatewayName = "ag" + testId;
        pipNames = new String[] {"pipa" + testId, "pipb" + testId};
    }

    /** Minimalistic internal (private) app gateway test. */
    public class PrivateMinimal extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        PrivateMinimal(ResourceManagerUtils.InternalRuntimeContext internalContext) {
            initializeResourceNames(internalContext);
        }

        @Override
        public void print(ApplicationGateway resource) {
            printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            // Prepare a separate thread for resource creation
            Thread creationThread =
                new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // Create an application gateway
                            resources
                                .define(appGatewayName)
                                .withRegion(REGION)
                                .withNewResourceGroup(groupName)

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

            creationThread.join();

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.manager().applicationGateways().getById(resourceId);
            Assertions.assertTrue(appGateway != null);
            Assertions.assertTrue(ApplicationGatewayTier.STANDARD.equals(appGateway.tier()));
            Assertions.assertTrue(ApplicationGatewaySkuName.STANDARD_SMALL.equals(appGateway.size()));
            Assertions.assertTrue(appGateway.instanceCount() == 1);

            // Verify frontend ports
            Assertions.assertTrue(appGateway.frontendPorts().size() == 1);
            Assertions.assertTrue(appGateway.frontendPortNameFromNumber(80) != null);

            // Verify frontends
            Assertions.assertTrue(appGateway.isPrivate());
            Assertions.assertTrue(!appGateway.isPublic());
            Assertions.assertTrue(appGateway.frontends().size() == 1);

            // Verify listeners
            Assertions.assertTrue(appGateway.listeners().size() == 1);
            Assertions.assertTrue(appGateway.listenerByPortNumber(80) != null);

            // Verify backends
            Assertions.assertTrue(appGateway.backends().size() == 1);

            // Verify backend HTTP configs
            Assertions.assertTrue(appGateway.backendHttpConfigurations().size() == 1);

            // Verify rules
            Assertions.assertTrue(appGateway.requestRoutingRules().size() == 1);
            ApplicationGatewayRequestRoutingRule rule = appGateway.requestRoutingRules().get("rule1");
            Assertions.assertTrue(rule != null);
            Assertions.assertTrue(rule.frontendPort() == 80);
            Assertions.assertTrue(ApplicationGatewayProtocol.HTTP.equals(rule.frontendProtocol()));
            Assertions.assertTrue(rule.listener() != null);
            Assertions.assertTrue(rule.listener().frontend() != null);
            Assertions.assertTrue(!rule.listener().frontend().isPublic());
            Assertions.assertTrue(rule.listener().frontend().isPrivate());
            Assertions.assertTrue(rule.listener().subnetName() != null);
            Assertions.assertTrue(rule.listener().networkId() != null);
            Assertions.assertTrue(rule.backendAddresses().size() == 2);
            Assertions.assertTrue(rule.backend() != null);
            Assertions.assertTrue(rule.backend().containsIPAddress("11.1.1.1"));
            Assertions.assertTrue(rule.backend().containsIPAddress("11.1.1.2"));
            Assertions.assertTrue(rule.backendPort() == 8080);

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            resource
                .update()
                .withInstanceCount(2)
                .withSize(ApplicationGatewaySkuName.STANDARD_MEDIUM)
                .withFrontendPort(81, "port81") // Add a new port
                .withoutBackendIPAddress("11.1.1.1") // Remove from all existing backends
                .defineListener("listener2")
                .withPrivateFrontend()
                .withFrontendPort(81)
                .withHttps()
                .withSslCertificateFromPfxFile(
                    new File(getClass().getClassLoader().getResource("myTest.pfx").getFile()))
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

            Assertions.assertTrue(resource.tags().containsKey("tag1"));
            Assertions.assertTrue(resource.tags().containsKey("tag2"));
            Assertions.assertTrue(ApplicationGatewaySkuName.STANDARD_MEDIUM.equals(resource.size()));
            Assertions.assertTrue(resource.instanceCount() == 2);

            // Verify frontend ports
            Assertions.assertTrue(resource.frontendPorts().size() == 2);
            Assertions.assertTrue(resource.frontendPorts().containsKey("port81"));
            Assertions.assertTrue("port81".equalsIgnoreCase(resource.frontendPortNameFromNumber(81)));

            // Verify listeners
            Assertions.assertTrue(resource.listeners().size() == 2);
            ApplicationGatewayListener listener = resource.listeners().get("listener2");
            Assertions.assertTrue(listener != null);
            Assertions.assertTrue(listener.frontend().isPrivate());
            Assertions.assertTrue(!listener.frontend().isPublic());
            Assertions.assertTrue("port81".equalsIgnoreCase(listener.frontendPortName()));
            Assertions.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(listener.protocol()));
            Assertions.assertTrue(listener.sslCertificate() != null);

            // Verify backends
            Assertions.assertTrue(resource.backends().size() == 2);
            ApplicationGatewayBackend backend = resource.backends().get("backend2");
            Assertions.assertTrue(backend != null);
            Assertions.assertTrue(backend.addresses().size() == 1);
            Assertions.assertTrue(backend.containsIPAddress("11.1.1.3"));

            // Verify HTTP configs
            Assertions.assertTrue(resource.backendHttpConfigurations().size() == 2);
            ApplicationGatewayBackendHttpConfiguration config = resource.backendHttpConfigurations().get("config2");
            Assertions.assertTrue(config != null);
            Assertions.assertTrue(config.cookieBasedAffinity());
            Assertions.assertTrue(config.port() == 8081);
            Assertions.assertTrue(config.requestTimeout() == 33);

            // Verify request routing rules
            Assertions.assertTrue(resource.requestRoutingRules().size() == 2);
            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule2");
            Assertions.assertTrue(rule != null);
            Assertions.assertTrue(rule.listener() != null);
            Assertions.assertTrue("listener2".equals(rule.listener().name()));
            Assertions.assertTrue(rule.backendHttpConfiguration() != null);
            Assertions.assertTrue("config2".equalsIgnoreCase(rule.backendHttpConfiguration().name()));
            Assertions.assertTrue(rule.backend() != null);
            Assertions.assertTrue("backend2".equalsIgnoreCase(rule.backend().name()));

            resource.updateTags().withTag("tag3", "value3").withoutTag("tag1").applyTags();
            Assertions.assertEquals("value3", resource.tags().get("tag3"));
            Assertions.assertFalse(resource.tags().containsKey("tag1"));
            return resource;
        }
    }

    /** Minimalistic internal (private) app gateway test. */
    public class UrlPathBased extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        UrlPathBased(ResourceManagerUtils.InternalRuntimeContext internalContext) {
            initializeResourceNames(internalContext);
        }

        @Override
        public void print(ApplicationGateway resource) {
            printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            // Prepare a separate thread for resource creation
            Thread creationThread =
                new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // Create an application gateway
                            resources
                                .define(appGatewayName)
                                .withRegion(REGION)
                                .withNewResourceGroup(groupName)
                                .definePathBasedRoutingRule("pathMap")
                                .fromListener("myListener")
                                .toBackendHttpConfiguration("config1")
                                .toBackend("backendPool")
                                .definePathRule("pathRule")
                                .toBackendHttpConfiguration("config1")
                                .toBackend("backendPool")
                                .withPath("/images/*")
                                .attach()
                                .attach()
                                .defineListener("myListener")
                                .withPublicFrontend()
                                .withFrontendPort(80)
                                .attach()
                                .defineBackend("backendPool")
                                .attach()
                                .defineBackendHttpConfiguration("config1")
                                .withCookieBasedAffinity()
                                .withPort(8081)
                                .withRequestTimeout(33)
                                .attach()
                                .create();
                        }
                    });

            // Start the creation...
            creationThread.start();

            // ...But bail out after 30 sec, as it is enough to test the results
            creationThread.join();

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.manager().applicationGateways().getById(resourceId);
            Assertions.assertNotNull(appGateway);
            Assertions.assertEquals(ApplicationGatewayTier.STANDARD, appGateway.tier());
            Assertions.assertEquals(ApplicationGatewaySkuName.STANDARD_SMALL, appGateway.size());
            Assertions.assertEquals(1, appGateway.instanceCount());

            // Verify frontend ports
            Assertions.assertEquals(1, appGateway.frontendPorts().size());
            Assertions.assertNotNull(appGateway.frontendPortNameFromNumber(80));

            // Verify frontends
            Assertions.assertTrue(appGateway.isPublic());
            Assertions.assertEquals(1, appGateway.frontends().size());

            // Verify listeners
            Assertions.assertEquals(1, appGateway.listeners().size());
            Assertions.assertNotNull(appGateway.listenerByPortNumber(80));

            // Verify backends
            Assertions.assertEquals(1, appGateway.backends().size());

            // Verify backend HTTP configs
            Assertions.assertEquals(1, appGateway.backendHttpConfigurations().size());

            // Verify rules
            Assertions.assertEquals(1, appGateway.requestRoutingRules().size());
            ApplicationGatewayRequestRoutingRule rule = appGateway.requestRoutingRules().get("pathMap");
            Assertions.assertNotNull(rule);
            Assertions.assertEquals(80, rule.frontendPort());
            Assertions.assertEquals(ApplicationGatewayProtocol.HTTP, rule.frontendProtocol());
            Assertions.assertNotNull(rule.listener());
            Assertions.assertNotNull(rule.listener().frontend());
            Assertions.assertTrue(rule.listener().frontend().isPublic());
            Assertions.assertTrue(!rule.listener().frontend().isPrivate());
            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            resource
                .update()
                .withoutUrlPathMap("pathMap")
                // Request routing rules
                .definePathBasedRoutingRule("rule2")
                .fromListener("myListener")
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .toBackendIPAddress("11.1.1.2")
                .definePathRule("newRule")
                .toBackendHttpConfiguration("config1")
                .toBackend("backendPool")
                .withPath("/pictures/*")
                .attach()
                .definePathRule("newRule2")
                .toBackendHttpConfiguration("config1")
                .toBackend("backendPool2")
                .withPath("/video/*")
                .attach()
                .attach()
                .defineBackend("backendPool2")
                .attach()
                .apply();
            resource.refresh();
            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule2");
            Assertions.assertNotNull(rule);
            Assertions.assertEquals(0, rule.backendAddresses().size());
            ApplicationGatewayUrlPathMap pathMap = resource.urlPathMaps().get("rule2");
            Assertions.assertNotNull(pathMap.defaultBackend());
            Assertions.assertEquals(2, pathMap.defaultBackend().addresses().size());
            Assertions.assertTrue(pathMap.defaultBackend().containsIPAddress("11.1.1.1"));
            Assertions.assertTrue(pathMap.defaultBackend().containsIPAddress("11.1.1.2"));
            Assertions.assertEquals(8080, pathMap.defaultBackendHttpConfiguration().port());
            Assertions.assertEquals(1, resource.urlPathMaps().size());
            Assertions.assertFalse(resource.requestRoutingRules().containsKey("pathMap"));
            Assertions.assertTrue(resource.requestRoutingRules().containsKey("rule2"));
            Assertions.assertEquals(2, pathMap.pathRules().size());
            Assertions.assertEquals("/pictures/*", pathMap.pathRules().get("newRule").paths().get(0));
            return resource;
        }
    }

    /** Complex internal (private) app gateway test. */
    public class PrivateComplex extends TestTemplate<ApplicationGateway, ApplicationGateways> {

        /**
         * Tests minimal internal app gateways.
         *
         * @throws Exception when something goes wrong
         */
        public PrivateComplex(ResourceManagerUtils.InternalRuntimeContext internalContext) throws Exception {
            initializeResourceNames(internalContext);
        }

        @Override
        public void print(ApplicationGateway resource) {
            printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            ensurePIPs(resources.manager().publicIpAddresses());

            final Network vnet =
                resources
                    .manager()
                    .networks()
                    .define("net" + testId)
                    .withRegion(REGION)
                    .withNewResourceGroup(groupName)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/29")
                    .withSubnet("subnet2", "10.0.0.8/29")
                    .create();

            Thread.UncaughtExceptionHandler threadException =
                new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread th, Throwable ex) {
                        System.out.println("Uncaught exception: " + ex);
                    }
                };

            // Prepare for execution in a separate thread to shorten the test
            Thread creationThread =
                new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // Create an application gateway
                            try {
                                resources
                                    .define(appGatewayName)
                                    .withRegion(REGION)
                                    .withExistingResourceGroup(groupName)

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
                                    .withSslCertificateFromPfxFile(
                                        new File(getClass().getClassLoader().getResource("myTest.pfx").getFile()))
                                    .withSslCertificatePassword("Abc123")
                                    .toBackendHttpConfiguration("config1")
                                    .toBackend("backend1")
                                    .attach()
                                    .defineRequestRoutingRule("rule9000")
                                    .fromListener("listener1")
                                    .toBackendHttpConfiguration("config1")
                                    .toBackend("backend1")
                                    .attach()
                                    .defineRequestRoutingRule("ruleRedirect")
                                    .fromPrivateFrontend()
                                    .fromFrontendHttpsPort(444)
                                    .withSslCertificate("cert1")
                                    .withRedirectConfiguration("redirect1")
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
                                    .withPfxFromFile(
                                        new File(getClass().getClassLoader().getResource("myTest2.pfx").getFile()))
                                    .withPfxPassword("Abc123")
                                    .attach()

                                    // Authentication certificates
                                    .defineAuthenticationCertificate("auth2")
                                    .fromFile(
                                        new File(getClass().getClassLoader().getResource("myTest2.cer").getFile()))
                                    .attach()

                                    // Additional/explicit backend HTTP setting configs
                                    .defineBackendHttpConfiguration("config1")
                                    .withPort(8081)
                                    .withRequestTimeout(45)
                                    .withHttps()
                                    .withAuthenticationCertificateFromFile(
                                        new File(getClass().getClassLoader().getResource("myTest.cer").getFile()))
                                    .attach()
                                    .defineBackendHttpConfiguration("config2")
                                    .withPort(8082)
                                    .withHttps()
                                    .withAuthenticationCertificate("auth2")
                                    // Add the same cert, so only one should be added
                                    .withAuthenticationCertificateFromFile(
                                        new File(getClass().getClassLoader().getResource("myTest2.cer").getFile()))
                                    .attach()

                                    // Redirect configurations
                                    .defineRedirectConfiguration("redirect1")
                                    .withType(ApplicationGatewayRedirectType.PERMANENT)
                                    .withTargetListener("listener1")
                                    .withPathIncluded()
                                    .attach()
                                    .defineRedirectConfiguration("redirect2")
                                    .withType(ApplicationGatewayRedirectType.TEMPORARY)
                                    .withTargetUrl("http://www.microsoft.com")
                                    .withQueryStringIncluded()
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

            creationThread.join();

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.getById(resourceId);
            Assertions.assertNotNull(appGateway);
            Assertions.assertEquals(ApplicationGatewayTier.STANDARD, appGateway.tier());
            Assertions.assertEquals(ApplicationGatewaySkuName.STANDARD_MEDIUM, appGateway.size());
            Assertions.assertEquals(2, appGateway.instanceCount());
            Assertions.assertFalse(appGateway.isPublic());
            Assertions.assertTrue(appGateway.isPrivate());
            Assertions.assertEquals(1, appGateway.ipConfigurations().size());

            // Verify redirect configurations
            Assertions.assertEquals(2, appGateway.redirectConfigurations().size());
            ApplicationGatewayRedirectConfiguration redirect = appGateway.redirectConfigurations().get("redirect1");
            Assertions.assertNotNull(redirect);
            Assertions.assertEquals(ApplicationGatewayRedirectType.PERMANENT, redirect.type());
            Assertions.assertNotNull(redirect.targetListener());
            Assertions.assertEquals("listener1", redirect.targetListener().name());
            Assertions.assertNull(redirect.targetUrl());
            Assertions.assertTrue(redirect.isPathIncluded());
            Assertions.assertFalse(redirect.isQueryStringIncluded());
            Assertions.assertEquals(1, redirect.requestRoutingRules().size());

            redirect = appGateway.redirectConfigurations().get("redirect2");
            Assertions.assertNotNull(redirect);
            Assertions.assertEquals(ApplicationGatewayRedirectType.TEMPORARY, redirect.type());
            Assertions.assertNull(redirect.targetListener());
            Assertions.assertNotNull(redirect.targetUrl());
            Assertions.assertEquals("http://www.microsoft.com", redirect.targetUrl());
            Assertions.assertTrue(redirect.isQueryStringIncluded());
            Assertions.assertFalse(redirect.isPathIncluded());

            // Verify frontend ports
            Assertions.assertEquals(4, appGateway.frontendPorts().size());
            Assertions.assertNotNull(appGateway.frontendPortNameFromNumber(80));
            Assertions.assertNotNull(appGateway.frontendPortNameFromNumber(443));
            Assertions.assertNotNull(appGateway.frontendPortNameFromNumber(9000));
            Assertions.assertNotNull(appGateway.frontendPortNameFromNumber(444));

            // Verify frontends
            Assertions.assertEquals(1, appGateway.frontends().size());
            Assertions.assertTrue(appGateway.publicFrontends().isEmpty());
            Assertions.assertEquals(1, appGateway.privateFrontends().size());
            ApplicationGatewayFrontend frontend = appGateway.privateFrontends().values().iterator().next();
            Assertions.assertFalse(frontend.isPublic());
            Assertions.assertTrue(frontend.isPrivate());

            // Verify listeners
            Assertions.assertEquals(4, appGateway.listeners().size());
            ApplicationGatewayListener listener = appGateway.listeners().get("listener1");
            Assertions.assertNotNull(listener);
            Assertions.assertEquals(9000, listener.frontendPortNumber());
            Assertions.assertEquals(ApplicationGatewayProtocol.HTTP, listener.protocol());
            Assertions.assertNotNull(listener.frontend());
            Assertions.assertTrue(listener.frontend().isPrivate());
            Assertions.assertFalse(listener.frontend().isPublic());
            Assertions.assertNotNull(appGateway.listenerByPortNumber(80));
            Assertions.assertNotNull(appGateway.listenerByPortNumber(443));
            Assertions.assertNotNull(appGateway.listenerByPortNumber(444));

            // Verify SSL certificates
            Assertions.assertEquals(2, appGateway.sslCertificates().size());
            Assertions.assertTrue(appGateway.sslCertificates().containsKey("cert1"));

            // Verify backend HTTP settings configs
            Assertions.assertEquals(3, appGateway.backendHttpConfigurations().size());
            ApplicationGatewayBackendHttpConfiguration config = appGateway.backendHttpConfigurations().get("config1");
            Assertions.assertNotNull(config);
            Assertions.assertEquals(8081, config.port());
            Assertions.assertEquals(45, config.requestTimeout());
            Assertions.assertEquals(1, config.authenticationCertificates().size());

            ApplicationGatewayBackendHttpConfiguration config2 = appGateway.backendHttpConfigurations().get("config2");
            Assertions.assertNotNull(config2);

            // Verify authentication certificates
            Assertions.assertEquals(2, appGateway.authenticationCertificates().size());
            ApplicationGatewayAuthenticationCertificate authCert2 =
                appGateway.authenticationCertificates().get("auth2");
            Assertions.assertNotNull(authCert2);
            Assertions.assertNotNull(authCert2.data());

            ApplicationGatewayAuthenticationCertificate authCert =
                config.authenticationCertificates().values().iterator().next();
            Assertions.assertNotNull(authCert);

            Assertions.assertEquals(1, config2.authenticationCertificates().size());
            Assertions
                .assertEquals(authCert2.name(), config2.authenticationCertificates().values().iterator().next().name());

            // Verify backends
            Assertions.assertEquals(3, appGateway.backends().size());
            ApplicationGatewayBackend backend = appGateway.backends().get("backend1");
            Assertions.assertNotNull(backend);
            Assertions.assertEquals(2, backend.addresses().size());
            Assertions.assertTrue(backend.containsIPAddress("11.1.1.3"));
            Assertions.assertTrue(backend.containsIPAddress("11.1.1.4"));
            Assertions.assertTrue(appGateway.backends().containsKey("backend2"));

            // Verify request routing rules
            Assertions.assertEquals(4, appGateway.requestRoutingRules().size());
            ApplicationGatewayRequestRoutingRule rule;

            rule = appGateway.requestRoutingRules().get("rule80");
            Assertions.assertNotNull(rule);
            Assertions.assertTrue(vnet.id().equalsIgnoreCase(rule.listener().frontend().networkId()));
            Assertions.assertEquals(80, rule.frontendPort());
            Assertions.assertEquals(8080, rule.backendPort());
            Assertions.assertTrue(rule.cookieBasedAffinity());
            Assertions.assertEquals(2, rule.backendAddresses().size());
            Assertions.assertTrue(rule.backend().containsIPAddress("11.1.1.1"));
            Assertions.assertTrue(rule.backend().containsIPAddress("11.1.1.2"));

            rule = appGateway.requestRoutingRules().get("rule443");
            Assertions.assertNotNull(rule);
            Assertions.assertTrue(vnet.id().equalsIgnoreCase(rule.listener().frontend().networkId()));
            Assertions.assertEquals(443, rule.frontendPort());
            Assertions.assertEquals(ApplicationGatewayProtocol.HTTPS, rule.frontendProtocol());
            Assertions.assertNotNull(rule.sslCertificate());
            Assertions.assertNotNull(rule.backendHttpConfiguration());
            Assertions.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assertions.assertNotNull(rule.backend());
            Assertions.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

            rule = appGateway.requestRoutingRules().get("rule9000");
            Assertions.assertNotNull(rule);
            Assertions.assertNotNull(rule.listener());
            Assertions.assertTrue(rule.listener().name().equalsIgnoreCase("listener1"));
            Assertions.assertNotNull(rule.listener().subnetName());
            Assertions.assertNotNull(rule.listener().networkId());
            Assertions.assertNotNull(rule.backendHttpConfiguration());
            Assertions.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assertions.assertNotNull(rule.backend());
            Assertions.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

            rule = appGateway.requestRoutingRules().get("ruleRedirect");
            Assertions.assertNotNull(rule);
            Assertions.assertNotNull(rule.redirectConfiguration());
            Assertions.assertEquals("redirect1", rule.redirectConfiguration().name());

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
            final ApplicationGatewayAuthenticationCertificate authCert1 =
                resource
                    .backendHttpConfigurations()
                    .get("config1")
                    .authenticationCertificates()
                    .values()
                    .iterator()
                    .next();
            Assertions.assertNotNull(authCert1);

            PublicIpAddress pip = resource.manager().publicIpAddresses().getByResourceGroup(groupName, pipNames[0]);
            ApplicationGatewayListener listener443 = resource.requestRoutingRules().get("rule443").listener();
            Assertions.assertNotNull(listener443);
            ApplicationGatewayListener listenerRedirect = resource.requestRoutingRules().get("ruleRedirect").listener();
            Assertions.assertNotNull(listenerRedirect);

            resource
                .update()
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
                .updateListener(listener443.name())
                .withHostname("foobar")
                .parent()
                .updateListener(listenerRedirect.name())
                .withHttp()
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
                .updateRequestRoutingRule("rule443")
                .withoutRedirectConfiguration()
                .parent()
                .updateRedirectConfiguration("redirect1")
                .withTargetUrl("http://azure.com")
                .withType(ApplicationGatewayRedirectType.FOUND)
                .withQueryStringIncluded()
                .withoutPathIncluded()
                .parent()
                .withoutRedirectConfiguration("redirect2")
                .withExistingPublicIpAddress(pip) // Associate with a public IP as well
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();

            resource.refresh();

            // Get the resource created so far
            Assertions.assertTrue(resource.tags().containsKey("tag1"));
            Assertions.assertTrue(resource.tags().containsKey("tag2"));
            Assertions.assertEquals(ApplicationGatewaySkuName.STANDARD_SMALL, resource.size());
            Assertions.assertEquals(1, resource.instanceCount());

            // Verify redirect configurations
            Assertions.assertEquals(1, resource.redirectConfigurations().size());
            ApplicationGatewayRedirectConfiguration redirect = resource.redirectConfigurations().get("redirect1");
            Assertions.assertNotNull(redirect);
            Assertions.assertEquals(ApplicationGatewayRedirectType.FOUND, redirect.type());
            Assertions.assertNull(redirect.targetListener());
            Assertions.assertNotNull(redirect.targetUrl());
            Assertions.assertEquals("http://azure.com", redirect.targetUrl());

            // Verify frontend ports
            Assertions.assertEquals(portCount - 1, resource.frontendPorts().size());
            Assertions.assertNull(resource.frontendPortNameFromNumber(9000));

            // Verify frontends
            Assertions.assertEquals(frontendCount + 1, resource.frontends().size());
            Assertions.assertEquals(1, resource.publicFrontends().size());
            Assertions
                .assertTrue(
                    resource
                        .publicFrontends()
                        .values()
                        .iterator()
                        .next()
                        .publicIpAddressId()
                        .equalsIgnoreCase(pip.id()));
            Assertions.assertEquals(1, resource.privateFrontends().size());
            ApplicationGatewayFrontend frontend = resource.privateFrontends().values().iterator().next();
            Assertions.assertFalse(frontend.isPublic());
            Assertions.assertTrue(frontend.isPrivate());

            // Verify listeners
            Assertions.assertEquals(listenerCount - 1, resource.listeners().size());
            Assertions.assertFalse(resource.listeners().containsKey("listener1"));

            // Verify backends
            Assertions.assertEquals(backendCount - 1, resource.backends().size());
            Assertions.assertFalse(resource.backends().containsKey("backend2"));
            ApplicationGatewayBackend backend = resource.backends().get("backend1");
            Assertions.assertNotNull(backend);
            Assertions.assertEquals(1, backend.addresses().size());
            Assertions.assertTrue(backend.containsIPAddress("11.1.1.5"));
            Assertions.assertFalse(backend.containsIPAddress("11.1.1.3"));
            Assertions.assertFalse(backend.containsIPAddress("11.1.1.4"));

            // Verify HTTP configs
            Assertions.assertEquals(configCount - 1, resource.backendHttpConfigurations().size());
            Assertions.assertFalse(resource.backendHttpConfigurations().containsKey("config2"));
            ApplicationGatewayBackendHttpConfiguration config = resource.backendHttpConfigurations().get("config1");
            Assertions.assertEquals(8082, config.port());
            Assertions.assertEquals(20, config.requestTimeout());
            Assertions.assertTrue(config.cookieBasedAffinity());
            Assertions.assertEquals(1, config.authenticationCertificates().size());
            Assertions.assertFalse(config.authenticationCertificates().containsKey(authCert1.name()));
            Assertions.assertTrue(config.authenticationCertificates().containsKey("auth2"));

            // Verify rules
            Assertions.assertEquals(ruleCount - 1, resource.requestRoutingRules().size());
            Assertions.assertFalse(resource.requestRoutingRules().containsKey("rule9000"));

            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule80");
            Assertions.assertNotNull(rule);
            Assertions.assertNotNull(rule.backend());
            Assertions.assertTrue("backend1".equalsIgnoreCase(rule.backend().name()));
            Assertions.assertNotNull(rule.backendHttpConfiguration());
            Assertions.assertTrue("config1".equalsIgnoreCase(rule.backendHttpConfiguration().name()));

            rule = resource.requestRoutingRules().get("rule443");
            Assertions.assertNotNull(rule);
            Assertions.assertNotNull(rule.listener());
            Assertions.assertTrue("foobar".equalsIgnoreCase(rule.listener().hostname()));
            Assertions.assertNull(rule.redirectConfiguration());

            // Verify SSL certificates
            Assertions.assertEquals(sslCertCount - 1, resource.sslCertificates().size());
            Assertions.assertFalse(resource.sslCertificates().containsKey("cert1"));

            // Verify authentication certificates
            Assertions.assertEquals(authCertCount - 1, resource.authenticationCertificates().size());
            Assertions.assertFalse(resource.authenticationCertificates().containsKey("auth1"));

            return resource;
        }
    }

    /** Complex Internet-facing (public) app gateway test. */
    public class PublicComplex extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        /**
         * Tests minimal internal app gateways.
         *
         * @throws Exception when something goes wrong with test PIP creation
         */
        public PublicComplex(ResourceManagerUtils.InternalRuntimeContext internalContext) throws Exception {
            initializeResourceNames(internalContext);
        }

        @Override
        public void print(ApplicationGateway resource) {
            printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            ensurePIPs(resources.manager().publicIpAddresses());
            Thread.UncaughtExceptionHandler threadException =
                new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread th, Throwable ex) {
                        System.out.println("Uncaught exception: " + ex);
                    }
                };

            final PublicIpAddress pip =
                resources.manager().publicIpAddresses().getByResourceGroup(groupName, pipNames[0]);

            // Prepare for execution in a separate thread to shorten the test
            Thread creationThread =
                new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // Create an application gateway
                            try {
                                resources
                                    .define(appGatewayName)
                                    .withRegion(REGION)
                                    .withExistingResourceGroup(groupName)

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
                                    .withSslCertificateFromPfxFile(
                                        new File(getClass().getClassLoader().getResource("myTest.pfx").getFile()))
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
                                    .withSslCertificateFromPfxFile(
                                        new File(getClass().getClassLoader().getResource("myTest2.pfx").getFile()))
                                    .withSslCertificatePassword("Abc123")
                                    .withServerNameIndication()
                                    .withHostname("www.fabricam.com")
                                    .attach()

                                    // Additional/explicit backends
                                    .defineBackend("backend1")
                                    .withIPAddress("11.1.1.1")
                                    .withIPAddress("11.1.1.2")
                                    .attach()
                                    .withExistingPublicIpAddress(pip)
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
                                    .withDisabledSslProtocols(
                                        ApplicationGatewaySslProtocol.TLSV1_0, ApplicationGatewaySslProtocol.TLSV1_1)
                                    .withHttp2()
                                    .create();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            // Start creating in a separate thread...
            creationThread.setUncaughtExceptionHandler(threadException);
            creationThread.start();

            creationThread.join();

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.getById(resourceId);
            Assertions.assertNotNull(appGateway);
            Assertions.assertTrue(appGateway.isPublic());
            Assertions.assertTrue(!appGateway.isPrivate());
            Assertions.assertEquals(ApplicationGatewayTier.STANDARD, appGateway.tier());
            Assertions.assertEquals(ApplicationGatewaySkuName.STANDARD_MEDIUM, appGateway.size());
            Assertions.assertEquals(2, appGateway.instanceCount());
            Assertions.assertEquals(1, appGateway.ipConfigurations().size());
            Assertions.assertTrue(appGateway.isHttp2Enabled());

            // Verify frontend ports
            Assertions.assertEquals(3, appGateway.frontendPorts().size());
            Assertions.assertNotNull(appGateway.frontendPortNameFromNumber(80));
            Assertions.assertNotNull(appGateway.frontendPortNameFromNumber(443));
            Assertions.assertNotNull(appGateway.frontendPortNameFromNumber(9000));

            // Verify frontends
            Assertions.assertEquals(1, appGateway.frontends().size());
            Assertions.assertEquals(1, appGateway.publicFrontends().size());
            Assertions.assertEquals(0, appGateway.privateFrontends().size());
            ApplicationGatewayFrontend frontend = appGateway.publicFrontends().values().iterator().next();
            Assertions.assertTrue(frontend.isPublic());
            Assertions.assertTrue(!frontend.isPrivate());

            // Verify listeners
            Assertions.assertEquals(3, appGateway.listeners().size());
            ApplicationGatewayListener listener = appGateway.listeners().get("listener1");
            Assertions.assertNotNull(listener);
            Assertions.assertEquals(9000, listener.frontendPortNumber());
            Assertions.assertTrue("www.fabricam.com".equalsIgnoreCase(listener.hostname()));
            Assertions.assertTrue(listener.requiresServerNameIndication());
            Assertions.assertNotNull(listener.frontend());
            Assertions.assertFalse(listener.frontend().isPrivate());
            Assertions.assertTrue(listener.frontend().isPublic());
            Assertions.assertEquals(ApplicationGatewayProtocol.HTTPS, listener.protocol());
            Assertions.assertNotNull(appGateway.listenerByPortNumber(80));
            Assertions.assertNotNull(appGateway.listenerByPortNumber(443));

            // Verify SSL certificates
            Assertions.assertEquals(2, appGateway.sslCertificates().size());

            // Verify backends
            Assertions.assertEquals(2, appGateway.backends().size());
            ApplicationGatewayBackend backend = appGateway.backends().get("backend1");
            Assertions.assertNotNull(backend);
            Assertions.assertEquals(2, backend.addresses().size());

            // Verify request routing rules
            Assertions.assertEquals(3, appGateway.requestRoutingRules().size());
            ApplicationGatewayRequestRoutingRule rule, rule80;

            rule80 = appGateway.requestRoutingRules().get("rule80");
            Assertions.assertNotNull(rule80);
            Assertions.assertTrue(pip.id().equalsIgnoreCase(rule80.publicIpAddressId()));
            Assertions.assertEquals(80, rule80.frontendPort());
            Assertions.assertEquals(8080, rule80.backendPort());
            Assertions.assertTrue(rule80.cookieBasedAffinity());
            Assertions.assertEquals(4, rule80.backendAddresses().size());
            Assertions.assertTrue(rule80.backend().containsIPAddress("11.1.1.2"));
            Assertions.assertTrue(rule80.backend().containsIPAddress("11.1.1.1"));
            Assertions.assertTrue(rule80.backend().containsFqdn("www.microsoft.com"));
            Assertions.assertTrue(rule80.backend().containsFqdn("www.example.com"));

            rule = appGateway.requestRoutingRules().get("rule443");
            Assertions.assertNotNull(rule);
            Assertions.assertTrue(pip.id().equalsIgnoreCase(rule.publicIpAddressId()));
            Assertions.assertEquals(443, rule.frontendPort());
            Assertions.assertEquals(ApplicationGatewayProtocol.HTTPS, rule.frontendProtocol());
            Assertions.assertNotNull(rule.sslCertificate());
            Assertions.assertNotNull(rule.backendHttpConfiguration());
            Assertions.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assertions.assertNotNull(rule.backend());
            Assertions.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

            rule = appGateway.requestRoutingRules().get("rule9000");
            Assertions.assertNotNull(rule);
            Assertions.assertNotNull(rule.listener());
            Assertions.assertTrue(rule.listener().name().equalsIgnoreCase("listener1"));
            Assertions.assertNotNull(rule.backendHttpConfiguration());
            Assertions.assertTrue(rule.backendHttpConfiguration().name().equalsIgnoreCase("config1"));
            Assertions.assertNotNull(rule.backend());
            Assertions.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));

            // Verify backend HTTP settings configs
            Assertions.assertEquals(2, appGateway.backendHttpConfigurations().size());
            ApplicationGatewayBackendHttpConfiguration config = appGateway.backendHttpConfigurations().get("config1");
            Assertions.assertNotNull(config);
            Assertions.assertEquals(8081, config.port());
            Assertions.assertEquals(45, config.requestTimeout());
            Assertions.assertNotNull(config.probe());
            Assertions.assertEquals("probe1", config.probe().name());
            Assertions.assertFalse(config.isHostHeaderFromBackend());
            Assertions.assertEquals("foo", config.hostHeader());
            Assertions.assertEquals(100, config.connectionDrainingTimeoutInSeconds());
            Assertions.assertEquals("/path/", config.path());
            Assertions.assertEquals("cookie", config.affinityCookieName());

            // Verify probes
            Assertions.assertEquals(2, appGateway.probes().size());
            ApplicationGatewayProbe probe;
            probe = appGateway.probes().get("probe1");
            Assertions.assertNotNull(probe);
            Assertions.assertEquals("microsoft.com", probe.host().toLowerCase());
            Assertions.assertEquals(ApplicationGatewayProtocol.HTTP, probe.protocol());
            Assertions.assertEquals("/", probe.path());
            Assertions.assertEquals(5, probe.retriesBeforeUnhealthy());
            Assertions.assertEquals(9, probe.timeBetweenProbesInSeconds());
            Assertions.assertEquals(10, probe.timeoutInSeconds());
            Assertions.assertNotNull(probe.healthyHttpResponseStatusCodeRanges());
            Assertions.assertEquals(1, probe.healthyHttpResponseStatusCodeRanges().size());
            Assertions.assertTrue(probe.healthyHttpResponseStatusCodeRanges().contains("200-249"));

            probe = appGateway.probes().get("probe2");
            Assertions.assertNotNull(probe);
            Assertions.assertEquals(ApplicationGatewayProtocol.HTTPS, probe.protocol());
            Assertions.assertEquals(2, probe.healthyHttpResponseStatusCodeRanges().size());
            Assertions.assertTrue(probe.healthyHttpResponseStatusCodeRanges().contains("600-610"));
            Assertions.assertTrue(probe.healthyHttpResponseStatusCodeRanges().contains("650-660"));
            Assertions.assertEquals("I am too healthy for this test.", probe.healthyHttpResponseBodyContents());

            // Verify SSL policy - disabled protocols
            Assertions.assertEquals(2, appGateway.disabledSslProtocols().size());
            Assertions.assertTrue(appGateway.disabledSslProtocols().contains(ApplicationGatewaySslProtocol.TLSV1_0));
            Assertions.assertTrue(appGateway.disabledSslProtocols().contains(ApplicationGatewaySslProtocol.TLSV1_1));
            Assertions.assertTrue(!appGateway.disabledSslProtocols().contains(ApplicationGatewaySslProtocol.TLSV1_2));

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            final int rulesCount = resource.requestRoutingRules().size();
            ApplicationGatewayRequestRoutingRule rule80 = resource.requestRoutingRules().get("rule80");
            Assertions.assertNotNull(rule80);
            ApplicationGatewayBackendHttpConfiguration backendConfig80 = rule80.backendHttpConfiguration();
            Assertions.assertNotNull(backendConfig80);

            resource
                .update()
                .withSize(ApplicationGatewaySkuName.STANDARD_SMALL)
                .withInstanceCount(1)
                .updateListener("listener1")
                .withHostname("www.contoso.com")
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
                .withoutDisabledSslProtocols(
                    ApplicationGatewaySslProtocol.TLSV1_0, ApplicationGatewaySslProtocol.TLSV1_1)
                .withoutHttp2()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();

            resource.refresh();

            // Get the resource created so far
            Assertions.assertTrue(resource.tags().containsKey("tag1"));
            Assertions.assertTrue(resource.size().equals(ApplicationGatewaySkuName.STANDARD_SMALL));
            Assertions.assertTrue(resource.instanceCount() == 1);
            Assertions.assertFalse(resource.isHttp2Enabled());

            // Verify listeners
            ApplicationGatewayListener listener = resource.listeners().get("listener1");
            Assertions.assertTrue("www.contoso.com".equalsIgnoreCase(listener.hostname()));

            // Verify request routing rules
            Assertions.assertTrue(resource.requestRoutingRules().size() == rulesCount - 1);
            Assertions.assertTrue(!resource.requestRoutingRules().containsKey("rule9000"));
            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule443");
            Assertions.assertTrue(rule != null);
            Assertions.assertTrue("listener1".equalsIgnoreCase(rule.listener().name()));

            // Verify probes
            Assertions.assertEquals(1, resource.probes().size());
            ApplicationGatewayProbe probe = resource.probes().get("probe2");
            Assertions.assertNotNull(probe);
            Assertions.assertTrue(probe.healthyHttpResponseStatusCodeRanges().isEmpty());
            Assertions.assertNull(probe.healthyHttpResponseBodyContents());

            // Verify backend configs
            ApplicationGatewayBackendHttpConfiguration backendConfig =
                resource.backendHttpConfigurations().get("config1");
            Assertions.assertNotNull(backendConfig);
            Assertions.assertNull(backendConfig.probe());
            Assertions.assertFalse(backendConfig.isHostHeaderFromBackend());
            Assertions.assertNull(backendConfig.hostHeader());
            Assertions.assertEquals(0, backendConfig.connectionDrainingTimeoutInSeconds());
            Assertions.assertNull(backendConfig.affinityCookieName());
            Assertions.assertNull(backendConfig.path());

            rule80 = resource.requestRoutingRules().get("rule80");
            Assertions.assertNotNull(rule80);
            backendConfig80 = rule80.backendHttpConfiguration();
            Assertions.assertNotNull(backendConfig80);
            Assertions.assertTrue(backendConfig80.isHostHeaderFromBackend());
            Assertions.assertNull(backendConfig80.hostHeader());

            // Verify SSL policy - disabled protocols
            Assertions.assertEquals(0, resource.disabledSslProtocols().size());
            return resource;
        }
    }

    /** Internet-facing LB test with NAT pool test. */
    public class PublicMinimal extends TestTemplate<ApplicationGateway, ApplicationGateways> {
        PublicMinimal(ResourceManagerUtils.InternalRuntimeContext internalContext) {
            initializeResourceNames(internalContext);
        }

        @Override
        public void print(ApplicationGateway resource) {
            printAppGateway(resource);
        }

        @Override
        public ApplicationGateway createResource(final ApplicationGateways resources) throws Exception {
            // Prepare a separate thread for resource creation
            Thread creationThread =
                new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // Create an application gateway
                            try {
                                resources
                                    .define(appGatewayName)
                                    .withRegion(REGION)
                                    .withNewResourceGroup(groupName)

                                    // Request routing rules
                                    .defineRequestRoutingRule("rule1")
                                    .fromPublicFrontend()
                                    .fromFrontendHttpsPort(443)
                                    .withSslCertificateFromPfxFile(
                                        new File(getClass().getClassLoader().getResource("myTest.pfx").getFile()))
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

            creationThread.join();

            // Get the resource as created so far
            String resourceId = createResourceId(resources.manager().subscriptionId());
            ApplicationGateway appGateway = resources.manager().applicationGateways().getById(resourceId);
            Assertions.assertTrue(appGateway != null);
            Assertions.assertTrue(ApplicationGatewayTier.STANDARD.equals(appGateway.tier()));
            Assertions.assertTrue(ApplicationGatewaySkuName.STANDARD_SMALL.equals(appGateway.size()));
            Assertions.assertTrue(appGateway.instanceCount() == 1);

            // Verify frontend ports
            Assertions.assertTrue(appGateway.frontendPorts().size() == 1);
            Assertions.assertTrue(appGateway.frontendPortNameFromNumber(443) != null);

            // Verify frontends
            Assertions.assertTrue(!appGateway.isPrivate());
            Assertions.assertTrue(appGateway.isPublic());
            Assertions.assertTrue(appGateway.frontends().size() == 1);

            // Verify listeners
            Assertions.assertTrue(appGateway.listeners().size() == 1);
            Assertions.assertTrue(appGateway.listenerByPortNumber(443) != null);

            // Verify backends
            Assertions.assertTrue(appGateway.backends().size() == 1);

            // Verify backend HTTP configs
            Assertions.assertTrue(appGateway.backendHttpConfigurations().size() == 1);

            // Verify rules
            Assertions.assertTrue(appGateway.requestRoutingRules().size() == 1);
            ApplicationGatewayRequestRoutingRule rule = appGateway.requestRoutingRules().get("rule1");
            Assertions.assertTrue(rule != null);
            Assertions.assertTrue(rule.frontendPort() == 443);
            Assertions.assertTrue(ApplicationGatewayProtocol.HTTPS.equals(rule.frontendProtocol()));
            Assertions.assertTrue(rule.listener() != null);
            Assertions.assertTrue(rule.listener().frontend() != null);
            Assertions.assertTrue(rule.listener().frontend().isPublic());
            Assertions.assertTrue(!rule.listener().frontend().isPrivate());
            Assertions.assertTrue(rule.backendPort() == 8080);
            Assertions.assertTrue(rule.sslCertificate() != null);
            Assertions.assertTrue(rule.backendAddresses().size() == 2);
            Assertions.assertTrue(rule.backend().containsIPAddress("11.1.1.1"));
            Assertions.assertTrue(rule.backend().containsIPAddress("11.1.1.2"));

            // Verify certificates
            Assertions.assertTrue(appGateway.sslCertificates().size() == 1);

            return appGateway;
        }

        @Override
        public ApplicationGateway updateResource(final ApplicationGateway resource) throws Exception {
            resource
                .update()
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

            Assertions.assertTrue(resource.tags().containsKey("tag1"));
            Assertions.assertTrue(resource.tags().containsKey("tag2"));
            Assertions.assertTrue(ApplicationGatewaySkuName.STANDARD_MEDIUM.equals(resource.size()));
            Assertions.assertTrue(resource.instanceCount() == 2);

            // Verify frontend ports
            Assertions.assertTrue(resource.frontendPorts().size() == 2);
            Assertions.assertTrue(resource.frontendPortNameFromNumber(80) != null);

            // Verify listeners
            Assertions.assertTrue(resource.listeners().size() == 2);
            ApplicationGatewayListener listener = resource.listeners().get("listener2");
            Assertions.assertTrue(listener != null);
            Assertions.assertTrue(!listener.frontend().isPrivate());
            Assertions.assertTrue(listener.frontend().isPublic());
            Assertions.assertTrue(listener.frontendPortNumber() == 80);
            Assertions.assertTrue(ApplicationGatewayProtocol.HTTP.equals(listener.protocol()));
            Assertions.assertTrue(listener.sslCertificate() == null);

            // Verify backends
            Assertions.assertTrue(resource.backends().size() == 2);
            ApplicationGatewayBackend backend = resource.backends().get("backend2");
            Assertions.assertTrue(backend != null);
            Assertions.assertTrue(backend.addresses().size() == 1);
            Assertions.assertTrue(backend.containsIPAddress("11.1.1.3"));

            // Verify HTTP configs
            Assertions.assertTrue(resource.backendHttpConfigurations().size() == 2);
            ApplicationGatewayBackendHttpConfiguration config = resource.backendHttpConfigurations().get("config2");
            Assertions.assertTrue(config != null);
            Assertions.assertTrue(config.cookieBasedAffinity());
            Assertions.assertTrue(config.port() == 8081);
            Assertions.assertTrue(config.requestTimeout() == 33);

            // Verify request routing rules
            Assertions.assertTrue(resource.requestRoutingRules().size() == 2);
            ApplicationGatewayRequestRoutingRule rule = resource.requestRoutingRules().get("rule2");
            Assertions.assertTrue(rule != null);
            Assertions.assertTrue(rule.listener() != null);
            Assertions.assertTrue("listener2".equals(rule.listener().name()));
            Assertions.assertTrue(rule.backendHttpConfiguration() != null);
            Assertions.assertTrue("config2".equalsIgnoreCase(rule.backendHttpConfiguration().name()));
            Assertions.assertTrue(rule.backend() != null);
            Assertions.assertTrue("backend2".equalsIgnoreCase(rule.backend().name()));

            return resource;
        }
    }

    // Create VNet for the app gateway
    private Map<String, PublicIpAddress> ensurePIPs(PublicIpAddresses pips) throws Exception {
        List<Creatable<PublicIpAddress>> creatablePips = new ArrayList<>();
        for (int i = 0; i < pipNames.length; i++) {
            creatablePips.add(pips.define(pipNames[i]).withRegion(REGION).withNewResourceGroup(groupName));
        }

        return pips.create(creatablePips);
    }

    // Print app gateway info
    static void printAppGateway(ApplicationGateway resource) {
        StringBuilder info = new StringBuilder();
        info
            .append("Application gateway: ")
            .append(resource.id())
            .append("Name: ")
            .append(resource.name())
            .append("\n\tResource group: ")
            .append(resource.resourceGroupName())
            .append("\n\tRegion: ")
            .append(resource.region())
            .append("\n\tTags: ")
            .append(resource.tags())
            .append("\n\tSKU: ")
            .append(resource.sku().toString())
            .append("\n\tOperational state: ")
            .append(resource.operationalState())
            .append("\n\tInternet-facing? ")
            .append(resource.isPublic())
            .append("\n\tInternal? ")
            .append(resource.isPrivate())
            .append("\n\tDefault private IP address: ")
            .append(resource.privateIpAddress())
            .append("\n\tPrivate IP address allocation method: ")
            .append(resource.privateIpAllocationMethod())
            .append("\n\tDisabled SSL protocols: ")
            .append(resource.disabledSslProtocols().toString());

        // Show IP configs
        Map<String, ApplicationGatewayIpConfiguration> ipConfigs = resource.ipConfigurations();
        info.append("\n\tIP configurations: ").append(ipConfigs.size());
        for (ApplicationGatewayIpConfiguration ipConfig : ipConfigs.values()) {
            info
                .append("\n\t\tName: ")
                .append(ipConfig.name())
                .append("\n\t\t\tNetwork id: ")
                .append(ipConfig.networkId())
                .append("\n\t\t\tSubnet name: ")
                .append(ipConfig.subnetName());
        }

        // Show frontends
        Map<String, ApplicationGatewayFrontend> frontends = resource.frontends();
        info.append("\n\tFrontends: ").append(frontends.size());
        for (ApplicationGatewayFrontend frontend : frontends.values()) {
            info.append("\n\t\tName: ").append(frontend.name()).append("\n\t\t\tPublic? ").append(frontend.isPublic());

            if (frontend.isPublic()) {
                // Show public frontend info
                info.append("\n\t\t\tPublic IP address ID: ").append(frontend.publicIpAddressId());
            }

            if (frontend.isPrivate()) {
                // Show private frontend info
                info
                    .append("\n\t\t\tPrivate IP address: ")
                    .append(frontend.privateIpAddress())
                    .append("\n\t\t\tPrivate IP allocation method: ")
                    .append(frontend.privateIpAllocationMethod())
                    .append("\n\t\t\tSubnet name: ")
                    .append(frontend.subnetName())
                    .append("\n\t\t\tVirtual network ID: ")
                    .append(frontend.networkId());
            }
        }

        // Show backends
        Map<String, ApplicationGatewayBackend> backends = resource.backends();
        info.append("\n\tBackends: ").append(backends.size());
        for (ApplicationGatewayBackend backend : backends.values()) {
            info
                .append("\n\t\tName: ")
                .append(backend.name())
                .append("\n\t\t\tAssociated NIC IP configuration IDs: ")
                .append(backend.backendNicIPConfigurationNames().keySet());

            // Show addresses
            Collection<ApplicationGatewayBackendAddress> addresses = backend.addresses();
            info.append("\n\t\t\tAddresses: ").append(addresses.size());
            for (ApplicationGatewayBackendAddress address : addresses) {
                info
                    .append("\n\t\t\t\tFQDN: ")
                    .append(address.fqdn())
                    .append("\n\t\t\t\tIP: ")
                    .append(address.ipAddress());
            }
        }

        // Show backend HTTP configurations
        Map<String, ApplicationGatewayBackendHttpConfiguration> httpConfigs = resource.backendHttpConfigurations();
        info.append("\n\tHTTP Configurations: ").append(httpConfigs.size());
        for (ApplicationGatewayBackendHttpConfiguration httpConfig : httpConfigs.values()) {
            info
                .append("\n\t\tName: ")
                .append(httpConfig.name())
                .append("\n\t\t\tCookie based affinity: ")
                .append(httpConfig.cookieBasedAffinity())
                .append("\n\t\t\tPort: ")
                .append(httpConfig.port())
                .append("\n\t\t\tRequest timeout in seconds: ")
                .append(httpConfig.requestTimeout())
                .append("\n\t\t\tProtocol: ")
                .append(httpConfig.protocol())
                .append("\n\t\tHost header: ")
                .append(httpConfig.hostHeader())
                .append("\n\t\tHost header comes from backend? ")
                .append(httpConfig.isHostHeaderFromBackend())
                .append("\n\t\tConnection draining timeout in seconds: ")
                .append(httpConfig.connectionDrainingTimeoutInSeconds())
                .append("\n\t\tAffinity cookie name: ")
                .append(httpConfig.affinityCookieName())
                .append("\n\t\tPath: ")
                .append(httpConfig.path());

            if (httpConfig.probe() != null) {
                info.append("\n\t\t\tProbe: " + httpConfig.probe().name());
            }
            info.append("\n\t\tIs probe enabled? ").append(httpConfig.isProbeEnabled());
        }

        // Show SSL certificates
        Map<String, ApplicationGatewaySslCertificate> sslCerts = resource.sslCertificates();
        info.append("\n\tSSL certificates: ").append(sslCerts.size());
        for (ApplicationGatewaySslCertificate cert : sslCerts.values()) {
            info.append("\n\t\tName: ").append(cert.name()).append("\n\t\t\tCert data: ").append(cert.publicData());
        }

        // Show HTTP listeners
        Map<String, ApplicationGatewayListener> listeners = resource.listeners();
        info.append("\n\tHTTP listeners: ").append(listeners.size());
        for (ApplicationGatewayListener listener : listeners.values()) {
            info
                .append("\n\t\tName: ")
                .append(listener.name())
                .append("\n\t\t\tHost name: ")
                .append(listener.hostname())
                .append("\n\t\t\tServer name indication required? ")
                .append(listener.requiresServerNameIndication())
                .append("\n\t\t\tAssociated frontend name: ")
                .append(listener.frontend().name())
                .append("\n\t\t\tFrontend port name: ")
                .append(listener.frontendPortName())
                .append("\n\t\t\tFrontend port number: ")
                .append(listener.frontendPortNumber())
                .append("\n\t\t\tProtocol: ")
                .append(listener.protocol().toString());
            if (listener.sslCertificate() != null) {
                info.append("\n\t\t\tAssociated SSL certificate: ").append(listener.sslCertificate().name());
            }
        }

        // Show probes
        Map<String, ApplicationGatewayProbe> probes = resource.probes();
        info.append("\n\tProbes: ").append(probes.size());
        for (ApplicationGatewayProbe probe : probes.values()) {
            info
                .append("\n\t\tName: ")
                .append(probe.name())
                .append("\n\t\tProtocol:")
                .append(probe.protocol().toString())
                .append("\n\t\tInterval in seconds: ")
                .append(probe.timeBetweenProbesInSeconds())
                .append("\n\t\tRetries: ")
                .append(probe.retriesBeforeUnhealthy())
                .append("\n\t\tTimeout: ")
                .append(probe.timeoutInSeconds())
                .append("\n\t\tHost: ")
                .append(probe.host())
                .append("\n\t\tHealthy HTTP response status code ranges: ")
                .append(probe.healthyHttpResponseStatusCodeRanges())
                .append("\n\t\tHealthy HTTP response body contents: ")
                .append(probe.healthyHttpResponseBodyContents());
        }

        // Show authentication certificates
        Map<String, ApplicationGatewayAuthenticationCertificate> certs = resource.authenticationCertificates();
        info.append("\n\tAuthentication certificates: ").append(certs.size());
        for (ApplicationGatewayAuthenticationCertificate cert : certs.values()) {
            info.append("\n\t\tName: ").append(cert.name()).append("\n\t\tBase-64 encoded data: ").append(cert.data());
        }

        // Show redirect configurations
        Map<String, ApplicationGatewayRedirectConfiguration> redirects = resource.redirectConfigurations();
        info.append("\n\tRedirect configurations: ").append(redirects.size());
        for (ApplicationGatewayRedirectConfiguration redirect : redirects.values()) {
            info
                .append("\n\t\tName: ")
                .append(redirect.name())
                .append("\n\t\tTarget URL: ")
                .append(redirect.type())
                .append("\n\t\tTarget URL: ")
                .append(redirect.targetUrl())
                .append("\n\t\tTarget listener: ")
                .append(redirect.targetListener() != null ? redirect.targetListener().name() : null)
                .append("\n\t\tIs path included? ")
                .append(redirect.isPathIncluded())
                .append("\n\t\tIs query string included? ")
                .append(redirect.isQueryStringIncluded())
                .append("\n\t\tReferencing request routing rules: ")
                .append(redirect.requestRoutingRules().values());
        }

        // Show request routing rules
        Map<String, ApplicationGatewayRequestRoutingRule> rules = resource.requestRoutingRules();
        info.append("\n\tRequest routing rules: ").append(rules.size());
        for (ApplicationGatewayRequestRoutingRule rule : rules.values()) {
            info
                .append("\n\t\tName: ")
                .append(rule.name())
                .append("\n\t\tType: ")
                .append(rule.ruleType())
                .append("\n\t\tPublic IP address ID: ")
                .append(rule.publicIpAddressId())
                .append("\n\t\tHost name: ")
                .append(rule.hostname())
                .append("\n\t\tServer name indication required? ")
                .append(rule.requiresServerNameIndication())
                .append("\n\t\tFrontend port: ")
                .append(rule.frontendPort())
                .append("\n\t\tFrontend protocol: ")
                .append(rule.frontendProtocol().toString())
                .append("\n\t\tBackend port: ")
                .append(rule.backendPort())
                .append("\n\t\tCookie based affinity enabled? ")
                .append(rule.cookieBasedAffinity())
                .append("\n\t\tRedirect configuration: ")
                .append(rule.redirectConfiguration() != null ? rule.redirectConfiguration().name() : "(none)");

            // Show backend addresses
            Collection<ApplicationGatewayBackendAddress> addresses = rule.backendAddresses();
            info.append("\n\t\t\tBackend addresses: ").append(addresses.size());
            for (ApplicationGatewayBackendAddress address : addresses) {
                info.append("\n\t\t\t\t").append(address.fqdn()).append(" [").append(address.ipAddress()).append("]");
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
                info.append(listener.name());
            }
        }
        System.out.println(info.toString());
    }
}
