// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.core.util.CoreUtils;
import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.azure.search.documents.indexes.SimpleFieldProperty;

public class HotelWithArray {
    private String hotelId;
    private String[] tags;

    @SimpleFieldProperty(isKey = true, isSortable = true)
    public String getHotelId() {
        return hotelId;
    }

    @SearchableFieldProperty
    public String[] getTags() {
        return CoreUtils.clone(tags);
    }
}
