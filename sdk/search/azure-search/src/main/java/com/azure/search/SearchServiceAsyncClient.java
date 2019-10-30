// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.SearchServiceUrlParser.SearchServiceUrlParts;
import com.azure.search.implementation.SearchServiceRestClientBuilder;
import com.azure.search.implementation.SearchServiceRestClientImpl;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzeResult;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.IndexGetStatisticsResult;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.IndexerListResult;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.Skillset;
import com.azure.search.models.SkillsetListResult;
import com.azure.search.models.SynonymMap;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.core.implementation.util.FluxUtil.withContext;

@ServiceClient(builder = SearchServiceClientBuilder.class, isAsync = true)
public class SearchServiceAsyncClient {

    /**
     * Search REST API Version
     */
    private final String apiVersion;

    /**
     * The endpoint for the Azure Cognitive Search service.
     */
    private final String endpoint;

    /**
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchServiceAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchServiceRestClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    SearchServiceAsyncClient(String endpoint, String apiVersion, HttpPipeline httpPipeline) {

        SearchServiceUrlParts parts = SearchServiceUrlParser.parseServiceUrlParts(endpoint);

        if (StringUtils.isBlank(apiVersion)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid apiVersion"));
        }
        if (httpPipeline == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid httpPipeline"));
        }

        this.endpoint = endpoint;
        this.apiVersion = apiVersion;
        this.httpPipeline = httpPipeline;

        this.restClient = new SearchServiceRestClientBuilder()
            .searchServiceName(parts.serviceName)
            .searchDnsSuffix(parts.dnsSuffix)
            .apiVersion(apiVersion)
            .pipeline(httpPipeline)
            .build();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * Initializes a new {@link SearchIndexAsyncClient} using the given Index name and the
     * same configuration as the SearchServiceAsyncClient.
     *
     * @param indexName the name of the Index for the client
     * @return a {@link SearchIndexAsyncClient} created from the service client configuration
     */
    public SearchIndexAsyncClient getIndexClient(String indexName) {
        return new SearchIndexAsyncClient(
            endpoint,
            indexName,
            apiVersion,
            httpPipeline);
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
     * Gets the endpoint for the Azure Cognitive Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Creates a new Azure Search datasource or updates a datasource if it already exists
     *
     * @param dataSource The definition of the datasource to create or update.
     * @return The datasource that was created or updated.
     */
    public Mono<DataSource> createOrUpdateDataSource(DataSource dataSource) {
        return withContext(context ->
            createOrUpdateDataSource(dataSource.getName(),
                dataSource, null, null, context));
    }

    /**
     * Creates a new Azure Search datasource or updates a datasource if it already exists
     *
     * @param dataSourceName The name of the datasource to create or update.
     * @param dataSource The definition of the datasource to create or update.
     * @param requestOptions Request options
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param context Context
     * @return The newly created datasource
     */
    Mono<DataSource> createOrUpdateDataSource(
        String dataSourceName,
        DataSource dataSource,
        RequestOptions requestOptions,
        AccessCondition accessCondition,
        Context context) {
        return createOrUpdateDataSourceWithResponse(dataSourceName,
            dataSource, requestOptions, accessCondition, context)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Search datasource or updates a datasource if it already exists
     *
     * @param dataSourceName The name of the datasource to create or update.
     * @param dataSource The definition of the datasource to create or update.
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param context Context
     * @return a datasource response
     */
    Mono<Response<DataSource>> createOrUpdateDataSourceWithResponse(
        String dataSourceName,
        DataSource dataSource,
        RequestOptions requestOptions,
        AccessCondition accessCondition,
        Context context) {
        return restClient.dataSources().createOrUpdateWithRestResponseAsync(
            dataSourceName, dataSource, requestOptions, accessCondition, context)
            .map(Function.identity());
    }

    /**
     * @return the DataSource.
     * @throws NotImplementedException not implemented
     */
    public Mono<DataSource> getDataSource() {
        throw logger.logExceptionAsError(
            new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the DataSource.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<DataSource>> getDataSourceWithResponse() {
        throw logger.logExceptionAsError(
            new NotImplementedException("not implemented."));
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @return a list of DataSources
     */
    public PagedFlux<DataSource> listDataSources() {
        return this.listDataSources(null, null);
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of DataSource definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties.
     *
     * @return a list of DataSources
     */
    public PagedFlux<DataSource> listDataSources(String select) {
        return this.listDataSources(select, null);
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of DataSource definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging.
     * @return a list of DataSources
     */
    public PagedFlux<DataSource> listDataSources(String select, RequestOptions requestOptions) {
        return new PagedFlux<>(
            () -> withContext(context -> this.listDataSourcesWithResponse(select, requestOptions, context)),
            nextLink -> Mono.empty());
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of DataSource definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return a list of DataSources
     */
    public PagedFlux<DataSource> listDataSources(String select, RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
            () -> this.listDataSourcesWithResponse(select, requestOptions, context),
            nextLink -> Mono.empty());
    }

    public Mono<PagedResponse<DataSource>> listDataSourcesWithResponse(
        String select, RequestOptions requestOptions, Context context) {
        return restClient.dataSources()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getDataSources(),
                null,
                deserializeHeaders(response.getHeaders()))
            );
    }

    /**
     * @return the updated DataSource.
     * @throws NotImplementedException not implemented
     */
    public Mono<DataSource> createOrUpdateDataSource() {
        throw logger.logExceptionAsError(
            new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the updated DataSource.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<DataSource>> createOrUpdateDataSourceWithResponse() {
        throw logger.logExceptionAsError(
            new NotImplementedException("not implemented."));
    }

    /**
     * Delete a DataSource
     *
     * @param dataSourceName the name of the data source for deletion
     * @return a void Mono
     */
    public Mono<Void> deleteDataSource(String dataSourceName) {
        return withContext(context ->
            deleteDataSourceWithResponse(dataSourceName, null, null, context)
        ).map(Response::getValue);
    }

    /**
     * Deletes an Azure Search datasource.
     *
     * @param dataSourceName The name of the datasource to delete.
     * @param requestOptions Additional parameters for the operation.
     * @param accessCondition Additional parameters for the operation.
     * @return a valid Mono
     */
    public Mono<Void> deleteDataSource(String dataSourceName,
                                       RequestOptions requestOptions,
                                       AccessCondition accessCondition) {
        return withContext(context ->
            restClient.dataSources()
                .deleteWithRestResponseAsync(
                    dataSourceName,
                    requestOptions,
                    accessCondition,
                    context)).map(Response::getValue);
    }

    /**
     * Deletes an Azure Search datasource.
     *
     * @param dataSourceName The name of the datasource to delete.
     * @param requestOptions Additional parameters for the operation.
     * @param accessCondition Additional parameters for the operation.
     * @return a mono response
     */
    public Mono<Response<Void>> deleteDataSourceWithResponse(String dataSourceName,
                                       RequestOptions requestOptions,
                                       AccessCondition accessCondition) {
        return withContext(context ->
            deleteDataSourceWithResponse(
                    dataSourceName,
                    requestOptions,
                    accessCondition,
                    context));
    }

    /**
     * Deletes an Azure Search datasource.
     *
     * @param dataSourceName The name of the datasource to delete.
     * @param requestOptions Additional parameters for the operation.
     * @param accessCondition Additional parameters for the operation.
     * @return a mono response
     */
    Mono<Response<Void>> deleteDataSourceWithResponse(String dataSourceName,
                                                   RequestOptions requestOptions,
                                                   AccessCondition accessCondition,
                                                   Context context) {
        return restClient.dataSources()
                .deleteWithRestResponseAsync(
                    dataSourceName,
                    requestOptions,
                    accessCondition,
                    context).map(Function.identity());
    }

    /**
     * @return the created Indexer.
     * @throws NotImplementedException not implemented
     */
    public Mono<Indexer> createIndexer() {
        throw logger.logExceptionAsError(
            new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the created Indexer.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Indexer>> createIndexerWithResponse() {
        throw logger.logExceptionAsError(
            new NotImplementedException("not implemented."));
    }

    /**
     * @return the Indexer.
     * @throws NotImplementedException not implemented
     */
    public Mono<Indexer> getIndexer() {
        throw logger.logExceptionAsError(
            new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Indexer.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Indexer>> getIndexerWithResponse() {
        throw logger.logExceptionAsError(
            new NotImplementedException("not implemented."));
    }

    /**
     * @return all Indexers from the Search service.
     * @throws NotImplementedException not implemented
     */
    public Mono<IndexerListResult> listIndexers() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing all Indexers from the Search service.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<IndexerListResult>> listIndexersWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the updated Indexer.
     * @throws NotImplementedException not implemented
     */
    public Mono<Indexer> createOrUpdateIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the updated Indexer.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Indexer>> createOrUpdateIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
     */
    public Mono<Void> deleteIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Response<Void>>> deleteIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
     */
    public Mono<Void> resetIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Response<Void>>> resetIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
     */
    public Mono<Void> runIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Response<Void>>> runIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the Indexer execution information.
     * @throws NotImplementedException not implemented
     */
    public Mono<IndexerExecutionInfo> deleteIndexerStatus() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Indexer execution information.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<IndexerExecutionInfo>> deleteIndexerStatusWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Creates a new Azure Cognitive Search index.
     *
     * @param index definition of the index to create.
     * @return the created Index.
     */
    public Mono<Index> createIndex(Index index) {
        return this.createIndexWithResponse(index, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index.
     *
     * @param index definition of the index to create
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the created Index.
     */
    public Mono<Index> createIndex(Index index, RequestOptions requestOptions) {
        return this.createIndexWithResponse(index, requestOptions)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index.
     *
     * @param index definition of the index to create
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
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
     *
     * @param indexName The name of the index to retrieve
     * @return the Index.
     */
    public Mono<Index> getIndex(String indexName) {
        return this.getIndexWithResponse(indexName, null)
            .map(Response::getValue);
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName The name of the index to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the Index.
     */
    public Mono<Index> getIndex(String indexName, RequestOptions requestOptions) {
        return this.getIndexWithResponse(indexName, requestOptions)
            .map(Response::getValue);
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName the name of the index to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the Index.
     */
    public Mono<Response<Index>> getIndexWithResponse(String indexName, RequestOptions requestOptions) {
        return withContext(context -> getIndexWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<Index>> getIndexWithResponse(String indexName, RequestOptions requestOptions, Context context) {
        return restClient
            .indexes()
            .getWithRestResponseAsync(indexName, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     *
     * @param indexName the name of the index
     * @return true if the index exists; false otherwise.
     */
    public Mono<Boolean> indexExists(String indexName) {
        return this.indexExistsWithResponse(indexName, null).map(Response::getValue);
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     *
     * @param indexName the name of the index
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return true if the index exists; false otherwise.
     */
    public Mono<Boolean> indexExists(String indexName, RequestOptions requestOptions) {
        return this.indexExistsWithResponse(indexName, requestOptions).map(Response::getValue);
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     *
     * @param indexName the name of the index
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return true if the index exists; false otherwise.
     */
    public Mono<Response<Boolean>> indexExistsWithResponse(String indexName, RequestOptions requestOptions) {
        return withContext(context -> this.indexExistsWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<Boolean>> indexExistsWithResponse(String indexName,
                                                    RequestOptions requestOptions,
                                                    Context context) {
        return this.resourceExistsWithResponse(() -> this.getIndexWithResponse(indexName, requestOptions, context));
    }

    /**
     * @return the Index statistics.
     * @throws NotImplementedException not implemented
     */
    public Mono<IndexGetStatisticsResult> getIndexStatistics() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Index statistics.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<IndexGetStatisticsResult>> getIndexStatisticsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of indexes.
     */
    public PagedFlux<Index> listIndexes() {
        return this.listIndexes(null, null);
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties
     * @return a reactive response emitting the list of indexes.
     */
    public PagedFlux<Index> listIndexes(String select) {
        return this.listIndexes(select, null);
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
     *                       Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *                       The default is all properties
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a reactive response emitting the list of indexes.
     */
    public PagedFlux<Index> listIndexes(String select, RequestOptions requestOptions) {
        return new PagedFlux<>(
            () -> withContext(context -> this.listIndexesWithResponse(select, requestOptions, context)),
            nextLink -> Mono.empty());
    }

    PagedFlux<Index> listIndexes(String select, RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
            () -> this.listIndexesWithResponse(select, requestOptions, context),
            nextLink -> Mono.empty());
    }

    private Mono<PagedResponse<Index>> listIndexesWithResponse(String select,
                                                               RequestOptions requestOptions,
                                                               Context context) {
        return restClient.indexes()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getIndexes(),
                null,
                deserializeHeaders(response.getHeaders()))
            );
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @return the index that was created or updated.
     */
    public Mono<Index> createOrUpdateIndex(Index index) {
        return this.createOrUpdateIndexWithResponse(index, false, null, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes
     * indexing and query requests to fail. Performance and write availability of the index
     * can be impaired for several minutes after the index is updated, or longer for very
     * large indexes
     * @return the index that was created or updated
     */
    public Mono<Index> createOrUpdateIndex(Index index, boolean allowIndexDowntime) {
        return this.createOrUpdateIndexWithResponse(index, allowIndexDowntime, null, null)
                .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @return the index that was created or updated
     */
    public Mono<Index> createOrUpdateIndex(Index index,
                                           AccessCondition accessCondition) {
        return this.createOrUpdateIndexWithResponse(index, false, accessCondition, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes
     * indexing and query requests to fail. Performance and write availability of the index
     * can be impaired for several minutes after the index is updated, or longer for very
     * large indexes
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @return the index that was created or updated
     */
    public Mono<Index> createOrUpdateIndex(Index index, boolean allowIndexDowntime, AccessCondition accessCondition) {
        return this.createOrUpdateIndexWithResponse(index, allowIndexDowntime, accessCondition, null)
                .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes
     * indexing and query requests to fail. Performance and write availability of the index
     * can be impaired for several minutes after the index is updated, or longer for very
     * large indexes
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the index that was created or updated
     */
    public Mono<Index> createOrUpdateIndex(Index index,
                                           boolean allowIndexDowntime,
                                           AccessCondition accessCondition,
                                           RequestOptions requestOptions) {
        return this.createOrUpdateIndexWithResponse(index,
            allowIndexDowntime,
            accessCondition,
            requestOptions).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes
     * indexing and query requests to fail. Performance and write availability of the index
     * can be impaired for several minutes after the index is updated, or longer for very
     * large indexes
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the index that was created or updated
     */
    public Mono<Response<Index>> createOrUpdateIndexWithResponse(Index index,
                                                                 boolean allowIndexDowntime,
                                                                 AccessCondition accessCondition,
                                                                 RequestOptions requestOptions) {
        return withContext(context -> createOrUpdateIndexWithResponse(index,
            allowIndexDowntime,
            accessCondition,
            requestOptions,
            context));
    }

    Mono<Response<Index>> createOrUpdateIndexWithResponse(Index index,
                                                          boolean allowIndexDowntime,
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
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @return a response signalling completion.
     */
    public Mono<Void> deleteIndex(String indexName, AccessCondition accessCondition) {
        return this.deleteIndexWithResponse(indexName,
                accessCondition,
                null)
                .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response signalling completion.
     */
    public Mono<Void> deleteIndex(String indexName, AccessCondition accessCondition, RequestOptions requestOptions) {
        return this.deleteIndexWithResponse(indexName,
            accessCondition,
            requestOptions)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteIndexWithResponse(String indexName,
                                                        AccessCondition accessCondition,
                                                        RequestOptions requestOptions) {
        return withContext(context -> deleteIndexWithResponse(indexName,
            accessCondition,
            requestOptions,
            context));
    }

    Mono<Response<Void>> deleteIndexWithResponse(String indexName,
                                                 AccessCondition accessCondition,
                                                 RequestOptions requestOptions,
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
     * @return the Index analysis results.
     * @throws NotImplementedException not implemented
     */
    public Mono<AnalyzeResult> analyzeIndex() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Index analysis results.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<AnalyzeResult>> analyzeIndexWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the created Skillset.
     * @throws NotImplementedException not implemented
     */
    public Mono<Skillset> createSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the created Skillset.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Skillset>> createSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the Skillset.
     * @throws NotImplementedException not implemented
     */
    public Mono<Skillset> getSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Skillset.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Skillset>> getSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return all Skillsets in the Search service.
     * @throws NotImplementedException not implemented
     */
    public Mono<SkillsetListResult> listSkillsets() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing all Skillsets in the Search service.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<SkillsetListResult>> listSkillsetsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the updated Skillset.
     * @throws NotImplementedException not implemented
     */
    public Mono<Skillset> createOrUpdateSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the updated Skillset.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Skillset>> createOrUpdateSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
     */
    public Mono<Void> deleteSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
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
     * Contains the tracking ID sent with the request to help with debugging
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
     * Contains the tracking ID sent with the request to help with debugging
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

    /** Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @return the {@link SynonymMap} definition
     */
    public Mono<SynonymMap> getSynonymMap(String synonymMapName) {
        return this.getSynonymMapWithResponse(synonymMapName, null)
            .map(Response::getValue);
    }

    /** Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the {@link SynonymMap} definition
     */
    public Mono<SynonymMap> getSynonymMap(String synonymMapName, RequestOptions requestOptions) {
        return this.getSynonymMapWithResponse(synonymMapName, requestOptions)
            .map(Response::getValue);
    }

    /** Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the SynonymMap.
     */
    public Mono<Response<SynonymMap>> getSynonymMapWithResponse(String synonymMapName,
                                                                RequestOptions requestOptions) {
        return withContext(context -> getSynonymMapWithResponse(synonymMapName, requestOptions, context));
    }

    Mono<Response<SynonymMap>> getSynonymMapWithResponse(String synonymMapName,
                                                         RequestOptions requestOptions,
                                                         Context context) {
        return restClient
            .synonymMaps()
            .getWithRestResponseAsync(synonymMapName, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of synonym maps.
     */
    public PagedFlux<SynonymMap> listSynonymMaps() {
        return this.listSynonymMaps(null, null);
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the synonym maps to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties
     * @return a reactive response emitting the list of synonym maps.
     */
    public PagedFlux<SynonymMap> listSynonymMaps(String select) {
        return this.listSynonymMaps(select, null);
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the synonym maps to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a reactive response emitting the list of synonym maps.
     */
    public PagedFlux<SynonymMap> listSynonymMaps(String select, RequestOptions requestOptions) {
        return new PagedFlux<>(
            () -> withContext(context -> this.listSynonymMapsWithResponse(select, requestOptions, context)),
            nextLink -> Mono.empty());
    }

    PagedFlux<SynonymMap> listSynonymMaps(String select, RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
            () -> this.listSynonymMapsWithResponse(select, requestOptions, context),
            nextLink -> Mono.empty());
    }

    private Mono<PagedResponse<SynonymMap>> listSynonymMapsWithResponse(String select,
                                                                        RequestOptions requestOptions,
                                                                        Context context) {
        return restClient
            .synonymMaps()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getSynonymMaps(),
                null,
                deserializeHeaders(response.getHeaders()))
            );
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @return the synonym map that was created or updated.
     */
    public Mono<SynonymMap> createOrUpdateSynonymMap(SynonymMap synonymMap) {
        return this.createOrUpdateSynonymMapWithResponse(synonymMap, null, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @return the synonym map that was created or updated.
     */
    public Mono<SynonymMap> createOrUpdateSynonymMap(SynonymMap synonymMap, AccessCondition accessCondition) {
        return this.createOrUpdateSynonymMapWithResponse(synonymMap, accessCondition, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the synonym map that was created or updated.
     */
    public Mono<SynonymMap> createOrUpdateSynonymMap(SynonymMap synonymMap,
                                                     AccessCondition accessCondition,
                                                     RequestOptions requestOptions) {
        return this.createOrUpdateSynonymMapWithResponse(synonymMap, accessCondition, requestOptions)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the synonym map that was created or updated.
     */
    public Mono<Response<SynonymMap>> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
                                                                           AccessCondition accessCondition,
                                                                           RequestOptions requestOptions) {
        return withContext(context -> createOrUpdateSynonymMapWithResponse(synonymMap,
            accessCondition,
            requestOptions,
            context));
    }

    Mono<Response<SynonymMap>> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
                                                                    AccessCondition accessCondition,
                                                                    RequestOptions requestOptions,
                                                                    Context context) {
        return restClient
            .synonymMaps()
            .createOrUpdateWithRestResponseAsync(synonymMap.getName(),
                synonymMap,
                requestOptions,
                accessCondition,
                context)
                .map(Function.identity());
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
     */
    public Mono<Void> deleteSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a reactive response signalling completion.
     * @throws NotImplementedException not implemented
     */
    public Mono<Response<Response<Void>>> deleteSynonymMapWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Determines whether or not the given synonym map exists.
     *
     * @param synonymMapName the name of the synonym map
     * @return true if the synonym map exists; false otherwise.
     */
    public Mono<Boolean> synonymMapExists(String synonymMapName) {
        return this.synonymMapExistsWithResponse(synonymMapName, null).map(Response::getValue);
    }

    /**
     * Determines whether or not the given synonym map exists.
     *
     * @param synonymMapName the name of the synonym map
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return true if the synonym map exists; false otherwise.
     */
    public Mono<Boolean> synonymMapExists(String synonymMapName, RequestOptions requestOptions) {
        return this.synonymMapExistsWithResponse(synonymMapName, requestOptions).map(Response::getValue);
    }

    /**
     * Determines whether or not the given synonym map exists.
     *
     * @param synonymMapName the name of the synonym map
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return true if the synonym map exists; false otherwise.
     */
    public Mono<Response<Boolean>> synonymMapExistsWithResponse(String synonymMapName, RequestOptions requestOptions) {
        return withContext(context -> this.synonymMapExistsWithResponse(synonymMapName, requestOptions, context));
    }

    Mono<Response<Boolean>> synonymMapExistsWithResponse(String synonymMapName,
                                                         RequestOptions requestOptions,
                                                         Context context) {
        return resourceExistsWithResponse(() ->
            this.getSynonymMapWithResponse(synonymMapName, requestOptions, context));
    }

    /**
     * Runs an async action and determines if a resource exists or not
     *
     * @param action the runnable async action
     * @return true if the resource exists (service returns a '200' status code); otherwise false.
     */
    private static <T> Mono<Response<Boolean>> resourceExistsWithResponse(Supplier<Mono<Response<T>>> action) {
        return action.get()
            .map(i ->
                (Response<Boolean>) new SimpleResponse<>(i, i.getStatusCode() == 200))
            .onErrorResume(
                t -> t instanceof HttpResponseException
                    && ((HttpResponseException) t).getResponse().getStatusCode() == 404,
                t -> {
                    HttpResponse response = ((HttpResponseException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    private static String deserializeHeaders(HttpHeaders headers) {
        return headers.toMap().entrySet().stream().map((entry) ->
            entry.getKey() + "," + entry.getValue()
        ).collect(Collectors.joining(","));
    }
}
