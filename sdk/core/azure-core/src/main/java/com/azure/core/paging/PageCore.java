// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.paging;

import java.util.List;

/**
 * Type represents a Page from the service.
 *
 * @param <T> Type of items in the page
 */
public interface PageCore<T> {
    /**
     * @return list of items in the page returned by the service.
     */
    List<T> getItems();
}
