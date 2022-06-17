// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CatWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
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

    public static CatWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            String odataType = null;
            boolean hasBreed = false;
            String breed = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("@odata.type".equals(fieldName)) {
                    odataType = reader.getStringValue();
                } else if ("breed".equals(fieldName)) {
                    hasBreed = true;
                    breed = reader.getStringValue();
                } else {
                    reader.skipChildren();
                }
            }

            List<String> errors = new ArrayList<>();
            if (odataType != null && !Objects.equals(odataType, "#Favourite.Pet.CatWithTypeIdContainingDot")) {
                errors.add("'@odata.type' was expected to be null or '#Favourite.Pet.CatWithTypeIdContainingDot'. "
                    + "The actual '@odata.type' was '" + odataType + "'.");
            }

            if (!hasBreed) {
                errors.add("'breed' is a required field for '" + CatWithTypeIdContainingDot.class + "'. "
                    + "The JSON source didn't contain the expected 'breed' JSON property.");
            }

            if (!errors.isEmpty()) {
                throw new IllegalStateException(String.join(System.lineSeparator(), errors));
            }

            return new CatWithTypeIdContainingDot().withBreed(breed);
        });
    }
}
