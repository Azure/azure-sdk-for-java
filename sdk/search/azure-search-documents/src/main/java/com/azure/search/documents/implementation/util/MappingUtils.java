// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.documents.implementation.converters.GetIndexStatisticsResultConverter;
import com.azure.search.documents.implementation.converters.IndexerExecutionInfoConverter;
import com.azure.search.documents.implementation.converters.RequestOptionsConverter;
import com.azure.search.documents.implementation.converters.SearchIndexConverter;
import com.azure.search.documents.implementation.converters.SearchIndexerConverter;
import com.azure.search.documents.implementation.converters.SearchIndexerDataSourceConverter;
import com.azure.search.documents.implementation.converters.SearchIndexerSkillsetConverter;
import com.azure.search.documents.implementation.converters.ServiceStatisticsConverter;
import com.azure.search.documents.implementation.converters.SynonymMapConverter;
import com.azure.search.documents.implementation.converters.TokenInfoConverter;
import com.azure.search.documents.implementation.models.AnalyzeResult;
import com.azure.search.documents.implementation.models.ListDataSourcesResult;
import com.azure.search.documents.implementation.models.ListIndexersResult;
import com.azure.search.documents.implementation.models.ListIndexesResult;
import com.azure.search.documents.implementation.models.ListSkillsetsResult;
import com.azure.search.documents.implementation.models.ListSynonymMapsResult;
import com.azure.search.documents.models.AnalyzedTokenInfo;
import com.azure.search.documents.models.GetIndexStatisticsResult;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchIndex;
import com.azure.search.documents.models.SearchIndexer;
import com.azure.search.documents.models.SearchIndexerDataSource;
import com.azure.search.documents.models.SearchIndexerSkillset;
import com.azure.search.documents.models.SearchIndexerStatus;
import com.azure.search.documents.models.ServiceStatistics;
import com.azure.search.documents.models.SynonymMap;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class MappingUtils {

    public static Response<SearchIndexerDataSource> mappingExternalDataSource(
        Response<com.azure.search.documents.implementation.models.SearchIndexerDataSource> dataSourceResponse) {
        return new SimpleResponse<>(dataSourceResponse,
            SearchIndexerDataSourceConverter.convert(dataSourceResponse.getValue()));
    }

    public static PagedResponse<SearchIndexerDataSource> mappingPagingDataSource(
        Response<ListDataSourcesResult> dataSourceResponse) {
        List<SearchIndexerDataSource> dataSourceMaps = dataSourceResponse.getValue().getDataSources().stream()
            .map(SearchIndexerDataSourceConverter::convert).collect(toList());
        return new PagedResponseBase<HttpHeaders, SearchIndexerDataSource>(
            dataSourceResponse.getRequest(), dataSourceResponse.getStatusCode(), dataSourceResponse.getHeaders(),
            dataSourceMaps, null, null);
    }

    public static PagedResponse<SearchIndex> mappingPagingSearchIndex(
        Response<ListIndexesResult> searchIndexResponse) {
        List<SearchIndex> searchIndices = searchIndexResponse.getValue().getIndexes().stream()
            .map(SearchIndexConverter::convert).collect(toList());
        return new PagedResponseBase<HttpHeaders, SearchIndex>(
            searchIndexResponse.getRequest(), searchIndexResponse.getStatusCode(), searchIndexResponse.getHeaders(),
            searchIndices, null, null);
    }

    public static Response<SearchIndex> mappingExternalSearchIndex(
        Response<com.azure.search.documents.implementation.models.SearchIndex> indexResponse) {
        return new SimpleResponse<>(indexResponse, SearchIndexConverter.convert(indexResponse.getValue()));
    }

    public static PagedResponse<SearchIndexer> mappingPagingSearchIndexer(
        Response<ListIndexersResult> searchIndexerResponse) {
        List<SearchIndexer> searchIndexers = searchIndexerResponse.getValue().getIndexers().stream()
            .map(SearchIndexerConverter::convert).collect(toList());
        return new PagedResponseBase<HttpHeaders, SearchIndexer>(
            searchIndexerResponse.getRequest(), searchIndexerResponse.getStatusCode(), searchIndexerResponse.getHeaders(),
            searchIndexers, null, null);
    }

    public static Response<SearchIndexer> mappingExternalSearchIndexer(
        Response<com.azure.search.documents.implementation.models.SearchIndexer> indexerResponse) {
        return new SimpleResponse<>(indexerResponse, SearchIndexerConverter.convert(indexerResponse.getValue()));
    }

    public static Response<SearchIndexerSkillset> mappingExternalSkillset(
        Response<com.azure.search.documents.implementation.models.SearchIndexerSkillset> skillsetResponse) {
        return new SimpleResponse<>(skillsetResponse,
            SearchIndexerSkillsetConverter.convert(skillsetResponse.getValue()));
    }

    public static PagedResponse<SearchIndexerSkillset> mappingPagingSkillset(
        Response<ListSkillsetsResult> skillsetResponse) {
        List<SearchIndexerSkillset> synonymMaps = skillsetResponse.getValue().getSkillsets().stream()
            .map(SearchIndexerSkillsetConverter::convert).collect(toList());
        return new PagedResponseBase<HttpHeaders, SearchIndexerSkillset>(
            skillsetResponse.getRequest(), skillsetResponse.getStatusCode(), skillsetResponse.getHeaders(),
            synonymMaps, null, null);
    }

    public static Response<SynonymMap> mappingExternalSynonymMap(
        Response<com.azure.search.documents.implementation.models.SynonymMap> synonymMapResponse) {
        return new SimpleResponse<>(synonymMapResponse, SynonymMapConverter.convert(synonymMapResponse.getValue()));
    }

    public static PagedResponse<SynonymMap> mappingPagingSynonymMap(
        Response<ListSynonymMapsResult> synonymMapResponse) {
        List<SynonymMap> synonymMaps = synonymMapResponse.getValue().getSynonymMaps().stream()
            .map(SynonymMapConverter::convert).collect(toList());
        return new PagedResponseBase<HttpHeaders, SynonymMap>(
            synonymMapResponse.getRequest(), synonymMapResponse.getStatusCode(), synonymMapResponse.getHeaders(),
            synonymMaps, null, null);
    }

    public static Response<ServiceStatistics> mappingExternalServiceStatistics(
        Response<com.azure.search.documents.implementation.models.ServiceStatistics> statisticsResponse) {
        return new SimpleResponse<>(statisticsResponse,
            ServiceStatisticsConverter.convert(statisticsResponse.getValue()));
    }

    public static PagedResponse<AnalyzedTokenInfo> mappingTokenInfo(Response<AnalyzeResult> resultResponse) {
        List<AnalyzedTokenInfo> tokenInfos = resultResponse.getValue().getTokens().stream().map(TokenInfoConverter::convert).collect(toList());
        return new PagedResponseBase<HttpHeaders, com.azure.search.documents.models.AnalyzedTokenInfo> (
            resultResponse.getRequest(),
            resultResponse.getStatusCode(),
            resultResponse.getHeaders(), tokenInfos,
            null,
            null
        );
    }

    public static Response<SearchIndexerStatus> mappingIndexerStatus(Response<com.azure.search.documents.implementation.models.SearchIndexerStatus> indexerStatusResponse) {
        return new SimpleResponse<>(indexerStatusResponse, IndexerExecutionInfoConverter.convert(indexerStatusResponse.getValue()));
    }

    public static Response<GetIndexStatisticsResult> mappingGetIndexStatistics(Response<com.azure.search.documents.implementation.models.GetIndexStatisticsResult> indexStatisticsResponse) {
        return new SimpleResponse<>(indexStatisticsResponse.getRequest(), indexStatisticsResponse.getStatusCode(),
            indexStatisticsResponse.getHeaders(),
            GetIndexStatisticsResultConverter.convert(indexStatisticsResponse.getValue()));
    }
}
