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
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.SearchServiceUrlParser.SearchServiceUrlParts;
import com.azure.search.implementation.SearchServiceRestClientBuilder;
import com.azure.search.implementation.SearchServiceRestClientImpl;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzeRequest;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.IndexGetStatisticsResult;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.ServiceStatistics;
import com.azure.search.models.Skillset;
import com.azure.search.models.SynonymMap;
import com.azure.search.models.TokenInfo;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * TODO: Add class description
 */
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
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * @param dataSource the definition of the data source to create or update
     * @return the data source that was created or updated.
     */
    public Mono<DataSource> createOrUpdateDataSource(DataSource dataSource) {
        return this.createOrUpdateDataSourceWithResponse(dataSource, null, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * @param dataSource The definition of the data source to create or update.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a data source response.
     */
    public Mono<Response<DataSource>> createOrUpdateDataSourceWithResponse(DataSource dataSource,
                                                                           AccessCondition accessCondition,
                                                                           RequestOptions requestOptions) {
        return withContext(context -> this.createOrUpdateDataSourceWithResponse(dataSource,
            accessCondition, requestOptions, context));
    }

    Mono<Response<DataSource>> createOrUpdateDataSourceWithResponse(DataSource dataSource,
                                                                    AccessCondition accessCondition,
                                                                    RequestOptions requestOptions,
                                                                    Context context) {
        return restClient
            .dataSources()
            .createOrUpdateWithRestResponseAsync(dataSource.getName(),
                dataSource, requestOptions, accessCondition, context)
            .map(Function.identity());
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource The definition of the dataSource to create.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<DataSource> createDataSource(DataSource dataSource) {
        return this.createDataSourceWithResponse(dataSource, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource The definition of the data source to create.
     * @param requestOptions Additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<Response<DataSource>> createDataSourceWithResponse(DataSource dataSource,
                                                                   RequestOptions requestOptions) {
        return withContext(context -> this.createDataSourceWithResponse(dataSource, requestOptions, context));
    }

    Mono<Response<DataSource>> createDataSourceWithResponse(DataSource dataSource,
                                                            RequestOptions requestOptions,
                                                            Context context) {
        return restClient
            .dataSources()
            .createWithRestResponseAsync(dataSource, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @return the DataSource.
     */
    public Mono<DataSource> getDataSource(String dataSourceName) {
        return this.getDataSourceWithResponse(dataSourceName, null)
            .map(Response::getValue);
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging.
     * @return a response containing the DataSource.
     */
    public Mono<Response<DataSource>> getDataSourceWithResponse(String dataSourceName,
                                                                RequestOptions requestOptions) {
        return withContext(context -> this.getDataSourceWithResponse(dataSourceName, requestOptions, context));
    }

    Mono<Response<DataSource>> getDataSourceWithResponse(String dataSourceName,
                                                         RequestOptions requestOptions,
                                                         Context context) {
        return restClient
            .dataSources()
            .getWithRestResponseAsync(dataSourceName, requestOptions, context)
            .map(Function.identity());
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
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging.
     * @return a list of DataSources
     */
    public PagedFlux<DataSource> listDataSources(String select, RequestOptions requestOptions) {
        return new PagedFlux<>(
            () -> withContext(context -> this.listDataSourcesWithResponse(select, requestOptions, context)),
            nextLink -> Mono.empty());
    }

    PagedFlux<DataSource> listDataSources(String select, RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
            () -> this.listDataSourcesWithResponse(select, requestOptions, context),
            nextLink -> Mono.empty());
    }

    private Mono<PagedResponse<DataSource>> listDataSourcesWithResponse(String select,
                                                                        RequestOptions requestOptions,
                                                                        Context context) {
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
     * Delete a DataSource
     *
     * @param dataSourceName the name of the data source for deletion
     * @return a void Mono
     */
    public Mono<Void> deleteDataSource(String dataSourceName) {
        return this.deleteDataSourceWithResponse(dataSourceName, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an Azure Cognitive Search data source.
     *
     * @param dataSourceName The name of the data source to delete.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a mono response
     */
    public Mono<Response<Void>> deleteDataSourceWithResponse(String dataSourceName,
                                                             AccessCondition accessCondition,
                                                             RequestOptions requestOptions) {
        return withContext(context -> this.deleteDataSourceWithResponse(dataSourceName,
            accessCondition, requestOptions, context));
    }

    Mono<Response<Void>> deleteDataSourceWithResponse(String dataSourceName,
                                                      AccessCondition accessCondition,
                                                      RequestOptions requestOptions,
                                                      Context context) {
        return restClient.dataSources()
            .deleteWithRestResponseAsync(
                dataSourceName,
                requestOptions,
                accessCondition,
                context).map(Function.identity());
    }


    /**
     * Determines whether or not the given data source exists.
     *
     * @param dataSourceName the name of the data source
     * @return true if the data source exists; false otherwise.
     */
    public Mono<Boolean> dataSourceExists(String dataSourceName) {
        return this.dataSourceExistsWithResponse(dataSourceName, null).map(Response::getValue);
    }

    /**
     * Determines whether or not the given data source exists.
     *
     * @param dataSourceName the name of the data source
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return true if the data source exists; false otherwise.
     */
    public Mono<Response<Boolean>> dataSourceExistsWithResponse(String dataSourceName, RequestOptions requestOptions) {
        return withContext(context -> this.dataSourceExistsWithResponse(dataSourceName, requestOptions, context));
    }

    Mono<Response<Boolean>> dataSourceExistsWithResponse(String dataSourceName,
                                                         RequestOptions requestOptions,
                                                         Context context) {
        return resourceExistsWithResponse(() ->
            this.getDataSourceWithResponse(dataSourceName, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create.
     * @return the created Indexer.
     */
    public Mono<Indexer> createIndexer(Indexer indexer) {
        return this.createIndexerWithResponse(indexer, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the created Indexer.
     */
    public Mono<Response<Indexer>> createIndexerWithResponse(Indexer indexer, RequestOptions requestOptions) {
        return withContext(context -> this.createIndexerWithResponse(indexer, requestOptions, context));
    }

    Mono<Response<Indexer>> createIndexerWithResponse(Indexer indexer, RequestOptions requestOptions, Context context) {
        return restClient
            .indexers()
            .createWithRestResponseAsync(indexer, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @return a response containing the created Indexer.
     */
    public Mono<Indexer> createOrUpdateIndexer(Indexer indexer) {
        return this.createOrUpdateIndexerWithResponse(indexer, null, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer the definition of the indexer to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the created Indexer.
     */
    public Mono<Response<Indexer>> createOrUpdateIndexerWithResponse(Indexer indexer,
                                                                     AccessCondition accessCondition,
                                                                     RequestOptions requestOptions) {
        return withContext(context -> this.createOrUpdateIndexerWithResponse(indexer,
            accessCondition, requestOptions, context));
    }

    Mono<Response<Indexer>> createOrUpdateIndexerWithResponse(Indexer indexer,
                                                              AccessCondition accessCondition,
                                                              RequestOptions requestOptions,
                                                              Context context) {
        return restClient
            .indexers()
            .createOrUpdateWithRestResponseAsync(indexer.getName(), indexer, requestOptions, accessCondition, context)
            .map(Function.identity());
    }

    /**
     * Retrieves an indexer definition.
     *
     * @param indexerName the name of the indexer to retrieve
     * @return the indexer.
     */
    public Mono<Indexer> getIndexer(String indexerName) {
        return this.getIndexerWithResponse(indexerName, null)
            .map(Response::getValue);
    }

    /**
     * Retrieves an indexer definition.
     *
     * @param indexerName the name of the indexer to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the indexer.
     */
    public Mono<Response<Indexer>> getIndexerWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(
            context -> this.getIndexerWithResponse(indexerName, requestOptions, context)
        );
    }

    Mono<Response<Indexer>> getIndexerWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        return restClient
            .indexers()
            .getWithRestResponseAsync(indexerName, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * @return all Indexers from the Search service.
     */
    public PagedFlux<Indexer> listIndexers() {
        return this.listIndexers(null, null);
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of the indexers to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     * @return a response containing all Indexers from the Search service.
     */
    public PagedFlux<Indexer> listIndexers(String select, RequestOptions requestOptions) {
        return new PagedFlux<>(
            () -> withContext(context -> this.listIndexersWithResponse(select, requestOptions, context)),
            nextLink -> Mono.empty());
    }

    PagedFlux<Indexer> listIndexers(String select, RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
            () -> this.listIndexersWithResponse(select, requestOptions, context),
            nextLink -> Mono.empty());
    }

    private Mono<PagedResponse<Indexer>> listIndexersWithResponse(String select,
                                                                  RequestOptions requestOptions,
                                                                  Context context) {
        return restClient
            .indexers()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getIndexers(),
                null,
                deserializeHeaders(response.getHeaders()))
            );
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @return a response signalling completion.
     */
    public Mono<Void> deleteIndexer(String indexerName) {
        return this.deleteIndexerWithResponse(indexerName, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteIndexerWithResponse(String indexerName,
                                                          AccessCondition accessCondition,
                                                          RequestOptions requestOptions) {
        return withContext(context -> this.deleteIndexerWithResponse(indexerName,
            accessCondition, requestOptions, context));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context the context
     * @return a response signalling completion.
     */
    Mono<Response<Void>> deleteIndexerWithResponse(String indexerName, AccessCondition accessCondition,
                                                   RequestOptions requestOptions, Context context) {
        return restClient.indexers().deleteWithRestResponseAsync(
            indexerName, requestOptions, accessCondition, context)
            .map(Function.identity());
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * @param indexerName the name of the indexer to reset
     * @return a response signalling completion.
     */
    public Mono<Void> resetIndexer(String indexerName) {
        return this.resetIndexerWithResponse(indexerName, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * @param indexerName the name of the indexer to reset
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> resetIndexerWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> this.resetIndexerWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<Void>> resetIndexerWithResponse(String indexerName,
                                                  RequestOptions requestOptions,
                                                  Context context) {
        return restClient.indexers().resetWithRestResponseAsync(indexerName, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * Runs an indexer on-demand.
     *
     * @param indexerName the name of the indexer to run
     * @return a response signalling completion.
     */
    public Mono<Void> runIndexer(String indexerName) {
        return this.runIndexerWithResponse(indexerName, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Runs an indexer on-demand.
     *
     * @param indexerName the name of the indexer to run
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> runIndexerWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> this.runIndexerWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<Void>> runIndexerWithResponse(String indexerName,
                                                RequestOptions requestOptions,
                                                Context context) {
        return restClient.indexers().runWithRestResponseAsync(indexerName, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @return the indexer execution info.
     */
    public Mono<IndexerExecutionInfo> getIndexerStatus(String indexerName) {
        return this.getIndexerStatusWithResponse(indexerName, null).map(Response::getValue);
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response with the indexer execution info.
     */
    public Mono<Response<IndexerExecutionInfo>> getIndexerStatusWithResponse(String indexerName,
                                                                             RequestOptions requestOptions) {
        return withContext(context -> this.getIndexerStatusWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<IndexerExecutionInfo>> getIndexerStatusWithResponse(String indexerName,
                                                                      RequestOptions requestOptions,
                                                                      Context context) {
        return restClient.indexers().getStatusWithRestResponseAsync(
            indexerName, requestOptions, context)
            .map(Function.identity());
    }


    /**
     * Determines whether or not the given indexer exists.
     *
     * @param indexerName the name of the indexer
     * @return true if the indexer exists; false otherwise.
     */
    public Mono<Boolean> indexerExists(String indexerName) {
        return this.indexerExistsWithResponse(indexerName, null).map(Response::getValue);
    }

    /**
     * Determines whether or not the given indexer exists.
     *
     * @param indexerName the name of the indexer
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return true if the indexer exists; false otherwise.
     */
    public Mono<Response<Boolean>> indexerExistsWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> this.indexerExistsWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<Boolean>> indexerExistsWithResponse(String indexerName,
                                                      RequestOptions requestOptions,
                                                      Context context) {
        return resourceExistsWithResponse(() -> this.getIndexerWithResponse(indexerName, requestOptions, context));
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
     * @return a response containing the created Index.
     */
    public Mono<Response<Index>> createIndexWithResponse(Index index, RequestOptions requestOptions) {
        return withContext(context -> this.createIndexWithResponse(index, requestOptions, context));
    }

    Mono<Response<Index>> createIndexWithResponse(Index index, RequestOptions requestOptions, Context context) {
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
     * @param indexName the name of the index to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the Index.
     */
    public Mono<Response<Index>> getIndexWithResponse(String indexName, RequestOptions requestOptions) {
        return withContext(context -> this.getIndexWithResponse(indexName, requestOptions, context));
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
    public Mono<Response<Boolean>> indexExistsWithResponse(String indexName, RequestOptions requestOptions) {
        return withContext(context -> this.indexExistsWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<Boolean>> indexExistsWithResponse(String indexName,
                                                    RequestOptions requestOptions,
                                                    Context context) {
        return resourceExistsWithResponse(() -> this.getIndexWithResponse(indexName, requestOptions, context));
    }

    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @return the index statistics result.
     */
    public Mono<IndexGetStatisticsResult> getIndexStatistics(String indexName) {
        return this.getIndexStatisticsWithResponse(indexName, null)
            .map(Response::getValue);
    }

    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the index statistics result.
     */
    public Mono<Response<IndexGetStatisticsResult>> getIndexStatisticsWithResponse(String indexName,
                                                                                   RequestOptions requestOptions) {
        return withContext(context -> this.getIndexStatisticsWithResponse(indexName, requestOptions, context));
    }

    Mono<Response<IndexGetStatisticsResult>> getIndexStatisticsWithResponse(String indexName,
                                                                            RequestOptions requestOptions,
                                                                            Context context) {
        return restClient
            .indexes()
            .getStatisticsWithRestResponseAsync(indexName, requestOptions, context)
            .map(Function.identity());
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
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties
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
                                                               RequestOptions requestOptions, Context context) {
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
        return this.createOrUpdateIndexWithResponse(index,
            false, null, null)
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
     * @return a response containing the index that was created or updated
     */
    public Mono<Response<Index>> createOrUpdateIndexWithResponse(Index index,
                                                                 boolean allowIndexDowntime,
                                                                 AccessCondition accessCondition,
                                                                 RequestOptions requestOptions) {
        return withContext(context -> this.createOrUpdateIndexWithResponse(index,
            allowIndexDowntime, accessCondition, requestOptions, context));
    }

    Mono<Response<Index>> createOrUpdateIndexWithResponse(Index index,
                                                          boolean allowIndexDowntime,
                                                          AccessCondition accessCondition,
                                                          RequestOptions requestOptions,
                                                          Context context) {
        return restClient
            .indexes()
            .createOrUpdateWithRestResponseAsync(index.getName(),
                index, allowIndexDowntime, requestOptions, accessCondition, context)
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
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteIndexWithResponse(String indexName,
                                                        AccessCondition accessCondition,
                                                        RequestOptions requestOptions) {
        return withContext(context -> this.deleteIndexWithResponse(indexName,
            accessCondition, requestOptions, context));
    }

    Mono<Response<Void>> deleteIndexWithResponse(String indexName,
                                                 AccessCondition accessCondition,
                                                 RequestOptions requestOptions,
                                                 Context context) {
        return restClient
            .indexes()
            .deleteWithRestResponseAsync(indexName, requestOptions, accessCondition, context)
            .map(Function.identity());
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @return analyze result.
     */
    public PagedFlux<TokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest) {
        return this.analyzeText(indexName, analyzeRequest, null);
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing analyze result.
     */
    public PagedFlux<TokenInfo> analyzeText(String indexName,
                                            AnalyzeRequest analyzeRequest, RequestOptions requestOptions) {
        return new PagedFlux<>(
            () -> withContext(context -> this.analyzeTextWithResponse(indexName,
                analyzeRequest, requestOptions, context)),
            nextLink -> Mono.empty());
    }

    PagedFlux<TokenInfo> analyzeText(String indexName,
                                     AnalyzeRequest analyzeRequest,
                                     RequestOptions requestOptions,
                                     Context context) {
        return new PagedFlux<>(
            () -> this.analyzeTextWithResponse(indexName, analyzeRequest, requestOptions, context),
            nextLink -> Mono.empty());
    }

    private Mono<PagedResponse<TokenInfo>> analyzeTextWithResponse(String indexName,
                                                                   AnalyzeRequest analyzeRequest,
                                                                   RequestOptions requestOptions,
                                                                   Context context) {
        return restClient.indexes()
            .analyzeWithRestResponseAsync(indexName, analyzeRequest, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getTokens(),
                null,
                deserializeHeaders(response.getHeaders())));
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return the created Skillset.
     */
    public Mono<Skillset> createSkillset(Skillset skillset) {
        return this.createSkillsetWithResponse(skillset, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the created Skillset.
     */
    public Mono<Response<Skillset>> createSkillsetWithResponse(Skillset skillset, RequestOptions requestOptions) {
        return withContext(context -> createSkillsetWithResponse(skillset, requestOptions, context));
    }

    Mono<Response<Skillset>> createSkillsetWithResponse(Skillset skillset,
                                                        RequestOptions requestOptions,
                                                        Context context) {
        return restClient
            .skillsets()
            .createWithRestResponseAsync(skillset, requestOptions, context)
            .map(Function.identity());
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @return the Skillset.
     */
    public Mono<Skillset> getSkillset(String skillsetName) {
        return this.getSkillsetWithResponse(skillsetName, null)
            .map(Response::getValue);
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the Skillset.
     */
    public Mono<Response<Skillset>> getSkillsetWithResponse(String skillsetName,
                                                            RequestOptions requestOptions) {
        return withContext(context ->
            this.getSkillsetWithResponse(skillsetName, requestOptions, context));
    }

    Mono<Response<Skillset>> getSkillsetWithResponse(String skillsetName,
                                                     RequestOptions requestOptions,
                                                     Context context) {
        return this.restClient
            .skillsets()
            .getWithRestResponseAsync(skillsetName, requestOptions, context)
            .map(result -> result);
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @return a reactive response emitting the list of skillsets.
     */
    public PagedFlux<Skillset> listSkillsets() {
        return this.listSkillsets(null, null);
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the skillset definitions to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a reactive response emitting the list of skillsets.
     */
    public PagedFlux<Skillset> listSkillsets(String select, RequestOptions requestOptions) {
        return new PagedFlux<>(
            () -> withContext(context -> this.listSkillsetsWithResponse(select, requestOptions, context)),
            nextLink -> Mono.empty());
    }

    PagedFlux<Skillset> listSkillsets(String select, RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
            () -> this.listSkillsetsWithResponse(select, requestOptions, context),
            nextLink -> Mono.empty());
    }

    private Mono<PagedResponse<Skillset>> listSkillsetsWithResponse(String select,
                                                                    RequestOptions requestOptions,
                                                                    Context context) {
        return this.restClient.skillsets()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getSkillsets(),
                null,
                deserializeHeaders(response.getHeaders()))
            );
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @return the skillset that was created or updated.
     */
    public Mono<Skillset> createOrUpdateSkillset(Skillset skillset) {
        return this.createOrUpdateSkillsetWithResponse(skillset, null, null)
            .map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the skillset that was created or updated.
     */
    public Mono<Response<Skillset>> createOrUpdateSkillsetWithResponse(Skillset skillset,
                                                                       AccessCondition accessCondition,
                                                                       RequestOptions requestOptions) {
        return withContext(context -> this.createOrUpdateSkillsetWithResponse(skillset,
            accessCondition,
            requestOptions,
            context));
    }

    Mono<Response<Skillset>> createOrUpdateSkillsetWithResponse(Skillset skillset,
                                                                AccessCondition accessCondition,
                                                                RequestOptions requestOptions,
                                                                Context context) {
        return restClient
            .skillsets()
            .createOrUpdateWithRestResponseAsync(skillset.getName(),
                skillset, requestOptions, accessCondition, context)
            .map(Function.identity());
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     * @return a response signalling completion.
     */
    public Mono<Void> deleteSkillset(String skillsetName) {
        return this.deleteSkillsetWithResponse(skillsetName, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteSkillsetWithResponse(String skillsetName,
                                                           AccessCondition accessCondition,
                                                           RequestOptions requestOptions) {
        return withContext(context -> this.deleteSkillsetWithResponse(skillsetName, accessCondition, requestOptions,
            context));
    }

    Mono<Response<Void>> deleteSkillsetWithResponse(String skillsetName,
                                                    AccessCondition accessCondition,
                                                    RequestOptions requestOptions,
                                                    Context context) {
        return restClient
            .skillsets()
            .deleteWithRestResponseAsync(skillsetName, requestOptions, accessCondition, context)
            .map(Function.identity());
    }

    /**
     * Determines whether or not the given skillset exists.
     *
     * @param skillsetName the name of the skillset
     * @return true if the skillset exists; false otherwise.
     */
    public Mono<Boolean> skillsetExists(String skillsetName) {
        return this.skillsetExistsWithResponse(skillsetName, null).map(Response::getValue);
    }

    /**
     * Determines whether or not the given skillset exists.
     *
     * @param skillsetName the name of the skillset
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return true if the skillset exists; false otherwise.
     */
    public Mono<Response<Boolean>> skillsetExistsWithResponse(String skillsetName, RequestOptions requestOptions) {
        return withContext(context -> this.skillsetExistsWithResponse(skillsetName, requestOptions, context));
    }

    Mono<Response<Boolean>> skillsetExistsWithResponse(String skillsetName,
                                                       RequestOptions requestOptions,
                                                       Context context) {
        return resourceExistsWithResponse(() ->
            this.getSkillsetWithResponse(skillsetName, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    public Mono<SynonymMap> createSynonymMap(SynonymMap synonymMap) {
        return this.createSynonymMapWithResponse(synonymMap, null)
            .map(Response::getValue);
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
        return withContext(context -> this.createSynonymMapWithResponse(synonymMap, requestOptions, context));
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
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @return the {@link SynonymMap} definition
     */
    public Mono<SynonymMap> getSynonymMap(String synonymMapName) {
        return this.getSynonymMapWithResponse(synonymMapName, null)
            .map(Response::getValue);
    }

    /**
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the SynonymMap.
     */
    public Mono<Response<SynonymMap>> getSynonymMapWithResponse(String synonymMapName, RequestOptions requestOptions) {
        return withContext(context -> this.getSynonymMapWithResponse(synonymMapName, requestOptions, context));
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
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the synonym map that was created or updated.
     */
    public Mono<Response<SynonymMap>> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
                                                                           AccessCondition accessCondition,
                                                                           RequestOptions requestOptions) {
        return withContext(context -> this.createOrUpdateSynonymMapWithResponse(synonymMap,
            accessCondition, requestOptions, context));
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
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the synonym map to delete
     * @return a response signalling completion.
     */
    public Mono<Void> deleteSynonymMap(String synonymMapName) {
        return this.deleteSynonymMapWithResponse(synonymMapName, null, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the synonym map to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteSynonymMapWithResponse(String synonymMapName,
                                                             AccessCondition accessCondition,
                                                             RequestOptions requestOptions) {
        return withContext(context ->
            this.deleteSynonymMapWithResponse(synonymMapName, accessCondition, requestOptions, context));
    }

    Mono<Response<Void>> deleteSynonymMapWithResponse(String synonymMapName,
                                                      AccessCondition accessCondition,
                                                      RequestOptions requestOptions,
                                                      Context context) {
        return restClient
            .synonymMaps()
            .deleteWithRestResponseAsync(synonymMapName, requestOptions, accessCondition, context)
            .map(Function.identity());
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
     * Contains the tracking ID sent with the request to help with debugging
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
    private <T> Mono<Response<Boolean>> resourceExistsWithResponse(Supplier<Mono<Response<T>>> action) {
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

    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * Contains the tracking ID sent with the request to help with debugging
     * @return the search service statistics result.
     */
    public Mono<ServiceStatistics> getServiceStatistics() {
        return this.getServiceStatisticsWithResponse(null).map(Response::getValue);
    }


    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the search service statistics result.
     */
    public Mono<Response<ServiceStatistics>> getServiceStatisticsWithResponse(RequestOptions requestOptions) {
        return withContext(context -> this.getServiceStatisticsWithResponse(requestOptions, context));
    }

    Mono<Response<ServiceStatistics>> getServiceStatisticsWithResponse(RequestOptions requestOptions,
                                                                       Context context) {
        return restClient.getServiceStatisticsWithRestResponseAsync(requestOptions, context).map(Function.identity());
    }


    private static String deserializeHeaders(HttpHeaders headers) {
        return headers.toMap().entrySet().stream().map((entry) ->
            entry.getKey() + "," + entry.getValue()
        ).collect(Collectors.joining(","));
    }
}
