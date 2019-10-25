// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzeResult;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceListResult;
import com.azure.search.models.Index;
import com.azure.search.models.IndexGetStatisticsResult;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.IndexerListResult;
import com.azure.search.models.Skillset;
import com.azure.search.models.SkillsetListResult;
import com.azure.search.models.SynonymMap;
import com.azure.search.models.SynonymMapListResult;
import com.azure.search.models.RequestOptions;

import org.apache.commons.lang3.NotImplementedException;

@ServiceClient(builder = SearchServiceClientBuilder.class)
public class SearchServiceClient {

    private final ClientLogger logger = new ClientLogger(SearchServiceClient.class);

    private final SearchServiceAsyncClient asyncClient;

    SearchServiceClient(SearchServiceAsyncClient searchServiceAsyncClient) {
        this.asyncClient = searchServiceAsyncClient;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.asyncClient.getHttpPipeline();
    }

    /**
     * Initializes a new {@link SearchIndexClient} using the given Index name and the
     * same configuration as the SearchServiceClient.
     * @param indexName the name of the Index for the client
     * @return a {@link SearchIndexClient} created from the service client configuration
     */
    public SearchIndexClient getIndexClient(String indexName) {
        return new SearchIndexClient(asyncClient.getIndexClient(indexName));
    }

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.asyncClient.getApiVersion();
    }

    /**
     * Gets The DNS suffix of the Azure Cognitive Search service. The default is search.windows.net.
     *
     * @return the searchDnsSuffix value.
     */
    public String getSearchDnsSuffix() {
        return this.asyncClient.getSearchDnsSuffix();
    }

    /**
     * Gets The name of the Azure Cognitive Search service.
     *
     * @return the searchServiceName value.
     */
    public String getSearchServiceName() {
        return this.asyncClient.getSearchServiceName();
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the created DataSource.
     */
    public DataSource createDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the created DataSource.
     */
    public Response<DataSource> createDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the DataSource.
     */
    public DataSource getDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the DataSource.
     */
    public Response<DataSource> getDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return all DataSources from the Search service.
     */
    public DataSourceListResult listDataSources() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all DataSources from the Search service.
     */
    public Response<DataSourceListResult> listDataSourcesWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the updated DataSource.
     */
    public DataSource createOrUpdateDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated DataSource.
     */
    public Response<DataSource> createOrUpdateDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void deleteDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response signalling completion
     */
    public Response<Response<Void>> deleteDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the created Indexer.
     */
    public Indexer createIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the created Indexer.
     */
    public Response<Indexer> createIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Indexer.
     */
    public Indexer getIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Indexer.
     */
    public Response<Indexer> getIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return all Indexers from the Search service.
     */
    public IndexerListResult listIndexers() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all Indexers from the Search service.
     */
    public Response<IndexerListResult> listIndexersWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the updated Indexer.
     */
    public Indexer createOrUpdateIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated Indexer.
     */
    public Response<Indexer> createOrUpdateIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void deleteIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response signalling completion
     */
    public Response<Response<Void>> deleteIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void resetIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response signalling completion
     */
    public Response<Response<Void>> resetIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void runIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response signalling completion
     */
    public Response<Response<Void>> runIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Indexer execution information.
     */
    public IndexerExecutionInfo deleteIndexerStatus() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Indexer execution information.
     */
    public Response<IndexerExecutionInfo> deleteIndexerStatusWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Creates a new Azure Cognitive Search index
     * @param index definition of the index to create
     * @return the created Index.
     */
    public Index createIndex(Index index) {
        return this.createIndexWithResponse(index, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index
     * @param index definition of the index to create
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the created Index.
     */
    public Index createIndex(Index index, RequestOptions requestOptions) {
        return this.createIndexWithResponse(index, requestOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index
     * @param index definition of the index to create
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response containing the created Index.
     */
    public Response<Index> createIndexWithResponse(Index index,
                                                   RequestOptions requestOptions,
                                                   Context context) {
        return asyncClient.createIndexWithResponse(index, requestOptions, context).block();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     * @param indexName the name of the index to retrieve
     * @return the Index.
     */
    public Index getIndex(String indexName) {
        return this.getIndexWithResponse(indexName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     * @param indexName the name of the index to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the Index.
     */
    public Index getIndex(String indexName,
                          RequestOptions requestOptions) {
        return this.getIndexWithResponse(indexName, requestOptions, Context.NONE).getValue();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     * @param indexName the name of the index to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response containing the Index.
     */
    public Response<Index> getIndexWithResponse(String indexName,
                                                RequestOptions requestOptions,
                                                Context context) {
        return asyncClient.getIndexWithResponse(indexName, requestOptions, context).block();
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     * @param indexName the name of the index
     * @return true if the index exists; false otherwise.
     */
    public Boolean indexExists(String indexName) {
        return indexExistsWithResponse(indexName, null, Context.NONE).getValue();
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     * @param indexName the name of the index
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return true if the index exists; false otherwise.
     */
    public Boolean indexExists(String indexName,
                               RequestOptions requestOptions) {
        return indexExistsWithResponse(indexName, requestOptions, Context.NONE).getValue();
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     * @param indexName the name of the index
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return true if the index exists; false otherwise.
     */
    public Response<Boolean> indexExistsWithResponse(String indexName,
                                                     RequestOptions requestOptions,
                                                     Context context) {
        return asyncClient.indexExistsWithResponse(indexName, requestOptions, context).block();
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Index statistics.
     */
    public IndexGetStatisticsResult getIndexStatistics() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Index statistics.
     */
    public Response<IndexGetStatisticsResult> getIndexStatisticsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @return the list of indexes.
     */
    public PagedIterable<Index> listIndexes() {
        return this.listIndexes(null, null, Context.NONE);
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
                     Specified as a comma-separated list of JSON property names, or '*' for all properties.
                     The default is all properties
     * @return the list of indexes.
     */
    public PagedIterable<Index> listIndexes(String select) {
        return this.listIndexes(select, null, Context.NONE);
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
                     Specified as a comma-separated list of JSON property names, or '*' for all properties.
                     The default is all properties
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the list of indexes.
     */
    public PagedIterable<Index> listIndexes(String select, RequestOptions requestOptions) {
        return this.listIndexes(select, requestOptions, Context.NONE);
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
                     Specified as a comma-separated list of JSON property names, or '*' for all properties.
                     The default is all properties
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of indexes.
     */
    public PagedIterable<Index> listIndexes(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listIndexes(select, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     * @param index the definition of the index to create or update
     * @return the index that was created or updated
     */
    public Index createOrUpdateIndex(Index index) {
        return this.createOrUpdateIndexWithResponse(index, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     * @param index the definition of the index to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                        doesn't match specified values.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the index that was created or updated
     */
    public Index createOrUpdateIndex(Index index,
                                     AccessCondition accessCondition,
                                     RequestOptions requestOptions) {
        return this.createOrUpdateIndexWithResponse(index,
            null,
            accessCondition,
            requestOptions,
            Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     *                           index by taking the index offline for at least a few seconds. This temporarily causes
     *                           indexing and query requests to fail. Performance and write availability of the index
     *                           can be impaired for several minutes after the index is updated, or longer for very
     *                           large indexes.
     * @return the index that was created or updated
     */
    public Index createOrUpdateIndex(Index index, Boolean allowIndexDowntime) {
        return this.createOrUpdateIndexWithResponse(index,
            allowIndexDowntime,
            null,
            null,
            Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     *                           index by taking the index offline for at least a few seconds. This temporarily causes
     *                           indexing and query requests to fail. Performance and write availability of the index
     *                           can be impaired for several minutes after the index is updated, or longer for very
     *                           large indexes.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                        doesn't match specified values.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the index that was created or updated
     */
    public Index createOrUpdateIndex(Index index,
                                     Boolean allowIndexDowntime,
                                     AccessCondition accessCondition,
                                     RequestOptions requestOptions) {
        return this.createOrUpdateIndexWithResponse(index,
            allowIndexDowntime,
            accessCondition,
            requestOptions,
            Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     * @param index the definition of the index to create or update
     * @return a response containing the Index that was created or updated.
     */
    public Response<Index> createOrUpdateIndexWithResponse(Index index) {
        return asyncClient.createOrUpdateIndexWithResponse(index,
            null,
            null,
            null,
            Context.NONE).block();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     *                           index by taking the index offline for at least a few seconds. This temporarily causes
     *                           indexing and query requests to fail. Performance and write availability of the index
     *                           can be impaired for several minutes after the index is updated, or longer for very
     *                           large indexes.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                        doesn't match specified values.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response containing the Index that was created or updated.
     */
    public Response<Index> createOrUpdateIndexWithResponse(Index index,
                                                           Boolean allowIndexDowntime,
                                                           AccessCondition accessCondition,
                                                           RequestOptions requestOptions,
                                                           Context context) {
        return asyncClient.createOrUpdateIndexWithResponse(index,
            allowIndexDowntime,
            accessCondition,
            requestOptions,
            context).block();
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     */
    public void deleteIndex(String indexName) {
        this.deleteIndexWithResponse(indexName, null, null, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                        doesn't match specified values.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     */
    public void deleteIndex(String indexName,
                            AccessCondition accessCondition,
                            RequestOptions requestOptions) {
        this.deleteIndexWithResponse(indexName, accessCondition, requestOptions, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                        doesn't match specified values.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteIndexWithResponse(String indexName,
                                                  AccessCondition accessCondition,
                                                  RequestOptions requestOptions,
                                                  Context context) {
        return asyncClient.deleteIndexWithResponse(indexName,
            accessCondition,
            requestOptions,
            context).block();
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Index analysis results.
     */
    public Response<AnalyzeResult> analyzeIndex() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Index analysis results.
     */
    public Response<AnalyzeResult> analyzeIndexWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the created Skillset.
     */
    public Skillset createSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the created Skillset.
     */
    public Response<Skillset> createSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the Skillset.
     */
    public Skillset getSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the Skillset.
     */
    public Response<Skillset> getSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return all Skillsets in the Search service.
     */
    public SkillsetListResult listSkillsets() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all Skillsets in the Search service.
     */
    public Response<SkillsetListResult> listSkillsetsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the updated Skillset.
     */
    public Skillset createOrUpdateSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated Skillset.
     */
    public Response<Skillset> createOrUpdateSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void deleteSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response signalling completion
     */
    public Response<Response<Void>> deleteSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    public SynonymMap createSynonymMap(SynonymMap synonymMap) {
        return this.createSynonymMapWithResponse(synonymMap, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the created {@link SynonymMap}.
     */
    public SynonymMap createSynonymMap(SynonymMap synonymMap,
                                       RequestOptions requestOptions) {
        return this.createSynonymMapWithResponse(synonymMap, requestOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created SynonymMap.
     */
    public Response<SynonymMap> createSynonymMapWithResponse(SynonymMap synonymMap,
                                                             RequestOptions requestOptions,
                                                             Context context) {
        return asyncClient.createSynonymMapWithResponse(synonymMap,
                requestOptions,
                context).block();
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the SynonymMap.
     */
    public SynonymMap getSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the SynonymMap.
     */
    public Response<SynonymMap> getSynonymMapWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return all SynonymMaps in the Search service.
     */
    public SynonymMapListResult listSynonymMaps() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all SynonymMaps in the Search service.
     */
    public Response<SynonymMapListResult> listSynonymMapsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the updated SynonymMap.
     */
    public SynonymMap createOrUpdateSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated SynonymMap.
     */
    public Response<SynonymMap> createOrUpdateSynonymMapWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void deleteSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response signalling completion
     */
    public Response<Response<Void>> deleteSynonymMapWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }
}
