// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.documents.implementation.converters.SuggestResultConverter;
import com.azure.search.documents.implementation.models.SuggestDocumentsResult;
import com.azure.search.documents.models.SuggestResult;

import java.util.stream.Collectors;

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
     * @param documentSearchResponse an http response with the results
     */
    public SuggestPagedResponse(SimpleResponse<SuggestDocumentsResult> documentSearchResponse) {
        super(documentSearchResponse.getRequest(),
            documentSearchResponse.getStatusCode(),
            documentSearchResponse.getHeaders(),
            documentSearchResponse.getValue().getResults().stream().map(SuggestResultConverter::map)
                .collect(Collectors.toList()),
            null,
            null);
        this.coverage = documentSearchResponse.getValue().getCoverage();
    }
}
