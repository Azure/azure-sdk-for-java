// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.util.List;

public class RabbitWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    private Integer tailLength;
    private List<String> meals;

    public Integer filters() {
        return this.tailLength;
    }

    public RabbitWithTypeIdContainingDot withTailLength(Integer tailLength) {
        this.tailLength = tailLength;
        return this;
    }

    public List<String> meals() {
        return this.meals;
    }

    public RabbitWithTypeIdContainingDot withMeals(List<String> meals) {
        this.meals = meals;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject()
            .writeStringField("@odata.type", "#Favourite.Pet.RabbitWithTypeIdContainingDot");

        if (tailLength != null) {
            jsonWriter.writeIntField("tailLength", tailLength);
        }

        JsonUtils.writeArray(jsonWriter, "meals", meals, JsonWriter::writeString);

        return jsonWriter.writeEndObject().flush();
    }

    public static <T extends AnimalWithTypeIdContainingDot> T fromJson(JsonReader jsonReader) {
        return fromJsonInternal(jsonReader, "#Favourite.Pet.RabbitWithTypeIdContainingDot");
    }
}
