// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import java.util.List;

/**
 * Type represents a Page.
 *
 * @param <T> Type of items in the page
 */
public interface PageCore<T> {
    /**
     * @return list of items in the page.
     */
    List<T> getItems();
}
