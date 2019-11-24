// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.paging;

/**
 * Type represents a Page from the service and expose continuation token.
 *
 * @param <C> Type of continuation token
 * @param <T> Type of items in the page
 */
public interface ContinuablePage<C extends ContinuationToken, T> extends PageCore<T> {
    /**
     * @return the continuation token to retrieve one or more next pages.
     */
    C getContinuationToken();
}
