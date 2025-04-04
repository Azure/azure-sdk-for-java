// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.kusto.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.kusto.fluent.models.OutboundNetworkDependenciesEndpointProperties;
import com.azure.resourcemanager.kusto.models.EndpointDependency;
import com.azure.resourcemanager.kusto.models.EndpointDetail;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class OutboundNetworkDependenciesEndpointPropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        OutboundNetworkDependenciesEndpointProperties model = BinaryData.fromString(
            "{\"category\":\"joqrvqqaatj\",\"endpoints\":[{\"domainName\":\"goupmfiibfg\",\"endpointDetails\":[{\"port\":1249202476,\"ipAddress\":\"vrwxkv\"},{\"port\":46928565,\"ipAddress\":\"llqwjygvjayvblmh\"},{\"port\":1458242132,\"ipAddress\":\"hbxvvyhgsopbyrqu\"},{\"port\":175567556,\"ipAddress\":\"uvwzfbnh\"}]}],\"provisioningState\":\"Canceled\"}")
            .toObject(OutboundNetworkDependenciesEndpointProperties.class);
        Assertions.assertEquals("joqrvqqaatj", model.category());
        Assertions.assertEquals("goupmfiibfg", model.endpoints().get(0).domainName());
        Assertions.assertEquals(1249202476, model.endpoints().get(0).endpointDetails().get(0).port());
        Assertions.assertEquals("vrwxkv", model.endpoints().get(0).endpointDetails().get(0).ipAddress());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        OutboundNetworkDependenciesEndpointProperties model = new OutboundNetworkDependenciesEndpointProperties()
            .withCategory("joqrvqqaatj")
            .withEndpoints(Arrays.asList(new EndpointDependency().withDomainName("goupmfiibfg")
                .withEndpointDetails(Arrays.asList(new EndpointDetail().withPort(1249202476).withIpAddress("vrwxkv"),
                    new EndpointDetail().withPort(46928565).withIpAddress("llqwjygvjayvblmh"),
                    new EndpointDetail().withPort(1458242132).withIpAddress("hbxvvyhgsopbyrqu"),
                    new EndpointDetail().withPort(175567556).withIpAddress("uvwzfbnh")))));
        model = BinaryData.fromObject(model).toObject(OutboundNetworkDependenciesEndpointProperties.class);
        Assertions.assertEquals("joqrvqqaatj", model.category());
        Assertions.assertEquals("goupmfiibfg", model.endpoints().get(0).domainName());
        Assertions.assertEquals(1249202476, model.endpoints().get(0).endpointDetails().get(0).port());
        Assertions.assertEquals("vrwxkv", model.endpoints().get(0).endpointDetails().get(0).ipAddress());
    }
}
