// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

/**
 * Represents Page from service that has reference to next set of one or more pages.
 *
 * @param <T> Type of the items in the page.
 */
public interface SimplePage<T> extends PageCore<T> {
    /**
     * @return A reference to the next page, or {@code null} if there are no more pages.
     */
    String getContinuationToken();
}
