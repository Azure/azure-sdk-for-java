// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

import static com.azure.ai.personalizer.TestUtils.deserializationHelper;

public class FeatureMetadata implements JsonSerializable<FeatureMetadata> {
    String featureType;

    public String getFeatureType() {
        return featureType;
    }

    public FeatureMetadata setFeatureType(String featureType) {
        this.featureType = featureType;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeStringField("featureType", featureType).writeEndObject();
    }

    public static FeatureMetadata fromJson(JsonReader jsonReader) throws IOException {
        return deserializationHelper(jsonReader, FeatureMetadata::new, (reader, fieldName, featureMetadata) -> {
            if ("featureType".equals(fieldName)) {
                featureMetadata.featureType = reader.getString();
            } else {
                reader.skipChildren();
            }
        });
    }
}
