// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.elastic.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.elastic.models.FilteringTag;
import com.azure.resourcemanager.elastic.models.LogRules;
import com.azure.resourcemanager.elastic.models.TagAction;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class LogRulesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        LogRules model = BinaryData.fromString(
            "{\"sendAadLogs\":true,\"sendSubscriptionLogs\":false,\"sendActivityLogs\":true,\"filteringTags\":[{\"name\":\"ttmrywnuzoqf\",\"value\":\"yqzrnkcqvyxlw\",\"action\":\"Exclude\"}]}")
            .toObject(LogRules.class);
        Assertions.assertEquals(true, model.sendAadLogs());
        Assertions.assertEquals(false, model.sendSubscriptionLogs());
        Assertions.assertEquals(true, model.sendActivityLogs());
        Assertions.assertEquals("ttmrywnuzoqf", model.filteringTags().get(0).name());
        Assertions.assertEquals("yqzrnkcqvyxlw", model.filteringTags().get(0).value());
        Assertions.assertEquals(TagAction.EXCLUDE, model.filteringTags().get(0).action());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        LogRules model = new LogRules().withSendAadLogs(true)
            .withSendSubscriptionLogs(false)
            .withSendActivityLogs(true)
            .withFilteringTags(Arrays.asList(
                new FilteringTag().withName("ttmrywnuzoqf").withValue("yqzrnkcqvyxlw").withAction(TagAction.EXCLUDE)));
        model = BinaryData.fromObject(model).toObject(LogRules.class);
        Assertions.assertEquals(true, model.sendAadLogs());
        Assertions.assertEquals(false, model.sendSubscriptionLogs());
        Assertions.assertEquals(true, model.sendActivityLogs());
        Assertions.assertEquals("ttmrywnuzoqf", model.filteringTags().get(0).name());
        Assertions.assertEquals("yqzrnkcqvyxlw", model.filteringTags().get(0).value());
        Assertions.assertEquals(TagAction.EXCLUDE, model.filteringTags().get(0).action());
    }
}
