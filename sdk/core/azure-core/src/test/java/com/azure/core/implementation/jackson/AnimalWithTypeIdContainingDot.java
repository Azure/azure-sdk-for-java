// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.util.List;
import java.util.Objects;

public abstract class AnimalWithTypeIdContainingDot implements JsonSerializable<AnimalWithTypeIdContainingDot> {
    /**
     * Creates an instance of {@link AnimalWithTypeIdContainingDot} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link AnimalWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link AnimalWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    public static AnimalWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return fromJsonInternal(jsonReader, null);
    }

    static AnimalWithTypeIdContainingDot fromJsonInternal(JsonReader jsonReader,
        String expectedODataType) {
        return JsonUtils.readObject(jsonReader, reader -> {
            String odataType = null;
            String breed = null;
            boolean hasBreed = false;
            Integer cuteLevel = null;
            Integer tailLength = null;
            List<String> meals = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("@odata.type".equals(fieldName)) {
                    odataType = reader.getStringValue();
                } else if ("breed".equals(fieldName)) {
                    hasBreed = true;
                    breed = reader.getStringValue();
                } else if ("tailLength".equals(fieldName)) {
                    tailLength = JsonUtils.getNullableProperty(reader, JsonReader::getIntValue);
                } else if ("meals".equals(fieldName) && reader.currentToken() == JsonToken.START_ARRAY) {
                    meals = JsonUtils.readArray(reader,
                        r -> JsonUtils.getNullableProperty(r, JsonReader::getStringValue));
                } else if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("cuteLevel".equals(fieldName)) {
                            cuteLevel = JsonUtils.getNullableProperty(reader, JsonReader::getIntValue);
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            // When called from a subtype, the expected @odata.type will be passed and verified, as long as the
            // @odata.type in the JSON wasn't null or missing.
            // TODO (alzimmer): Should this throw if it was present and null?
            if (expectedODataType != null && odataType != null && !Objects.equals(expectedODataType, odataType)) {
                throw new IllegalStateException("Discriminator field '@odata.type' didn't match expected value: "
                    + "'" + expectedODataType + "'. It was: '" + odataType + "'.");
            }

            if ("#Favourite.Pet.DogWithTypeIdContainingDot".equals(odataType)) {
                return new DogWithTypeIdContainingDot()
                    .withBreed(breed)
                    .withCuteLevel(cuteLevel);
            } else if ("#Favourite.Pet.CatWithTypeIdContainingDot".equals(odataType)) {
                if (!hasBreed) {
                    throw new IllegalStateException("'breed' is a required field for "
                        + CatWithTypeIdContainingDot.class
                        + ". The JSON source for the JsonReader didn't contain the expected 'breed' JSON property.");
                }

                return new CatWithTypeIdContainingDot()
                    .withBreed(breed);
            } else if ("#Favourite.Pet.RabbitWithTypeIdContainingDot".equals(odataType)) {
                return new RabbitWithTypeIdContainingDot()
                    .withTailLength(tailLength)
                    .withMeals(meals);
            } else {
                throw new IllegalStateException("Discriminator field '@odata.type' was either missing or didn't match "
                    + "one of the expected values '#Favourite.Pet.DogWithTypeIdContainingDot', "
                    + "'#Favourite.Pet.CatWithTypeIdContainingDot', or "
                    + "'#Favourite.Pet.RabbitWithTypeIdContainingDot'.");
            }
        });
    }
}
