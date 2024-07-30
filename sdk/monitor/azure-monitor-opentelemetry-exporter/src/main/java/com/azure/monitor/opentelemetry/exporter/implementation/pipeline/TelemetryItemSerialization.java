// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class TelemetryItemSerialization {

    // visible for testing
    // deserialize multiple TelemetryItem raw bytes with newline delimiters to a list of TelemetryItems
    public static List<TelemetryItem> deserialize(byte[] rawBytes) {
        try (JsonReader jsonReader = JsonProviders.createReader(rawBytes)) {
            JsonToken token = jsonReader.currentToken();
            if (token == null) {
                token = jsonReader.nextToken();
            }

            List<TelemetryItem> result = new ArrayList<>();
            do {
                result.add(TelemetryItem.fromJson(jsonReader));
            } while (jsonReader.nextToken() == JsonToken.START_OBJECT);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // visible for testing
    // un-gzip TelemetryItems raw bytes back to original un-gzipped TelemetryItems raw bytes
    public static byte[] ungzip(byte[] rawBytes) {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(rawBytes));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] data = new byte[1024];
            int read;
            while ((read = in.read(data)) != -1) {
                baos.write(data, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode byte[]", e);
        }
    }

    private TelemetryItemSerialization() {
    }
}
