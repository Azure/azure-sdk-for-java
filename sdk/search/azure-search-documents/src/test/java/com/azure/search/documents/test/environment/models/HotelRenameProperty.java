// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HotelRenameProperty {
    private String hotelId;
    private String hotelName;
    private String description;

    @SimpleField(isKey = true, isSortable = true)
    @JsonProperty
    public String getHotelId() {
        return this.hotelId;
    }

    public HotelRenameProperty setHotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    @SearchableField(isSortable = true, analyzerName = "en.lucene")
    @JsonProperty(value = "HotelName")
    public String getHotelName() {
        return this.hotelName;
    }

    public HotelRenameProperty setHotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    @SimpleField
    public String getDescription() {
        return this.description;
    }

    public HotelRenameProperty setDescription(String description) {
        this.description = description;
        return this;
    }
}
