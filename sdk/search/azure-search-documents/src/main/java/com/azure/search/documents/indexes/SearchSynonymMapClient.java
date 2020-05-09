// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SynonymMap;

/**
 * Synchronous Client to manage and query synonym map, as well as manage other resources,
 * on a Cognitive Search service.
 */
public class SearchSynonymMapClient {
    private final SearchSynonymMapAsyncClient asyncClient;

    SearchSynonymMapClient(SearchSynonymMapAsyncClient searchServiceAsyncClient) {
        this.asyncClient = searchServiceAsyncClient;
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    public SynonymMap create(SynonymMap synonymMap) {
        return createWithResponse(synonymMap, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created SynonymMap.
     */
    public Response<SynonymMap> createWithResponse(SynonymMap synonymMap, RequestOptions requestOptions,
        Context context) {
        return asyncClient.createWithResponse(synonymMap, requestOptions, context).block();
    }

    /**
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @return the {@link SynonymMap} definition
     */
    public SynonymMap getSynonymMap(String synonymMapName) {
        return getSynonymMapWithResponse(synonymMapName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context a context that is passed through the HTTP pipeline during the service call
     * @return a response containing the SynonymMap.
     */
    public Response<SynonymMap> getSynonymMapWithResponse(String synonymMapName, RequestOptions requestOptions,
        Context context) {
        return asyncClient.getSynonymMapWithResponse(synonymMapName, requestOptions, context).block();
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @return the list of synonym maps.
     */
    public PagedIterable<SynonymMap> listSynonymMaps() {
        return listSynonymMaps(null, Context.NONE);
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of synonym maps.
     */
    public PagedIterable<SynonymMap> listSynonymMaps(RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listSynonymMaps(null, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @return the synonym map that was created or updated.
     */
    public SynonymMap createOrUpdate(SynonymMap synonymMap) {
        return createOrUpdateWithResponse(synonymMap, false, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the synonym map that was created or updated.
     */
    public Response<SynonymMap> createOrUpdateWithResponse(SynonymMap synonymMap,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateWithResponse(synonymMap, onlyIfUnchanged, requestOptions, context)
            .block();
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the synonym map to delete
     */
    public void delete(String synonymMapName) {
        deleteWithResponse(new SynonymMap().setName(synonymMapName), false, null, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMap the {@link SynonymMap} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteWithResponse(SynonymMap synonymMap, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        String etag = onlyIfUnchanged ? synonymMap.getETag() : null;
        return asyncClient.deleteWithResponse(synonymMap.getName(), etag, requestOptions, context)
            .block();
    }

    /**
     * List all SynonymMap names from an Azure Cognitive Search service.
     *
     * @return a list of SynonymMap names.
     */
    public PagedIterable<SynonymMap> listSynonymMapNames() {
        return listSynonymMapNames(null, Context.NONE);
    }

    /**
     * List all SynonymMap names from an Azure Cognitive Search service.
     *
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a list of SynonymMap names
     */
    public PagedIterable<SynonymMap> listSynonymMapNames(RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listSynonymMapNames(requestOptions, context));
    }
}
