// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = CatWithTypeIdContainingDot.class)
@JsonTypeName("#Favourite.Pet.CatWithTypeIdContainingDot")
public class CatWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    @JsonProperty(value = "breed", required = true)
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
}
