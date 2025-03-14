// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.cognitiveservices.fluent.models.RaiContentFilterInner;
import com.azure.resourcemanager.cognitiveservices.models.RaiContentFilterListResult;
import com.azure.resourcemanager.cognitiveservices.models.RaiContentFilterProperties;
import com.azure.resourcemanager.cognitiveservices.models.RaiPolicyContentSource;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class RaiContentFilterListResultTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        RaiContentFilterListResult model = BinaryData.fromString(
            "{\"nextLink\":\"pglydz\",\"value\":[{\"properties\":{\"name\":\"eevt\",\"isMultiLevelFilter\":false,\"source\":\"Completion\"},\"id\":\"utnwytpzdmovzvf\",\"name\":\"aawzqadfl\",\"type\":\"z\"},{\"properties\":{\"name\":\"glae\",\"isMultiLevelFilter\":false,\"source\":\"Prompt\"},\"id\":\"icokpv\",\"name\":\"mlqtmldgxob\",\"type\":\"irclnpk\"},{\"properties\":{\"name\":\"yzriykhy\",\"isMultiLevelFilter\":true,\"source\":\"Prompt\"},\"id\":\"lboxqvkjl\",\"name\":\"xhom\",\"type\":\"ynhdwdigum\"},{\"properties\":{\"name\":\"aauzzptjazysd\",\"isMultiLevelFilter\":false,\"source\":\"Completion\"},\"id\":\"wva\",\"name\":\"qyuvvfonkp\",\"type\":\"hqyikvy\"}]}")
            .toObject(RaiContentFilterListResult.class);
        Assertions.assertEquals("pglydz", model.nextLink());
        Assertions.assertEquals("eevt", model.value().get(0).properties().name());
        Assertions.assertEquals(false, model.value().get(0).properties().isMultiLevelFilter());
        Assertions.assertEquals(RaiPolicyContentSource.COMPLETION, model.value().get(0).properties().source());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        RaiContentFilterListResult model = new RaiContentFilterListResult().withNextLink("pglydz")
            .withValue(Arrays.asList(
                new RaiContentFilterInner().withProperties(new RaiContentFilterProperties().withName("eevt")
                    .withIsMultiLevelFilter(false)
                    .withSource(RaiPolicyContentSource.COMPLETION)),
                new RaiContentFilterInner().withProperties(new RaiContentFilterProperties().withName("glae")
                    .withIsMultiLevelFilter(false)
                    .withSource(RaiPolicyContentSource.PROMPT)),
                new RaiContentFilterInner().withProperties(new RaiContentFilterProperties().withName("yzriykhy")
                    .withIsMultiLevelFilter(true)
                    .withSource(RaiPolicyContentSource.PROMPT)),
                new RaiContentFilterInner().withProperties(new RaiContentFilterProperties().withName("aauzzptjazysd")
                    .withIsMultiLevelFilter(false)
                    .withSource(RaiPolicyContentSource.COMPLETION))));
        model = BinaryData.fromObject(model).toObject(RaiContentFilterListResult.class);
        Assertions.assertEquals("pglydz", model.nextLink());
        Assertions.assertEquals("eevt", model.value().get(0).properties().name());
        Assertions.assertEquals(false, model.value().get(0).properties().isMultiLevelFilter());
        Assertions.assertEquals(RaiPolicyContentSource.COMPLETION, model.value().get(0).properties().source());
    }
}
