// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.documents.models.AutocompleteItem;
import com.azure.search.documents.models.AutocompleteResult;

/**
 * This class represents a response from the autocomplete API. It contains the {@link AutocompleteItem
 * AutocompleteItems} returned from the service.
 */
@Immutable
public final class AutocompletePagedResponse extends PagedResponseBase<Void, AutocompleteItem> {
    private final Double coverage;

    /**
     * Creates an {@link AutocompletePagedResponse} from the returned {@link Response}.
     *
     * @param autocompleteResponse Autocomplete response returned from the service.
     */
    public AutocompletePagedResponse(SimpleResponse<AutocompleteResult> autocompleteResponse) {
        super(autocompleteResponse.getRequest(), autocompleteResponse.getStatusCode(),
            autocompleteResponse.getHeaders(), autocompleteResponse.getValue().getResults(), null, null);

        this.coverage = autocompleteResponse.getValue().getCoverage();
    }

    /**
     * The percentage of the index covered in the autocomplete request.
     * <p>
     * If {@code minimumCoverage} wasn't supplied in the request this will be {@code null}.
     *
     * @return The percentage of the index covered in the suggest request if {@code minimumCoverage} was set in the
     * request, otherwise {@code null}.
     */
    public Double getCoverage() {
        return coverage;
    }
}
