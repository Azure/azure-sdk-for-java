// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.Objects;

public class NonEmptyAnimalWithTypeIdContainingDot implements JsonSerializable<NonEmptyAnimalWithTypeIdContainingDot> {
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
        return jsonWriter.writeStartObject()
            .writeStringField("@odata.type", "NonEmptyAnimalWithTypeIdContainingDot")
            .writeIntegerField("age", age, false)
            .writeEndObject()
            .flush();
    }

    public static NonEmptyAnimalWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return fromJsonInternal(jsonReader, null);
    }

    static NonEmptyAnimalWithTypeIdContainingDot fromJsonInternal(JsonReader jsonReader,
        String expectedODataType) {
        // Assumption time, super classes will have access to their subclasses and they'll be in the same package.
        return JsonUtils.readObject(jsonReader, reader -> {
            String odataType = null;
            Integer age = null;
            Integer size = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("@odata.type".equals(fieldName)) {
                    odataType = jsonReader.getStringValue();
                } else if ("age".equals(fieldName)) {
                    age = reader.getIntegerNullableValue();
                } else if ("size".equals(fieldName)) {
                    size = reader.getIntegerNullableValue();
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

            if ((expectedODataType == null && odataType == null)
                || "NonEmptyAnimalWithTypeIdContainingDot".equals(odataType)) {
                return new NonEmptyAnimalWithTypeIdContainingDot().withAge(age);
            } else if ("#Favourite.Pet.TurtleWithTypeIdContainingDot".equals(odataType)
                || "#Favourite.Pet.TurtleWithTypeIdContainingDot".equals(expectedODataType)) {
                TurtleWithTypeIdContainingDot turtle = new TurtleWithTypeIdContainingDot().withSize(size);
                turtle.withAge(age);

                return turtle;
            } else {
                throw new IllegalStateException("Discriminator field '@odata.type' didn't match expected values: "
                    + "'#Favourite.Pet.TurtleWithTypeIdContainingDot' or 'NonEmptyAnimalWithTypeIdContainingDot'. "
                    + "It was: '" + odataType + "'.");
            }
        });
    }
}
