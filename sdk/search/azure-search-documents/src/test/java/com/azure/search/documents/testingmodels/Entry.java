// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.testingmodels;

import com.azure.search.documents.indexes.BasicField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@SuppressWarnings("ALL")
public class Entry {
    @BasicField(name = "Entry")
    @JsonProperty
    OffsetDateTime offsetDateTime;

    public OffsetDateTime offsetDateTime() {
        return offsetDateTime;
    }

    public void offsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }
}
