// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class FeatureMetadata implements JsonSerializable<FeatureMetadata> {
    public String getFeatureType() {
        return featureType;
    }

    public FeatureMetadata setFeatureType(String featureType) {
        this.featureType = featureType;
        return this;
    }

    String featureType;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("featureType", featureType)
            .writeEndObject();
    }

    public static FeatureMetadata fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FeatureMetadata featureMetadata = new FeatureMetadata();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("featureType".equals(fieldName)) {
                    featureMetadata.featureType = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return featureMetadata;
        });
    }
}
