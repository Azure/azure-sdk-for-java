// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonFlatten
public class AnimalShelter implements JsonCapable<AnimalShelter> {
    @JsonProperty(value = "properties.description")
    private String description;

    @JsonProperty(value = "properties.animalsInfo", required = true)
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
    public StringBuilder toJson(StringBuilder stringBuilder) {
        stringBuilder.append("{\"properties\":{");

        JsonUtils.appendNullableField(stringBuilder, "description", description)
            .append(",\"animalsInfo\":");

        if (animalsInfo == null) {
            stringBuilder.append("null");
        } else {
            stringBuilder.append("[");

            for (FlattenableAnimalInfo animalInfo : animalsInfo) {
                animalInfo.toJson(stringBuilder);
            }

            stringBuilder.append("]");
        }

        return stringBuilder.append("}}");
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject()
            .writeFieldName("properties")
            .writeStartObject()
            .writeStringField("description", description);

        return JsonUtils.serializeArray(jsonWriter, "animalsInfo", animalsInfo,
            (writer, animalInfo) -> animalInfo.toJson(writer))
            .writeEndObject()
            .writeEndObject()
            .flush();
    }
}
