// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.servicefabricmanagedclusters.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ClusterHealthPolicy;
import org.junit.jupiter.api.Assertions;

public final class ClusterHealthPolicyTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ClusterHealthPolicy model = BinaryData
            .fromString("{\"maxPercentUnhealthyNodes\":322973840,\"maxPercentUnhealthyApplications\":552523145}")
            .toObject(ClusterHealthPolicy.class);
        Assertions.assertEquals(322973840, model.maxPercentUnhealthyNodes());
        Assertions.assertEquals(552523145, model.maxPercentUnhealthyApplications());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        ClusterHealthPolicy model = new ClusterHealthPolicy().withMaxPercentUnhealthyNodes(322973840)
            .withMaxPercentUnhealthyApplications(552523145);
        model = BinaryData.fromObject(model).toObject(ClusterHealthPolicy.class);
        Assertions.assertEquals(322973840, model.maxPercentUnhealthyNodes());
        Assertions.assertEquals(552523145, model.maxPercentUnhealthyApplications());
    }
}
