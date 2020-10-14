// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.SearchableField;

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
    @SearchableField
    public List<Integer> getPasscode() {
        return passcode;
    }

    /**
     * Sets passcode.
     *
     * @param passcode the passcode of hotel.
     * @return The {@link HotelSearchableExceptionOnList} object itself.
     */
    public HotelSearchableExceptionOnList setPasscode(List<Integer> passcode) {
        this.passcode = passcode;
        return this;
    }
}
