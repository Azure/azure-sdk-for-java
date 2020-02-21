// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;

import java.util.List;

/**
 * // TODO: add java doc string
 *
 *
 * @param <T> The type of items contained in the {@link Page}
 */
@Immutable
public final class TextAnalyticsPagedResponse<T> extends PagedResponseBase<Void, T> {
    private final String modelVersion;
    private final TextDocumentBatchStatistics statistics;

    /**
     * // TODO: update it
     * @param request a
     * @param statusCode a
     * @param headers a
     * @param items a
     * @param continuationToken a
     * @param modelVersion a
     * @param statistics a
     */
    public TextAnalyticsPagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items,
        String continuationToken, String modelVersion, TextDocumentBatchStatistics statistics) {
        super(request, statusCode, headers, items, continuationToken, null);
        this.modelVersion = modelVersion;
        this.statistics = statistics;
    }

    /**
     * // TODO: update it
     *
     * @return a
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * // TODO: update it
     *
     * @return a
     */
    public TextDocumentBatchStatistics getStatistics() {
        return statistics;
    }
}
