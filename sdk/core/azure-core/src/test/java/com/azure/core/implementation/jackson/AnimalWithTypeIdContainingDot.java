// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = AnimalWithTypeIdContainingDot.class)
@JsonTypeName("AnimalWithTypeIdContainingDot")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "#Favourite.Pet.DogWithTypeIdContainingDot",
        value = DogWithTypeIdContainingDot.class),
    @JsonSubTypes.Type(name = "#Favourite.Pet.CatWithTypeIdContainingDot",
        value = CatWithTypeIdContainingDot.class),
    @JsonSubTypes.Type(name = "#Favourite.Pet.RabbitWithTypeIdContainingDot",
        value = RabbitWithTypeIdContainingDot.class)
})
public abstract class AnimalWithTypeIdContainingDot implements JsonCapable<AnimalWithTypeIdContainingDot> {
    /**
     * Creates an instance of {@link AnimalWithTypeIdContainingDot} by reading the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link AnimalWithTypeIdContainingDot} if the {@link JsonReader} is pointing to
     * {@link AnimalWithTypeIdContainingDot} JSON content, or null if it is pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to the correct {@link JsonToken} when
     * passed.
     */
    @SuppressWarnings("unchecked")
    public static <T extends AnimalWithTypeIdContainingDot> T fromJsonBase(JsonReader jsonReader) {
        // There are two paths that can be taken when dealing with inheritance.
        //
        // If we're lucky the discriminator field will be the first property in the JSON and no buffering will be
        // needed.
        //
        // If we're unlucky the discriminator field isn't the first property, so we'll want to buffer the entire object
        // and pass the buffered object as a new JsonReader into the subclass deserialization method.
        return (T) JsonUtils.readObject(jsonReader, (reader, token) -> {
            boolean canUseOptimizedPath = true;
            String discriminator = null;

            while ((token = reader.nextToken()) != null) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("@odata.type".equals(fieldName)) {
                    discriminator = jsonReader.getStringValue();
                    if (canUseOptimizedPath) {
                        if ("#Favourite.Pet.DogWithTypeIdContainingDot".equals(discriminator)) {
                            return DogWithTypeIdContainingDot.fromJsonOptimized(jsonReader,
                                "#Favourite.Pet.DogWithTypeIdContainingDot");
                        } else if ("#Favourite.Pet.CatWithTypeIdContainingDot".equals(discriminator)) {
                            return CatWithTypeIdContainingDot.fromJsonOptimized(jsonReader,
                                "#Favourite.Pet.CatWithTypeIdContainingDot");
                        } else if ("#Favourite.Pet.RabbitWithTypeIdContainingDot".equals(discriminator)) {
                            return RabbitWithTypeIdContainingDot.fromJsonOptimized(jsonReader,
                                "#Favourite.Pet.RabbitWithTypeIdContainingDot");
                        }
                    }
                }

                canUseOptimizedPath = false;
            }

            throw new IllegalStateException("Discriminator field '@odata.type' was either missing or didn't match one "
                + "of the expected values '#Favourite.Pet.DogWithTypeIdContainingDot', "
                + "'#Favourite.Pet.CatWithTypeIdContainingDot', or '#Favourite.Pet.RabbitWithTypeIdContainingDot'.");
        });
    }
}
