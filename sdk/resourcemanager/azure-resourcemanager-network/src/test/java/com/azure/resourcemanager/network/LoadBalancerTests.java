// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network;

import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerHttpProbe;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.ProbeProtocol;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoadBalancerTests extends NetworkManagementTest {
    static final String SUBNET_NAME = "subnet1";
    static final String RULE_NAME_1 = "httpRule";
    static final String RULE_NAME_2 = "httpsRule";
    static final String PROBE_NAME_1 = "httpProbe";
    static final String PROBE_NAME_2 = "httpsProbe";

    @Test
    public void canCRUDProbe() throws Exception {
        String vmName = sdkContext.randomResourceName("vm", 8);
        String lbName = sdkContext.randomResourceName("lb", 8);

        ResourceGroup resourceGroup =
            resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();

        Network network =
            networkManager
                .networks()
                .define(vmName)
                .withRegion(resourceGroup.region())
                .withExistingResourceGroup(resourceGroup.name())
                .withAddressSpace("172.18.0.0/28")
                .withSubnet(SUBNET_NAME, "172.18.0.0/28")
                .create();

        LoadBalancer loadBalancer = createLoadBalancer(networkManager, resourceGroup, network, lbName);

        // verify created probes
        Assertions.assertEquals(2, loadBalancer.loadBalancingRules().size());
        Assertions.assertEquals(0, loadBalancer.tcpProbes().size());
        Assertions.assertEquals(1, loadBalancer.httpProbes().size());
        Assertions.assertEquals(1, loadBalancer.httpsProbes().size());
        LoadBalancerHttpProbe httpProbe = loadBalancer.httpProbes().get(PROBE_NAME_1);
        Assertions.assertNotNull(httpProbe);
        Assertions.assertEquals(1, httpProbe.loadBalancingRules().size());
        LoadBalancerHttpProbe httpsProbe = loadBalancer.httpsProbes().get(PROBE_NAME_2);
        Assertions.assertEquals(1, httpsProbe.loadBalancingRules().size());
        // verify https probe
        Assertions.assertEquals(ProbeProtocol.HTTPS, httpsProbe.protocol());
        Assertions.assertEquals(443, httpsProbe.port());
        Assertions.assertEquals("/", httpsProbe.requestPath());

        // update probe
        loadBalancer
            .update()
            .updateHttpsProbe(PROBE_NAME_2)
            .withIntervalInSeconds(60)
            .withRequestPath("/health")
            .parent()
            .apply();

        // verify probe updated
        Assertions.assertEquals(1, loadBalancer.httpProbes().size());
        Assertions.assertEquals(1, loadBalancer.httpsProbes().size());
        Assertions.assertEquals(1, httpProbe.loadBalancingRules().size());
        httpsProbe = loadBalancer.httpsProbes().get(PROBE_NAME_2);
        Assertions.assertEquals(1, httpsProbe.loadBalancingRules().size());
        Assertions.assertEquals(ProbeProtocol.HTTPS, httpsProbe.protocol());
        Assertions.assertEquals(443, httpsProbe.port());
        Assertions.assertEquals(60, httpsProbe.intervalInSeconds());
        Assertions.assertEquals("/health", httpsProbe.requestPath());

        // delete probe
        loadBalancer.update().withoutProbe(PROBE_NAME_2).apply();

        // verify probe deleted (and deref from rule)
        Assertions.assertEquals(1, loadBalancer.httpProbes().size());
        Assertions.assertEquals(0, loadBalancer.httpsProbes().size());
        Assertions.assertNull(loadBalancer.loadBalancingRules().get(RULE_NAME_2).probe());

        // add probe
        loadBalancer.update().defineHttpsProbe(PROBE_NAME_2).withRequestPath("/").attach().apply();

        // verify probe added
        loadBalancer.refresh();
        Assertions.assertEquals(1, loadBalancer.httpProbes().size());
        Assertions.assertEquals(1, loadBalancer.httpsProbes().size());
        httpsProbe = loadBalancer.httpsProbes().get(PROBE_NAME_2);
        Assertions.assertEquals(0, httpsProbe.loadBalancingRules().size());
    }

    private static LoadBalancer createLoadBalancer(
        NetworkManager networkManager, ResourceGroup resourceGroup, Network network, String lbName) {
        final String frontendName = lbName + "-FE1";
        final String backendPoolName1 = lbName + "-BAP1";
        final String backendPoolName2 = lbName + "-BAP2";
        final String natPool50XXto22 = lbName + "natPool50XXto22";
        final String natPool60XXto23 = lbName + "natPool60XXto23";

        LoadBalancer loadBalancer1 =
            networkManager
                .loadBalancers()
                .define(lbName)
                .withRegion(resourceGroup.region())
                .withExistingResourceGroup(resourceGroup.name())
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule(RULE_NAME_1)
                .withProtocol(TransportProtocol.TCP)
                .fromFrontend(frontendName)
                .fromFrontendPort(80)
                .toBackend(backendPoolName1)
                .withProbe(PROBE_NAME_1)
                .attach()
                .defineLoadBalancingRule(RULE_NAME_2)
                .withProtocol(TransportProtocol.TCP)
                .fromFrontend(frontendName)
                .fromFrontendPort(443)
                .toBackend(backendPoolName2)
                .withProbe(PROBE_NAME_2)
                .attach()
                // Add nat pools to enable direct VM connectivity for
                //  SSH to port 22 and TELNET to port 23
                .defineInboundNatPool(natPool50XXto22)
                .withProtocol(TransportProtocol.TCP)
                .fromFrontend(frontendName)
                .fromFrontendPortRange(5000, 5099)
                .toBackendPort(22)
                .attach()
                .defineInboundNatPool(natPool60XXto23)
                .withProtocol(TransportProtocol.TCP)
                .fromFrontend(frontendName)
                .fromFrontendPortRange(6000, 6099)
                .toBackendPort(23)
                .attach()
                // Explicitly define the frontend
                .definePrivateFrontend(frontendName)
                .withExistingSubnet(network, SUBNET_NAME)
                .attach()
                // Add two probes one per rule
                .defineHttpProbe(PROBE_NAME_1)
                .withRequestPath("/")
                .attach()
                .defineHttpsProbe(PROBE_NAME_2)
                .withRequestPath("/")
                .attach()
                .withSku(LoadBalancerSkuType.STANDARD)
                .create();

        return loadBalancer1;
    }
}
