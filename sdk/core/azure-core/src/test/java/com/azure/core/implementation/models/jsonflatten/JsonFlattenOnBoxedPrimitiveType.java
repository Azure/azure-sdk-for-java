// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
public final class JsonFlattenOnBoxedPrimitiveType {
    @JsonFlatten
    @JsonProperty("jsonflatten.boolean")
    private Boolean jsonFlattenBoolean;

    @JsonFlatten
    @JsonProperty("jsonflatten.decimal")
    private Double jsonFlattenDecimal;

    @JsonFlatten
    @JsonProperty("jsonflatten.number")
    private Integer jsonFlattenNumber;

    @JsonFlatten
    @JsonProperty("jsonflatten.string")
    private String jsonFlattenString;

    public JsonFlattenOnBoxedPrimitiveType setJsonFlattenBoolean(boolean jsonFlattenBoolean) {
        this.jsonFlattenBoolean = jsonFlattenBoolean;
        return this;
    }

    public Boolean isJsonFlattenBoolean() {
        return jsonFlattenBoolean;
    }

    public JsonFlattenOnBoxedPrimitiveType setJsonFlattenDecimal(double jsonFlattenDecimal) {
        this.jsonFlattenDecimal = jsonFlattenDecimal;
        return this;
    }

    public Double getJsonFlattenDecimal() {
        return jsonFlattenDecimal;
    }

    public JsonFlattenOnBoxedPrimitiveType setJsonFlattenNumber(int jsonFlattenNumber) {
        this.jsonFlattenNumber = jsonFlattenNumber;
        return this;
    }

    public Integer getJsonFlattenNumber() {
        return jsonFlattenNumber;
    }

    public JsonFlattenOnBoxedPrimitiveType setJsonFlattenString(String jsonFlattenString) {
        this.jsonFlattenString = jsonFlattenString;
        return this;
    }

    public String getJsonFlattenString() {
        return jsonFlattenString;
    }
}
