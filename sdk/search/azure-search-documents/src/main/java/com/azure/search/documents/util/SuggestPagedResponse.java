// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.search.documents.models.SuggestResult;

import java.util.List;

/**
 * Represents an HTTP response from the suggest API request that contains a list of items deserialized into a {@link
 * Page}. Each page contains additional information returned by the API request. In the Suggest API case the additional
 * information is: coverage - coverage value.
 */
@Immutable
public final class SuggestPagedResponse extends PagedResponseBase<Void, SuggestResult> {

    /**
     * The percentage of the index covered in the suggest request.
     * <p>
     * If {@code minimumCoverage} wasn't supplied in the request this will be {@code null}.
     *
     * @return The percentage of the index covered in the suggest request if {@code minimumCoverage} was set in the
     * request, otherwise {@code null}.
     */
    public Double getCoverage() {
        return coverage;
    }

    private final Double coverage;

    /**
     * Constructor
     *
     * @param response The response containing information such as the request, status code, headers, and values.
     * @param coverage Percent of the index used in the suggest operation.
     */
    public SuggestPagedResponse(Response<List<SuggestResult>> response, Double coverage) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(), null, null);

        this.coverage = coverage;
    }
}
