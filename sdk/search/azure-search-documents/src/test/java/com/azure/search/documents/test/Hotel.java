package com.azure.search.documents.test;

import com.azure.search.annotation.SimpleFieldProperty;

public class Hotel {
    @SimpleFieldProperty(isSortable = true, isKey = true)
    private String hotelId;

    private String reviews;
    private String[] tags;

}
