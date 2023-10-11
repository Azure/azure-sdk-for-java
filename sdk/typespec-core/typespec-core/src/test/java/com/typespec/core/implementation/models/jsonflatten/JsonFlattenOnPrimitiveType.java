// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.models.jsonflatten;

import com.typespec.core.annotation.Fluent;
import com.typespec.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
public final class JsonFlattenOnPrimitiveType {
    @JsonFlatten
    @JsonProperty("jsonflatten.boolean")
    private boolean jsonFlattenBoolean;

    @JsonFlatten
    @JsonProperty("jsonflatten.decimal")
    private double jsonFlattenDecimal;

    @JsonFlatten
    @JsonProperty("jsonflatten.number")
    private int jsonFlattenNumber;

    @JsonFlatten
    @JsonProperty("jsonflatten.string")
    private String jsonFlattenString;

    public JsonFlattenOnPrimitiveType setJsonFlattenBoolean(boolean jsonFlattenBoolean) {
        this.jsonFlattenBoolean = jsonFlattenBoolean;
        return this;
    }

    public boolean isJsonFlattenBoolean() {
        return jsonFlattenBoolean;
    }

    public JsonFlattenOnPrimitiveType setJsonFlattenDecimal(double jsonFlattenDecimal) {
        this.jsonFlattenDecimal = jsonFlattenDecimal;
        return this;
    }

    public double getJsonFlattenDecimal() {
        return jsonFlattenDecimal;
    }

    public JsonFlattenOnPrimitiveType setJsonFlattenNumber(int jsonFlattenNumber) {
        this.jsonFlattenNumber = jsonFlattenNumber;
        return this;
    }

    public int getJsonFlattenNumber() {
        return jsonFlattenNumber;
    }

    public JsonFlattenOnPrimitiveType setJsonFlattenString(String jsonFlattenString) {
        this.jsonFlattenString = jsonFlattenString;
        return this;
    }

    public String getJsonFlattenString() {
        return jsonFlattenString;
    }
}
