// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.models.AccessCondition;
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
    public DataSource replaceDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated DataSource.
     */
    public Response<DataSource> replaceDataSourceWithResponse() {
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
    public Indexer replaceIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated Indexer.
     */
    public Response<Indexer> replaceIndexerWithResponse() {
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
     * @throws NotImplementedException not implemented
     * @return all the Indexes in the Search service.
     */
    public IndexListResult listIndexes() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing all Indexes in the Search service.
     */
    public Response<IndexListResult> listIndexesWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return the updated Index.
     */
    public Index replaceIndex() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated Index.
     */
    public Response<Index> replaceIndexWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete.
     */
    public void deleteIndex(String indexName) {
        this.deleteIndexWithResponse(indexName, null, null, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param accessCondition the access condition
     */
    public void deleteIndex(String indexName,
                            RequestOptions requestOptions,
                            AccessCondition accessCondition) {
        this.deleteIndexWithResponse(indexName, requestOptions, accessCondition, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param accessCondition the access condition
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteIndexWithResponse(String indexName,
                                                  RequestOptions requestOptions,
                                                  AccessCondition accessCondition,
                                                  Context context) {
        return asyncClient.deleteIndexWithResponse(indexName,
            requestOptions,
            accessCondition,
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
    public Skillset replaceSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated Skillset.
     */
    public Response<Skillset> replaceSkillsetWithResponse() {
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
    public SynonymMap replaceSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     * @return a response containing the updated SynonymMap.
     */
    public Response<SynonymMap> replaceSynonymMapWithResponse() {
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
