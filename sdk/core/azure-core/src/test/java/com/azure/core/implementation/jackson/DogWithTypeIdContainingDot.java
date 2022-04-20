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
    defaultImpl = DogWithTypeIdContainingDot.class)
@JsonTypeName("#Favourite.Pet.DogWithTypeIdContainingDot")
public class DogWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    @JsonProperty(value = "breed")
    private String breed;

    // Flattenable property
    @JsonProperty(value = "properties.cuteLevel")
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
            .writeStringField("@odata.type", "#Favourite.Pet.DogWithTypeIdContainingDot");

        JsonUtils.writeNonNullStringField(jsonWriter, "breed", breed);

        if (cuteLevel != null) {
            jsonWriter.writeFieldName("properties")
                .writeStartObject()
                .writeIntField("cuteLevel", cuteLevel)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject().flush();
    }

    /**
     * Creates an instance of {@link DogWithTypeIdContainingDot} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link DogWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link DogWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    public static DogWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, DogWithTypeIdContainingDot::fromJsonInternal);
    }

    /**
     * Creates an instance of {@link DogWithTypeIdContainingDot} by reading the {@link JsonReader}.
     * <p>
     * This API is called by {@link AnimalWithTypeIdContainingDot} when an optimized subtype deserialization can be
     * performed. This begins in a different state than {@link #fromJson(JsonReader)} where the current token pointer
     * will be the field value for the discriminator type.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link DogWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link DogWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    static DogWithTypeIdContainingDot fromJsonOptimized(JsonReader jsonReader, String odataType) {
        return fromJsonInternal(jsonReader, jsonReader.currentToken());
    }

    private static DogWithTypeIdContainingDot fromJsonInternal(JsonReader jsonReader, JsonToken token) {
        String breed = null;
        Integer cuteLevel = null;

        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            token = jsonReader.nextToken();

            if ("breed".equals(fieldName)) {
                breed = jsonReader.getStringValue();
            } else if ("properties".equals(fieldName)) {
                if (token == JsonToken.START_OBJECT) {
                    while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = jsonReader.getFieldName();
                        token = jsonReader.nextToken();

                        if ("cuteLevel".equals(fieldName)) {
                            cuteLevel = (token == JsonToken.NULL) ? null : jsonReader.getIntValue();
                        }
                    }
                }
            }
        }

        return new DogWithTypeIdContainingDot().withBreed(breed).withCuteLevel(cuteLevel);
    }
}
