// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.avs.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.avs.fluent.models.QuotaInner;

public final class QuotaInnerTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        QuotaInner model = BinaryData
            .fromString("{\"hostsRemaining\":{\"vplwzbhv\":1229662931,\"u\":1239569697},\"quotaEnabled\":\"Enabled\"}")
            .toObject(QuotaInner.class);
    }
}
