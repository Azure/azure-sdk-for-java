// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonSerializable;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchRequest;
import reactor.core.publisher.Mono;

/**
 * Implementation utilities helper class.
 */
public final class SearchUtils {
    /**
     * Converts the public API {@link SearchOptions} into {@link SearchRequest}.
     *
     * @param options The {@link SearchOptions}.
     * @return An instance of {@link SearchRequest}.
     */
    public static SearchRequest fromSearchOptions(SearchOptions options) {
        if (options == null) {
            return null;
        }

        return new SearchRequest().setIncludeTotalCount(options.isIncludeTotalCount())
            .setFacets(options.getFacets())
            .setFilter(options.getFilter())
            .setHighlightFields(options.getHighlightFields())
            .setHighlightPostTag(options.getHighlightPostTag())
            .setHighlightPreTag(options.getHighlightPreTag())
            .setMinimumCoverage(options.getMinimumCoverage())
            .setOrderBy(options.getOrderBy())
            .setQueryType(options.getQueryType())
            .setScoringStatistics(options.getScoringStatistics())
            .setSessionId(options.getSessionId())
            .setScoringParameters(options.getScoringParameters())
            .setScoringProfile(options.getScoringProfile())
            .setDebug(options.getDebug())
            .setSearchText(options.getSearchText())
            .setSearchFields(options.getSearchFields())
            .setSearchMode(options.getSearchMode())
            .setSelect(options.getSelect())
            .setSkip(options.getSkip())
            .setTop(options.getTop())
            .setSemanticConfigurationName(options.getSemanticConfigurationName())
            .setSemanticErrorHandling(options.getSemanticErrorHandling())
            .setSemanticMaxWaitInMilliseconds(options.getSemanticMaxWaitInMilliseconds())
            .setSemanticQuery(options.getSemanticQuery())
            .setAnswers(options.getAnswers())
            .setCaptions(options.getCaptions())
            .setVectorQueries(options.getVectorQueries())
            .setVectorFilterMode(options.getVectorFilterMode());
    }

    /**
     * Adds headers from {@link SearchOptions} to {@link RequestOptions}.
     *
     * @param requestOptions The {@link RequestOptions}.
     * @param searchOptions The {@link SearchOptions}.
     * @return The updated {@link RequestOptions}.
     */
    public static RequestOptions addSearchHeaders(RequestOptions requestOptions, SearchOptions searchOptions) {

        return requestOptions;
    }

    /**
     * Converts a {@link Response} of {@link BinaryData} to a {@link Response} of a specific type that extends
     * {@link JsonSerializable}.
     *
     * @param <T> The type of the class extending {@link JsonSerializable}.
     * @param response The {@link Response} of {@link BinaryData}.
     * @param clazz The class type to convert to.
     * @return The converted {@link Response}.
     */
    public static <T extends JsonSerializable<T>> Response<T> convertResponse(Response<BinaryData> response,
        Class<T> clazz) {
        return new SimpleResponse<>(response, response.getValue().toObject(clazz));
    }

    /**
     * Maps a {@link Mono} of {@link Response} of {@link BinaryData} to a {@link Mono} of {@link Response} of a
     * specific type that extends {@link JsonSerializable}.
     *
     * @param <T> The type of the class extending {@link JsonSerializable}.
     * @param mono The {@link Mono} of {@link Response} of {@link BinaryData}.
     * @param clazz The class type to convert to.
     * @return The mapped {@link Mono} of {@link Response}.
     */
    public static <T extends JsonSerializable<T>> Mono<Response<T>> mapResponse(Mono<Response<BinaryData>> mono,
        Class<T> clazz) {
        return mono.map(response -> new SimpleResponse<>(response, response.getValue().toObject(clazz)));
    }

    private SearchUtils() {
    }
}
