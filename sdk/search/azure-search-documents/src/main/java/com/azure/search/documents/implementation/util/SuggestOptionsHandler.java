// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.util.CoreUtils;
import com.azure.search.documents.models.SuggestOptions;

/**
 * Utility class that ensures all suggest parameters are correct, use this validate {@link SuggestOptions}.
 */
public final class SuggestOptionsHandler {

    /**
     * Ensures that all suggest parameters are correctly set. This method should be used when {@link SuggestOptions} is
     * passed to the Search service.
     *
     * @param suggestOptions suggest parameters
     * @return SuggestOptions ensured suggest parameters
     */
    public static SuggestOptions ensureSuggestOptions(SuggestOptions suggestOptions) {
        if (suggestOptions == null) {
            return null;
        }

        return CoreUtils.isNullOrEmpty(suggestOptions.getSelect()) ? suggestOptions.setSelect("*") : suggestOptions;
    }

    private SuggestOptionsHandler() {
    }
}
