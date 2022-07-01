// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.List;

public class AnimalShelter implements JsonSerializable<AnimalShelter> {
    private String description;
    private List<FlattenableAnimalInfo> animalsInfo;

    public String description() {
        return this.description;
    }

    public AnimalShelter withDescription(String description) {
        this.description = description;
        return this;
    }

    public List<FlattenableAnimalInfo> animalsInfo() {
        return this.animalsInfo;
    }

    public AnimalShelter withAnimalsInfo(List<FlattenableAnimalInfo> animalsInfo) {
        this.animalsInfo = animalsInfo;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        if (description == null && animalsInfo == null) {
            return jsonWriter.writeEndObject().flush();
        }

        jsonWriter.writeStartObject("properties")
            .writeStringField("description", description, false)
            .writeArrayField("animalsInfo", animalsInfo, false, JsonWriter::writeJson);

        return jsonWriter.writeEndObject().writeEndObject().flush();
    }

    /**
     * Creates an instance of {@link AnimalShelter} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link AnimalShelter} if the {@link JsonReader} is pointing to {@link AnimalShelter} JSON
     * content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    public static AnimalShelter fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            String description = null;
            List<FlattenableAnimalInfo> animalsInfo = null;

            // Boolean tracking flag as 'properties.animalsInfo' may be null.
            boolean hasAnimalsInfo = false;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    // Loop over the flattened properties.
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("animalsInfo".equals(fieldName)) {
                            hasAnimalsInfo = true;
                            animalsInfo = reader.readArray(FlattenableAnimalInfo::fromJson);
                        } else if ("description".equals(fieldName)) {
                            description = reader.getStringValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                }
            }

            // Should this be thrown in the if block for handling 'properties'?
            if (!hasAnimalsInfo) {
                throw new IllegalStateException("'animalsInfo' is a required field. The JSON source for the JsonReader"
                    + " didn't contain the expected 'properties' -> 'animalsInfo' JSON property.");
            }

            return new AnimalShelter().withAnimalsInfo(animalsInfo).withDescription(description);
        });
    }
}
