// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.common;

import com.azure.search.models.SuggestOptions;

public class SuggestOptionsHandler {

    /**
     * Ensure all suggest parameters are correct
     * Use this method whenever SuggestOptions are passed to the search service
     *
     * @param suggestOptions suggest parameters
     * @return SuggestOptions ensured suggest parameters
     */
    public static SuggestOptions ensureSuggestOptions(SuggestOptions suggestOptions) {
        if (suggestOptions == null) {
            return null;
        }
        if (suggestOptions.getSelect() == null || suggestOptions.getSelect().isEmpty()) {
            suggestOptions.setSelect("*");
        }
        return suggestOptions;
    }
}
