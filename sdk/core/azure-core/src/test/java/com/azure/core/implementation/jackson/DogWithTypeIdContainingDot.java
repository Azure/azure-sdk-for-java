// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

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
        return (DogWithTypeIdContainingDot) fromJsonInternal(jsonReader, "#Favourite.Pet.DogWithTypeIdContainingDot");
    }
}
