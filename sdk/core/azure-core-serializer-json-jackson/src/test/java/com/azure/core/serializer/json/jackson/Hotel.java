// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

public class Hotel {
    String hotelName;
    String HTTP;

    public Hotel() {
    }

    public String getHotelName() {
        return hotelName;
    }

    public Hotel setHotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    public String getHTTP() {
        return HTTP;
    }

    public Hotel setHTTP(String HTTP) {
        this.HTTP = HTTP;
        return this;
    }
}
