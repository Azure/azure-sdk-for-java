// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

public class TurtleWithTypeIdContainingDot extends NonEmptyAnimalWithTypeIdContainingDot {
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
        return jsonWriter.writeStartObject()
            .writeStringField("@odata.type", "#Favourite.Pet.TurtleWithTypeIdContainingDot")
            .writeIntegerField("age", age(), false)
            .writeIntegerField("size", size, false)
            .writeEndObject()
            .flush();
    }

    public static TurtleWithTypeIdContainingDot fromJson(JsonReader jsonReader) {
        return (TurtleWithTypeIdContainingDot) fromJsonInternal(jsonReader,
            "#Favourite.Pet.TurtleWithTypeIdContainingDot");
    }
}
