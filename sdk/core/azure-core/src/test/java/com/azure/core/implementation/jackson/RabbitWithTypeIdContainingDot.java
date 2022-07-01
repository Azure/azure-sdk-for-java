// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.List;
import java.util.Objects;

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
        return jsonWriter.writeStartObject()
            .writeStringField("@odata.type", "#Favourite.Pet.RabbitWithTypeIdContainingDot")
            .writeIntegerField("tailLength", tailLength, false)
            .writeArrayField("meals", meals, false, JsonWriter::writeString)
            .writeEndObject()
            .flush();
    }

    public static RabbitWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            String odataType = null;
            Integer tailLength = null;
            List<String> meals = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("@odata.type".equals(fieldName)) {
                    odataType = reader.getStringValue();
                } else if ("tailLength".equals(fieldName)) {
                    tailLength = reader.getIntegerNullableValue();
                } else if ("meals".equals(fieldName) && reader.currentToken() == JsonToken.START_ARRAY) {
                    meals = reader.readArray(JsonReader::getStringValue);
                } else {
                    reader.skipChildren();
                }
            }

            if (odataType != null && !Objects.equals(odataType, "#Favourite.Pet.RabbitWithTypeIdContainingDot")) {
                throw new IllegalStateException(
                    "'@odata.type' was expected to be null or '#Favourite.Pet.RabbitWithTypeIdContainingDot'. "
                        + "The actual '@odata.type' was '" + odataType + "'.");
            }

            return new RabbitWithTypeIdContainingDot().withTailLength(tailLength).withMeals(meals);
        });
    }
}
