// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.search.documents.SearchServiceVersion;

import java.io.IOException;

/**
 * Continuation token used when searching documents to iterate through REST API pages.
 */
public final class SearchContinuationToken implements JsonSerializable<SearchContinuationToken> {
    private final SearchRequest nextPageParameters;
    private final SearchServiceVersion apiVersion;

    /**
     * Creates a new {@link SearchContinuationToken}.
     *
     * @param nextPageParameters The {@link SearchRequest} to use when retrieving the next page of search results.
     * @param apiVersion The {@link SearchServiceVersion} used when searching, subsequent page requests must use the
     * same {@link SearchServiceVersion}.
     */
    public SearchContinuationToken(SearchRequest nextPageParameters, SearchServiceVersion apiVersion) {
        this.nextPageParameters = nextPageParameters;
        this.apiVersion = apiVersion;
    }

    /**
     * Get the nextPageParameters property: Continuation JSON payload returned when the query can't return all the
     * requested results in a single response. You can use this JSON along with.
     *
     * @return the nextPageParameters value.
     */
    public SearchRequest getNextPageParameters() {
        return this.nextPageParameters;
    }

    /**
     * Gets the apiVersion property: API version used when sending the query request. Must remain consistent for all
     * requests in the paged operation.
     *
     * @return the apiVersion value.
     */
    public SearchServiceVersion getApiVersion() {
        return this.apiVersion;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeJsonField("nextPageParameters", nextPageParameters)
            .writeStringField("apiVersion", apiVersion.getVersion())
            .writeEndObject();
    }
}
