// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

public class CatWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    private String breed;

    public String breed() {
        return this.breed;
    }

    public CatWithTypeIdContainingDot withBreed(String presetName) {
        this.breed = presetName;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeStringField("@odata.type", "#Favourite.Pet.CatWithTypeIdContainingDot")
            .writeStringField("breed", breed)
            .writeEndObject()
            .flush();
    }

    public static CatWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return (CatWithTypeIdContainingDot) fromJsonInternal(jsonReader, "#Favourite.Pet.CatWithTypeIdContainingDot");
    }
}
