// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
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
        jsonWriter.writeStartObject().writeStringField("@odata.type", "#Favourite.Pet.TurtleWithTypeIdContainingDot");

        JsonUtils.writeNonNullIntegerField(jsonWriter, "age", age());
        JsonUtils.writeNonNullIntegerField(jsonWriter, "size", size);

        return jsonWriter.writeEndObject().flush();
    }
}

