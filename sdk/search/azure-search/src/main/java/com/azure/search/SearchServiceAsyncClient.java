// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.implementation.SearchServiceRestClientBuilder;
import com.azure.search.implementation.SearchServiceRestClientImpl;
import com.azure.search.models.AnalyzeResult;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceListResult;
import com.azure.search.models.Index;
import com.azure.search.models.IndexGetStatisticsResult;
import com.azure.search.models.IndexListResult;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.IndexerListResult;
import com.azure.search.models.Skillset;
import com.azure.search.models.SkillsetListResult;
import com.azure.search.models.SynonymMap;
import com.azure.search.models.SynonymMapListResult;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.RequestOptions;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import static com.azure.core.implementation.util.FluxUtil.withContext;

@ServiceClient(builder = SearchServiceClientBuilder.class, isAsync = true)
public class SearchServiceAsyncClient {

    /**
     * Search Service dns suffix
     */
    private final String searchDnsSuffix;

    /**
     * Search REST API Version
     */
    private final String apiVersion;

    /**
     * The name of the Azure Cognitive Search service.
     */
    private final String searchServiceName;

    /**
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchServiceAsyncClient.class);

    /**
     * The underlying REST client to be used to actually interact with the Search service
     */
    private final SearchServiceRestClientImpl restClient;

    SearchServiceAsyncClient(
        String searchServiceName, String searchDnsSuffix, String apiVersion,
        HttpClient httpClient, List<HttpPipelinePolicy> policies) {
        if (StringUtils.isBlank(searchServiceName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid searchServiceName"));
        }
        if (StringUtils.isBlank(searchDnsSuffix)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid searchDnsSuffix"));
        }
        if (StringUtils.isBlank(apiVersion)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid apiVersion"));
        }
        if (httpClient == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid httpClient"));
        }
        if (policies == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid policies"));
        }

        this.searchServiceName = searchServiceName;
        this.searchDnsSuffix = searchDnsSuffix;
        this.apiVersion = apiVersion;
        this.restClient = new SearchServiceRestClientBuilder()
            .searchServiceName(searchServiceName)
            .searchDnsSuffix(searchDnsSuffix)
            .apiVersion(apiVersion)
            .pipeline(new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(policies.toArray(new HttpPipelinePolicy[0])).build())
            .build();
    }

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
    }

    /**
     * Gets The DNS suffix of the Azure Cognitive Search service. The default is search.windows.net.
     *
     * @return the searchDnsSuffix value.
     */
    public String getSearchDnsSuffix() {
        return this.searchDnsSuffix;
    }

