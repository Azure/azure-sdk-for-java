// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import com.azure.core.annotation.Immutable;

/**
 * This class contains the information necessary to continue a search paging operation.
 */
@Immutable
public final class SearchNextPageParameters {
    private final Integer top;
    private final Integer skip;

    /**
     * Constructs the information to continue a search paging operation.
     *
     * @param top The number of elements to select in the next page.
     * @param skip The number of elements to skip before retrieving for the next page.
     */
    public SearchNextPageParameters(Integer top, Integer skip) {
        this.top = top;
        this.skip = skip;
    }

    /**
     * @return The number of elements to skip in the result set before retrieving for the next page.
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     * @return The number of elements to select in the next page.
     */
    public Integer getTop() {
        return top;
    }
}
