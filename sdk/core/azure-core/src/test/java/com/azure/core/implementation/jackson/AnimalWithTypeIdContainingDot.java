// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.DefaultJsonReader;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;

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
        return JsonUtils.readObject(jsonReader, reader -> {
            String discriminatorValue = null;
            JsonReader readerToUse = null;

            // Read the first field name and determine if it's the discriminator field.
            jsonReader.nextToken();
            if ("@odata.type".equals(jsonReader.getFieldName())) {
                jsonReader.nextToken();
                discriminatorValue = jsonReader.getStringValue();
                readerToUse = jsonReader;
            } else {
                // If it isn't the discriminator field buffer the JSON structure to make it replayable and find the
                // discriminator field value.
                String json = JsonUtils.bufferJsonObject(jsonReader);
                JsonReader replayReader = DefaultJsonReader.fromString(json);
                while (replayReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = replayReader.getFieldName();
                    replayReader.nextToken();

                    if ("@odata.type".equals(fieldName)) {
                        discriminatorValue = replayReader.getStringValue();
                        break;
                    } else {
                        replayReader.skipChildren();
                    }
                }

                if (discriminatorValue != null) {
                    readerToUse = DefaultJsonReader.fromString(json);
                }
            }

            // Use the discriminator value to determine which subtype should be deserialized.
            if ("#Favourite.Pet.DogWithTypeIdContainingDot".equals(discriminatorValue)) {
                return DogWithTypeIdContainingDot.fromJson(readerToUse);
            } else if ("#Favourite.Pet.CatWithTypeIdContainingDot".equals(discriminatorValue)) {
                return CatWithTypeIdContainingDot.fromJson(readerToUse);
            } else if ("#Favourite.Pet.RabbitWithTypeIdContainingDot".equals(discriminatorValue)) {
                return RabbitWithTypeIdContainingDot.fromJson(readerToUse);
            } else {
                throw new IllegalStateException("Discriminator field '@odata.type' was either missing or didn't match "
                    + "one of the expected values '#Favourite.Pet.DogWithTypeIdContainingDot', "
                    + "'#Favourite.Pet.CatWithTypeIdContainingDot', or "
                    + "'#Favourite.Pet.RabbitWithTypeIdContainingDot'. It was: '" + discriminatorValue + "'.");
            }
        });
    }
}
