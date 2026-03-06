// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.testingmodels;

import com.azure.search.documents.indexes.BasicField;

import java.util.ArrayList;
import java.util.List;

public class HotelWithUnsupportedField {
    @BasicField(name = "HotelId")
    private String hotelId;

    @BasicField(name = "SomeByte")
    private Byte someByte;

    @BasicField(name = "SomeListBytes")
    private List<Byte> someListBytes;

    public String getHotelId() {
        return hotelId;
    }

    public Byte getSomeByte() {
        return someByte;
    }

    public List<Byte> getSomeListBytes() {
        return (someListBytes == null) ? null : new ArrayList<>(someListBytes);
    }
}
