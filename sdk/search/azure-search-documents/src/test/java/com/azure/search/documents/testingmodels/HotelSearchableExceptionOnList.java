// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.testingmodels;

import com.azure.search.documents.indexes.BasicField;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class on test searchable exception field.
 */
public class HotelSearchableExceptionOnList {
    private List<Integer> passcode;

    /**
     * Gets passcode.
     * @return the passcode of hotel.
     */
    @BasicField(name = "Passcode", isSearchable = BasicField.BooleanHelper.TRUE)
    public List<Integer> getPasscode() {
        return (passcode == null) ? null : new ArrayList<>(passcode);
    }

    /**
     * Sets passcode.
     *
     * @param passcode the passcode of hotel.
     * @return The {@link HotelSearchableExceptionOnList} object itself.
     */
    public HotelSearchableExceptionOnList setPasscode(List<Integer> passcode) {
        this.passcode = (passcode == null) ? null : new ArrayList<>(passcode);
        return this;
    }
}
