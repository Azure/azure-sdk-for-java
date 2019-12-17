// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteResult;

import java.util.stream.Collectors;

/**
 * Represents an HTTP response from the autocomplete API request
 * that contains a list of items deserialized into a {@link Page}.
 */
public class AutocompletePagedResponse extends PagedResponseBase<String, AutocompleteItem> {

    /**
     * Constructor
     * @param autocompleteResponse an http response with the results
     */
    public AutocompletePagedResponse(SimpleResponse<AutocompleteResult> autocompleteResponse) {
        super(autocompleteResponse.getRequest(),
            autocompleteResponse.getStatusCode(),
            autocompleteResponse.getHeaders(),
            autocompleteResponse.getValue().getResults(),
            null,
            deserializeHeaders(autocompleteResponse.getHeaders()));
    }

    private static String deserializeHeaders(HttpHeaders headers) {
        return headers.toMap().entrySet().stream().map((entry) ->
            entry.getKey() + "," + entry.getValue()
        ).collect(Collectors.joining(","));
    }
}
