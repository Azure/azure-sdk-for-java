// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.accesshelpers;

import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Helper class to access non-public APIs of {@link FeedResponse}.
 */
public final class FeedResponseHelper {
    private static FeedResponseAccessor accessor;

    /*
     * Since FeedResponseAccessor has an API that will call the constructor of FeedResponse it will need to ensure that
     * FeedResponse has been loaded.
     */
    static {
        // Access the FeedResponse class to ensure it has been loaded.
        try {
            Class<?> ensureLoaded = Class.forName(FeedResponse.class.getName());
        } catch (ClassNotFoundException ex) {
            // This should never happen.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Type defining the methods that access non-public APIs of {@link FeedResponse}.
     */
    public interface FeedResponseAccessor {
        /**
         * Converts the {@code response} to a {@link FeedResponse} page.
         *
         * @param response The document service response being converted to a {@link FeedResponse} page.
         * @param factoryMethod Method that converts the document service response.
         * @param cls The class type.
         * @param <T> The class type.
         * @return A {@link FeedResponse} page converted from the document service response.
         */
        <T> FeedResponse<T> toFeedResponsePage(RxDocumentServiceResponse response, Function<JsonNode, T> factoryMethod,
            Class<T> cls);

        /**
         * Converts the list of results to a {@link FeedResponse} page.
         *
         * @param results The list of results being converted to a {@link FeedResponse} page.
         * @param headers The response headers.
         * @param noChanges Whether there are no changes.
         * @param <T> The result type.
         * @return A {@link FeedResponse} page converted from the list of results.
         */
        <T> FeedResponse<T> toFeedResponsePage(List<T> results, Map<String, String> headers, boolean noChanges);

        /**
         * Converts the {@code response} to a {@link FeedResponse} page.
         *
         * @param response The document service response being converted to a {@link FeedResponse} page.
         * @param factoryMethod Method that converts the document service response.
         * @param cls The class type.
         * @param <T> The class type.
         * @return A {@link FeedResponse} page converted from the document service response.
         */
        <T> FeedResponse<T> toChangeFeedResponsePage(RxDocumentServiceResponse response,
            Function<JsonNode, T> factoryMethod, Class<T> cls);

        /**
         * Whether there are no changes in the {@link FeedResponse}.
         *
         * @param page The {@link FeedResponse}.
         * @param <T> The class type.
         * @return Whether there are no changes in the {@link FeedResponse}.
         */
        <T> boolean noChanges(FeedResponse<T> page);

        /**
         * Creates a new instance of {@link FeedResponse}.
         *
         * @param results The list of results being converted to a {@link FeedResponse} page.
         * @param headers The response headers.
         * @param <T> The result type.
         * @return A new instance of {@link FeedResponse}.
         */
        <T> FeedResponse<T> createFeedResponse(List<T> results, Map<String, String> headers);

        /**
         * Creates a new instance of {@link FeedResponse}.
         *
         * @param results The list of results being converted to a {@link FeedResponse} page.
         * @param headers The response headers.
         * @param queryMetricsMap The query metric map.
         * @param diagnosticsContext The diagnostic context.
         * @param useEtagAsContinuation Whether to use ETag as continuation.
         * @param isNoChanges Whether there are no changes in the {@link FeedResponse}.
         * @param <T> The result type.
         * @return A new instance of {@link FeedResponse}.
         */
        <T> FeedResponse<T> createFeedResponseWithQueryMetrics(List<T> results, Map<String, String> headers,
            ConcurrentMap<String, QueryMetrics> queryMetricsMap,
            QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext,
            boolean useEtagAsContinuation, boolean isNoChanges);

        /**
         * Gets the query metrics map of the {@link FeedResponse}.
         *
         * @param feedResponse The {@link FeedResponse}.
         * @param <T> The result type.
         * @return The {@link FeedResponse} query metrics map.
         */
        <T> ConcurrentMap<String, QueryMetrics> queryMetricsMap(FeedResponse<T> feedResponse);

        /**
         * Gets the query metrics of the {@link FeedResponse}.
         *
         * @param feedResponse The {@link FeedResponse}.
         * @param <T> The result type.
         * @return The {@link FeedResponse} query metrics.
         */
        <T> ConcurrentMap<String, QueryMetrics> queryMetrics(FeedResponse<T> feedResponse);

        /**
         * Gets the query plan diagnostics context of the {@link FeedResponse}.
         *
         * @param feedResponse The {@link FeedResponse}.
         * @param <T> The class type.
         * @return The query plan diagnostics context.
         */
        <T> QueryInfo.QueryPlanDiagnosticsContext getQueryPlanDiagnosticsContext(FeedResponse<T> feedResponse);

        /**
         * Converts an instance of {@link FeedResponse} using a conversion function to a new type.
         *
         * @param feedResponse The {@link FeedResponse} to convert.
         * @param conversion The conversion function.
         * @param <T> The current type.
         * @param <TNew> The new type.
         * @return A new instance of {@link FeedResponse} with converted values.
         */
        <T, TNew> FeedResponse<TNew> convertGenericType(FeedResponse<T> feedResponse, Function<T, TNew> conversion);
    }

    /**
     * The method called from {@link FeedResponse} to set its accessor.
     *
     * @param feedResponseAccessor The accessor.
     */
    public static void setAccessor(final FeedResponseAccessor feedResponseAccessor) {
        accessor = feedResponseAccessor;
    }

    /**
     * Converts the {@code response} to a {@link FeedResponse} page.
     *
     * @param response The document service response being converted to a {@link FeedResponse} page.
     * @param factoryMethod Method that converts the document service response.
     * @param cls The class type.
     * @param <T> The class type.
     * @return A {@link FeedResponse} page converted from the document service response.
     */
    public static <T> FeedResponse<T> toFeedResponsePage(RxDocumentServiceResponse response,
        Function<JsonNode, T> factoryMethod, Class<T> cls) {
        return accessor.toFeedResponsePage(response, factoryMethod, cls);
    }

    /**
     * Converts the list of results to a {@link FeedResponse} page.
     *
     * @param results The list of results being converted to a {@link FeedResponse} page.
     * @param headers The response headers.
     * @param noChanges Whether there are no changes.
     * @param <T> The result type.
     * @return A {@link FeedResponse} page converted from the list of results.
     */
    public static <T> FeedResponse<T> toFeedResponsePage(List<T> results, Map<String, String> headers,
        boolean noChanges) {
        return accessor.toFeedResponsePage(results, headers, noChanges);
    }

    /**
     * Converts the {@code response} to a {@link FeedResponse} page.
     *
     * @param response The document service response being converted to a {@link FeedResponse} page.
     * @param factoryMethod Method that converts the document service response.
     * @param cls The class type.
     * @param <T> The class type.
     * @return A {@link FeedResponse} page converted from the document service response.
     */
    public static <T> FeedResponse<T> toChangeFeedResponsePage(RxDocumentServiceResponse response,
        Function<JsonNode, T> factoryMethod, Class<T> cls) {
        return accessor.toChangeFeedResponsePage(response, factoryMethod, cls);
    }

    /**
     * Whether there are no changes in the {@link FeedResponse}.
     *
     * @param page The {@link FeedResponse}.
     * @param <T> The class type.
     * @return Whether there are no changes in the {@link FeedResponse}.
     */
    public static <T> boolean noChanges(FeedResponse<T> page) {
        return accessor.noChanges(page);
    }

    /**
     * Creates a new instance of {@link FeedResponse}.
     *
     * @param results The list of results being converted to a {@link FeedResponse} page.
     * @param headers The response headers.
     * @param <T> The result type.
     * @return A new instance of {@link FeedResponse}.
     */
    public static <T> FeedResponse<T> createFeedResponse(List<T> results, Map<String, String> headers) {
        return accessor.createFeedResponse(results, headers);
    }

    /**
     * Creates a new instance of {@link FeedResponse}.
     *
     * @param results The list of results being converted to a {@link FeedResponse} page.
     * @param headers The response headers.
     * @param queryMetricsMap The query metric map.
     * @param diagnosticsContext The diagnostic context.
     * @param useEtagAsContinuation Whether to use ETag as continuation.
     * @param isNoChanges Whether there are no changes in the {@link FeedResponse}.
     * @param <T> The result type.
     * @return A new instance of {@link FeedResponse}.
     */
    public static <T> FeedResponse<T> createFeedResponseWithQueryMetrics(List<T> results, Map<String, String> headers,
        ConcurrentMap<String, QueryMetrics> queryMetricsMap,
        QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext,
        boolean useEtagAsContinuation, boolean isNoChanges) {
        return accessor.createFeedResponseWithQueryMetrics(results, headers, queryMetricsMap, diagnosticsContext,
            useEtagAsContinuation, isNoChanges);
    }

    /**
     * Gets the query metrics map of the {@link FeedResponse}.
     *
     * @param feedResponse The {@link FeedResponse}.
     * @param <T> The result type.
     * @return The {@link FeedResponse} query metrics map.
     */
    public static <T> ConcurrentMap<String, QueryMetrics> queryMetricsMap(FeedResponse<T> feedResponse) {
        return accessor.queryMetricsMap(feedResponse);
    }

    /**
     * Gets the query metrics of the {@link FeedResponse}.
     *
     * @param feedResponse The {@link FeedResponse}.
     * @param <T> The result type.
     * @return The {@link FeedResponse} query metrics.
     */
    public static <T> ConcurrentMap<String, QueryMetrics> queryMetrics(FeedResponse<T> feedResponse) {
        return accessor.queryMetrics(feedResponse);
    }

    /**
     * Gets the query plan diagnostics context of the {@link FeedResponse}.
     *
     * @param feedResponse The {@link FeedResponse}.
     * @param <T> The class type.
     * @return The query plan diagnostics context.
     */
    public static <T> QueryInfo.QueryPlanDiagnosticsContext getQueryPlanDiagnosticsContext(
        FeedResponse<T> feedResponse) {
        return accessor.getQueryPlanDiagnosticsContext(feedResponse);
    }

    /**
     * Converts an instance of {@link FeedResponse} using a conversion function to a new type.
     *
     * @param feedResponse The {@link FeedResponse} to convert.
     * @param conversion The conversion function.
     * @param <T> The current type.
     * @param <TNew> The new type.
     * @return A new instance of {@link FeedResponse} with converted values.
     */
    public static <T, TNew> FeedResponse<TNew> convertGenericType(FeedResponse<T> feedResponse,
        Function<T, TNew> conversion) {
        return accessor.convertGenericType(feedResponse, conversion);
    }
}
