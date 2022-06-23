// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.implementation.converters.IndexDocumentsResultConverter;
import com.azure.search.documents.implementation.models.IndexDocumentsResult;
import com.azure.search.documents.indexes.implementation.models.AnalyzeResult;
import com.azure.search.documents.indexes.implementation.models.ListDataSourcesResult;
import com.azure.search.documents.indexes.implementation.models.ListIndexersResult;
import com.azure.search.documents.indexes.implementation.models.ListSkillsetsResult;
import com.azure.search.documents.indexes.implementation.models.ListSynonymMapsResult;
import com.azure.search.documents.indexes.implementation.models.SearchErrorException;
import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;
import com.azure.search.documents.indexes.models.BlobIndexerDataToExtract;
import com.azure.search.documents.indexes.models.BlobIndexerImageAction;
import com.azure.search.documents.indexes.models.BlobIndexerParsingMode;
import com.azure.search.documents.indexes.models.BlobIndexerPdfTextRotationAlgorithm;
import com.azure.search.documents.indexes.models.IndexerExecutionEnvironment;
import com.azure.search.documents.indexes.models.IndexingParametersConfiguration;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SynonymMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class MappingUtils {

    public static PagedResponse<SearchIndexerDataSourceConnection> mappingPagingDataSource(
        Response<ListDataSourcesResult> dataSourceResponse) {
        List<SearchIndexerDataSourceConnection> dataSourceMaps = dataSourceResponse.getValue().getDataSources();
        return new PagedResponseBase<HttpHeaders, SearchIndexerDataSourceConnection>(
            dataSourceResponse.getRequest(), dataSourceResponse.getStatusCode(), dataSourceResponse.getHeaders(),
            dataSourceMaps, null, null);
    }

    public static PagedResponse<String> mappingPagingDataSourceNames(
        Response<ListDataSourcesResult> dataSourceResponse) {
        List<String> dataSourceNames = dataSourceResponse.getValue().getDataSources().stream()
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

    public static PagedResponse<SearchIndexer> mappingPagingSearchIndexer(
        Response<ListIndexersResult> searchIndexerResponse) {
        List<SearchIndexer> searchIndexers = searchIndexerResponse.getValue().getIndexers();
        return new PagedResponseBase<HttpHeaders, SearchIndexer>(
            searchIndexerResponse.getRequest(), searchIndexerResponse.getStatusCode(),
            searchIndexerResponse.getHeaders(), searchIndexers, null, null);
    }

    public static PagedResponse<String> mappingPagingSearchIndexerNames(
        Response<ListIndexersResult> searchIndexerResponse) {
        List<String> searchIndexerNames = searchIndexerResponse.getValue().getIndexers().stream()
            .map(SearchIndexer::getName).collect(toList());
        return new PagedResponseBase<HttpHeaders, String>(
            searchIndexerResponse.getRequest(), searchIndexerResponse.getStatusCode(),
            searchIndexerResponse.getHeaders(), searchIndexerNames, null, null);
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

    /**
     * Helper method to convert a {@link Map} of configurations to an {@link IndexingParametersConfiguration}.
     *
     * @param configuration The Map of configurations.
     * @return An {@link IndexingParametersConfiguration} based on the Map of configurations or null if the Map was
     * null or empty.
     */
    public static IndexingParametersConfiguration mapToIndexingParametersConfiguration(
        Map<String, Object> configuration) {
        if (CoreUtils.isNullOrEmpty(configuration)) {
            return null;
        }

        IndexingParametersConfiguration config = new IndexingParametersConfiguration();

        Map<String, Object> additionalProperties = null;
        for (Map.Entry<String, Object> kvp : configuration.entrySet()) {
            String key = kvp.getKey();
            if (key == null) {
                continue;
            }

            Object value = kvp.getValue();
            switch (key) {
                case "parsingMode":
                    config.setParsingMode(converter(value, BlobIndexerParsingMode::fromString));
                    break;

                case "excludedFileNameExtensions":
                    config.setExcludedFileNameExtensions(converter(value, Function.identity()));
                    break;

                case "indexedFileNameExtensions":
                    config.setIndexedFileNameExtensions(converter(value, Function.identity()));
                    break;

                case "failOnUnsupportedContentType":
                    config.setFailOnUnsupportedContentType(converter(value, Boolean::parseBoolean));
                    break;

                case "failOnUnprocessableDocument":
                    config.setFailOnUnprocessableDocument(converter(value, Boolean::parseBoolean));
                    break;

                case "indexStorageMetadataOnlyForOversizedDocuments":
                    config.setIndexStorageMetadataOnlyForOversizedDocuments(converter(value, Boolean::parseBoolean));
                    break;

                case "delimitedTextHeaders":
                    config.setDelimitedTextHeaders(converter(value, Function.identity()));
                    break;

                case "delimitedTextDelimiter":
                    config.setDelimitedTextDelimiter(converter(value, Function.identity()));
                    break;

                case "firstLineContainsHeaders":
                    config.setFirstLineContainsHeaders(converter(value, Boolean::parseBoolean));
                    break;

                case "documentRoot":
                    config.setDocumentRoot(converter(value, Function.identity()));
                    break;

                case "dataToExtract":
                    config.setDataToExtract(converter(value, BlobIndexerDataToExtract::fromString));
                    break;

                case "imageAction":
                    config.setImageAction(converter(value, BlobIndexerImageAction::fromString));
                    break;

                case "allowSkillsetToReadFileData":
                    config.setAllowSkillsetToReadFileData(converter(value, Boolean::parseBoolean));
                    break;

                case "pdfTextRotationAlgorithm":
                    config.setPdfTextRotationAlgorithm(
                        converter(value, BlobIndexerPdfTextRotationAlgorithm::fromString));
                    break;

                case "executionEnvironment":
                    config.setExecutionEnvironment(converter(value, IndexerExecutionEnvironment::fromString));
                    break;

                case "queryTimeout":
                    config.setQueryTimeout(converter(value, Function.identity()));
                    break;

                default:
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(key, value);
                    break;
            }
        }

        return config.setAdditionalProperties(additionalProperties);
    }

    private static <T> T converter(Object value, Function<String, T> conv) {
        return value == null ? null : conv.apply(String.valueOf(value));
    }

    public static Map<String, Object> indexingParametersConfigurationToMap(IndexingParametersConfiguration params) {
        if (params == null) {
            return null;
        }

        Map<String, Object> configuration = new LinkedHashMap<>();

        setConfigurationValue(params.getParsingMode(), "parsingMode", configuration);
        setConfigurationValue(params.getExcludedFileNameExtensions(), "excludedFileNameExtensions", configuration);
        setConfigurationValue(params.getIndexedFileNameExtensions(), "indexedFileNameExtensions", configuration);
        setConfigurationValue(params.isFailOnUnsupportedContentType(), "failOnUnsupportedContentType", configuration);
        setConfigurationValue(params.isFailOnUnprocessableDocument(), "failOnUnprocessableDocument", configuration);
        setConfigurationValue(params.isIndexStorageMetadataOnlyForOversizedDocuments(),
            "indexStorageMetadataOnlyForOversizedDocuments", configuration);
        setConfigurationValue(params.getDelimitedTextHeaders(), "delimitedTextHeaders", configuration);
        setConfigurationValue(params.getDelimitedTextDelimiter(), "delimitedTextDelimiter", configuration);
        setConfigurationValue(params.isFirstLineContainsHeaders(), "firstLineContainsHeaders", configuration);
        setConfigurationValue(params.getDocumentRoot(), "documentRoot", configuration);
        setConfigurationValue(params.getDataToExtract(), "dataToExtract", configuration);
        setConfigurationValue(params.getImageAction(), "imageAction", configuration);
        setConfigurationValue(params.isAllowSkillsetToReadFileData(), "allowSkillsetToReadFileData", configuration);
        setConfigurationValue(params.getPdfTextRotationAlgorithm(), "pdfTextRotationAlgorithm", configuration);
        setConfigurationValue(params.getExecutionEnvironment(), "executionEnvironment", configuration);
        setConfigurationValue(params.getQueryTimeout(), "queryTimeout", configuration);

        Map<String, Object> additionalProperties = params.getAdditionalProperties();
        if (!CoreUtils.isNullOrEmpty(additionalProperties)) {
            configuration.putAll(additionalProperties);
        }

        return configuration;
    }

    private static void setConfigurationValue(Object value, String key, Map<String, Object> configuration) {
        if (value == null) {
            return;
        }

        configuration.put(key, String.valueOf(value));
    }
}
