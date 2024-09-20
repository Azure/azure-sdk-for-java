// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class Context implements JsonSerializable<Context> {
    public CurrentFeatures getFeatures() {
        return currentFeatures;
    }

    public Context setCurrentFeatures(CurrentFeatures currentFeatures) {
        this.currentFeatures = currentFeatures;
        return this;
    }

    CurrentFeatures currentFeatures;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeJsonField("currentFeatures", currentFeatures)
            .writeEndObject();
    }

    public static Context fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Context context = new Context();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("currentFeatures".equals(fieldName)) {
                    context.currentFeatures = CurrentFeatures.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return context;
        });
    }
}
