// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = RabbitWithTypeIdContainingDot.class)
@JsonTypeName("#Favourite.Pet.RabbitWithTypeIdContainingDot")
public class RabbitWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    @JsonProperty(value = "tailLength")
    private Integer tailLength;

    @JsonProperty(value = "meals")
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

    /**
     * Creates an instance of {@link RabbitWithTypeIdContainingDot} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link RabbitWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link RabbitWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    public static RabbitWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, RabbitWithTypeIdContainingDot::fromJsonInternal);
    }

    /**
     * Creates an instance of {@link RabbitWithTypeIdContainingDot} by reading the {@link JsonReader}.
     * <p>
     * This API is called by {@link AnimalWithTypeIdContainingDot} when an optimized subtype deserialization can be
     * performed. This begins in a different state than {@link #fromJson(JsonReader)} where the current token pointer
     * will be the field value for the discriminator type.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link RabbitWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link RabbitWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    static RabbitWithTypeIdContainingDot fromJsonOptimized(JsonReader jsonReader, String odataType) {
        return fromJsonInternal(jsonReader, jsonReader.currentToken());
    }

    private static RabbitWithTypeIdContainingDot fromJsonInternal(JsonReader jsonReader, JsonToken token) {
        Integer tailLength = null;
        List<String> meals = null;

        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            token = jsonReader.nextToken();

            if ("tailLength".equals(fieldName)) {
                tailLength = (token == JsonToken.NULL) ? null : jsonReader.getIntValue();
            } else if ("meals".equals(fieldName)) {
                meals = JsonUtils.readArray(jsonReader, (reader, t) -> reader.getStringValue());
            }
        }

        return new RabbitWithTypeIdContainingDot().withTailLength(tailLength).withMeals(meals);
    }
}
