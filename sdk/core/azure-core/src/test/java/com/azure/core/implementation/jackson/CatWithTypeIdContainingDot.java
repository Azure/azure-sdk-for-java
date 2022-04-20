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

    /**
     * Creates an instance of {@link CatWithTypeIdContainingDot} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link CatWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link CatWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    public static CatWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, CatWithTypeIdContainingDot::fromJsonInternal);
    }

    /**
     * Creates an instance of {@link CatWithTypeIdContainingDot} by reading the {@link JsonReader}.
     * <p>
     * This API is called by {@link AnimalWithTypeIdContainingDot} when an optimized subtype deserialization can be
     * performed. This begins in a different state than {@link #fromJson(JsonReader)} where the current token pointer
     * will be the field value for the discriminator type.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link CatWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link CatWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    static CatWithTypeIdContainingDot fromJsonOptimized(JsonReader jsonReader, String odataType) {
        return fromJsonInternal(jsonReader, jsonReader.currentToken());
    }

    private static CatWithTypeIdContainingDot fromJsonInternal(JsonReader jsonReader, JsonToken token) {
        String breed = null;

        boolean hasBreed = false;

        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            jsonReader.nextToken();

            if ("breed".equals(fieldName)) {
                hasBreed = true;
                breed = jsonReader.getStringValue();
            }
        }

        if (!hasBreed) {
            throw new IllegalStateException("'breed' is a required field for " + CatWithTypeIdContainingDot.class
                + ". The JSON source for the JsonReader didn't contain the expected 'breed' JSON property.");
        }

        return new CatWithTypeIdContainingDot().withBreed(breed);
    }
}
