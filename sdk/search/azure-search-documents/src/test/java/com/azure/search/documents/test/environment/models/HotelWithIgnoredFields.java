// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.FieldBuilderIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class HotelWithIgnoredFields {
    private String hotelId;
    private String hotelName;
    private String notIgnoredName;

    @FieldBuilderIgnore
    public String getHotelId() {
        return hotelId;
    }

    @JsonIgnore
    public String getHotelName() {
        return hotelName;
    }

    public String getNotIgnoredName() {
        return notIgnoredName;
    }
}
