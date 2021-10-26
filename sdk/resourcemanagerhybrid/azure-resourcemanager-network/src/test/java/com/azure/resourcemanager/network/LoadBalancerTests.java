// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerHttpProbe;
import com.azure.resourcemanager.network.models.LoadBalancerOutboundRule;
import com.azure.resourcemanager.network.models.LoadBalancerOutboundRuleProtocol;
import com.azure.resourcemanager.network.models.LoadBalancerPublicFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.ProbeProtocol;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoadBalancerTests extends NetworkManagementTest {
    static final String SUBNET_NAME = "subnet1";
    static final String RULE_NAME_1 = "httpRule";
    static final String RULE_NAME_2 = "httpsRule";
    static final String PROBE_NAME_1 = "httpProbe";
    static final String PROBE_NAME_2 = "httpsProbe";

    @Test
    public void canCRUDProbe() throws Exception {
        String vmName = generateRandomResourceName("vm", 8);
        String lbName = generateRandomResourceName("lb", 8);

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

        LoadBalancer loadBalancer = createLoadBalancerWithPrivateFrontend(networkManager, resourceGroup, network, lbName);

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

    @Test
    public void canCreateOutboundRule() {
        String lbName = generateRandomResourceName("lb", 8);

        String frontendName1 = lbName + "-FE1";
        String frontendName2 = lbName + "-FE2";
        String backendPoolName = lbName + "-BAP1";
        String publicIpName1 = generateRandomResourceName("pip", 15);
        String publicIpName2 = generateRandomResourceName("pip", 15);
        String outboundRuleName = lbName + "-OutboundRule1";

        ResourceGroup resourceGroup =
            resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();

        LoadBalancer loadBalancer = createLoadBalancerWithPublicFrontendAndOutboundRule(networkManager, resourceGroup, lbName, frontendName1, frontendName2, backendPoolName, publicIpName1, publicIpName2, outboundRuleName);

        // assertions for loadbalancer properties
        Assertions.assertEquals(lbName, loadBalancer.name());
        Assertions.assertEquals(1, loadBalancer.loadBalancingRules().size());
        Assertions.assertEquals(1, loadBalancer.httpProbes().size());

        List<LoadBalancerFrontend> frontends = new ArrayList<>(loadBalancer.frontends().values());
        Assertions.assertEquals(2, frontends.size());
        Assertions.assertEquals(frontendName1, frontends.get(0).name());
        Assertions.assertEquals(0, frontends.get(0).outboundRules().size());
        Assertions.assertEquals(true, frontends.get(0).isPublic());
        LoadBalancerPublicFrontend publicFrontend1 = (LoadBalancerPublicFrontend) frontends.get(0);
        Assertions.assertNotNull(publicFrontend1.getPublicIpAddress());
        Assertions.assertEquals(publicIpName1, publicFrontend1.getPublicIpAddress().name());

        Assertions.assertEquals(frontendName2, frontends.get(1).name());
        Assertions.assertEquals(1, frontends.get(1).outboundRules().size());
        Assertions.assertTrue(frontends.get(1).outboundRules().containsKey(outboundRuleName));
        Assertions.assertEquals(true, frontends.get(1).isPublic());
        LoadBalancerPublicFrontend publicFrontend2 = (LoadBalancerPublicFrontend) frontends.get(1);
        Assertions.assertNotNull(publicFrontend2.getPublicIpAddress());
        Assertions.assertEquals(publicIpName2, publicFrontend2.getPublicIpAddress().name());

        Assertions.assertEquals(1, loadBalancer.backends().size());
        Assertions.assertTrue(loadBalancer.backends().containsKey(backendPoolName));

        // assertions for outbound rule
        Map<String, LoadBalancerOutboundRule> outboundRules = loadBalancer.outboundRules();
        Assertions.assertEquals(1, outboundRules.size());
        Assertions.assertTrue(outboundRules.containsKey(outboundRuleName));
        LoadBalancerOutboundRule outboundRule = outboundRules.get(outboundRuleName);
        Assertions.assertEquals(outboundRuleName, outboundRule.name());
        Assertions.assertEquals(1024, outboundRule.allocatedOutboundPorts());
        Assertions.assertEquals(backendPoolName, outboundRule.backend().name());
        Assertions.assertEquals(5, outboundRule.idleTimeoutInMinutes());
        Assertions.assertEquals(false, outboundRule.tcpResetEnabled());

        List<LoadBalancerFrontend> outboundRuleFrontends = new ArrayList<>(outboundRule.frontends().values());
        Assertions.assertEquals(1, outboundRuleFrontends.size());
        Assertions.assertEquals(frontendName2, outboundRuleFrontends.get(0).name());
        Assertions.assertEquals(true, outboundRuleFrontends.get(0).isPublic());
        LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) outboundRuleFrontends.get(0);
        Assertions.assertNotNull(publicFrontend.getPublicIpAddress());
        Assertions.assertEquals(publicIpName2, publicFrontend2.getPublicIpAddress().name());

    }

    @Test
    public void canUpdateOutboundRule() {
        String lbName = generateRandomResourceName("lb", 8);

        String frontendName1 = lbName + "-FE1";
        String frontendName2 = lbName + "-FE2";
        String backendPoolName = lbName + "-BAP1";
        String publicIpName1 = generateRandomResourceName("pip", 15);
        String publicIpName2 = generateRandomResourceName("pip", 15);
        String outboundRuleName1 = lbName + "-OutboundRule1";
        String outboundRuleName2 = lbName + "-OutboundRule2";

        ResourceGroup resourceGroup =
            resourceManager.resourceGroups().define(rgName).withRegion(Region.US_EAST).create();



        LoadBalancer loadBalancer = createLoadBalancerWithPublicFrontendAndOutboundRule(networkManager, resourceGroup, lbName, frontendName1, frontendName2, backendPoolName, publicIpName1, publicIpName2, outboundRuleName1);
        // 1. update loadbalancer, update outbound rule
        loadBalancer
            .update()
            .updateOutboundRule(outboundRuleName1)
            .withIdleTimeoutInMinutes(50)
            .parent()
            .apply();

        Map<String, LoadBalancerOutboundRule> outboundRules = loadBalancer.outboundRules();
        Assertions.assertEquals(1, outboundRules.size());
        Assertions.assertTrue(outboundRules.containsKey(outboundRuleName1));
        LoadBalancerOutboundRule outboundRule = outboundRules.get(outboundRuleName1);
        Assertions.assertEquals(outboundRuleName1, outboundRule.name());
        Assertions.assertEquals(1024, outboundRule.allocatedOutboundPorts());
        Assertions.assertEquals(backendPoolName, outboundRule.backend().name());

        List<LoadBalancerFrontend> outboundRuleFrontends = new ArrayList<>(outboundRule.frontends().values());
        Assertions.assertEquals(1, outboundRuleFrontends.size());
        Assertions.assertEquals(frontendName2, outboundRuleFrontends.get(0).name());
        Assertions.assertEquals(1, outboundRuleFrontends.get(0).outboundRules().size());
        Assertions.assertTrue(outboundRuleFrontends.get(0).outboundRules().containsKey(outboundRuleName1));
        Assertions.assertEquals(true, outboundRuleFrontends.get(0).isPublic());
        LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) outboundRuleFrontends.get(0);
        Assertions.assertNotNull(publicFrontend.getPublicIpAddress());
        Assertions.assertEquals(publicIpName2, publicFrontend.getPublicIpAddress().name());

        Assertions.assertEquals(50, outboundRule.idleTimeoutInMinutes());
        Assertions.assertEquals(false, outboundRule.tcpResetEnabled());

        // 2. update loadbalancer, remove outbound rule
        loadBalancer
            .update()
            .withoutOutboundRule(outboundRuleName1)
            .apply();

        Assertions.assertEquals(0, loadBalancer.outboundRules().size());

        // 3. update loadbalancer, define a new outbound rule
        loadBalancer
            .update()
            .defineOutboundRule(outboundRuleName2)
            .withProtocol(LoadBalancerOutboundRuleProtocol.TCP)
            .fromBackend(backendPoolName)
            .toFrontend(frontendName2)
            .withIdleTimeoutInMinutes(10)
            .attach()
            .apply();

        Map<String, LoadBalancerOutboundRule> outboundRulesOnUpdateDefine = loadBalancer.outboundRules();
        Assertions.assertEquals(1, outboundRulesOnUpdateDefine.size());
        Assertions.assertTrue(outboundRulesOnUpdateDefine.containsKey(outboundRuleName2));
        LoadBalancerOutboundRule outboundRuleOnUpdateDefine = outboundRulesOnUpdateDefine.get(outboundRuleName2);
        Assertions.assertEquals(outboundRuleName2, outboundRuleOnUpdateDefine.name());
        Assertions.assertEquals(1024, outboundRuleOnUpdateDefine.allocatedOutboundPorts());
        Assertions.assertEquals(backendPoolName, outboundRuleOnUpdateDefine.backend().name());

        List<LoadBalancerFrontend> outboundRuleFrontendsOnUpdateDefine = new ArrayList<>(outboundRuleOnUpdateDefine.frontends().values());
        Assertions.assertEquals(1, outboundRuleFrontendsOnUpdateDefine.size());
        Assertions.assertEquals(frontendName2, outboundRuleFrontendsOnUpdateDefine.get(0).name());
        Assertions.assertEquals(1, outboundRuleFrontendsOnUpdateDefine.get(0).outboundRules().size());
        Assertions.assertTrue(outboundRuleFrontendsOnUpdateDefine.get(0).outboundRules().containsKey(outboundRuleName2));
        Assertions.assertEquals(true, outboundRuleFrontendsOnUpdateDefine.get(0).isPublic());
        LoadBalancerPublicFrontend publicFrontendOnUpdateDefine = (LoadBalancerPublicFrontend) outboundRuleFrontendsOnUpdateDefine.get(0);
        Assertions.assertNotNull(publicFrontendOnUpdateDefine.getPublicIpAddress());
        Assertions.assertEquals(publicIpName2, publicFrontendOnUpdateDefine.getPublicIpAddress().name());

        Assertions.assertEquals(10, outboundRuleOnUpdateDefine.idleTimeoutInMinutes());
        Assertions.assertEquals(false, outboundRuleOnUpdateDefine.tcpResetEnabled());

    }

    private static LoadBalancer createLoadBalancerWithPrivateFrontend(
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

    private static LoadBalancer createLoadBalancerWithPublicFrontendAndOutboundRule(
        NetworkManager networkManager, ResourceGroup resourceGroup, String lbName, String frontendName1, String frontendName2, String backendPoolName, String publicIpName1, String publicIpName2, String outboundRuleName) {

        PublicIpAddress pip1 =
            networkManager
                .publicIpAddresses()
                .define(publicIpName1)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(resourceGroup)
                .withSku(PublicIPSkuType.STANDARD)
                .withStaticIP()
                .create();

        PublicIpAddress pip2 =
            networkManager
                .publicIpAddresses()
                .define(publicIpName2)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(resourceGroup)
                .withSku(PublicIPSkuType.STANDARD)
                .withStaticIP()
                .create();

        LoadBalancer loadBalancer1 =
            networkManager
                .loadBalancers()
                .define(lbName)
                .withRegion(resourceGroup.region())
                .withExistingResourceGroup(resourceGroup)
                // Add rule that uses above backend and probe
                .defineLoadBalancingRule(RULE_NAME_1)
                .withProtocol(TransportProtocol.TCP)
                .fromFrontend(frontendName1)
                .fromFrontendPort(80)
                .toBackend(backendPoolName)
                .withProbe(PROBE_NAME_1)
                .attach()
                // Explicitly define the frontend
                .definePublicFrontend(frontendName1)
                .withExistingPublicIpAddress(pip1)
                .attach()
                .definePublicFrontend(frontendName2)
                .withExistingPublicIpAddress(pip2)
                .attach()
                // add outbound rule
                .defineOutboundRule(outboundRuleName)
                .withProtocol(LoadBalancerOutboundRuleProtocol.TCP)
                .fromBackend(backendPoolName)
                .toFrontend(frontendName2)
                .withEnableTcpReset(false)
                .withIdleTimeoutInMinutes(5)
                .attach()
                // Add one probe
                .defineHttpProbe(PROBE_NAME_1)
                .withRequestPath("/")
                .attach()
                .withSku(LoadBalancerSkuType.STANDARD)
                .create();

        return loadBalancer1;
    }
}
