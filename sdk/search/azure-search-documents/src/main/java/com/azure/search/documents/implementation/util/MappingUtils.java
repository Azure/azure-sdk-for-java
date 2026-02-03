// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.implementation.models.ErrorResponseException;
import com.azure.search.documents.indexes.implementation.models.AnalyzeResult;
import com.azure.search.documents.indexes.implementation.models.ListDataSourcesResult;
import com.azure.search.documents.indexes.implementation.models.ListIndexersResult;
import com.azure.search.documents.indexes.implementation.models.ListSkillsetsResult;
import com.azure.search.documents.indexes.implementation.models.ListSynonymMapsResult;
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

public class MappingUtils {

    public static PagedResponse<SearchIndexerDataSourceConnection>
        mapPagedDataSources(Response<ListDataSourcesResult> response) {
        return pagedResponse(response, response.getValue().getDataSources());
    }

    public static PagedResponse<String> mapPagedDataSourceNames(Response<ListDataSourcesResult> response) {
        return pagedResponse(response,
            mapToNames(response.getValue().getDataSources(), SearchIndexerDataSourceConnection::getName));
    }

    public static PagedResponse<String> mapPagedSearchIndexNames(PagedResponse<SearchIndex> response) {
        return new PagedResponseBase<HttpHeaders, String>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), mapToNames(response.getValue(), SearchIndex::getName),
            response.getContinuationToken(), null);
    }

    public static PagedResponse<SearchIndexer> mapPagedSearchIndexers(Response<ListIndexersResult> response) {
        return pagedResponse(response, response.getValue().getIndexers());
    }

    public static PagedResponse<String> mapPagedSearchIndexerNames(Response<ListIndexersResult> response) {
        return pagedResponse(response, mapToNames(response.getValue().getIndexers(), SearchIndexer::getName));
    }

    public static PagedResponse<SearchIndexerSkillset> mapPagedSkillsets(Response<ListSkillsetsResult> response) {
        return pagedResponse(response, response.getValue().getSkillsets());
    }

    public static PagedResponse<String> mapPagedSkillsetNames(Response<ListSkillsetsResult> response) {
        return pagedResponse(response, mapToNames(response.getValue().getSkillsets(), SearchIndexerSkillset::getName));
    }

    public static PagedResponse<SynonymMap> mapPagedSynonymMaps(Response<ListSynonymMapsResult> response) {
        return pagedResponse(response, response.getValue().getSynonymMaps());
    }

    public static PagedResponse<String> mapPagedSynonymMapNames(Response<ListSynonymMapsResult> response) {
        return pagedResponse(response, mapToNames(response.getValue().getSynonymMaps(), SynonymMap::getName));
    }

    public static PagedResponse<AnalyzedTokenInfo> mapPagedTokenInfos(Response<AnalyzeResult> response) {
        return pagedResponse(response, response.getValue().getTokens());
    }

    public static Throwable exceptionMapper(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException exception = (ErrorResponseException) throwable;
            return new HttpResponseException(exception.getMessage(), exception.getResponse());
        }

        if (throwable instanceof com.azure.search.documents.indexes.implementation.models.ErrorResponseException) {
            com.azure.search.documents.indexes.implementation.models.ErrorResponseException exception
                = (com.azure.search.documents.indexes.implementation.models.ErrorResponseException) throwable;
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
    public static IndexingParametersConfiguration
        mapToIndexingParametersConfiguration(Map<String, Object> configuration) {
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
                    config
                        .setPdfTextRotationAlgorithm(converter(value, BlobIndexerPdfTextRotationAlgorithm::fromString));
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

    private static <T> PagedResponse<T> pagedResponse(Response<?> response, List<T> values) {
        return new PagedResponseBase<HttpHeaders, T>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), values, null, null);
    }

    private static <T> List<String> mapToNames(List<T> values, Function<T, String> mapper) {
        return values.stream().map(mapper).collect(() -> new ArrayList<>(values.size()), List::add, List::addAll);
    }
}
