// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
public final class JsonFlattenOnArrayType {
    @JsonFlatten
    @JsonProperty("jsonflatten.array")
    private String[] jsonFlattenArray;

    public JsonFlattenOnArrayType setJsonFlattenArray(String[] jsonFlattenArray) {
        this.jsonFlattenArray = CoreUtils.clone(jsonFlattenArray);
        return this;
    }

    public String[] getJsonFlattenArray() {
        return CoreUtils.clone(jsonFlattenArray);
    }
}
