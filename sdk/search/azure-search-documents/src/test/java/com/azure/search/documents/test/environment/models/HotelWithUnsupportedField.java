// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.SimpleField;

import java.util.ArrayList;
import java.util.List;

public class HotelWithUnsupportedField {
    @SimpleField(name = "HotelId")
    private String hotelId;

    @SimpleField(name = "SomeByte")
    private Byte someByte;

    @SimpleField(name = "SomeListBytes")
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
