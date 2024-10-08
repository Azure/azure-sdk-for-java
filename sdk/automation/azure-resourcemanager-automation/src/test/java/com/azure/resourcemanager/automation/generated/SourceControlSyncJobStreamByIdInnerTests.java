// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.automation.fluent.models.SourceControlSyncJobStreamByIdInner;
import com.azure.resourcemanager.automation.models.StreamType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class SourceControlSyncJobStreamByIdInnerTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SourceControlSyncJobStreamByIdInner model = BinaryData.fromString(
            "{\"id\":\"bahwfl\",\"properties\":{\"sourceControlSyncJobStreamId\":\"dtmhrkwofyyvoqa\",\"summary\":\"iexpbtgiwbwo\",\"time\":\"2021-02-28T13:28:15Z\",\"streamType\":\"Output\",\"streamText\":\"rtdtkcnqxw\",\"value\":{\"qiiobyuqer\":\"datakulpiujwaasi\",\"bdbutauvf\":\"dataqlpqwcciuq\",\"afnn\":\"datatkuwhhmhykojo\"}}}")
            .toObject(SourceControlSyncJobStreamByIdInner.class);
        Assertions.assertEquals("dtmhrkwofyyvoqa", model.sourceControlSyncJobStreamId());
        Assertions.assertEquals("iexpbtgiwbwo", model.summary());
        Assertions.assertEquals(StreamType.OUTPUT, model.streamType());
        Assertions.assertEquals("rtdtkcnqxw", model.streamText());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SourceControlSyncJobStreamByIdInner model = new SourceControlSyncJobStreamByIdInner()
            .withSourceControlSyncJobStreamId("dtmhrkwofyyvoqa")
            .withSummary("iexpbtgiwbwo")
            .withStreamType(StreamType.OUTPUT)
            .withStreamText("rtdtkcnqxw")
            .withValue(
                mapOf("qiiobyuqer", "datakulpiujwaasi", "bdbutauvf", "dataqlpqwcciuq", "afnn", "datatkuwhhmhykojo"));
        model = BinaryData.fromObject(model).toObject(SourceControlSyncJobStreamByIdInner.class);
        Assertions.assertEquals("dtmhrkwofyyvoqa", model.sourceControlSyncJobStreamId());
        Assertions.assertEquals("iexpbtgiwbwo", model.summary());
        Assertions.assertEquals(StreamType.OUTPUT, model.streamType());
        Assertions.assertEquals("rtdtkcnqxw", model.streamText());
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
