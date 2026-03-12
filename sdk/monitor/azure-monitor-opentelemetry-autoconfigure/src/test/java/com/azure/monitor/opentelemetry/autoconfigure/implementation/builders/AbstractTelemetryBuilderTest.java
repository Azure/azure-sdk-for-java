// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.builders;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractTelemetryBuilderTest {

    @Test
    public void regularPropertyIsTruncatedAt8192() {
        MessageTelemetryBuilder builder = MessageTelemetryBuilder.create();
        String longValue = repeat("a", 10000);
        builder.addProperty("some.regular.property", longValue);

        TelemetryItem item = builder.build();
        Map<String, String> properties = getProperties(item);
        assertEquals(8192, properties.get("some.regular.property").length());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "gen_ai.input.messages",
            "gen_ai.output.messages",
            "gen_ai.system_instructions",
            "gen_ai.tool.definitions",
            "gen_ai.tool.call.arguments",
            "gen_ai.tool.call.result",
            "gen_ai.evaluation.explanation" })
    public void genAiPropertyIsNotTruncated(String key) {
        MessageTelemetryBuilder builder = MessageTelemetryBuilder.create();
        String longValue = repeat("a", 50000);
        builder.addProperty(key, longValue);

        TelemetryItem item = builder.build();
        Map<String, String> properties = getProperties(item);
        assertEquals(50000, properties.get(key).length());
        assertEquals(longValue, properties.get(key));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getProperties(TelemetryItem item) {
        if (item.getData() == null || item.getData().getBaseData() == null) {
            return null;
        }
        Object baseData = item.getData().getBaseData();
        try {
            return (Map<String, String>) baseData.getClass().getMethod("getProperties").invoke(baseData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
