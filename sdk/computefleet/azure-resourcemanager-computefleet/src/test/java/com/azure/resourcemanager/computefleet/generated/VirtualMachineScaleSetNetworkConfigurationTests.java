// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.computefleet.generated;

import com.azure.core.management.SubResource;
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.computefleet.models.ApiEntityReference;
import com.azure.resourcemanager.computefleet.models.DeleteOptions;
import com.azure.resourcemanager.computefleet.models.IPVersion;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliaryMode;
import com.azure.resourcemanager.computefleet.models.NetworkInterfaceAuxiliarySku;
import com.azure.resourcemanager.computefleet.models.PublicIPAddressSku;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIPConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIPConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfigurationDnsSettings;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetPublicIPAddressConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetPublicIPAddressConfigurationProperties;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class VirtualMachineScaleSetNetworkConfigurationTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        VirtualMachineScaleSetNetworkConfiguration model = BinaryData.fromString(
            "{\"name\":\"dystkiiuxhqyud\",\"properties\":{\"primary\":false,\"enableAcceleratedNetworking\":true,\"disableTcpStateTracking\":true,\"enableFpga\":false,\"networkSecurityGroup\":{\"id\":\"yifqrvkdvjsllrmv\"},\"dnsSettings\":{\"dnsServers\":[\"atkpnp\",\"lexxbczwtru\",\"iqzbq\",\"vsovmyokac\"]},\"ipConfigurations\":[{\"name\":\"kwlhzdo\",\"properties\":{\"subnet\":{\"id\":\"mflbv\"},\"primary\":true,\"publicIPAddressConfiguration\":{\"name\":\"rkcciwwzjuqk\",\"properties\":{},\"sku\":{}},\"privateIPAddressVersion\":\"IPv6\",\"applicationGatewayBackendAddressPools\":[{},{},{},{}],\"applicationSecurityGroups\":[{}],\"loadBalancerBackendAddressPools\":[{},{},{},{}],\"loadBalancerInboundNatPools\":[{},{},{}]}},{\"name\":\"kg\",\"properties\":{\"subnet\":{\"id\":\"uimjmvx\"},\"primary\":true,\"publicIPAddressConfiguration\":{\"name\":\"ugidyjrr\",\"properties\":{},\"sku\":{}},\"privateIPAddressVersion\":\"IPv6\",\"applicationGatewayBackendAddressPools\":[{}],\"applicationSecurityGroups\":[{}],\"loadBalancerBackendAddressPools\":[{},{},{}],\"loadBalancerInboundNatPools\":[{}]}}],\"enableIPForwarding\":true,\"deleteOption\":\"Detach\",\"auxiliaryMode\":\"None\",\"auxiliarySku\":\"A2\"}}")
            .toObject(VirtualMachineScaleSetNetworkConfiguration.class);
        Assertions.assertEquals("dystkiiuxhqyud", model.name());
        Assertions.assertEquals(false, model.properties().primary());
        Assertions.assertEquals(true, model.properties().enableAcceleratedNetworking());
        Assertions.assertEquals(true, model.properties().disableTcpStateTracking());
        Assertions.assertEquals(false, model.properties().enableFpga());
        Assertions.assertEquals("yifqrvkdvjsllrmv", model.properties().networkSecurityGroup().id());
        Assertions.assertEquals("atkpnp", model.properties().dnsSettings().dnsServers().get(0));
        Assertions.assertEquals("kwlhzdo", model.properties().ipConfigurations().get(0).name());
        Assertions.assertEquals("mflbv", model.properties().ipConfigurations().get(0).properties().subnet().id());
        Assertions.assertEquals(true, model.properties().ipConfigurations().get(0).properties().primary());
        Assertions.assertEquals("rkcciwwzjuqk",
            model.properties().ipConfigurations().get(0).properties().publicIPAddressConfiguration().name());
        Assertions.assertEquals(IPVersion.IPV6,
            model.properties().ipConfigurations().get(0).properties().privateIPAddressVersion());
        Assertions.assertEquals(true, model.properties().enableIPForwarding());
        Assertions.assertEquals(DeleteOptions.DETACH, model.properties().deleteOption());
        Assertions.assertEquals(NetworkInterfaceAuxiliaryMode.NONE, model.properties().auxiliaryMode());
        Assertions.assertEquals(NetworkInterfaceAuxiliarySku.A2, model.properties().auxiliarySku());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        VirtualMachineScaleSetNetworkConfiguration model
            = new VirtualMachineScaleSetNetworkConfiguration().withName("dystkiiuxhqyud")
                .withProperties(new VirtualMachineScaleSetNetworkConfigurationProperties().withPrimary(false)
                    .withEnableAcceleratedNetworking(true)
                    .withDisableTcpStateTracking(true)
                    .withEnableFpga(false)
                    .withNetworkSecurityGroup(new SubResource().withId("yifqrvkdvjsllrmv"))
                    .withDnsSettings(new VirtualMachineScaleSetNetworkConfigurationDnsSettings()
                        .withDnsServers(Arrays.asList("atkpnp", "lexxbczwtru", "iqzbq", "vsovmyokac")))
                    .withIpConfigurations(Arrays.asList(
                        new VirtualMachineScaleSetIPConfiguration().withName("kwlhzdo")
                            .withProperties(new VirtualMachineScaleSetIPConfigurationProperties()
                                .withSubnet(new ApiEntityReference().withId("mflbv"))
                                .withPrimary(true)
                                .withPublicIPAddressConfiguration(
                                    new VirtualMachineScaleSetPublicIPAddressConfiguration().withName("rkcciwwzjuqk")
                                        .withProperties(
                                            new VirtualMachineScaleSetPublicIPAddressConfigurationProperties())
                                        .withSku(new PublicIPAddressSku()))
                                .withPrivateIPAddressVersion(IPVersion.IPV6)
                                .withApplicationGatewayBackendAddressPools(Arrays.asList(new SubResource(),
                                    new SubResource(), new SubResource(), new SubResource()))
                                .withApplicationSecurityGroups(Arrays.asList(new SubResource()))
                                .withLoadBalancerBackendAddressPools(Arrays
                                    .asList(new SubResource(), new SubResource(), new SubResource(), new SubResource()))
                                .withLoadBalancerInboundNatPools(
                                    Arrays.asList(new SubResource(), new SubResource(), new SubResource()))),
                        new VirtualMachineScaleSetIPConfiguration().withName("kg")
                            .withProperties(new VirtualMachineScaleSetIPConfigurationProperties()
                                .withSubnet(new ApiEntityReference().withId("uimjmvx"))
                                .withPrimary(true)
                                .withPublicIPAddressConfiguration(
                                    new VirtualMachineScaleSetPublicIPAddressConfiguration().withName("ugidyjrr")
                                        .withProperties(
                                            new VirtualMachineScaleSetPublicIPAddressConfigurationProperties())
                                        .withSku(new PublicIPAddressSku()))
                                .withPrivateIPAddressVersion(IPVersion.IPV6)
                                .withApplicationGatewayBackendAddressPools(Arrays.asList(new SubResource()))
                                .withApplicationSecurityGroups(Arrays.asList(new SubResource()))
                                .withLoadBalancerBackendAddressPools(
                                    Arrays.asList(new SubResource(), new SubResource(), new SubResource()))
                                .withLoadBalancerInboundNatPools(Arrays.asList(new SubResource())))))
                    .withEnableIPForwarding(true)
                    .withDeleteOption(DeleteOptions.DETACH)
                    .withAuxiliaryMode(NetworkInterfaceAuxiliaryMode.NONE)
                    .withAuxiliarySku(NetworkInterfaceAuxiliarySku.A2));
        model = BinaryData.fromObject(model).toObject(VirtualMachineScaleSetNetworkConfiguration.class);
        Assertions.assertEquals("dystkiiuxhqyud", model.name());
        Assertions.assertEquals(false, model.properties().primary());
        Assertions.assertEquals(true, model.properties().enableAcceleratedNetworking());
        Assertions.assertEquals(true, model.properties().disableTcpStateTracking());
        Assertions.assertEquals(false, model.properties().enableFpga());
        Assertions.assertEquals("yifqrvkdvjsllrmv", model.properties().networkSecurityGroup().id());
        Assertions.assertEquals("atkpnp", model.properties().dnsSettings().dnsServers().get(0));
        Assertions.assertEquals("kwlhzdo", model.properties().ipConfigurations().get(0).name());
        Assertions.assertEquals("mflbv", model.properties().ipConfigurations().get(0).properties().subnet().id());
        Assertions.assertEquals(true, model.properties().ipConfigurations().get(0).properties().primary());
        Assertions.assertEquals("rkcciwwzjuqk",
            model.properties().ipConfigurations().get(0).properties().publicIPAddressConfiguration().name());
        Assertions.assertEquals(IPVersion.IPV6,
            model.properties().ipConfigurations().get(0).properties().privateIPAddressVersion());
        Assertions.assertEquals(true, model.properties().enableIPForwarding());
        Assertions.assertEquals(DeleteOptions.DETACH, model.properties().deleteOption());
        Assertions.assertEquals(NetworkInterfaceAuxiliaryMode.NONE, model.properties().auxiliaryMode());
        Assertions.assertEquals(NetworkInterfaceAuxiliarySku.A2, model.properties().auxiliarySku());
    }
}
