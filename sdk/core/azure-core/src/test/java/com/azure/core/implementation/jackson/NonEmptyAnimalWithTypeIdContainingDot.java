// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = NonEmptyAnimalWithTypeIdContainingDot.class)
@JsonTypeName("NonEmptyAnimalWithTypeIdContainingDot")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "#Favourite.Pet.TurtleWithTypeIdContainingDot",
        value = TurtleWithTypeIdContainingDot.class)
})
public class NonEmptyAnimalWithTypeIdContainingDot implements JsonCapable<NonEmptyAnimalWithTypeIdContainingDot> {
    @JsonProperty(value = "age")
    private Integer age;

    public Integer age() {
        return this.age;
    }

    public NonEmptyAnimalWithTypeIdContainingDot withAge(Integer age) {
        this.age = age;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject().writeStringField("@odata.type", "NonEmptyAnimalWithTypeIdContainingDot");

        return JsonUtils.writeNonNullIntegerField(jsonWriter, "age", age)
            .writeEndObject()
            .flush();
    }

    @SuppressWarnings("unchecked")
    public static <T extends NonEmptyAnimalWithTypeIdContainingDot> T fromJsonBase(JsonReader jsonReader) {
        return (T) JsonUtils.readObject(jsonReader, (reader, token) -> {
            boolean canUseOptimizedPath = true;
            String discriminator = null;

            while ((token = reader.nextToken()) != null) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("@odata.type".equals(fieldName)) {
                    discriminator = jsonReader.getStringValue();
                    if (canUseOptimizedPath) {
                        if ("NonEmptyAnimalWithTypeIdContainingDot".equals(discriminator)) {
                            return fromJsonInternal(jsonReader);
                        } else if ("#Favourite.Pet.TurtleWithTypeIdContainingDot".equals(discriminator)) {
                            return TurtleWithTypeIdContainingDot.fromJsonOptimized(jsonReader,
                                "#Favourite.Pet.TurtleWithTypeIdContainingDot");
                        }
                    }
                }

                canUseOptimizedPath = false;
            }

            throw new IllegalStateException("Discriminator field '@odata.type' was either missing or didn't match one "
                + "of the expected values 'NonEmptyAnimalWithTypeIdContainingDot', "
                + "or '#Favourite.Pet.TurtleWithTypeIdContainingDot'.");
        });
    }

    private static NonEmptyAnimalWithTypeIdContainingDot fromJsonInternal(JsonReader jsonReader) {
        Integer age = null;

        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            JsonToken token = jsonReader.nextToken();

            if ("age".equals(fieldName)) {
                age = (token == JsonToken.NULL) ? null : jsonReader.getIntValue();
            }
        }

        return new NonEmptyAnimalWithTypeIdContainingDot().withAge(age);
    }
}
