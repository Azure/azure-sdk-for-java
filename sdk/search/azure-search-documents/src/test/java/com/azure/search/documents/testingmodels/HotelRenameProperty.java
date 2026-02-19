// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.testingmodels;

import com.azure.search.documents.indexes.BasicField;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HotelRenameProperty {
    private String hotelId;
    private String hotelName;
    private String description;

    @BasicField(name = "HotelId", isKey = BasicField.BooleanHelper.TRUE, isSortable = BasicField.BooleanHelper.TRUE)
    @JsonProperty
    public String getHotelId() {
        return this.hotelId;
    }

    public HotelRenameProperty setHotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    @BasicField(
        name = "HotelName",
        isSearchable = BasicField.BooleanHelper.TRUE,
        isSortable = BasicField.BooleanHelper.TRUE,
        analyzerName = "en.lucene")
    @JsonProperty(value = "HotelName")
    public String getHotelName() {
        return this.hotelName;
    }

    public HotelRenameProperty setHotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    @BasicField(name = "Description")
    public String getDescription() {
        return this.description;
    }

    public HotelRenameProperty setDescription(String description) {
        this.description = description;
        return this;
    }
}
