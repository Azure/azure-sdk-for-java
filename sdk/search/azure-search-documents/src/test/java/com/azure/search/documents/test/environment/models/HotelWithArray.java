// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.azure.search.documents.indexes.SimpleFieldProperty;

public class HotelWithArray {
    @SimpleFieldProperty(isKey = true, isSortable = true)
    private String hotelId;

    @SearchableFieldProperty
    private String[] tags;
}
