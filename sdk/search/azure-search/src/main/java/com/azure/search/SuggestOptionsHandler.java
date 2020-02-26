// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.CoreUtils;
import com.azure.search.models.SuggestOptions;

/**
 * Ensure all suggest parameters are correct Use this whenever SuggestOptions are passed to the search service
 */
class SuggestOptionsHandler {

    /**
     * Ensures that all suggest parameters are correctly set. This method should be used when {@link SuggestOptions} is
     * passed to the Search service.
     *
     * @param suggestOptions suggest parameters
     * @return SuggestOptions ensured suggest parameters
     */
    static SuggestOptions ensureSuggestOptions(SuggestOptions suggestOptions) {
        if (suggestOptions == null) {
            return null;
        }

        return CoreUtils.isNullOrEmpty(suggestOptions.getSelect()) ? suggestOptions.setSelect("*") : suggestOptions;
    }
}
