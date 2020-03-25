// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;

import java.util.List;

/**
 * This type extends {@link PagedResponse} along with the model version that trained in service and the
 * {@link TextDocumentBatchStatistics batch statistics of response}.
 *
 * @param <T> The type of items contained in the {@link TextAnalyticsPagedResponse}
 *
 * @see PagedResponseBase
 */
@Immutable
public final class TextAnalyticsPagedResponse<T> extends PagedResponseBase<Void, T> {
    private final String modelVersion;
    private final TextDocumentBatchStatistics statistics;

    /**
     * Create a new instance of the {@link TextAnalyticsPagedResponse}.
     *
     * @param request The HttpRequest that was sent to the service whose response resulted in this response.
     * @param statusCode The status code from the response.
     * @param headers The headers from the response.
     * @param items The items returned from the service within the response.
     * @param continuationToken The continuation token returned from the service, to enable future requests to pick up
     *      from the same place in the paged iteration.
     * @param modelVersion The model version trained in service for the request.
     * @param statistics The batch statistics of response.
     */
    public TextAnalyticsPagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items,
        String continuationToken, String modelVersion, TextDocumentBatchStatistics statistics) {
        super(request, statusCode, headers, items, continuationToken, null);
        this.modelVersion = modelVersion;
        this.statistics = statistics;
    }

    /**
     * Get the model version trained in service for the request.
     *
     * @return The model version trained in service for the request.
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Get the batch statistics of response.
     *
     * @return The batch statistics of response.
     */
    public TextDocumentBatchStatistics getStatistics() {
        return statistics;
    }
}