    /**
     * Gets The name of the Azure Cognitive Search service.
     *
     * @return the searchServiceName value.
     */
    public String getSearchServiceName() {
        return this.searchServiceName;
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the created DataSource.
     */
    public Mono<DataSource> createDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the created DataSource.
     */
    public Mono<Response<DataSource>> createDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the DataSource.
     */
    public Mono<DataSource> getDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the DataSource.
     */
    public Mono<Response<DataSource>> getDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return all DataSources from the Search service.
     */
    public Mono<DataSourceListResult> listDataSources() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all DataSources from the Search service.
     */
    public Mono<Response<DataSourceListResult>> listDataSourcesWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the updated DataSource.
     */
    public Mono<DataSource> replaceDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated DataSource.
     */
    public Mono<Response<DataSource>> replaceDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Void> deleteDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Response<Response<Void>>> deleteDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the created Indexer.
     */
    public Mono<Indexer> createIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the created Indexer.
     */
    public Mono<Response<Indexer>> createIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Indexer.
     */
    public Mono<Indexer> getIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Indexer.
     */
    public Mono<Response<Indexer>> getIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return all Indexers from the Search service.
     */
    public Mono<IndexerListResult> listIndexers() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all Indexers from the Search service.
     */
    public Mono<Response<IndexerListResult>> listIndexersWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the updated Indexer.
     */
    public Mono<Indexer> replaceIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated Indexer.
     */
    public Mono<Response<Indexer>> replaceIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Void> deleteIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Response<Response<Void>>> deleteIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Void> resetIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Response<Response<Void>>> resetIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Void> runIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Response<Response<Void>>> runIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Indexer execution information.
     */
    public Mono<IndexerExecutionInfo> deleteIndexerStatus() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Indexer execution information.
     */
    public Mono<Response<IndexerExecutionInfo>> deleteIndexerStatusWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Creates a new Azure Cognitive Search index.
     * @param index definition of the index to create.
     * @return the created Index.
     */
    public Mono<Index> createIndex(Index index) {
        return this.createIndexWithResponse(index, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index.
     * @param index definition of the index to create.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the created Index.
     */
    public Mono<Response<Index>> createIndexWithResponse(Index index, RequestOptions requestOptions) {
        return withContext(context -> createIndexWithResponse(index, requestOptions, context));
    }

    Mono<Response<Index>> createIndexWithResponse(Index index,
                                                  RequestOptions requestOptions,
                                                  Context context) {
        return restClient
            .indexes()
            .createWithRestResponseAsync(index, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     * @param indexName The name of the index to retrieve
     * @return the Index.
     */
    public Mono<Index> getIndex(String indexName) {
        return this.getIndexWithResponse(indexName, null)
            .map(Response::getValue);
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     * @param indexName The name of the index to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the Index.
     */
    public Mono<Index> getIndex(String indexName, RequestOptions requestOptions) {
        return this.getIndexWithResponse(indexName, requestOptions)
            .map(Response::getValue);
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     * @param indexName The name of the index to retrieve
     * @param requestOptions Additional parameters for the operation
     * @return a response containing the Index.
     */
    public Mono<Response<Index>> getIndexWithResponse(String indexName, RequestOptions requestOptions) {
        return withContext(context -> getIndexWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<Index>> getIndexWithResponse(String indexName,
                                               RequestOptions requestOptions,
                                               Context context) {
        return restClient
            .indexes()
            .getWithRestResponseAsync(indexName, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     * @param indexName The name of the index
     * @return true if the index exists; false otherwise.
     */
    public Mono<Boolean> indexExists(String indexName) {
        return indexExistsWithResponse(indexName, null).map(Response::getValue);
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     * @param indexName The name of the index
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return true if the index exists; false otherwise.
     */
    public Mono<Boolean> indexExists(String indexName, RequestOptions requestOptions) {
        return indexExistsWithResponse(indexName, requestOptions).map(Response::getValue);
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     * @param indexName The name of the index
     * @param requestOptions Additional parameters for the operation
     * @return true if the index exists; false otherwise.
     */
    public Mono<Response<Boolean>> indexExistsWithResponse(String indexName,
                                                           RequestOptions requestOptions) {
        return withContext(context -> indexExistsWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<Boolean>> indexExistsWithResponse(String indexName,
                                                    RequestOptions requestOptions,
                                                    Context context) {
        return this.getIndexWithResponse(indexName, requestOptions, context)
            .map(i -> (Response<Boolean>) new SimpleResponse<>(i, true))
            .onErrorResume(
                t -> t instanceof HttpResponseException
                    && ((HttpResponseException) t).getResponse().getStatusCode() == 404,
                t -> {
                    HttpResponse response = ((HttpResponseException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Index statistics.
     */
    public Mono<IndexGetStatisticsResult> getIndexStatistics() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Index statistics.
     */
    public Mono<Response<IndexGetStatisticsResult>> getIndexStatisticsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return all the Indexes in the Search service.
     */
    public Mono<IndexListResult> listIndexes() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all Indexes in the Search service.
     */
    public Mono<Response<IndexListResult>> listIndexesWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @return the index that was created or updated
     */
    public Mono<Index> upsertIndex(Index index) {
        return this.upsertIndexWithResponse(index, null, null, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                        doesn't match specified values
     * @param requestOptions additional parameters for the operation
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the index that was created or updated
     */
    public Mono<Index> upsertIndex(Index index,
                                   AccessCondition accessCondition,
                                   RequestOptions requestOptions) {
        return this.upsertIndexWithResponse(index, null, accessCondition, requestOptions)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     *                           index by taking the index offline for at least a few seconds. This temporarily causes
     *                           indexing and query requests to fail. Performance and write availability of the index
     *                           can be impaired for several minutes after the index is updated, or longer for very
     *                           large indexes
     * @return the index that was created or updated
     */
    public Mono<Index> upsertIndex(Index index, Boolean allowIndexDowntime) {
        return this.upsertIndexWithResponse(index, allowIndexDowntime, null, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index The definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     *                           index by taking the index offline for at least a few seconds. This temporarily causes
     *                           indexing and query requests to fail. Performance and write availability of the index
     *                           can be impaired for several minutes after the index is updated, or longer for very
     *                           large indexes
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                        doesn't match specified values
     * @param requestOptions additional parameters for the operation
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the index that was created or updated
     */
    public Mono<Index> upsertIndex(Index index,
                                   Boolean allowIndexDowntime,
                                   AccessCondition accessCondition,
                                   RequestOptions requestOptions) {
        return this.upsertIndexWithResponse(index,
            allowIndexDowntime,
            accessCondition,
            requestOptions).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @return a response containing the index that was created or updated
     */
    public Mono<Response<Index>> upsertIndexWithResponse(Index index) {
        return withContext(context -> upsertIndexWithResponse(index,
            null,
            null,
            null,
            context));
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     *                           index by taking the index offline for at least a few seconds. This temporarily causes
     *                           indexing and query requests to fail. Performance and write availability of the index
     *                           can be impaired for several minutes after the index is updated, or longer for very
     *                           large indexes
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                        doesn't match specified values
     * @param requestOptions additional parameters for the operation
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the index that was created or updated
     */
    public Mono<Response<Index>> upsertIndexWithResponse(Index index,
                                                         Boolean allowIndexDowntime,
                                                         AccessCondition accessCondition,
                                                         RequestOptions requestOptions) {
        return withContext(context -> upsertIndexWithResponse(index,
            allowIndexDowntime,
            accessCondition,
            requestOptions,
            context));
    }

    Mono<Response<Index>> upsertIndexWithResponse(Index index,
                                                  Boolean allowIndexDowntime,
                                                  AccessCondition accessCondition,
                                                  RequestOptions requestOptions,
                                                  Context context) {
        return restClient
            .indexes()
            .createOrUpdateWithRestResponseAsync(index.getName(),
                index,
                allowIndexDowntime,
                requestOptions,
                accessCondition,
                context)
            .map(Function.identity());
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @return a response signalling completion.
     */
    public Mono<Void> deleteIndex(String indexName) {
        return this.deleteIndexWithResponse(indexName, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param accessCondition the access condition
     * @return a response signalling completion.
     */
    public Mono<Void> deleteIndex(String indexName,
                                  RequestOptions requestOptions,
                                  AccessCondition accessCondition) {
        return this.deleteIndexWithResponse(indexName,
            requestOptions,
            accessCondition)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param accessCondition the access condition
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteIndexWithResponse(String indexName,
                                                        RequestOptions requestOptions,
                                                        AccessCondition accessCondition) {
        return withContext(context -> deleteIndexWithResponse(indexName,
            requestOptions,
            accessCondition,
            context));
    }

    Mono<Response<Void>> deleteIndexWithResponse(String indexName,
                                                 RequestOptions requestOptions,
                                                 AccessCondition accessCondition,
                                                 Context context) {
        return restClient
            .indexes()
            .deleteWithRestResponseAsync(indexName,
                requestOptions,
                accessCondition,
                context)
            .map(Function.identity());
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Index analysis results.
     */
    public Mono<AnalyzeResult> analyzeIndex() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Index analysis results.
     */
    public Mono<Response<AnalyzeResult>> analyzeIndexWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the created Skillset.
     */
    public Mono<Skillset> createSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the created Skillset.
     */
    public Mono<Response<Skillset>> createSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Skillset.
     */
    public Mono<Skillset> getSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Skillset.
     */
    public Mono<Response<Skillset>> getSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return all Skillsets in the Search service.
     */
    public Mono<SkillsetListResult> listSkillsets() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all Skillsets in the Search service.
     */
    public Mono<Response<SkillsetListResult>> listSkillsetsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the updated Skillset.
     */
    public Mono<Skillset> replaceSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated Skillset.
     */
    public Mono<Response<Skillset>> replaceSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Void> deleteSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Response<Response<Void>>> deleteSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    public Mono<SynonymMap> createSynonymMap(SynonymMap synonymMap) {
        return this.createSynonymMapWithResponse(synonymMap, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the created {@link SynonymMap}.
     */
    public Mono<SynonymMap> createSynonymMap(SynonymMap synonymMap, RequestOptions requestOptions) {
        return this.createSynonymMapWithResponse(synonymMap, requestOptions).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the created SynonymMap.
     */
    public Mono<Response<SynonymMap>> createSynonymMapWithResponse(SynonymMap synonymMap,
                                                                   RequestOptions requestOptions) {
        return withContext(context -> createSynonymMapWithResponse(synonymMap,
                requestOptions,
                context));
    }

    Mono<Response<SynonymMap>> createSynonymMapWithResponse(SynonymMap synonymMap,
                                                            RequestOptions requestOptions,
                                                            Context context) {
        return restClient
                .synonymMaps()
                .createWithRestResponseAsync(synonymMap, requestOptions, context)
                .map(Function.identity());
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the SynonymMap.
     */
    public Mono<SynonymMap> getSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the SynonymMap.
     */
    public Mono<Response<SynonymMap>> getSynonymMapWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return all SynonymMaps in the Search service.
     */
    public Mono<SynonymMapListResult> listSynonymMaps() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all SynonymMaps in the Search service.
     */
    public Mono<Response<SynonymMapListResult>> listSynonymMapsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the updated SynonymMap.
     */
    public Mono<SynonymMap> replaceSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated SynonymMap.
     */
    public Mono<Response<SynonymMap>> replaceSynonymMapWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Void> deleteSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a reactive response signalling completion.
     */
    public Mono<Response<Response<Void>>> deleteSynonymMapWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }
}
