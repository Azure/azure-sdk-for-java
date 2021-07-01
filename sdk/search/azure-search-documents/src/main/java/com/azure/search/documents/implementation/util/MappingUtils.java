// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.documents.implementation.converters.IndexDocumentsResultConverter;
import com.azure.search.documents.implementation.converters.SearchIndexConverter;
import com.azure.search.documents.implementation.converters.SearchIndexerConverter;
import com.azure.search.documents.implementation.converters.SearchIndexerDataSourceConverter;
import com.azure.search.documents.implementation.models.IndexDocumentsResult;
import com.azure.search.documents.indexes.implementation.models.AnalyzeResult;
import com.azure.search.documents.indexes.implementation.models.ListDataSourcesResult;
import com.azure.search.documents.indexes.implementation.models.ListIndexersResult;
import com.azure.search.documents.indexes.implementation.models.ListSkillsetsResult;
import com.azure.search.documents.indexes.implementation.models.ListSynonymMapsResult;
import com.azure.search.documents.indexes.implementation.models.SearchErrorException;
import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SynonymMap;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MappingUtils {

    public static Response<SearchIndexerDataSourceConnection> mappingExternalDataSource(
        Response<com.azure.search.documents.indexes.implementation.models.SearchIndexerDataSource> dataSourceResponse) {
        return new SimpleResponse<>(dataSourceResponse,
            SearchIndexerDataSourceConverter.map(dataSourceResponse.getValue()));
    }

    public static PagedResponse<SearchIndexerDataSourceConnection> mappingPagingDataSource(
        Response<ListDataSourcesResult> dataSourceResponse) {
        List<SearchIndexerDataSourceConnection> dataSourceMaps = dataSourceResponse.getValue().getDataSources().stream()
            .map(SearchIndexerDataSourceConverter::map).collect(toList());
        return new PagedResponseBase<HttpHeaders, SearchIndexerDataSourceConnection>(
            dataSourceResponse.getRequest(), dataSourceResponse.getStatusCode(), dataSourceResponse.getHeaders(),
            dataSourceMaps, null, null);
    }

    public static PagedResponse<String> mappingPagingDataSourceNames(
        Response<ListDataSourcesResult> dataSourceResponse) {
        List<String> dataSourceNames = dataSourceResponse.getValue().getDataSources().stream()
            .map(SearchIndexerDataSourceConverter::map)
            .map(SearchIndexerDataSourceConnection::getName).collect(toList());
        return new PagedResponseBase<HttpHeaders, String>(
            dataSourceResponse.getRequest(), dataSourceResponse.getStatusCode(), dataSourceResponse.getHeaders(),
            dataSourceNames, null, null);
    }

    public static PagedResponse<String> mappingPagingSearchIndexNames(PagedResponse<SearchIndex>
        searchIndexResponse) {
        List<String> pageItems = new ArrayList<>();
        searchIndexResponse.getValue().forEach(item -> pageItems.add(item.getName()));
        return new PagedResponseBase<HttpHeaders, String>(
            searchIndexResponse.getRequest(), searchIndexResponse.getStatusCode(), searchIndexResponse.getHeaders(),
            pageItems, searchIndexResponse.getContinuationToken(), null);
    }

    public static Response<SearchIndex> mappingExternalSearchIndex(
        Response<com.azure.search.documents.indexes.implementation.models.SearchIndex> indexResponse) {
        return new SimpleResponse<>(indexResponse, SearchIndexConverter.map(indexResponse.getValue()));
    }

    public static PagedResponse<SearchIndex> mappingListingSearchIndex(
        PagedResponse<com.azure.search.documents.indexes.implementation.models.SearchIndex> indexResponse) {
        List<SearchIndex> pageItems = new ArrayList<>();
        indexResponse.getValue().forEach(item -> pageItems.add(SearchIndexConverter.map(item)));

        return new PagedResponseBase<HttpHeaders, SearchIndex>(indexResponse.getRequest(),
            indexResponse.getStatusCode(), indexResponse.getHeaders(), pageItems,
            indexResponse.getContinuationToken(), null);
    }

    public static PagedResponse<SearchIndexer> mappingPagingSearchIndexer(
        Response<ListIndexersResult> searchIndexerResponse) {
        List<SearchIndexer> searchIndexers = searchIndexerResponse.getValue().getIndexers().stream()
            .map(SearchIndexerConverter::map).collect(toList());
        return new PagedResponseBase<HttpHeaders, SearchIndexer>(
            searchIndexerResponse.getRequest(), searchIndexerResponse.getStatusCode(),
            searchIndexerResponse.getHeaders(), searchIndexers, null, null);
    }

    public static PagedResponse<String> mappingPagingSearchIndexerNames(
        Response<ListIndexersResult> searchIndexerResponse) {
        List<String> searchIndexerNames = searchIndexerResponse.getValue().getIndexers().stream()
            .map(SearchIndexerConverter::map).map(SearchIndexer::getName).collect(toList());
        return new PagedResponseBase<HttpHeaders, String>(
            searchIndexerResponse.getRequest(), searchIndexerResponse.getStatusCode(),
            searchIndexerResponse.getHeaders(), searchIndexerNames, null, null);
    }

    public static Response<SearchIndexer> mappingExternalSearchIndexer(
        Response<com.azure.search.documents.indexes.implementation.models.SearchIndexer> indexerResponse) {
        return new SimpleResponse<>(indexerResponse, SearchIndexerConverter.map(indexerResponse.getValue()));
    }

    public static PagedResponse<SearchIndexerSkillset> mappingPagingSkillset(
        Response<ListSkillsetsResult> skillsetResponse) {
        return new PagedResponseBase<HttpHeaders, SearchIndexerSkillset>(
            skillsetResponse.getRequest(), skillsetResponse.getStatusCode(), skillsetResponse.getHeaders(),
            skillsetResponse.getValue().getSkillsets(), null, null);
    }

    public static PagedResponse<String> mappingPagingSkillsetNames(
        Response<ListSkillsetsResult> skillsetResponse) {
        List<String> skillsetNames = skillsetResponse.getValue().getSkillsets().stream()
            .map(SearchIndexerSkillset::getName).collect(toList());
        return new PagedResponseBase<HttpHeaders, String>(
            skillsetResponse.getRequest(), skillsetResponse.getStatusCode(), skillsetResponse.getHeaders(),
            skillsetNames, null, null);
    }

    public static PagedResponse<SynonymMap> mappingPagingSynonymMap(
        Response<ListSynonymMapsResult> synonymMapResponse) {
        return new PagedResponseBase<HttpHeaders, SynonymMap>(
            synonymMapResponse.getRequest(), synonymMapResponse.getStatusCode(), synonymMapResponse.getHeaders(),
            synonymMapResponse.getValue().getSynonymMaps(), null, null);
    }

    public static PagedResponse<String> mappingPagingSynonymMapNames(
        Response<ListSynonymMapsResult> synonymMapsResponse) {
        List<String> synonymMapNames = synonymMapsResponse.getValue().getSynonymMaps().stream()
            .map(SynonymMap::getName).collect(toList());
        return new PagedResponseBase<HttpHeaders, String>(
            synonymMapsResponse.getRequest(), synonymMapsResponse.getStatusCode(), synonymMapsResponse.getHeaders(),
            synonymMapNames, null, null);
    }

    public static PagedResponse<AnalyzedTokenInfo> mappingTokenInfo(
        Response<AnalyzeResult> resultResponse) {
        List<AnalyzedTokenInfo> tokenInfos = resultResponse.getValue().getTokens();
        return new PagedResponseBase<HttpHeaders, AnalyzedTokenInfo>(
            resultResponse.getRequest(),
            resultResponse.getStatusCode(),
            resultResponse.getHeaders(), tokenInfos,
            null,
            null
        );
    }

    public static Response<com.azure.search.documents.models.IndexDocumentsResult> mappingIndexDocumentResultResponse(
        Response<IndexDocumentsResult> indexDocumentResponse) {
        return new SimpleResponse<>(indexDocumentResponse,
            IndexDocumentsResultConverter.map(indexDocumentResponse.getValue()));
    }

    public static Throwable exceptionMapper(Throwable throwable) {
        if (throwable instanceof SearchErrorException) {
            SearchErrorException exception = (SearchErrorException) throwable;
            return new HttpResponseException(exception.getMessage(), exception.getResponse());
        }

        if (throwable instanceof com.azure.search.documents.implementation.models.SearchErrorException) {
            com.azure.search.documents.implementation.models.SearchErrorException exception =
                (com.azure.search.documents.implementation.models.SearchErrorException) throwable;
            return new HttpResponseException(exception.getMessage(), exception.getResponse());
        }

        return throwable;
    }
}
