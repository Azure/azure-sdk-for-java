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
            // Buffer the JSON structure and make it replayable.
            String json = JsonUtils.bufferedJsonObject(jsonReader);

            // Use multiple passes to read the object as either this parent type or the children types.
            JsonReader replayReader = DefaultJsonReader.fromString(json);

            String discriminatorValue = null;

            // First reading will determine the sub-object type.
            while (replayReader.nextToken() != JsonToken.END_OBJECT) {
                if ("@odata.type".equals(replayReader.getFieldName())) {
                    replayReader.nextToken();
                    discriminatorValue = replayReader.getStringValue();
                    break;
                }
            }

            // Use the discriminator value to determine which subtype should be deserialized.
            if ("#Favourite.Pet.DogWithTypeIdContainingDot".equals(discriminatorValue)) {
                return DogWithTypeIdContainingDot.fromJson(DefaultJsonReader.fromString(json));
            } else if ("#Favourite.Pet.CatWithTypeIdContainingDot".equals(discriminatorValue)) {
                return CatWithTypeIdContainingDot.fromJson(DefaultJsonReader.fromString(json));
            } else if ("#Favourite.Pet.RabbitWithTypeIdContainingDot".equals(discriminatorValue)) {
                return RabbitWithTypeIdContainingDot.fromJson(DefaultJsonReader.fromString(json));
            } else {
                throw new IllegalStateException("Discriminator field '@odata.type' was either missing or didn't match "
                    + "one of the expected values '#Favourite.Pet.DogWithTypeIdContainingDot', "
                    + "'#Favourite.Pet.CatWithTypeIdContainingDot', or "
                    + "'#Favourite.Pet.RabbitWithTypeIdContainingDot'. It was: '" + discriminatorValue + "'.");
            }
        });
    }
}
