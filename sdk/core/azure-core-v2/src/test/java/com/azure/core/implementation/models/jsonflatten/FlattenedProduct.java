// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.v2.annotation.Fluent;
import com.azure.core.v2.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
@JsonFlatten
public class FlattenedProduct {
    // Flattened and escaped property
    @JsonProperty(value = "properties.p\\.name")
    private String productName;

    @JsonProperty(value = "properties.type")
    private String productType;

    public String getProductName() {
        return this.productName;
    }

    public FlattenedProduct setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public String getProductType() {
        return this.productType;
    }

    public FlattenedProduct setProductType(String productType) {
        this.productType = productType;
        return this;
    }
}
