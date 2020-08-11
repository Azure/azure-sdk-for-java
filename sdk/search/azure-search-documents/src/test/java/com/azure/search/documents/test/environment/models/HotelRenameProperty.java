// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.azure.search.documents.indexes.SimpleFieldProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HotelRenameProperty {
    @SimpleFieldProperty(isKey = true, isSortable = true)
    @JsonProperty()
    private String hotelId;

    @SearchableFieldProperty(isSortable = true, analyzerName = "en.lucene")
    @JsonProperty(value = "HotelName")
    private String hotelName;

    @SimpleFieldProperty
    private String description;

    public String hotelId() {
        return this.hotelId;
    }

    public HotelRenameProperty hotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    public String hotelName() {
        return this.hotelName;
    }

    public HotelRenameProperty hotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    public String description() {
        return this.description;
    }

    public HotelRenameProperty description(String description) {
        this.description = description;
        return this;
    }
}
