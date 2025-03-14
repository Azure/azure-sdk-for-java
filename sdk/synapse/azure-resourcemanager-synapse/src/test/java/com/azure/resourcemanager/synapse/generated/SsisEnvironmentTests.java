// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.synapse.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.synapse.models.SsisEnvironment;
import com.azure.resourcemanager.synapse.models.SsisVariable;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class SsisEnvironmentTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SsisEnvironment model = BinaryData.fromString(
            "{\"type\":\"Environment\",\"folderId\":7640942206625706999,\"variables\":[{\"id\":8945709107897326494,\"name\":\"zozk\",\"description\":\"wnf\",\"dataType\":\"hhhqosm\",\"sensitive\":true,\"value\":\"utycyarnroohguab\",\"sensitiveValue\":\"ghktdpy\"},{\"id\":1480571121331214079,\"name\":\"eocnhzqrottj\",\"description\":\"fyjzptwr\",\"dataType\":\"h\",\"sensitive\":true,\"value\":\"nfszpyglqdhmrjz\",\"sensitiveValue\":\"l\"},{\"id\":4160849224608918918,\"name\":\"yypsjoqc\",\"description\":\"nkyhf\",\"dataType\":\"vsqxfxjelgcmpzqj\",\"sensitive\":false,\"value\":\"xuwyvc\",\"sensitiveValue\":\"oyvivbsiz\"},{\"id\":4716726373168082286,\"name\":\"lbscmnlz\",\"description\":\"iufehgmvf\",\"dataType\":\"wyvq\",\"sensitive\":false,\"value\":\"rlniyl\",\"sensitiveValue\":\"yfw\"}],\"id\":9207125625982168561,\"name\":\"qztw\",\"description\":\"hmupgxyjtcdxabbu\"}")
            .toObject(SsisEnvironment.class);
        Assertions.assertEquals(9207125625982168561L, model.id());
        Assertions.assertEquals("qztw", model.name());
        Assertions.assertEquals("hmupgxyjtcdxabbu", model.description());
        Assertions.assertEquals(7640942206625706999L, model.folderId());
        Assertions.assertEquals(8945709107897326494L, model.variables().get(0).id());
        Assertions.assertEquals("zozk", model.variables().get(0).name());
        Assertions.assertEquals("wnf", model.variables().get(0).description());
        Assertions.assertEquals("hhhqosm", model.variables().get(0).dataType());
        Assertions.assertEquals(true, model.variables().get(0).sensitive());
        Assertions.assertEquals("utycyarnroohguab", model.variables().get(0).value());
        Assertions.assertEquals("ghktdpy", model.variables().get(0).sensitiveValue());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SsisEnvironment model = new SsisEnvironment().withId(9207125625982168561L)
            .withName("qztw")
            .withDescription("hmupgxyjtcdxabbu")
            .withFolderId(7640942206625706999L)
            .withVariables(Arrays.asList(
                new SsisVariable().withId(8945709107897326494L)
                    .withName("zozk")
                    .withDescription("wnf")
                    .withDataType("hhhqosm")
                    .withSensitive(true)
                    .withValue("utycyarnroohguab")
                    .withSensitiveValue("ghktdpy"),
                new SsisVariable().withId(1480571121331214079L)
                    .withName("eocnhzqrottj")
                    .withDescription("fyjzptwr")
                    .withDataType("h")
                    .withSensitive(true)
                    .withValue("nfszpyglqdhmrjz")
                    .withSensitiveValue("l"),
                new SsisVariable().withId(4160849224608918918L)
                    .withName("yypsjoqc")
                    .withDescription("nkyhf")
                    .withDataType("vsqxfxjelgcmpzqj")
                    .withSensitive(false)
                    .withValue("xuwyvc")
                    .withSensitiveValue("oyvivbsiz"),
                new SsisVariable().withId(4716726373168082286L)
                    .withName("lbscmnlz")
                    .withDescription("iufehgmvf")
                    .withDataType("wyvq")
                    .withSensitive(false)
                    .withValue("rlniyl")
                    .withSensitiveValue("yfw")));
        model = BinaryData.fromObject(model).toObject(SsisEnvironment.class);
        Assertions.assertEquals(9207125625982168561L, model.id());
        Assertions.assertEquals("qztw", model.name());
        Assertions.assertEquals("hmupgxyjtcdxabbu", model.description());
        Assertions.assertEquals(7640942206625706999L, model.folderId());
        Assertions.assertEquals(8945709107897326494L, model.variables().get(0).id());
        Assertions.assertEquals("zozk", model.variables().get(0).name());
        Assertions.assertEquals("wnf", model.variables().get(0).description());
        Assertions.assertEquals("hhhqosm", model.variables().get(0).dataType());
        Assertions.assertEquals(true, model.variables().get(0).sensitive());
        Assertions.assertEquals("utycyarnroohguab", model.variables().get(0).value());
        Assertions.assertEquals("ghktdpy", model.variables().get(0).sensitiveValue());
    }
}
