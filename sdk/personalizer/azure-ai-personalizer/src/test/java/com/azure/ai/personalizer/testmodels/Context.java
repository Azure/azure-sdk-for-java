// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

import static com.azure.ai.personalizer.TestUtils.deserializationHelper;

public class Context implements JsonSerializable<Context> {
    CurrentFeatures currentFeatures;

    public CurrentFeatures getCurrentFeatures() {
        return currentFeatures;
    }

    public Context setCurrentFeatures(CurrentFeatures currentFeatures) {
        this.currentFeatures = currentFeatures;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeJsonField("currentFeatures", currentFeatures).writeEndObject();
    }

    public static Context fromJson(JsonReader jsonReader) throws IOException {
        return deserializationHelper(jsonReader, Context::new, (reader, fieldName, context) -> {
            if ("currentFeatures".equals(fieldName)) {
                context.currentFeatures = CurrentFeatures.fromJson(reader);
            } else {
                reader.skipChildren();
            }
        });
    }
}
