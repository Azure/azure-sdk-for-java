// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.http.rest.PagedResponse;

import java.util.Objects;

/**
 * A helper to extract the query charge from the query response.
 * An Azure Digital Twins Query Unit (QU) is a unit of on-demand computation that's used to execute your Azure Digital Twins queries.
 */
public final class QueryChargeHelper {
    private static final String queryChargeHeader = "query-charge";

    /**
     * Extract the query-charge field from a page header.
     * @param page The page that contains the query-charge header.
     * @param <T> The generic type of the page.
     * @return The query charge.
     */
    public static <T> Float getQueryCharge(PagedResponse<T> page) {
        Objects.requireNonNull(page, "'page' cannot be null");

        String queryCharge = page.getHeaders().getValue(queryChargeHeader);

        return queryCharge !=null ? Float.parseFloat(queryCharge) : null;
    }
}
