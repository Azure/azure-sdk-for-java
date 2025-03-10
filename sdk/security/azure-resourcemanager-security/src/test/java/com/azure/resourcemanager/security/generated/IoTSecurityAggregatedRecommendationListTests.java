// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.security.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.security.fluent.models.IoTSecurityAggregatedRecommendationInner;
import com.azure.resourcemanager.security.models.IoTSecurityAggregatedRecommendationList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class IoTSecurityAggregatedRecommendationListTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        IoTSecurityAggregatedRecommendationList model = BinaryData.fromString(
            "{\"value\":[{\"properties\":{\"recommendationName\":\"pgylg\",\"recommendationDisplayName\":\"itxmedjvcslynqww\",\"description\":\"wzz\",\"recommendationTypeId\":\"gktrmgucnapkte\",\"detectedBy\":\"llwptfdy\",\"remediationSteps\":\"fqbuaceopzf\",\"reportedSeverity\":\"Low\",\"healthyDevices\":1682904075393771384,\"unhealthyDeviceCount\":390727694338753786,\"logAnalyticsQuery\":\"cq\"},\"tags\":{\"dahzxctobg\":\"ol\",\"postmgrcfbunrm\":\"kdmoi\",\"ymjhxxjyngudivkr\":\"qjhhkxbpv\"},\"id\":\"swbxqz\",\"name\":\"szjfauvjfdxxivet\",\"type\":\"t\"},{\"properties\":{\"recommendationName\":\"qtdo\",\"recommendationDisplayName\":\"cbxvwvxyslqbh\",\"description\":\"xoblytkbl\",\"recommendationTypeId\":\"ewwwfbkrvrnsv\",\"detectedBy\":\"q\",\"remediationSteps\":\"hxcr\",\"reportedSeverity\":\"Low\",\"healthyDevices\":1071359428326279323,\"unhealthyDeviceCount\":740852722562408962,\"logAnalyticsQuery\":\"v\"},\"tags\":{\"ybsrfbjfdtwss\":\"sqfsubcgjbirxb\",\"tpvjzbexilzznfqq\":\"t\",\"taruoujmkcj\":\"vwpm\",\"ervnaenqpehi\":\"wqytjrybnwjewgdr\"},\"id\":\"doy\",\"name\":\"mifthnzdnd\",\"type\":\"l\"}],\"nextLink\":\"ayqigynduhav\"}")
            .toObject(IoTSecurityAggregatedRecommendationList.class);
        Assertions.assertEquals("ol", model.value().get(0).tags().get("dahzxctobg"));
        Assertions.assertEquals("pgylg", model.value().get(0).recommendationName());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        IoTSecurityAggregatedRecommendationList model
            = new IoTSecurityAggregatedRecommendationList()
                .withValue(
                    Arrays
                        .asList(
                            new IoTSecurityAggregatedRecommendationInner()
                                .withTags(mapOf("dahzxctobg", "ol", "postmgrcfbunrm", "kdmoi", "ymjhxxjyngudivkr",
                                    "qjhhkxbpv"))
                                .withRecommendationName("pgylg"),
                            new IoTSecurityAggregatedRecommendationInner()
                                .withTags(mapOf("ybsrfbjfdtwss", "sqfsubcgjbirxb", "tpvjzbexilzznfqq", "t",
                                    "taruoujmkcj", "vwpm", "ervnaenqpehi", "wqytjrybnwjewgdr"))
                                .withRecommendationName("qtdo")));
        model = BinaryData.fromObject(model).toObject(IoTSecurityAggregatedRecommendationList.class);
        Assertions.assertEquals("ol", model.value().get(0).tags().get("dahzxctobg"));
        Assertions.assertEquals("pgylg", model.value().get(0).recommendationName());
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
