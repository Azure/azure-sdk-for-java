// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.FieldIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class HotelWithIgnoredFields {
    @FieldIgnore
    private String hotelId;
    @JsonIgnore
    private String hotelName;

    private String notIgnoredName;

    public String getHotelId() {
        return hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getNotIgnoredName() {
        return notIgnoredName;
    }
}
