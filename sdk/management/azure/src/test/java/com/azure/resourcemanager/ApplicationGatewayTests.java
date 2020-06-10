// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHealth;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfigurationHealth;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendServerHealth;
import com.azure.resourcemanager.network.models.ApplicationGatewayOperationalState;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRule;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApplicationGatewayTests extends TestBase {
    private Azure azure;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        Azure.Authenticated azureAuthed =
            Azure.authenticate(httpPipeline, profile).withSdkContext(sdkContext);
        azure = azureAuthed.withDefaultSubscription();
    }

    @Override
    protected void cleanUpResources() {
    }

    /**
     * Tests a complex internal application gateway.
     *
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternalComplex() throws Exception {
        new TestApplicationGateway().new PrivateComplex(azure.sdkContext())
            .runTest(azure.applicationGateways(), azure.resourceGroups());
    }

    /**
     * Tests application gateway with path-based routing rule.
     *
     * @throws Exception
     */
    @Test
    public void testAppGatewaysPublicUrlPathBased() throws Exception {
        new TestApplicationGateway().new UrlPathBased(azure.sdkContext())
            .runTest(azure.applicationGateways(), azure.resourceGroups());
    }

    @Test
    public void testAppGatewayBackendHealthCheck() throws Exception {
        String testId = azure.applicationGateways().manager().sdkContext().randomResourceName("", 15);
        String name = "ag" + testId;
        Region region = Region.US_EAST;
        String password = azure.applicationGateways().manager().sdkContext().randomResourceName("Abc.123", 12);
        String vnetName = "net" + testId;
        String rgName = "rg" + testId;

        try {
            // Create a vnet
            Network network =
                azure
                    .networks()
                    .define(vnetName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/29")
                    .withSubnet("subnet2", "10.0.0.8/29")
                    .create();

            // Create VMs for the backend in the network to connect to
            List<Creatable<VirtualMachine>> vmsDefinitions = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                vmsDefinitions
                    .add(
                        azure
                            .virtualMachines()
                            .define("vm" + i + testId)
                            .withRegion(region)
                            .withExistingResourceGroup(rgName)
                            .withExistingPrimaryNetwork(network)
                            .withSubnet("subnet2")
                            .withPrimaryPrivateIPAddressDynamic()
                            .withoutPrimaryPublicIPAddress()
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUsername("tester")
                            .withRootPassword(password));
            }

            CreatedResources<VirtualMachine> createdVms = azure.virtualMachines().create(vmsDefinitions);
            VirtualMachine[] vms = new VirtualMachine[createdVms.size()];
            for (int i = 0; i < vmsDefinitions.size(); i++) {
                vms[i] = createdVms.get(vmsDefinitions.get(i).key());
            }

            String[] ipAddresses = new String[vms.length];
            for (int i = 0; i < vms.length; i++) {
                ipAddresses[i] = vms[i].getPrimaryNetworkInterface().primaryPrivateIP();
            }

            // Create the app gateway in the other subnet of the same vnet and point the backend at the VMs
            ApplicationGateway appGateway =
                azure
                    .applicationGateways()
                    .define(name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .defineRequestRoutingRule("rule1")
                    .fromPrivateFrontend()
                    .fromFrontendHttpPort(80)
                    .toBackendHttpPort(8080)
                    .toBackendIPAddresses(ipAddresses) // Connect the VMs via IP addresses
                    .attach()
                    .defineRequestRoutingRule("rule2")
                    .fromPrivateFrontend()
                    .fromFrontendHttpPort(25)
                    .toBackendHttpPort(22)
                    .toBackend("nicBackend")
                    .attach()
                    .withExistingSubnet(network.subnets().get("subnet1")) // Backend for connecting the VMs via NICs
                    .create();

            // Connect the 1st VM via NIC IP config
            NetworkInterface nic = vms[0].getPrimaryNetworkInterface();
            Assertions.assertNotNull(nic);
            ApplicationGatewayBackend appGatewayBackend = appGateway.backends().get("nicBackend");
            Assertions.assertNotNull(appGatewayBackend);
            nic
                .update()
                .updateIPConfiguration(nic.primaryIPConfiguration().name())
                .withExistingApplicationGatewayBackend(appGateway, appGatewayBackend.name())
                .parent()
                .apply();

            // Get the health of the VMs
            appGateway.refresh();
            Map<String, ApplicationGatewayBackendHealth> backendHealths = appGateway.checkBackendHealth();

            StringBuilder info = new StringBuilder();
            info.append("\nApplication gateway backend healths: ").append(backendHealths.size());
            for (ApplicationGatewayBackendHealth backendHealth : backendHealths.values()) {
                info
                    .append("\n\tApplication gateway backend name: ")
                    .append(backendHealth.name())
                    .append("\n\t\tHTTP configuration healths: ")
                    .append(backendHealth.httpConfigurationHealths().size());
                Assertions.assertNotNull(backendHealth.backend());
                for (ApplicationGatewayBackendHttpConfigurationHealth backendConfigHealth
                    : backendHealth.httpConfigurationHealths().values()) {
                    info
                        .append("\n\t\t\tHTTP configuration name: ")
                        .append(backendConfigHealth.name())
                        .append("\n\t\t\tServers: ")
                        .append(backendConfigHealth.inner().servers().size());
                    Assertions.assertNotNull(backendConfigHealth.backendHttpConfiguration());
                    for (ApplicationGatewayBackendServerHealth serverHealth
                        : backendConfigHealth.serverHealths().values()) {
                        NicIpConfiguration ipConfig = serverHealth.getNetworkInterfaceIPConfiguration();
                        if (ipConfig != null) {
                            info
                                .append("\n\t\t\t\tServer NIC ID: ")
                                .append(ipConfig.parent().id())
                                .append("\n\t\t\t\tIP Config name: ")
                                .append(ipConfig.name());
                        } else {
                            info.append("\n\t\t\t\tServer IP: " + serverHealth.ipAddress());
                        }
                        info.append("\n\t\t\t\tHealth status: ").append(serverHealth.status());
                    }
                }
            }
            System.out.println(info.toString());

            // Verify app gateway
            Assertions.assertEquals(2, appGateway.backends().size());
            ApplicationGatewayRequestRoutingRule rule1 = appGateway.requestRoutingRules().get("rule1");
            Assertions.assertNotNull(rule1);
            ApplicationGatewayBackend backend1 = rule1.backend();
            Assertions.assertNotNull(backend1);
            ApplicationGatewayRequestRoutingRule rule2 = appGateway.requestRoutingRules().get("rule2");
            Assertions.assertNotNull(rule2);
            ApplicationGatewayBackend backend2 = rule2.backend();
            Assertions.assertNotNull(backend2);

            Assertions.assertEquals(2, backendHealths.size());

            // Verify first backend (IP address-based)
            ApplicationGatewayBackendHealth backendHealth1 = backendHealths.get(backend1.name());
            Assertions.assertNotNull(backendHealth1);
            Assertions.assertNotNull(backendHealth1.backend());
            for (int i = 0; i < ipAddresses.length; i++) {
                Assertions.assertTrue(backend1.containsIPAddress(ipAddresses[i]));
            }

            // Verify second backend (NIC based)
            ApplicationGatewayBackendHealth backendHealth2 = backendHealths.get(backend2.name());
            Assertions.assertNotNull(backendHealth2);
            Assertions.assertNotNull(backendHealth2.backend());
            Assertions.assertEquals(backend2.name(), backendHealth2.name());
            Assertions.assertEquals(1, backendHealth2.httpConfigurationHealths().size());
            ApplicationGatewayBackendHttpConfigurationHealth httpConfigHealth2 =
                backendHealth2.httpConfigurationHealths().values().iterator().next();
            Assertions.assertNotNull(httpConfigHealth2.backendHttpConfiguration());
            Assertions.assertEquals(1, httpConfigHealth2.serverHealths().size());
            ApplicationGatewayBackendServerHealth serverHealth =
                httpConfigHealth2.serverHealths().values().iterator().next();
            NicIpConfiguration ipConfig2 = serverHealth.getNetworkInterfaceIPConfiguration();
            Assertions.assertEquals(nic.primaryIPConfiguration().name(), ipConfig2.name());
        } catch (Exception e) {
            throw e;
        } finally {
            if (azure.resourceGroups().contain(rgName)) {
                azure.resourceGroups().beginDeleteByName(rgName);
            }
        }
    }

    /**
     * Tests a minimal internal application gateway
     *
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternalMinimal() throws Exception {
        new TestApplicationGateway().new PrivateMinimal(azure.sdkContext())
            .runTest(azure.applicationGateways(), azure.resourceGroups());
    }

    @Test
    public void testAppGatewaysStartStop() throws Exception {
        String rgName = azure.sdkContext().randomResourceName("rg", 13);
        Region region = Region.US_EAST;
        String name = azure.sdkContext().randomResourceName("ag", 15);
        ApplicationGateway appGateway =
            azure
                .applicationGateways()
                .define(name)
                .withRegion(region)
                .withNewResourceGroup(rgName)

                // Request routing rules
                .defineRequestRoutingRule("rule1")
                .fromPrivateFrontend()
                .fromFrontendHttpPort(80)
                .toBackendHttpPort(8080)
                .toBackendIPAddress("11.1.1.1")
                .toBackendIPAddress("11.1.1.2")
                .attach()
                .create();

        // Test stop/start
        appGateway.stop();
        Assertions.assertEquals(ApplicationGatewayOperationalState.STOPPED, appGateway.operationalState());
        appGateway.start();
        Assertions.assertEquals(ApplicationGatewayOperationalState.RUNNING, appGateway.operationalState());

        azure.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void testApplicationGatewaysInParallel() throws Exception {
        String rgName = azure.applicationGateways().manager().sdkContext().randomResourceName("rg", 13);
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> resourceGroup = azure.resourceGroups().define(rgName).withRegion(region);
        List<Creatable<ApplicationGateway>> agCreatables = new ArrayList<>();

        agCreatables
            .add(
                azure
                    .applicationGateways()
                    .define(azure.applicationGateways().manager().sdkContext().randomResourceName("ag", 13))
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(resourceGroup)
                    .defineRequestRoutingRule("rule1")
                    .fromPrivateFrontend()
                    .fromFrontendHttpPort(80)
                    .toBackendHttpPort(8080)
                    .toBackendIPAddress("10.0.0.1")
                    .toBackendIPAddress("10.0.0.2")
                    .attach());

        agCreatables
            .add(
                azure
                    .applicationGateways()
                    .define(azure.applicationGateways().manager().sdkContext().randomResourceName("ag", 13))
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(resourceGroup)
                    .defineRequestRoutingRule("rule1")
                    .fromPrivateFrontend()
                    .fromFrontendHttpPort(80)
                    .toBackendHttpPort(8080)
                    .toBackendIPAddress("10.0.0.3")
                    .toBackendIPAddress("10.0.0.4")
                    .attach());

        CreatedResources<ApplicationGateway> created = azure.applicationGateways().create(agCreatables);
        List<ApplicationGateway> ags = new ArrayList<>();
        List<String> agIds = new ArrayList<>();
        for (Creatable<ApplicationGateway> creatable : agCreatables) {
            ApplicationGateway ag = created.get(creatable.key());
            Assertions.assertNotNull(ag);
            ags.add(ag);
            agIds.add(ag.id());
        }

        azure.applicationGateways().stop(agIds);

        for (ApplicationGateway ag : ags) {
            Assertions.assertEquals(ApplicationGatewayOperationalState.STOPPED, ag.refresh().operationalState());
        }

        azure.applicationGateways().start(agIds);

        for (ApplicationGateway ag : ags) {
            Assertions.assertEquals(ApplicationGatewayOperationalState.RUNNING, ag.refresh().operationalState());
        }

        azure.applicationGateways().deleteByIds(agIds);
        for (String id : agIds) {
            try {
                ApplicationGateway ag = azure.applicationGateways().getById(id);
                Assertions.assertNull(ag);
            } catch (ManagementException e) {
                Assertions.assertEquals(404, e.getResponse().getStatusCode());
            }
        }

        azure.resourceGroups().beginDeleteByName(rgName);
    }

    /**
     * Tests a minimal Internet-facing application gateway.
     *
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternetFacingMinimal() throws Exception {
        new TestApplicationGateway().new PublicMinimal(azure.sdkContext())
            .runTest(azure.applicationGateways(), azure.resourceGroups());
    }

    /**
     * Tests a complex Internet-facing application gateway.
     *
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternetFacingComplex() throws Exception {
        new TestApplicationGateway().new PublicComplex(azure.sdkContext())
            .runTest(azure.applicationGateways(), azure.resourceGroups());
    }
}
