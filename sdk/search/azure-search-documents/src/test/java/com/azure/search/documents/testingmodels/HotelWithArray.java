// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.testingmodels;

import com.azure.core.util.CoreUtils;
import com.azure.search.documents.indexes.BasicField;

public class HotelWithArray {
    private String hotelId;
    private String[] tags;

    @BasicField(name = "HotelId", isKey = BasicField.BooleanHelper.TRUE, isSortable = BasicField.BooleanHelper.TRUE)
    public String getHotelId() {
        return hotelId;
    }

    @BasicField(name = "Tags", isSearchable = BasicField.BooleanHelper.TRUE)
    public String[] getTags() {
        return CoreUtils.clone(tags);
    }
}
