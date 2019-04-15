// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.mgmt;

import java.util.List;

/**
 * Defines a page interface in Azure responses.
 *
 * @param <E> the element type.
 */
public interface Page<E> {
    /**
     * Gets the link to the next page.
     *
     * @return the link.
     */
    String nextPageLink();

    /**
     * Gets the list of items.
     *
     * @return the list of items.
     */
    List<E> items();
}
