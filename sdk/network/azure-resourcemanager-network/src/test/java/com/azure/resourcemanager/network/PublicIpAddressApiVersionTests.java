// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.resourcemanager.network.implementation.NetworkManagementClientBuilder;
import com.azure.resourcemanager.network.implementation.NetworkManagementClientImpl;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class PublicIpAddressApiVersionTests {
    private static final String VMSS_IP_CONFIGURATION_PATH
        = "/providers/Microsoft.Compute/virtualMachineScaleSets/vmss/virtualMachines/0/networkInterfaces/nic/"
            + "ipconfigurations/ipconfig/publicipaddresses";

    @Test
    public void getVirtualMachineScaleSetPublicIpAddressUsesComputeApiVersion() {
        NetworkManagementClientImpl client = createClient("2018-10-01", VMSS_IP_CONFIGURATION_PATH + "/pip");

        Assertions.assertNotNull(client.getPublicIpAddresses()
            .getVirtualMachineScaleSetPublicIpAddressWithResponseAsync("rg", "vmss", "0", "nic", "ipconfig", "pip",
                null)
            .block());
        Assertions.assertNotNull(client.getPublicIpAddresses()
            .getVirtualMachineScaleSetPublicIpAddressWithResponse("rg", "vmss", "0", "nic", "ipconfig", "pip", null,
                Context.NONE));
    }

    @Test
    public void listVirtualMachineScaleSetPublicIpAddressesUsesComputeApiVersion() {
        NetworkManagementClientImpl client
            = createClient("2018-10-01", "/providers/Microsoft.Compute/virtualMachineScaleSets/vmss/publicipaddresses");

        Assertions.assertNotNull(client.getPublicIpAddresses()
            .listVirtualMachineScaleSetPublicIpAddressesAsync("rg", "vmss")
            .byPage()
            .blockFirst());
        Assertions.assertNotNull(client.getPublicIpAddresses()
            .listVirtualMachineScaleSetPublicIpAddresses("rg", "vmss", Context.NONE)
            .iterableByPage()
            .iterator()
            .next());
    }

    @Test
    public void listVirtualMachineScaleSetVMPublicIpAddressesUsesComputeApiVersion() {
        NetworkManagementClientImpl client = createClient("2018-10-01", VMSS_IP_CONFIGURATION_PATH);

        Assertions.assertNotNull(client.getPublicIpAddresses()
            .listVirtualMachineScaleSetVMPublicIpAddressesAsync("rg", "vmss", "0", "nic", "ipconfig")
            .byPage()
            .blockFirst());
        Assertions.assertNotNull(client.getPublicIpAddresses()
            .listVirtualMachineScaleSetVMPublicIpAddresses("rg", "vmss", "0", "nic", "ipconfig", Context.NONE)
            .iterableByPage()
            .iterator()
            .next());
    }

    @Test
    public void getPublicIpAddressUsesNetworkApiVersion() {
        NetworkManagementClientImpl client
            = createClient("2026-01-01", "/providers/Microsoft.Network/publicIPAddresses/pip");

        Assertions.assertNotNull(
            client.getPublicIpAddresses().getByResourceGroupWithResponseAsync("rg", "pip", null).block());
        Assertions.assertNotNull(
            client.getPublicIpAddresses().getByResourceGroupWithResponse("rg", "pip", null, Context.NONE));
    }

    private static NetworkManagementClientImpl createClient(String apiVersion, String path) {
        return new NetworkManagementClientBuilder().subscriptionId("00000000-0000-0000-0000-000000000000")
            .pipeline(new HttpPipelineBuilder().httpClient(request -> {
                Assertions.assertEquals("api-version=" + apiVersion, request.getUrl().getQuery());
                Assertions.assertTrue(request.getUrl().getPath().endsWith(path), request.getUrl().getPath());
                String body = path.endsWith("/pip") ? "{}" : "{\"value\":[]}";
                return Mono.just(new MockHttpResponse(request, 200,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                    body.getBytes(StandardCharsets.UTF_8)));
            }).build())
            .buildClient();
    }
}
