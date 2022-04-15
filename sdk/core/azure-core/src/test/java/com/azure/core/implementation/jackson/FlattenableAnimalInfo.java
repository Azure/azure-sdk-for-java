// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FlattenableAnimalInfo implements JsonCapable<FlattenableAnimalInfo> {
    @JsonProperty(value = "home")
    private String home;

    @JsonProperty(value = "animal", required = true)
    private AnimalWithTypeIdContainingDot animal;

    public String home() {
        return this.home;
    }

    public FlattenableAnimalInfo withHome(String home) {
        this.home = home;
        return this;
    }

    public AnimalWithTypeIdContainingDot animal() {
        return this.animal;
    }

    public FlattenableAnimalInfo withAnimal(AnimalWithTypeIdContainingDot animal) {
        this.animal = animal;
        return this;
    }

    @Override
    public StringBuilder toJson(StringBuilder stringBuilder) {
        stringBuilder.append("{");

        JsonUtils.appendNullableField(stringBuilder, "home", home);

        if (animal == null) {
            stringBuilder.append(",\"animal\":null");
        } else {
            stringBuilder.append(",\"animal\":");
            animal.toJson(stringBuilder);
        }

        return stringBuilder.append("}");
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject()
            .writeStringField("home", home);

        if (animal == null) {
            jsonWriter.writeNullField("animal");
        } else {
            jsonWriter.writeFieldName("animal");
            animal.toJson(jsonWriter);
        }

        return jsonWriter.writeEndObject().flush();
    }
}
