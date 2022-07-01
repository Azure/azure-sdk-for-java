// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

public class FlattenableAnimalInfo implements JsonSerializable<FlattenableAnimalInfo> {
    private String home;
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
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeStringField("home", home, false)
            .writeJsonField("animal", animal, false)
            .writeEndObject()
            .flush();
    }

    /**
     * Creates an instance of {@link FlattenableAnimalInfo} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link FlattenableAnimalInfo} if the {@link JsonReader} is pointing to
     * {@link FlattenableAnimalInfo} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    public static FlattenableAnimalInfo fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            String home = null;
            AnimalWithTypeIdContainingDot animal = null;

            // Boolean tracking flag as 'animal' may be null.
            boolean hasAnimal = false;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("home".equals(fieldName)) {
                    home = reader.getStringValue();
                } else if ("animal".equals(fieldName)) {
                    hasAnimal = true;
                    animal = AnimalWithTypeIdContainingDot.fromJson(reader);
                }
            }

            if (!hasAnimal) {
                throw new IllegalStateException("'animal' is a required field. The JSON source for the JsonReader"
                    + " didn't contain the expected 'animal' JSON property.");
            }

            return new FlattenableAnimalInfo().withAnimal(animal).withHome(home);
        });
    }
}
