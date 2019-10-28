// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.common;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteResult;

import java.util.stream.Collectors;

public class AutoCompletePagedResponse extends PagedResponseBase<String, AutocompleteItem> {

    /**
     * Constructor
     *
     * @param autoCompleteResponse an http response with the results
     */
    public AutoCompletePagedResponse(SimpleResponse<AutocompleteResult> autoCompleteResponse) {
        super(autoCompleteResponse.getRequest(),
            autoCompleteResponse.getStatusCode(),
            autoCompleteResponse.getHeaders(),
            autoCompleteResponse.getValue().getResults(),
            null,
            deserializeHeaders(autoCompleteResponse.getHeaders()));
    }

    private static String deserializeHeaders(HttpHeaders headers) {
        return headers.toMap().entrySet().stream().map((entry) ->
            entry.getKey() + "," + entry.getValue()
        ).collect(Collectors.joining(","));
    }
}
