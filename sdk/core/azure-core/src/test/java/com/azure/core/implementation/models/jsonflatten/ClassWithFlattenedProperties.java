// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Immutable;
import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Immutable
public final class ClassWithFlattenedProperties {
    @JsonFlatten
    @JsonProperty(value = "@odata.type")
    private final String odataType;

    @JsonProperty(value = "@odata.etag")
    private final String odataETag;

    @JsonCreator
    public ClassWithFlattenedProperties(@JsonProperty(value = "@odata.type") String odataType,
        @JsonProperty(value = "@odata.etag") String odataETag) {
        this.odataType = odataType;
        this.odataETag = odataETag;
    }

    public String getOdataType() {
        return odataType;
    }

    public String getOdataETag() {
        return odataETag;
    }
}
