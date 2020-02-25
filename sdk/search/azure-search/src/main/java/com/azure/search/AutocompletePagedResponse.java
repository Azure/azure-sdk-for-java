// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteResult;

/**
 * This class represents a response from the autocomplete API. It contains the {@link AutocompleteItem
 * AutocompleteItems} returned from the service.
 */
public final class AutocompletePagedResponse extends PagedResponseBase<Void, AutocompleteItem> {

    /**
     * Creates an {@link AutocompletePagedResponse} from the returned {@link Response}.
     *
     * @param autocompleteResponse Autocomplete response returned from the service.
     */
    AutocompletePagedResponse(SimpleResponse<AutocompleteResult> autocompleteResponse) {
        super(autocompleteResponse.getRequest(),
            autocompleteResponse.getStatusCode(),
            autocompleteResponse.getHeaders(),
            autocompleteResponse.getValue().getResults(),
            null,
            null);
    }
}
