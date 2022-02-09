// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import java.util.List;

public class HotelWithUnsupportedField {
    private String hotelId;

    private Byte someByte;

    private List<Byte> someListBytes;

    public String getHotelId() {
        return hotelId;
    }

    public Byte getSomeByte() {
        return someByte;
    }

    public List<Byte> getSomeListBytes() {
        return someListBytes;
    }
}
