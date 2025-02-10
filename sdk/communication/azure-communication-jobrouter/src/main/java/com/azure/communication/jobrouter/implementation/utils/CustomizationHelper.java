// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.jobrouter.implementation.utils;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class to provide utility methods for customization.
 */
public final class CustomizationHelper {
    public static void serializeDurationToSeconds(JsonWriter jsonWriter, String fieldName, Duration duration)
        throws IOException {
        // Customization logic to serialize Duration to seconds.
        if (duration != null) {
            jsonWriter.writeNumberField(fieldName, duration.toMillis() / 1000.0D);
        }
    }

    public static Duration deserializeDurationFromSeconds(JsonReader jsonReader) throws IOException {
        // Customization logic to deserialize Duration from seconds.
        return Duration.ofMillis(Math.round(1000 * jsonReader.getDouble()));
    }

    public static void serializeDurationToMinutesMap(JsonWriter jsonWriter, String fieldName,
        Map<Integer, Duration> durationMap) throws IOException {
        if (durationMap == null) {
            return;
        }

        jsonWriter.writeStartObject(fieldName);

        for (Map.Entry<Integer, Duration> entry : durationMap.entrySet()) {
            if (entry.getValue() != null) {
                jsonWriter.writeDoubleField(String.valueOf(entry.getKey()), entry.getValue().getSeconds() / 60.0);
            } else {
                jsonWriter.writeNullField(String.valueOf(entry.getKey()));
            }
        }

        jsonWriter.writeEndObject();
    }

    public static Map<Integer, Duration> deserializeDurationFromMinutesMap(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Map<Integer, Duration> result = new LinkedHashMap<>();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                int priority = Integer.parseInt(reader.getFieldName());
                JsonToken token = reader.nextToken();

                if (token == JsonToken.NUMBER) {
                    result.put(priority, Duration.ofSeconds(Math.round(60 * reader.getDouble())));
                }
            }
            return result;
        });
    }

    private CustomizationHelper() {
    }
}
