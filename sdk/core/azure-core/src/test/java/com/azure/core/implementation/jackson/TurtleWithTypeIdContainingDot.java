// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = TurtleWithTypeIdContainingDot.class)
@JsonTypeName("#Favourite.Pet.TurtleWithTypeIdContainingDot")
public class TurtleWithTypeIdContainingDot extends NonEmptyAnimalWithTypeIdContainingDot {
    @JsonProperty(value = "size")
    private Integer size;

    public Integer size() {
        return this.size;
    }

    public TurtleWithTypeIdContainingDot withSize(Integer size) {
        this.size = size;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject().writeStringField("@odata.type", "#Favourite.Pet.TurtleWithTypeIdContainingDot");

        JsonUtils.writeNonNullIntegerField(jsonWriter, "age", age());
        JsonUtils.writeNonNullIntegerField(jsonWriter, "size", size);

        return jsonWriter.writeEndObject().flush();
    }

    /**
     * Creates an instance of {@link TurtleWithTypeIdContainingDot} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link TurtleWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link TurtleWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    public static TurtleWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, TurtleWithTypeIdContainingDot::fromJsonInternal);
    }

    /**
     * Creates an instance of {@link TurtleWithTypeIdContainingDot} by reading the {@link JsonReader}.
     * <p>
     * This API is called by {@link NonEmptyAnimalWithTypeIdContainingDot} when an optimized subtype deserialization can
     * be performed. This begins in a different state than {@link #fromJson(JsonReader)} where the current token pointer
     * will be the field value for the discriminator type.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link TurtleWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link TurtleWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    static TurtleWithTypeIdContainingDot fromJsonOptimized(JsonReader jsonReader, String odataType) {
        return fromJsonInternal(jsonReader, jsonReader.currentToken());
    }

    private static TurtleWithTypeIdContainingDot fromJsonInternal(JsonReader jsonReader, JsonToken token) {
        Integer age = null;
        Integer size = null;

        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            token = jsonReader.nextToken();

            if ("age".equals(fieldName)) {
                age = (token == JsonToken.NULL) ? null : jsonReader.getIntValue();
            } else if ("size".equals(fieldName)) {
                size = (token == JsonToken.NULL) ? null : jsonReader.getIntValue();
            }
        }

        TurtleWithTypeIdContainingDot turtle = new TurtleWithTypeIdContainingDot().withSize(size);
        turtle.withAge(age);

        return turtle;
    }
}

