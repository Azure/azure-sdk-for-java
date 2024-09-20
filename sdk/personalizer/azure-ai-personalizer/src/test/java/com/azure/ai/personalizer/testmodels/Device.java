// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class Device implements JsonSerializable<Device> {
    public boolean isMobile() {
        return isMobile;
    }

    public Device setMobile(boolean mobile) {
        isMobile = mobile;
        return this;
    }

    public boolean isWindows() {
        return isWindows;
    }

    public Device setWindows(boolean windows) {
        isWindows = windows;
        return this;
    }

    boolean isMobile;
    boolean isWindows;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeBooleanField("isMobile", isMobile)
            .writeBooleanField("isWindows", isWindows)
            .writeEndObject();
    }

    public static Device fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Device device = new Device();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("isMobile".equals(fieldName)) {
                    device.isMobile = reader.getBoolean();
                } else if ("isWindows".equals(fieldName)) {
                    device.isWindows = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }

            return device;
        });
    }
}
