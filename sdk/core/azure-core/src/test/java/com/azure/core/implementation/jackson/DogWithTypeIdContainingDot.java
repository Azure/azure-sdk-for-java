// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.Objects;

public class DogWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    private String breed;
    private Integer cuteLevel;

    public String breed() {
        return this.breed;
    }

    public DogWithTypeIdContainingDot withBreed(String audioLanguage) {
        this.breed = audioLanguage;
        return this;
    }

    public Integer cuteLevel() {
        return this.cuteLevel;
    }

    public DogWithTypeIdContainingDot withCuteLevel(Integer cuteLevel) {
        this.cuteLevel = cuteLevel;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject()
            .writeStringField("@odata.type", "#Favourite.Pet.DogWithTypeIdContainingDot")
            .writeStringField("breed", breed, false);

        if (cuteLevel != null) {
            jsonWriter.writeFieldName("properties")
                .writeStartObject()
                .writeIntField("cuteLevel", cuteLevel)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    public static DogWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            String odataType = null;
            String breed = null;
            Integer cuteLevel = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("@odata.type".equals(fieldName)) {
                    odataType = reader.getStringValue();
                } else if ("breed".equals(fieldName)) {
                    breed = reader.getStringValue();
                } else if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("cuteLevel".equals(fieldName)) {
                            cuteLevel = reader.getIntegerNullableValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            if (odataType != null && !Objects.equals(odataType, "#Favourite.Pet.DogWithTypeIdContainingDot")) {
                throw new IllegalStateException(
                    "'@odata.type' was expected to be null or '#Favourite.Pet.DogWithTypeIdContainingDot'. "
                        + "The actual '@odata.type' was '" + odataType + "'.");
            }

            return new DogWithTypeIdContainingDot().withBreed(breed).withCuteLevel(cuteLevel);
        });
    }
}
