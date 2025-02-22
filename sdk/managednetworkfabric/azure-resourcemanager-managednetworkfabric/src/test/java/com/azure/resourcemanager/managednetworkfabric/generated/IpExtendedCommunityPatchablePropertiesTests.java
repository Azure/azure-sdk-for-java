// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.managednetworkfabric.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.managednetworkfabric.models.CommunityActionTypes;
import com.azure.resourcemanager.managednetworkfabric.models.IpExtendedCommunityPatchableProperties;
import com.azure.resourcemanager.managednetworkfabric.models.IpExtendedCommunityRule;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class IpExtendedCommunityPatchablePropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        IpExtendedCommunityPatchableProperties model = BinaryData.fromString(
            "{\"ipExtendedCommunityRules\":[{\"action\":\"Deny\",\"sequenceNumber\":3069608234026742101,\"routeTargets\":[\"k\",\"ctwwgzw\",\"jlmec\"]},{\"action\":\"Permit\",\"sequenceNumber\":6971905808635311445,\"routeTargets\":[\"zyvneezaifghtmo\",\"qtlffhzbk\"]}]}")
            .toObject(IpExtendedCommunityPatchableProperties.class);
        Assertions.assertEquals(CommunityActionTypes.DENY, model.ipExtendedCommunityRules().get(0).action());
        Assertions.assertEquals(3069608234026742101L, model.ipExtendedCommunityRules().get(0).sequenceNumber());
        Assertions.assertEquals("k", model.ipExtendedCommunityRules().get(0).routeTargets().get(0));
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        IpExtendedCommunityPatchableProperties model
            = new IpExtendedCommunityPatchableProperties().withIpExtendedCommunityRules(Arrays.asList(
                new IpExtendedCommunityRule().withAction(CommunityActionTypes.DENY)
                    .withSequenceNumber(3069608234026742101L)
                    .withRouteTargets(Arrays.asList("k", "ctwwgzw", "jlmec")),
                new IpExtendedCommunityRule().withAction(CommunityActionTypes.PERMIT)
                    .withSequenceNumber(6971905808635311445L)
                    .withRouteTargets(Arrays.asList("zyvneezaifghtmo", "qtlffhzbk"))));
        model = BinaryData.fromObject(model).toObject(IpExtendedCommunityPatchableProperties.class);
        Assertions.assertEquals(CommunityActionTypes.DENY, model.ipExtendedCommunityRules().get(0).action());
        Assertions.assertEquals(3069608234026742101L, model.ipExtendedCommunityRules().get(0).sequenceNumber());
        Assertions.assertEquals("k", model.ipExtendedCommunityRules().get(0).routeTargets().get(0));
    }
}
