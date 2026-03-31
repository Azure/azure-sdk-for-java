// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.testingmodels;

import com.azure.search.documents.indexes.BasicField;

public class HotelWithIgnoredFields {
    private String hotelId;
    private String hotelName;
    private String notIgnoredName;

    public String getHotelId() {
        return hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    @BasicField(name = "NotIgnoredName")
    public String getNotIgnoredName() {
        return notIgnoredName;
    }
}
